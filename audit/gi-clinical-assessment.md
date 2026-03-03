# GI Clinical Assessment: AI Gut Health App -- 7-Day Patient Data Review

**Reviewing Clinician Role**: Board-Certified Gastroenterologist (Simulated)
**Date of Review**: 2026-03-02
**Data Period**: 7 days (2026-02-24 through 2026-03-02)
**Patient Identifier**: Anonymous (Firebase anonymous auth -- no PII)
**FODMAP Phase at Time of Logging**: Elimination

---

## 1. Patient Summary

### 7-Day Logging Overview

The patient logged data consistently over 7 consecutive days, including:

- **21 meals** (3 per day x 7 days) -- 100% compliance with recommended 3-meal logging
- **9 symptom entries** across 4 symptomatic days (Days 1, 3, 5, 6)
- **7 bowel movement logs** (1 per day) -- consistent daily logging
- **3 safe days** (Days 2, 4, 7) with zero symptoms logged
- **3 clear trigger days** (Days 1, 3, 5) with multiple symptoms
- **1 mixed day** (Day 6) with a single moderate symptom

### Compliance Level: Excellent

The patient demonstrated strong compliance with 3 meals, symptoms, and bowel movements logged every day. This is well above the minimum data density typically required for meaningful correlation analysis (at least 5 days with at least 2 meals/day). The alternating pattern of trigger and safe days is particularly valuable for isolating variables.

### Data Quality Assessment

Data quality is **good for an app-based diary** but has important limitations (discussed in Section 7). The patient used manual food entry (not photo-based AI analysis for most entries), which means FODMAP classification relied on the correlation engine's AI rather than the per-meal food scanner. Timestamps were set using the app's date/time picker to simulate past entries, which is appropriate methodology. Symptom severity was recorded on the 1-10 scale, and time offsets between meals and symptoms were tracked -- both critical for correlation analysis.

---

## 2. Key Findings

### Confirmed Trigger Correlations (from App Correlation Engine)

The app's correlation engine, powered by Gemini 3 Flash with HIGH thinking level, identified the following patterns:

| Trigger Food | Symptom | Confidence | Occurrences | Avg Time Lag | App Recommendation |
|---|---|---|---|---|---|
| **Garlic** | Gas | **90%** | 2x | ~2h | Switch to garlic-infused oils for flavor without water-soluble fructans |
| **Garlic** | Bloating | **90%** | 2x | ~2h | Monitor portion sizes of high-FODMAP aromatics; even small amounts in sauces can trigger significant bloating |
| **Onion** | Bloating | **85%** | 2x | ~2h | Try using the green parts of spring onions or chives as a low-FODMAP alternative to bulb onions |
| **Wheat bread** | Bloating | **80%** | 2x | ~3h | Consider swapping wheat bread for true sourdough or certified gluten-free options to reduce fructan load |

### Symptom Pattern Analysis

**Symptom types observed:**
- **Bloating**: Most frequent symptom, occurring on Days 1, 3, 5, and 6. Severity ranged from 4/10 (Day 6, wheat bread alone) to 8/10 (Day 3, onion soup + wheat bread combined). This is the patient's dominant GI complaint.
- **Gas**: Occurred on Days 1, 3, and 5. Severity 4-5/10. Consistently appeared approximately 2 hours post-exposure.
- **Cramping**: Occurred on Day 3 only, severity 6/10, approximately 4 hours after onion soup with wheat bread. The delayed onset and higher severity suggest a dose-dependent fructan response.

**Dose-response observations:**
- Day 3 produced the most severe symptoms (bloating 8/10, cramping 6/10) when TWO high-fructan triggers were consumed in the same meal (onion soup + wheat bread). This is consistent with the additive fructan load effect described in Monash University research, where fructans from multiple foods stack within the gut.
- Day 6 produced only mild bloating (4/10) from a single trigger (wheat bread in a sandwich), supporting a dose-response relationship.
- Day 5 (garlic + onion stir fry) produced moderate symptoms (bloating 7/10, gas 5/10) -- again consistent with combined fructan sources.

### Bristol Stool Assessment

| Day | Bristol Type | Color | Urgency | Context |
|---|---|---|---|---|
| Day 1 (trigger) | **Type 5** | Brown | Normal | Afternoon, following garlic bread at lunch |
| Day 2 (safe) | **Type 4** | Brown | Normal | Morning -- baseline |
| Day 3 (trigger) | **Type 6** | Brown | **Urgent** | Evening, following double-trigger lunch (onion soup + wheat bread) |
| Day 4 (safe) | **Type 4** | Brown | Normal | Morning -- return to baseline |
| Day 5 (trigger) | **Type 5** | Brown | Normal | Evening, following garlic/onion stir fry |
| Day 6 (mixed) | **Type 4** | Brown | Normal | Morning -- mild trigger day |
| Day 7 (safe) | **Type 4** | Brown | Normal | Morning -- baseline |

**Pattern**: The patient's baseline stool consistency is Bristol Type 4 (ideal, smooth and soft), which shifts to Type 5 (soft blobs, lacking fiber) or Type 6 (mushy, ragged edges -- mild diarrhea) on trigger days. The single episode of urgency (Day 3) coincided with the highest symptom severity day. Return to Type 4 within 24 hours of avoiding triggers suggests good gut resilience and supports a functional (rather than inflammatory) etiology. The absence of Type 7 (watery diarrhea) and absence of red/black stool colors are reassuring.

### Notable Absence: Milk/Lactose Correlation

The patient profile lists milk as a known trigger (lactose). On Day 3, cereal with milk was consumed at breakfast and gas (severity 5) was logged 2 hours later. However, the correlation engine did **not** identify milk as a trigger. This is likely because:

1. Milk appeared only once in the 7-day diary (single occurrence), and the engine requires minimum 2 occurrences.
2. Day 3 had multiple triggers (milk at breakfast, onion soup + wheat bread at lunch), creating confounding variables.

This is a clinically important miss that warrants extended monitoring. The engine correctly applied its minimum-occurrence threshold, but a single lactose exposure followed by symptoms should still be flagged for clinical attention.

---

## 3. Clinical Interpretation

### Primary FODMAP Sensitivity: Fructans (Oligosaccharides)

The data strongly supports a **fructan sensitivity** as the patient's primary FODMAP trigger. All three confirmed triggers -- garlic, onion, and wheat -- are high in fructans per Monash University FODMAP database:

- **Garlic (Allium sativum)**: High FODMAP due to fructans. Even 1 clove exceeds the low-FODMAP threshold per Monash. Fructans in garlic are water-soluble (not fat-soluble), which is why garlic-infused oil is tolerated.
- **Onion (Allium cepa)**: One of the highest fructan sources in the Western diet. Monash rates all varieties of bulb onion as high FODMAP at typical serving sizes. The low-FODMAP threshold is effectively zero for brown/white onion.
- **Wheat**: High FODMAP due to fructans (not gluten). Per Monash, 1 slice of regular wheat bread (approximately 35g) contains enough fructans to trigger symptoms. The low-FODMAP serving is approximately half a slice (about 24g or less), which is impractical. True sourdough fermentation reduces fructan content by up to 90% (Ziegler et al., 2016).

The 2-3 hour symptom onset observed in this patient is consistent with the typical oro-cecal transit time plus colonic fermentation lag for fructans, which are fermented by bacteria in the large intestine producing hydrogen and methane gas.

### Possible Secondary Sensitivity: Lactose (Disaccharide)

The single milk exposure on Day 3 produced gas at 2 hours, which is physiologically consistent with lactose malabsorption (undigested lactose reaching the colon for bacterial fermentation). However, this cannot be confirmed from a single exposure. Lactose intolerance affects approximately 65-70% of the global adult population, and IBS patients frequently have concurrent fructan + lactose sensitivities.

### Severity Assessment: Mild-to-Moderate IBS-D (Diarrhea-Predominant Pattern)

Based on the 7-day data:
- Symptom severity peaks at 8/10 but only with combined triggers
- Single-trigger severity ranges from 4-7/10
- Stool loosening (Type 5-6) occurs on trigger days but resolves within 24 hours
- Only one episode of urgency in 7 days
- 3 out of 7 days were entirely symptom-free
- No nocturnal symptoms reported

This pattern is consistent with **mild-to-moderate IBS with diarrhea-predominant features (IBS-D)** per Rome IV criteria (provided symptoms have been present for at least 6 months with symptom onset at least 6 months before diagnosis). The functional nature is supported by the rapid return to baseline on safe days and the clear food-trigger relationship.

### Consistency with Known IBS Patterns

This presentation aligns well with published literature:
- Fructan sensitivity is the most commonly identified FODMAP trigger in IBS populations (Shepherd et al., 2008; Halmos et al., 2014)
- The 2-4 hour lag between ingestion and symptoms matches expected colonic fermentation kinetics
- The dose-response relationship (worse symptoms with combined fructan sources) is a hallmark of FODMAP intolerance (Varney et al., 2017)
- Bristol Type 4 baseline with Type 5-6 on trigger days is a classic IBS-D pattern
- Resolution within 24 hours of trigger removal is expected in functional (non-inflammatory) disorders

---

## 4. Dietary Recommendations

### Foods to Avoid (Based on Confirmed Triggers)

| Food | FODMAP Group | Why to Avoid | Monash Serving Data |
|---|---|---|---|
| **Garlic (all forms)** | Fructans | 90% correlation with gas + bloating | High FODMAP at any serving size; no safe threshold for bulb garlic |
| **Onion (bulb -- brown, white, red)** | Fructans | 85% correlation with bloating | High FODMAP at any amount; even small amounts in sauces are problematic |
| **Wheat bread (regular)** | Fructans | 80% correlation with bloating | High FODMAP above ~24g (less than 1 slice); low-FODMAP at <24g per sitting |
| **Milk (cow's)** | Lactose (suspected) | Single occurrence with symptoms; needs further testing | High FODMAP above 1 cup (250ml); low FODMAP at <30ml (splash) |

### Safe Alternatives with Serving Sizes (Per Monash University Data)

| Instead Of | Try This | Safe Serving (Monash) | Notes |
|---|---|---|---|
| Garlic cloves | **Garlic-infused olive oil** | 1 tablespoon | Fructans are water-soluble, not fat-soluble. Oil carries flavor without FODMAPs |
| Garlic cloves | **Asafoetida (hing) powder** | 1/4 teaspoon | Provides similar savory depth; verify brand has no wheat filler |
| Onion (bulb) | **Green part of spring onion (scallion)** | Up to 1 cup, chopped | Green tops are low FODMAP; avoid white/light green bulb portion |
| Onion (bulb) | **Chives** | Up to 1 tablespoon | Low FODMAP at typical serving |
| Onion (bulb) | **Leek leaves (green part only)** | Up to 1 cup | Dark green portion only; avoid white/light green base |
| Wheat bread | **Sourdough spelt bread (true fermented)** | 2 slices (~54g) | Must be traditional long-fermentation sourdough (reduces fructans ~90%) |
| Wheat bread | **Gluten-free bread** | Per label (usually 2 slices) | Ensure certified GF; some brands contain onion/garlic powder |
| Wheat bread | **Rice cakes or corn tortillas** | 2-3 rice cakes / 2 tortillas | Naturally fructan-free grain alternatives |
| Cow's milk | **Lactose-free cow's milk** | 1 cup (250ml) | Same nutrition profile minus lactose |
| Cow's milk | **Almond milk (no inulin added)** | 1 cup (250ml) | Check label -- some brands add chicory root (inulin = fructans) |

### Foods Confirmed Safe in This Patient's Diet

The following foods were consumed on symptom-free days and can be considered safe at the recorded serving sizes:

- **Oatmeal/oats** (Day 2, 4, 6 -- no symptoms; low FODMAP up to 1/2 cup dry per Monash)
- **Blueberries** (Day 1 breakfast -- no symptoms; low FODMAP up to 1/2 cup per Monash)
- **Eggs** (Days 2, 4, 7 -- no symptoms; naturally FODMAP-free, any serving)
- **Rice** (Days 1, 3, 4, 5, 7 -- no symptoms; low FODMAP at any serving)
- **Chicken** (Days 1, 2, 4, 5, 7 -- no symptoms; FODMAP-free protein, any serving)
- **Carrots** (Days 2, 5, 7 -- no symptoms; low FODMAP up to 1 medium carrot per Monash)
- **Potatoes** (Days 2, 4, 6, 7 -- no symptoms; low FODMAP at any serving per Monash)
- **Bananas** (Days 4, 7 -- no symptoms; low FODMAP at 1 medium ripe banana per Monash; note: unripe/green bananas are high FODMAP)
- **Butter** (Day 5 -- no symptoms; FODMAP-free fat)
- **Gluten-free toast** (Day 2 -- no symptoms; confirms wheat fructans, not gluten, are the issue)

---

## 5. FODMAP Phase Guidance

### Recommendation: Transition from Elimination to Structured Reintroduction

**Rationale:**

The patient has completed 7 days of data logging during the elimination phase with clear, reproducible trigger identification. Per Monash University and NICE (National Institute for Health and Care Excellence) guidelines, the elimination phase should last **2-6 weeks** (typically 2-4 weeks is sufficient if symptoms clearly improve). Given that:

1. **Clear symptom improvement on safe days** -- Days 2, 4, and 7 demonstrated symptom-free periods with Bristol Type 4 stools, confirming that the low-FODMAP elimination diet is effective for this patient.
2. **Reproducible trigger identification** -- Fructan-containing foods (garlic, onion, wheat) triggered symptoms on multiple separate occasions with consistent time lags.
3. **Good baseline established** -- The patient has demonstrated 3+ symptom-free days, establishing a clear baseline.

However, **7 days may be insufficient** for full elimination. The standard recommendation is at least 2 weeks of strict elimination before reintroduction. I would recommend:

**Phase 1 (Current -- Continue 1-2 more weeks):** Maintain strict elimination of garlic, onion, wheat, and cow's milk. Continue logging all meals, symptoms, and bowel movements. If symptoms remain well-controlled on safe foods during this extended period, proceed to Phase 2.

**Phase 2 (Structured Reintroduction -- Start approximately Week 3-4):**

Reintroduce one FODMAP group at a time, in this recommended order:

1. **Lactose FIRST** (milk/dairy) -- Rationale: Only tested once in the current data, so sensitivity is unconfirmed. Lactose tolerance has a clear dose threshold, and lactose-free alternatives exist that preserve calcium and vitamin D intake. Reintroduce with 1/4 cup milk on Day 1, 1/2 cup on Day 2, 1 cup on Day 3. Wait 3 "washout" days.

2. **Wheat/Fructans (grain sources) SECOND** -- Rationale: Wheat showed the lowest correlation confidence (80%) and mildest symptoms (bloating 4/10 when consumed alone on Day 6). Many patients tolerate small servings of wheat. Test with 1/2 slice bread Day 1, 1 slice Day 2, 2 slices Day 3. Wait 3 washout days.

3. **Onion (fructans -- allium) THIRD** -- Rationale: Higher confidence (85%) but important to quantify threshold. Some patients tolerate small amounts of cooked onion. Test with 1/2 tablespoon cooked onion Day 1, 1 tablespoon Day 2, 2 tablespoons Day 3. Wait 3 washout days.

4. **Garlic (fructans -- allium) LAST** -- Rationale: Highest confidence trigger (90%), and Monash data shows garlic has no safe threshold for many fructan-sensitive individuals. Confirm with 1/4 clove Day 1, 1/2 clove Day 2, 1 clove Day 3.

**Phase 3 (Personalized Maintenance):** Based on reintroduction results, establish a personalized diet that includes tolerated FODMAP groups while avoiding confirmed triggers. The goal is the least restrictive diet that controls symptoms.

---

## 6. Concerns / Red Flags

### No Immediate Red Flags Identified

The following alarm features for organic GI disease are **absent** from this 7-day dataset:
- No blood in stool (no red or black stool colors reported)
- No nocturnal symptoms
- No progressive weight loss reported
- No fever
- No persistent vomiting
- No family history data available (limitation of anonymous app)

### Considerations That May Warrant In-Person GI Evaluation

1. **If symptoms persist despite strict FODMAP elimination**: This could suggest an alternative or co-existing diagnosis (e.g., SIBO, bile acid malabsorption, celiac disease, microscopic colitis). Celiac disease screening (tissue transglutaminase IgA + total IgA) should be performed BEFORE initiating a gluten-free diet if not already done.

2. **Age-dependent screening**: If the patient is over 45 (or over 40 with family history of colorectal cancer), new-onset changes in bowel habits warrant colonoscopy per AGA guidelines, regardless of IBS suspicion.

3. **Day 3 urgency episode**: A single episode of urgency with Bristol Type 6 stool is within the expected range for IBS-D after a high fructan load. However, if urgency becomes frequent or accompanied by incontinence, in-person evaluation is warranted.

4. **Nutrient deficiency risks from restricted diet**: A long-term low-FODMAP diet that eliminates wheat, onion, garlic, and potentially dairy carries risks of:
   - **Fiber deficiency** -- Wheat is a major source of dietary fiber in Western diets. Ensure adequate fiber from low-FODMAP sources (oats, rice, potatoes with skin, carrots, bananas).
   - **Calcium and Vitamin D deficiency** -- If dairy is restricted. Recommend lactose-free dairy products, fortified almond milk (without inulin), or supplementation (1000mg calcium, 1000-2000 IU Vitamin D daily).
   - **Prebiotic starvation** -- FODMAPs (particularly fructans and GOS) are prebiotics that feed beneficial gut bacteria. Long-term elimination can reduce Bifidobacterium populations (Staudacher et al., 2012). This is why reintroduction and personalization are critical -- the goal is NEVER permanent full elimination.
   - **B-vitamin risk** -- If wheat and dairy are both restricted, B-vitamin intake (particularly folate, B12) should be monitored.

5. **Psychological screening**: IBS has a strong brain-gut axis component. If this patient reports significant anxiety, depression, or catastrophizing around food, referral for gut-directed cognitive behavioral therapy or gut-directed hypnotherapy would be appropriate adjuncts to dietary management.

---

## 7. Assessment of App Data Quality

### What the App Captures Well

- **Temporal correlation**: Timestamps on meals and symptoms with user-adjustable date/time pickers enable lag-time calculation. This is critical and well-implemented.
- **Symptom specificity**: Type (bloating, gas, pain, cramping, etc.) and severity (1-10) are captured, allowing differentiation between symptom patterns.
- **Bristol Stool Chart**: Standardized stool classification with color and urgency provides clinically meaningful bowel habit data.
- **FODMAP classification**: AI-powered food analysis against Monash data adds value over plain food diaries.
- **Dose-response potential**: By capturing food items per meal with serving sizes, the app can theoretically capture dose-response relationships.
- **Correlation engine**: The 2-occurrence minimum threshold with time-lag analysis is methodologically sound for a consumer app.

### What a GI Doctor Would Want That Is Missing

The following data points would significantly enhance clinical utility:

1. **Fluid intake / hydration logging**: Dehydration affects stool consistency and can confound Bristol type data. Water, coffee (a known GI stimulant), and alcohol intake are all clinically relevant.

2. **Stress / anxiety level**: A daily stress score (1-10) or mood tracker would help identify brain-gut axis contributions. Many IBS patients report symptom flares during high-stress periods independent of dietary triggers.

3. **Sleep quality and duration**: Poor sleep is a known IBS trigger and can increase visceral hypersensitivity. A simple "hours slept" + "sleep quality" rating would be valuable.

4. **Menstrual cycle tracking** (for applicable patients): Hormonal fluctuations significantly affect GI motility and visceral sensitivity. IBS symptom severity often peaks during the late luteal and early menstrual phases.

5. **Medication and supplement log**: NSAIDs, antibiotics, probiotics, antidepressants, PPIs, and laxatives all affect GI function. Without this data, correlations may be confounded.

6. **Exercise / physical activity**: Both excessive and insufficient exercise affect GI motility. A simple activity log would help.

7. **Portion size precision**: The current system relies on AI estimation of serving sizes from photos or user text entry. Actual gram weights (from a food scale) would improve FODMAP dose calculations, though this is admittedly impractical for most users.

8. **Symptom duration**: The app captures when symptoms start and their severity but does not appear to track when symptoms resolve. Duration of symptoms (30 minutes vs. 6 hours of bloating) has clinical significance.

9. **Gas frequency/type**: Differentiating between flatulence and abdominal distension/bloating is clinically important but not captured.

10. **Family history and medical history intake**: Even with anonymous auth, a structured questionnaire about family history of IBD, celiac disease, colorectal cancer, and personal history of surgeries, infections (e.g., post-infectious IBS), and comorbidities would be valuable.

11. **Fecal calprotectin integration**: If the app could integrate with home fecal calprotectin test kits (e.g., from companies like TruCheck), this would help differentiate functional IBS from inflammatory bowel disease, which is one of the most important diagnostic distinctions in GI.

---

## 8. Recommendations for the App

### High Priority (Clinical Utility)

1. **Symptom duration tracking**: Add an "ended at" timestamp or duration field to symptom entries. A symptom lasting 30 minutes is very different from one lasting 8 hours.

2. **Multi-day correlation view**: The current engine analyzes food-to-symptom pairs, but a timeline visualization showing meals, symptoms, and bowel movements on a single horizontal timeline (hour by hour) would help both patients and clinicians see patterns visually.

3. **Cumulative FODMAP load per meal**: Rather than flagging individual foods, calculate the total fructan/lactose/GOS/polyol load per meal and per day. This would capture the dose-stacking effect observed on Day 3 (onion soup + wheat bread = higher combined fructan load = worse symptoms).

4. **Reintroduction protocol assistant**: Build a structured reintroduction workflow that guides patients through the Monash 3-day challenge + 3-day washout protocol for each FODMAP group, with specific food suggestions and serving size escalation at each step.

5. **Lactose/milk correlation flagging**: The engine correctly requires 2+ occurrences, but single-occurrence food-symptom pairs with plausible FODMAP mechanisms should be flagged as "suspected triggers -- needs more data" rather than being silently omitted. This would have caught the milk-gas association.

6. **Medication/supplement logging**: Add a simple daily medication log. This is essential for avoiding confounded correlations.

### Medium Priority (User Experience and Engagement)

7. **Stress and sleep trackers**: Add daily stress (1-10) and sleep (hours + quality) inputs to the daily logging flow. These are low-effort for the user and high-value for correlation.

8. **Fluid intake tracker**: A simple water/coffee/alcohol counter per day.

9. **Export to PDF for clinician**: Allow users to generate a structured PDF report of their logging data (meals, symptoms, correlations, Bristol chart) that they can bring to a GI appointment. This would bridge the gap between consumer app and clinical tool.

10. **Stool color educational content**: The app tracks stool color but the clinical implications are not surfaced. Red or black stool should trigger an immediate prominent warning to seek medical attention.

11. **Fiber intake estimation**: Given that low-FODMAP diets risk fiber deficiency, tracking estimated daily fiber intake and alerting when it falls below 25g/day would be a valuable safety feature.

### Lower Priority (Advanced Features)

12. **Gut microbiome integration**: If the app could ingest results from consumer microbiome tests (e.g., from Ombre, Viome, or Thryve), AI correlations between microbiome composition and symptom patterns could provide deeper insights.

13. **Wearable integration**: Heart rate variability (HRV) from Apple Watch or similar devices correlates with autonomic nervous system activity and stress, which affects GI function. Passive data collection would enrich the dataset without additional user burden.

14. **Clinician portal**: A separate web dashboard where a patient's GI doctor could review their app data (with patient consent) would make this a genuine clinical tool rather than a consumer wellness app.

---

## 9. Summary Assessment

This patient presents with a clear fructan sensitivity pattern consistent with IBS-D (mild-to-moderate). The app's correlation engine correctly identified the three primary fructan triggers (garlic, onion, wheat bread) with appropriate confidence levels and time-lag estimates. The alternating trigger/safe day pattern, the dose-response relationship on Day 3, and the rapid stool normalization on safe days all support a functional GI disorder responsive to FODMAP dietary management.

The patient should continue strict elimination for 1-2 more weeks, then begin structured reintroduction starting with lactose, followed by wheat fructans, then allium fructans (onion before garlic). Long-term management should focus on the least restrictive diet that maintains symptom control, with attention to fiber, calcium, and prebiotic intake.

The app provides a solid foundation for food-symptom tracking and AI-driven correlation analysis. The addition of symptom duration, stress/sleep tracking, medication logging, cumulative FODMAP load calculation, and a structured reintroduction protocol would elevate it from a useful consumer tool to a clinically meaningful IBS management platform.

---

## Disclaimer

**This is not medical advice. Please consult a healthcare professional for diagnosis and treatment.** This assessment is generated for educational and wellness purposes only. It is not intended to diagnose, treat, cure, or prevent any medical condition. The correlations and recommendations discussed herein are based on simulated patient data from a consumer wellness application and should not replace professional medical evaluation, diagnostic testing, or individualized treatment planning by a licensed gastroenterologist or registered dietitian specializing in digestive disorders.

---

### References

- Monash University FODMAP Diet App and Database (monashfodmap.com)
- Halmos EP, Power VA, Shepherd SJ, Gibson PR, Muir JG. A diet low in FODMAPs reduces symptoms of irritable bowel syndrome. *Gastroenterology*. 2014;146(1):67-75.
- Shepherd SJ, Parker FC, Muir JG, Gibson PR. Dietary triggers of abdominal symptoms in patients with irritable bowel syndrome: randomized placebo-controlled evidence. *Clin Gastroenterol Hepatol*. 2008;6(7):765-771.
- Staudacher HM, Lomer MC, Anderson JL, et al. Fermentable carbohydrate restriction reduces luminal bifidobacteria and gastrointestinal symptoms in patients with irritable bowel syndrome. *J Nutr*. 2012;142(8):1510-1518.
- Varney J, Barrett J, Scarlata K, Catsos P, Gibson PR, Muir JG. FODMAPs: food composition, defining cutoff values and international application. *J Gastroenterol Hepatol*. 2017;32(Suppl 1):53-61.
- Ziegler JU, Steiner D, Longin CFH, Wurschum T, Schweiggert RM, Carle R. Wheat and the irritable bowel syndrome -- FODMAP levels of modern and ancient species and their retention during bread making. *J Funct Foods*. 2016;25:257-266.
- Lacy BE, Mearin F, Chang L, et al. Bowel Disorders. *Gastroenterology*. 2016;150(6):1393-1407 (Rome IV criteria).
