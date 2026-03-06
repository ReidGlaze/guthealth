import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { GoogleGenAI, ThinkingLevel } from "@google/genai";
import * as admin from "firebase-admin";

admin.initializeApp();

// Initialize Google GenAI with Vertex AI (global endpoint for Gemini 3)
const ai = new GoogleGenAI({
  vertexai: true,
  project: process.env.GCLOUD_PROJECT || "guthealth-f4766",
  location: "global",
});

const db = admin.firestore();

// ============================================================
// Shared helpers
// ============================================================

const AI_DAILY_LIMIT = 30; // max AI calls per user per day

async function checkRateLimit(userId: string, functionName: string): Promise<void> {
  const today = new Date().toISOString().split("T")[0];
  const ref = db.doc(`rateLimits/${userId}`);

  const result = await db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);
    const data = doc.data() || {};
    const key = `${functionName}_${today}`;
    const count = (data[key] as number) || 0;

    if (count >= AI_DAILY_LIMIT) {
      return false;
    }

    tx.set(ref, { [key]: count + 1 }, { merge: true });
    return true;
  });

  if (!result) {
    throw new HttpsError(
      "resource-exhausted",
      `Daily limit reached (${AI_DAILY_LIMIT} calls/day). Try again tomorrow.`
    );
  }
}

// Extract JSON from AI response text, stripping markdown fences.
// When treatNoJsonAsEmpty=true and matchArray=true, prose responses with no JSON array
// are treated as empty results (returning "[]") rather than throwing — this handles cases
// where the model legitimately has no patterns to report and explains why in prose.
function extractJsonFromResponse(
  response: { text?: string | null; candidates?: Array<{ content?: { parts?: Array<{ text?: string }> } }> },
  fnName: string,
  matchArray = false,
  treatNoJsonAsEmpty = false
): string {
  let text = response.text;
  if (!text) {
    const parts = response.candidates?.[0]?.content?.parts;
    if (parts) {
      text = parts.filter(p => p.text).map(p => p.text).join("\n");
    }
  }
  if (!text) {
    throw new HttpsError("internal", `${fnName}: No response from Vertex AI`);
  }

  text = text.replace(/```json\s*/g, "").replace(/```\s*/g, "").trim();
  const pattern = matchArray ? /\[[\s\S]*\]/ : /\{[\s\S]*\}/;
  const jsonMatch = text.match(pattern);
  if (!jsonMatch) {
    if (treatNoJsonAsEmpty && matchArray) {
      // Model returned prose (e.g. "no patterns found") — treat as empty array
      console.log(`${fnName}: No JSON array in response — treating as empty result. Response preview: ${text.substring(0, 200)}`);
      return "[]";
    }
    throw new HttpsError("internal", `${fnName}: No JSON found in response`);
  }
  return jsonMatch[0];
}

const MAX_GEMINI_RETRIES = 3;

// Retry wrapper for Gemini calls that expect JSON responses.
// Re-calls Gemini with a reinforced JSON-only system message on parse failure.
// When treatNoJsonAsEmpty=true, prose responses with no JSON are treated as empty
// results rather than triggering retries (used for correlation engine).
async function callGeminiWithJsonRetry(opts: {
  model: string;
  contents: Array<{ role: string; parts: Array<Record<string, unknown>> }>;
  config: Record<string, unknown>;
  fnName: string;
  matchArray?: boolean;
  treatNoJsonAsEmpty?: boolean;
}): Promise<string> {
  let lastError: Error | null = null;

  for (let attempt = 1; attempt <= MAX_GEMINI_RETRIES; attempt++) {
    const contents = attempt === 1
      ? opts.contents
      : [
          ...opts.contents,
          {
            role: "user" as const,
            parts: [{ text: "Your previous response was not valid JSON. Return ONLY the raw JSON array with no explanation, no markdown, no extra text. If there are no patterns, return an empty array: []" }],
          },
        ];

    try {
      const response = await ai.models.generateContent({
        model: opts.model,
        contents,
        config: opts.config,
      });

      return extractJsonFromResponse(response, opts.fnName, opts.matchArray ?? false, opts.treatNoJsonAsEmpty ?? false);
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));
      // Don't retry auth/rate-limit errors
      if (error instanceof HttpsError && ["unauthenticated", "resource-exhausted", "invalid-argument"].includes(error.code)) {
        throw error;
      }
      // If treatNoJsonAsEmpty would have handled this, don't retry — the model
      // deliberately returned prose to indicate no results.
      if (opts.treatNoJsonAsEmpty && opts.matchArray &&
          lastError.message.includes("No JSON found in response")) {
        console.log(`${opts.fnName}: prose response on attempt ${attempt} — treating as empty result`);
        return "[]";
      }
      console.warn(`${opts.fnName}: attempt ${attempt}/${MAX_GEMINI_RETRIES} failed — ${lastError.message}`);
    }
  }

  throw new HttpsError("internal", `${opts.fnName}: Failed after ${MAX_GEMINI_RETRIES} attempts. Last error: ${lastError?.message}`);
}

// Validate image request: auth check + base64 validation + size limit
function validateImageRequest(request: { auth?: { uid: string }; data: { imageBase64?: string } }): string {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Must be logged in");
  }
  const { imageBase64 } = request.data;
  if (!imageBase64 || typeof imageBase64 !== "string") {
    throw new HttpsError("invalid-argument", "imageBase64 is required");
  }
  if (imageBase64.length > 10_000_000) {
    throw new HttpsError("invalid-argument", "Image too large. Maximum size is 10MB.");
  }
  return imageBase64;
}

function validateFoodItems(foods: unknown[]): FoodItem[] {
  const validLevels = ["low", "moderate", "high", "unknown"];
  return foods
    .filter((f): f is Record<string, unknown> =>
      typeof f === "object" && f !== null && typeof (f as Record<string, unknown>).name === "string"
    )
    .slice(0, 30)
    .map((f) => ({
      name: String(f.name).slice(0, 100),
      fodmapLevel: (validLevels.includes(String(f.fodmapLevel)) ? String(f.fodmapLevel) : "unknown") as FoodItem["fodmapLevel"],
      fodmapCategories: Array.isArray(f.fodmapCategories)
        ? (f.fodmapCategories as unknown[]).filter((c): c is string => typeof c === "string").slice(0, 10)
        : [],
      servingSize: typeof f.servingSize === "string" ? String(f.servingSize).slice(0, 50) : "",
      lowFodmapServing: typeof f.lowFodmapServing === "string" ? String(f.lowFodmapServing).slice(0, 50) : "",
      triggers: Array.isArray(f.triggers)
        ? (f.triggers as unknown[]).filter((t): t is string => typeof t === "string").slice(0, 10)
        : [],
    }));
}

// ============================================================
// 1. analyzeFoodPhoto — AI food scanner with FODMAP analysis
// ============================================================

interface AnalyzeFoodPhotoRequest {
  imageBase64: string;
  mealType?: "breakfast" | "lunch" | "dinner" | "snack";
}

interface FoodItem {
  name: string;
  fodmapLevel: "low" | "moderate" | "high" | "unknown";
  fodmapCategories: string[];
  servingSize: string;
  lowFodmapServing: string;
  triggers: string[];
}

interface AnalyzeFoodPhotoResponse {
  foods: FoodItem[];
}

export const analyzeFoodPhoto = onCall<AnalyzeFoodPhotoRequest>(
  {
    region: "us-central1",
    memory: "512MiB",
    timeoutSeconds: 60,
  },
  async (request): Promise<AnalyzeFoodPhotoResponse> => {
    const imageBase64 = validateImageRequest(request);
    await checkRateLimit(request.auth!.uid, "analyzeFoodPhoto");

    const prompt = `Analyze this photo of food/meal and identify each food item.

For EACH food item, provide:
- name: the food name
- fodmapLevel: "low", "moderate", or "high" based on Monash University FODMAP data
- fodmapCategories: which FODMAP groups apply (fructose, lactose, fructans, GOS, mannitol, sorbitol)
- servingSize: estimated serving size visible in photo
- lowFodmapServing: the maximum serving size considered low-FODMAP per Monash University data
- triggers: common gut symptom triggers for this food (e.g., "high fiber", "dairy", "fructans")

Return ONLY valid JSON: {"foods":[...]}

IMPORTANT: This is for educational wellness purposes only. Base FODMAP ratings on established Monash University data.`;

    try {
      const jsonStr = await callGeminiWithJsonRetry({
        model: "gemini-3-flash-preview",
        contents: [
          {
            role: "user",
            parts: [
              { inlineData: { data: imageBase64, mimeType: "image/jpeg" } },
              { text: prompt },
            ],
          },
        ],
        config: {
          temperature: 0.2,
          maxOutputTokens: 4096,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        },
        fnName: "analyzeFoodPhoto",
      });

      const parsed = JSON.parse(jsonStr);
      const validatedFoods = validateFoodItems(parsed.foods || []);
      return { foods: validatedFoods };
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      const msg = error instanceof Error ? error.message : String(error);
      if (msg.includes("429") || msg.includes("RESOURCE_EXHAUSTED")) {
        throw new HttpsError("resource-exhausted", "Too many requests. Please wait and try again.");
      }
      console.error("analyzeFoodPhoto error:", msg);
      throw new HttpsError("internal", "Failed to analyze food photo. Please try again.");
    }
  }
);

// ============================================================
// 2. classifyPoopPhoto — Bristol Stool Chart classification
// ============================================================

interface ClassifyPoopRequest {
  imageBase64: string;
}

interface ClassifyPoopResponse {
  bristolType: number;
  color: string;
  observations: string;
}

export const classifyPoopPhoto = onCall<ClassifyPoopRequest>(
  {
    region: "us-central1",
    memory: "512MiB",
    timeoutSeconds: 60,
  },
  async (request): Promise<ClassifyPoopResponse> => {
    const imageBase64 = validateImageRequest(request);
    await checkRateLimit(request.auth!.uid, "classifyPoopPhoto");

    const prompt = `Classify this stool sample using the Bristol Stool Chart (types 1-7), identify its color, and note any additional observations.

Bristol Stool Chart:
Type 1: Separate hard lumps (severe constipation)
Type 2: Lumpy sausage shape (mild constipation)
Type 3: Sausage with cracks (normal)
Type 4: Smooth soft sausage (ideal normal)
Type 5: Soft blobs with clear edges (lacking fiber)
Type 6: Mushy with ragged edges (mild diarrhea)
Type 7: Watery, no solid pieces (severe diarrhea)

Color options: "brown", "dark", "light", "green", "yellow", "red", "black"

For observations, briefly note anything visible beyond type and color: mucus, undigested food, floating/sinking, unusual texture. Keep it short (1-2 sentences). If nothing notable, return empty string.

Return ONLY valid JSON: {"bristolType":N,"color":"COLOR","observations":"..."}

This is for educational wellness tracking purposes only.`;

    try {
      const jsonStr = await callGeminiWithJsonRetry({
        model: "gemini-3-flash-preview",
        contents: [
          {
            role: "user",
            parts: [
              { inlineData: { data: imageBase64, mimeType: "image/jpeg" } },
              { text: prompt },
            ],
          },
        ],
        config: {
          temperature: 0.1,
          maxOutputTokens: 512,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        },
        fnName: "classifyPoopPhoto",
      });

      const parsed = JSON.parse(jsonStr);
      if (typeof parsed.bristolType !== "number" || parsed.bristolType < 1 || parsed.bristolType > 7) {
        throw new HttpsError("internal", "Invalid Bristol type from AI");
      }

      const validColors = ["brown", "dark", "light", "green", "yellow", "red", "black"];
      const color = typeof parsed.color === "string" && validColors.includes(parsed.color.toLowerCase())
        ? parsed.color.toLowerCase()
        : "brown";

      const observations = typeof parsed.observations === "string" ? parsed.observations.slice(0, 300) : "";

      return {
        bristolType: parsed.bristolType,
        color,
        observations,
      };
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      const msg = error instanceof Error ? error.message : String(error);
      console.error("classifyPoopPhoto error:", msg);
      throw new HttpsError("internal", "Failed to classify photo. Please try again.");
    }
  }
);

// ============================================================
// 3. runCorrelationEngine — Holistic AI narrative report
// ============================================================

export const runCorrelationEngine = onCall(
  {
    region: "us-central1",
    memory: "1GiB",
    timeoutSeconds: 300,
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Must be logged in");
    }

    const userId = request.auth.uid;
    await checkRateLimit(userId, "runCorrelationEngine");

    const allowedDays = [3, 7, 10];
    const rawDays = request.data?.daysBack;
    const daysBack = (typeof rawDays === "number" && allowedDays.includes(rawDays)) ? rawDays : 7;
    const cutoff = new Date();
    cutoff.setDate(cutoff.getDate() - daysBack);

    const [mealsSnap, symptomsSnap, poopLogsSnap] = await Promise.all([
      db.collection(`users/${userId}/meals`)
        .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(cutoff))
        .orderBy("createdAt")
        .get(),
      db.collection(`users/${userId}/symptoms`)
        .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(cutoff))
        .orderBy("createdAt")
        .get(),
      db.collection(`users/${userId}/poopLogs`)
        .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(cutoff))
        .orderBy("createdAt")
        .get(),
    ]);

    // Require at least 1 meal AND (1 symptom OR 1 poop log with abnormal Bristol type)
    const hasAbnormalPoop = poopLogsSnap.docs.some(d => {
      const bt = d.data().bristolType;
      return typeof bt === "number" && (bt <= 2 || bt >= 6);
    });
    if (mealsSnap.empty || (symptomsSnap.empty && !hasAbnormalPoop)) {
      return { message: "Not enough data for correlation analysis. Keep logging meals and symptoms!" };
    }

    // Serialize all three data streams for Gemini
    const meals = mealsSnap.docs.slice(0, 100).map(d => {
      const data = d.data();
      return {
        mealType: data.mealType || "unknown",
        createdAt: data.createdAt?.toDate?.()?.toISOString() || "",
        foods: (data.foods || []).slice(0, 20).map((f: Record<string, unknown>) => ({
          name: String(f.name || "").slice(0, 80),
          fodmapLevel: f.fodmapLevel || "unknown",
          fodmapCategories: Array.isArray(f.fodmapCategories) ? f.fodmapCategories : [],
        })),
        notes: typeof data.notes === "string" ? data.notes.slice(0, 200) : "",
      };
    });

    const symptoms = symptomsSnap.docs.slice(0, 100).map(d => {
      const data = d.data();
      return {
        type: data.type || "unknown",
        severity: data.severity || 0,
        createdAt: data.createdAt?.toDate?.()?.toISOString() || "",
        notes: typeof data.notes === "string" ? data.notes.slice(0, 200) : "",
      };
    });

    const poopLogs = poopLogsSnap.docs.slice(0, 100).map(d => {
      const data = d.data();
      return {
        bristolType: data.bristolType || null,
        color: data.color || "brown",
        urgency: data.urgency || "normal",
        createdAt: data.createdAt?.toDate?.()?.toISOString() || "",
      };
    });

    const periodStart = cutoff.toISOString().split("T")[0];
    const periodEnd = new Date().toISOString().split("T")[0];

    const prompt = `You are a gut health pattern analyst reviewing food diary, symptom, and bowel movement data for a single user. Write a concise, scannable report identifying patterns and commonly discussed management approaches.

ANALYSIS PERIOD: ${periodStart} to ${periodEnd}

MEALS LOGGED (${meals.length} total):
${JSON.stringify(meals)}

SYMPTOMS LOGGED (${symptoms.length} total):
${JSON.stringify(symptoms)}

BOWEL MOVEMENT LOGS (${poopLogs.length} total):
${JSON.stringify(poopLogs)}

INSTRUCTIONS:

1. Analyze all three data streams holistically. Look at the timing between meals and symptoms (gut reactions typically occur 30 minutes to 12 hours after eating).

2. Treat Bristol Type 1-2 (hard, lumpy) and Type 6-7 (mushy, watery) as "something went wrong" signals even without an explicit symptom log. A Type 6 or 7 after a high-FODMAP meal is meaningful.

3. Use poop data to strengthen or weaken meal-to-symptom correlations.

4. No numerical confidence scores. Use natural language: "seen 3 times", "every time you ate", "possible link", etc.

5. FORMAT YOUR REPORT EXACTLY LIKE THIS — use these emoji headers and bullet points:

Patterns Found
(bullet) [Food] led to [symptom] within [time window] (seen X times)
(bullet) [Another pattern with specific foods and timing]
(bullet) [One more if data supports it, skip this section header if no patterns found]

Stool Patterns
(bullet) [Bristol type observations tied to specific foods]
(bullet) [Baseline vs. post-trigger stool changes]

What's Working
(bullet) [Foods or days that went well]
(bullet) [Positive patterns worth continuing]

Commonly Discussed Approaches
(bullet) [Specific approach commonly discussed in FODMAP literature]
(bullet) [Another approach people commonly try]
(bullet) [A third if relevant]

RULES:
- Each section: 2-4 bullet points MAX. Be concise, one line per bullet.
- Reference specific foods, dates, and timing. Not vague generalities.
- Start each bullet with the bullet character: •
- Do NOT use em dashes (—). Use commas or periods instead.
- Do NOT write flowing paragraphs. Bullets only.
- If a section has nothing meaningful, write one bullet: "Not enough data yet."
- Do NOT include the disclaimer in the report body (it is added separately).

6. FODMAP TERMINOLOGY RULES (mandatory):
   - Always use "fructans" (not "gluten") for wheat, garlic, onion triggers. Gluten is a celiac concern; fructans are the FODMAP issue.
   - When recommending sourdough, specify "traditional long-fermentation sourdough (24+ hour proof)".
   - Use FODMAP subcategory names: fructans, GOS, lactose, fructose, mannitol, sorbitol.

Write the report now. Bullets only, no paragraphs.`;

    try {
      const response = await ai.models.generateContent({
        model: "gemini-3-flash-preview",
        contents: [{ role: "user", parts: [{ text: prompt }] }],
        config: {
          temperature: 0.4,
          maxOutputTokens: 8192,
          thinkingConfig: { thinkingLevel: ThinkingLevel.HIGH },
          httpOptions: { timeout: 240_000 },
        },
      });

      let aiReport = response.text;
      if (!aiReport) {
        const parts = response.candidates?.[0]?.content?.parts;
        if (parts) {
          aiReport = parts.filter(p => typeof p.text === "string").map(p => p.text as string).join("\n");
        }
      }
      if (!aiReport) {
        throw new HttpsError("internal", "runCorrelationEngine: No response from Vertex AI");
      }

      // Ensure disclaimer is present
      const disclaimer = "This is not medical advice";
      if (!aiReport.includes(disclaimer)) {
        aiReport = aiReport.trimEnd() + "\n\n" + disclaimer + " — please consult a registered dietitian or gastroenterologist for personalized guidance.";
      }

      // Write single report document to correlationReports collection
      const reportRef = db.collection(`users/${userId}/correlationReports`).doc();
      await reportRef.set({
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        periodStart,
        periodEnd,
        mealsAnalyzed: meals.length,
        symptomsAnalyzed: symptoms.length,
        poopLogsAnalyzed: poopLogs.length,
        aiReport,
        disclaimer,
      });

      return {
        reportId: reportRef.id,
        periodStart,
        periodEnd,
        mealsAnalyzed: meals.length,
        symptomsAnalyzed: symptoms.length,
        poopLogsAnalyzed: poopLogs.length,
        aiReport,
        disclaimer,
      };
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      const msg = error instanceof Error ? error.message : String(error);
      if (msg.includes("429") || msg.includes("RESOURCE_EXHAUSTED")) {
        throw new HttpsError("resource-exhausted", "Too many requests. Please wait and try again.");
      }
      throw new HttpsError("internal", `Correlation engine failed: ${msg}`);
    }
  }
);

// ============================================================
// 5. generateDailySummary — Nightly summary at 11pm
// ============================================================

export const generateDailySummary = onSchedule(
  {
    schedule: "every day 23:00",
    timeZone: "America/Denver",
    region: "us-central1",
    memory: "512MiB",
    timeoutSeconds: 300,
  },
  async () => {
    const usersSnap = await db.collection("users").get();
    const BATCH_SIZE = 5;

    async function processUserDaily(userDoc: FirebaseFirestore.QueryDocumentSnapshot) {
      try {
      const userId = userDoc.id;
      const today = new Date();
      const dateKey = today.toISOString().split("T")[0];
        const startOfDay = new Date(today);
        startOfDay.setHours(0, 0, 0, 0);

        const [mealsSnap, symptomsSnap, poopSnap] = await Promise.all([
          db.collection(`users/${userId}/meals`)
            .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(startOfDay))
            .get(),
          db.collection(`users/${userId}/symptoms`)
            .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(startOfDay))
            .get(),
          db.collection(`users/${userId}/poopLogs`)
            .where("createdAt", ">=", admin.firestore.Timestamp.fromDate(startOfDay))
            .get(),
        ]);

        // Skip if no data logged today
        if (mealsSnap.empty && symptomsSnap.empty && poopSnap.empty) return;

        const meals = mealsSnap.docs.map(d => d.data());
        const symptoms = symptomsSnap.docs.map(d => d.data());
        const poopLogs = poopSnap.docs.map(d => d.data());

        const highFodmapCount = meals.reduce((count, meal) => {
          const highFoods = (meal.foods || []).filter((f: FoodItem) => f.fodmapLevel === "high");
          return count + highFoods.length;
        }, 0);

        const severities = symptoms.map((s) => s.severity || 0);
        const avgSeverity = severities.length > 0
          ? Math.round(severities.reduce((a: number, b: number) => a + b, 0) / severities.length * 10) / 10
          : 0;

        // Find dominant symptom
        const symptomCounts: Record<string, number> = {};
        symptoms.forEach((s) => {
          symptomCounts[s.type] = (symptomCounts[s.type] || 0) + 1;
        });
        const dominantSymptom = Object.entries(symptomCounts)
          .sort((a, b) => b[1] - a[1])[0]?.[0] || null;

        // Generate AI summary
        let aiSummary = "";
        try {
          const summaryResponse = await ai.models.generateContent({
            model: "gemini-3-flash-preview",
            contents: [{
              role: "user",
              parts: [{
                text: `Write a 1-2 sentence daily gut health summary.
Meals: ${meals.length}, High FODMAP foods: ${highFodmapCount}, Symptoms: ${symptoms.length} (avg severity: ${avgSeverity}), Poop logs: ${poopLogs.length}.
Be encouraging and brief. Educational wellness content only.`,
              }],
            }],
            config: {
              temperature: 0.7,
              maxOutputTokens: 256,
              thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
            },
          });
          aiSummary = summaryResponse.text || "";
        } catch {
          aiSummary = `${meals.length} meals logged, ${symptoms.length} symptoms tracked.`;
        }

        await db.doc(`users/${userId}/dailySummaries/${dateKey}`).set({
          date: dateKey,
          totalMeals: meals.length,
          totalSymptoms: symptoms.length,
          totalPoopLogs: poopLogs.length,
          highFodmapCount,
          avgSymptomSeverity: avgSeverity,
          dominantSymptom,
          generatedAt: admin.firestore.FieldValue.serverTimestamp(),
          aiSummary,
          disclaimer: "This is not medical advice",
        });
      } catch (err) {
        console.error(`Daily summary failed for user ${userDoc.id}:`, err);
      }
    }

    // Process users in parallel batches
    for (let i = 0; i < usersSnap.docs.length; i += BATCH_SIZE) {
      const batch = usersSnap.docs.slice(i, i + BATCH_SIZE);
      await Promise.all(batch.map(processUserDaily));
    }
  }
);

// ============================================================
// 6. sendReminders — Hourly check for meal logging reminders
// ============================================================

export const sendReminders = onSchedule(
  {
    schedule: "every 1 hours",
    region: "us-central1",
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async () => {
    const usersSnap = await db.collection("users")
      .where("preferences.notificationsEnabled", "==", true)
      .get();

    const messages: admin.messaging.Message[] = [];

    for (const userDoc of usersSnap.docs) {
      const userData = userDoc.data();
      const reminderTimes: string[] = userData.preferences?.reminderTimes || [];
      const fcmToken = userData.fcmToken;
      const timezone = userData.preferences?.timezone || "America/Denver";

      // Get current hour in the user's timezone
      const userHour = new Date().toLocaleString("en-US", {
        timeZone: timezone,
        hour: "2-digit",
        hour12: false,
      }).replace(/\s/g, "");
      const currentHourNum = parseInt(userHour, 10);

      // Check if any reminder time falls within the current hour
      if (!fcmToken) continue;
      const hasMatchingReminder = reminderTimes.some((t: string) => {
        const reminderHour = parseInt(t.split(":")[0], 10);
        return reminderHour === currentHourNum;
      });
      if (!hasMatchingReminder) continue;

      messages.push({
        token: fcmToken,
        notification: {
          title: "Time to log your meal!",
          body: "Track what you ate to help identify your gut health triggers.",
        },
      });
    }

    if (messages.length > 0) {
      const result = await admin.messaging().sendEach(messages);
      console.log(`Sent ${result.successCount} reminders, ${result.failureCount} failures`);

      // Clean up stale FCM tokens using a pre-built map for O(1) lookup
      const tokenToDoc = new Map<string, FirebaseFirestore.QueryDocumentSnapshot>();
      for (const doc of usersSnap.docs) {
        const token = doc.data().fcmToken;
        if (token) tokenToDoc.set(token, doc);
      }

      const staleTokenUpdates: Promise<FirebaseFirestore.WriteResult>[] = [];
      result.responses.forEach((resp, idx) => {
        if (resp.error?.code === "messaging/registration-token-not-registered" ||
            resp.error?.code === "messaging/invalid-registration-token") {
          const msg = messages[idx] as { token?: string };
          const token = msg.token;
          if (token) {
            const userDoc = tokenToDoc.get(token);
            if (userDoc) {
              staleTokenUpdates.push(
                userDoc.ref.update({ fcmToken: admin.firestore.FieldValue.delete() })
              );
            }
          }
        }
      });
      if (staleTokenUpdates.length > 0) {
        await Promise.all(staleTokenUpdates);
        console.log(`Cleaned up ${staleTokenUpdates.length} stale FCM tokens`);
      }
    }
  }
);


