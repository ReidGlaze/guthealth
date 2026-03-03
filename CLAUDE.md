# AI Gut Health & IBS Tracker

## Owner
Reid Glaze — Twin Tip Solutions LLC

## App Store Listing
- **Title**: `AI Gut Health & IBS Tracker` (27 chars, max 30)
- **Subtitle**: `FODMAP Food Diary & Poop Log` (28 chars, max 30)
- **Bundle ID**: `com.twintipsolutions.guthealth`
- **Firebase Project**: `guthealth-f4766`

## Tech Stack
- **Backend**: GCP Firebase (Firestore, Storage, Auth, Cloud Functions, Messaging)
- **Cloud Functions**: TypeScript, Node 22, `@google/genai` SDK with `gemini-3-flash-preview`
- **iOS**: Swift 6.1+, SwiftUI, iOS 18.0+, SPM only (no CocoaPods)
- **Android**: Kotlin, Jetpack Compose, CameraX, Gradle with version catalog
- **Monetization**: Free (no paywall)

## Legal Positioning
- "Educational wellness tool" — NOT medical diagnosis
- Never use "diagnose", "treat", "cure" — triggers FDA SaMD classification
- Mandatory disclaimers on all AI-generated content: "This is not medical advice"
- Anonymous auth only (no PII collection)

---

## Firestore Data Model

### `/users/{userId}` — User Profile
```
{
  displayName: string,
  createdAt: Timestamp,
  preferences: {
    reminderTimes: string[],       // ["08:00", "12:00", "18:00"]
    timezone: string,              // "America/Denver"
    notificationsEnabled: boolean
  },
  fodmapPhase: "elimination" | "reintroduction" | "maintenance",
  fodmapPhaseStartDate: Timestamp,
  fcmToken: string
}
```

### `/users/{userId}/meals/{mealId}` — Food Diary
```
{
  createdAt: Timestamp,
  mealType: "breakfast" | "lunch" | "dinner" | "snack",
  photoUrl: string | null,
  foods: [{
    name: string,
    fodmapLevel: "low" | "moderate" | "high" | "unknown",
    fodmapCategories: string[],    // ["fructans", "lactose", etc.]
    servingSize: string,
    triggers: string[]             // known trigger categories
  }],
  notes: string,
  aiAnalysis: {
    rawResponse: string,
    analyzedAt: Timestamp
  }
}
```

### `/users/{userId}/symptoms/{symptomId}` — Symptom Log
```
{
  createdAt: Timestamp,
  type: "bloating" | "gas" | "pain" | "heartburn" | "nausea" | "diarrhea" | "constipation" | "cramping",
  severity: number,               // 1-10
  location: string | null,        // "upper", "lower", "left", "right"
  notes: string
}
```

### `/users/{userId}/poopLogs/{logId}` — Poop Tracker
```
{
  createdAt: Timestamp,
  bristolType: number,            // 1-7 (Bristol Stool Chart)
  color: "brown" | "dark" | "light" | "green" | "yellow" | "red" | "black",
  urgency: "normal" | "urgent" | "emergency",
  photoUrl: string | null,
  aiClassification: {
    bristolType: number,
    color: string,
    analyzedAt: Timestamp
  } | null,
  notes: string
}
```

### `/users/{userId}/dailySummaries/{dateKey}` — Daily Stats
```
// dateKey format: "2026-02-28"
{
  date: string,
  totalMeals: number,
  totalSymptoms: number,
  totalPoopLogs: number,
  highFodmapCount: number,
  avgSymptomSeverity: number,
  dominantSymptom: string | null,
  generatedAt: Timestamp,
  aiSummary: string               // one-paragraph AI summary
}
```

### `/users/{userId}/correlationReports/{reportId}` — Correlation Analysis Reports
```
{
  createdAt: Timestamp,
  periodStart: string,            // ISO date, 7 days before run
  periodEnd: string,              // ISO date, day of run
  mealsAnalyzed: number,
  symptomsAnalyzed: number,
  poopLogsAnalyzed: number,
  aiReport: string,               // full narrative analysis (triggers, patterns, stool changes, recommendations)
  disclaimer: string              // "This is not medical advice"
}
```

### `/fodmapDatabase/{foodId}` — FODMAP Reference (read-only)
```
{
  name: string,
  category: string,               // "fruit", "vegetable", "grain", "dairy", etc.
  fodmapLevel: "low" | "moderate" | "high",
  fodmapCategories: string[],     // ["fructose", "lactose", "fructans", "GOS", "mannitol", "sorbitol"]
  servingSize: string,            // "1 cup", "2 tablespoons"
  lowFodmapServing: string,       // safe serving size
  notes: string
}
```

---

## Cloud Functions

### 1. `analyzeFoodPhoto` (onCall)
- **Input**: `{ imageBase64: string, mealType?: string }`
- **Process**: Send photo to Gemini 3 Flash → identify foods, FODMAP levels, triggers
- **Output**: `{ foods: Food[] }`
- **Model**: `gemini-3-flash-preview` with LOW thinking (fast response needed)

### 2. `classifyPoopPhoto` (onCall)
- **Input**: `{ imageBase64: string }`
- **Process**: Send photo to Gemini → Bristol Stool Chart classification + color detection + observations
- **Output**: `{ bristolType: number, color: string, observations: string }`
- **Model**: `gemini-3-flash-preview` with LOW thinking

### 3. `runCorrelationEngine` (onCall)
- **Input**: `{ userId: string, daysBack?: 3 | 7 | 10 }`
- **Process**: Query user's meals + symptoms + poopLogs over last N days (default 7, options: 3, 7, 10) → send all data to Gemini → get narrative analysis report
- **All three signals**: Meals (what you ate), symptoms (how you felt), poop logs (what came out) — all analyzed together holistically
- **Poop as signal**: Abnormal Bristol types (1-2, 6-7) count as "something went wrong" even without an explicit symptom log. Poop data also strengthens/weakens meal→symptom correlations.
- **No numerical confidence scores**: AI writes plain-English narrative explaining what patterns it sees, how strong they are, and why (e.g., "You ate onions 4 times this week. Three of those times you had bloating within a few hours and your stool shifted from Type 4 to Type 6.")
- **Output**: Writes a single `correlationReports` document with the full narrative. Past reports are stored so users can look back over time.
- **Model**: `gemini-3-flash-preview` with HIGH thinking (complex analysis)

### 4. `generateDailySummary` (onSchedule — every day 11pm user's timezone)
- **Process**: Aggregate day's meals, symptoms, poop logs → generate summary
- **Output**: Writes dailySummary document

### 5. `sendReminders` (onSchedule — every hour)
- **Process**: Check users whose reminder times match current hour → send FCM push
- **Output**: Push notifications via FCM

---

## iOS Architecture

### Pattern: MV (Model-View) with SwiftUI
- No ViewModels — use `@State`, `@Observable`, `@Environment`, `@Binding`
- All features in a Swift Package (`GutHealthPackage/`)
- App target is a thin wrapper that imports the package
- Swift Concurrency only (async/await, actors, @MainActor) — no GCD
- Swift Testing framework for tests

### Project Structure
```
ios/
├── GutHealth.xcworkspace/
├── GutHealth.xcodeproj/
├── GutHealth/
│   ├── Assets.xcassets/
│   ├── GutHealthApp.swift          # @main entry point
│   ├── GoogleService-Info.plist
│   └── Podfile                     # Firebase pods
├── GutHealthPackage/
│   ├── Package.swift
│   ├── Sources/
│   │   ├── App/                    # Root navigation, tab bar
│   │   ├── Dashboard/              # Dashboard tab
│   │   ├── Log/                    # Quick log tab (meal/symptom/poop)
│   │   ├── FODMAPGuide/            # FODMAP guide tab
│   │   ├── Insights/               # Insights tab
│   │   ├── Models/                 # Shared data models
│   │   ├── Services/               # Firebase, AI, Auth services
│   │   └── Shared/                 # Reusable UI components
│   └── Tests/
└── Config/
    ├── Debug.xcconfig
    ├── Release.xcconfig
    └── Shared.xcconfig
```

### 4-Tab Navigation
1. **Dashboard** — daily view with date navigation, logged entries in 3 sections
2. **Log** — quick log buttons (meal photo, symptom, poop)
3. **FODMAP Guide** — elimination/reintroduction phase tracking, food search
4. **Insights** — run correlation analysis, view past reports (no gut score trend chart)

---

## Android Architecture

### Pattern: MVVM-Light with Jetpack Compose
- Compose for all UI, no XML layouts
- CameraX for food/poop photos
- Navigation Compose for screen routing
- Gradle with version catalog (`libs.versions.toml`)

### Project Structure
```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/twintipsolutions/guthealth/
│   │   │   ├── GutHealthApp.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── navigation/
│   │   │   │   └── AppNavigation.kt
│   │   │   ├── ui/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── log/
│   │   │   │   ├── fodmap/
│   │   │   │   └── insights/
│   │   │   ├── data/
│   │   │   │   ├── FirestoreService.kt
│   │   │   │   └── models/
│   │   │   └── util/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── google-services.json
```

### Same 4-Tab Navigation as iOS
Bottom navigation bar with Dashboard, Log, FODMAP Guide, Insights tabs.

---

## Agent Team Instructions

### ios-builder
- **MCP**: XcodeBuildMCP (build, run simulator, screenshots, tap/swipe, LLDB)
- **Workflow**: Write Swift → build → run on simulator → screenshot → validate UI → fix → iterate
- **Architecture**: Follow MV pattern in CLAUDE.md. All code in GutHealthPackage. Use @Observable, @State, @Environment.
- **Firebase**: SPM for Firebase SDK (no CocoaPods). Use `@Environment(FirebaseService.self)` for Firestore/Auth/Storage access.

### android-builder
- **MCP**: Mobile MCP (screenshots, taps, swipes, navigation, app install/launch)
- **Workflow**: Write Kotlin → gradle build → install on emulator → screenshot → validate UI → fix → iterate
- **Architecture**: MVVM-light with Compose. Use Hilt for DI. CameraX for camera.

### functions-dev
- **Tools**: Bash (firebase emulators), Edit/Write
- **Workflow**: Write TypeScript → build → deploy to emulator → test with curl → verify Firestore writes → fix → iterate
- **Architecture**: Follow patterns from ACL Rehab Tracker's index.ts. Use `@google/genai` SDK, `onCall` for client functions, `onSchedule` for cron jobs.
- **Model**: `gemini-3-flash-preview` with Vertex AI global endpoint

### Development Order
1. **functions-dev** builds backend first (Firestore model, all 5 Cloud Functions)
2. **ios-builder** and **android-builder** work in parallel once functions are ready
3. Each builder: write screen → build → run → screenshot → validate → fix → next screen
4. Team lead coordinates, reviews screenshots, ensures consistent UX

---

## MCP Servers

### XcodeBuildMCP (iOS)
```bash
claude mcp add XcodeBuildMCP -- npx -y xcodebuildmcp@latest mcp
```
59 tools: build, run simulator, screenshots, tap/swipe UI, LLDB debugging, video recording

### Mobile MCP (Android)
```bash
claude mcp add mobile-mcp -- npx -y @mobilenext/mobile-mcp@latest
```
Android emulator: screenshots, taps, swipes, navigation, app install/launch

---

## App Features Summary
1. **AI Food Scanner** — photo (camera OR photo library) → Gemini identifies foods + FODMAP levels
2. **Symptom Logger** — bloating/gas/pain/heartburn/nausea, severity 1-10
3. **Poop Tracker** — Bristol Stool Chart 1-7, optional photo classification (camera OR photo library)
4. **Dashboard** — daily overview with date navigation and logged entries
5. **Correlation Analysis** — AI analyzes meals + symptoms + poop logs together, produces narrative report explaining patterns and triggers
6. **Past Reports** — stored correlation reports so users can look back at their history
7. **FODMAP Elimination Guide** — guided elimination + reintroduction phases
8. **Push Reminders** — "log your meals" notifications (deferred — not on first launch, only when user enables)

---

## UX Decisions
- **Photo input**: Always offer both Camera and Photo Library options side by side for meal and poop logging
- **Poop log flow**: Single screen matching the meal flow — camera/library buttons at top, Bristol type + color + urgency fields always visible below. Nothing pre-selected by default — user must choose (or let AI fill from photo). Photo pre-fills Bristol type, color, and observations (notes). Save disabled until Bristol type and color are both selected.
- **Meal log flow**: Single screen — camera/library buttons at top, food list + manual "Add Food" below. Photo pre-fills the food list.
- **Notifications**: Do NOT request permission on app launch — defer to when user explicitly enables reminders
- **App display name**: "AI Gut Health" (with spaces) under home screen icon
- **FODMAP color coding**: Green = low, Yellow = moderate, Red = high (consistent across both platforms)
- **Timestamps**: Every log entry (meal, symptom, poop) must have a user-adjustable date/time picker, defaulting to now. Labels: "When did you eat this?" / "When did this start?" / "When did this happen?". Critical for correlation engine accuracy.
- **Medical disclaimers**: Required on every screen with AI-generated content
- **Theme**: Teal primary (#2AA6A6), green secondary (#4CAF50), consistent on both platforms
- **Dynamic colors**: Disabled on Android to maintain consistent teal/green branding
- **Onboarding**: 6-page flow — Welcome, How It Works, What Are FODMAPs, The Elimination Diet, Phase Selection, Stay Consistent
- **Serving size warnings**: AI food analysis shows amber warning when estimated portion differs from safe FODMAP serving (per Monash data)
- **Dashboard date navigation**: Tappable date at top with left/right arrows to browse any past day. Defaults to today. Shows entries grouped into 3 sections: Meals, Symptoms, Poop Logs.
- **Dashboard entry details**: Each entry shows its data including photos for meals. Poop photos are hidden by default — tap to reveal (privacy). Swipe left to delete an entry.
- **Streak card**: Always visible on dashboard with progress bar toward milestones at days 7, 14, 21
- **Today's checklist**: Dashboard shows logging checklist (meal/symptoms/poop) with green checkmarks for completed items (today only)
- **Stool color safety warning**: When user selects red or black stool color, show immediate warning: "Red or black stool can indicate bleeding. If you haven't eaten foods that could cause this color (beets, iron supplements, etc.), please seek medical attention."
- **FODMAP terminology**: Always use "fructans" (not "gluten") when referring to wheat/garlic/onion triggers — gluten is a celiac concern, fructans are the IBS/FODMAP trigger
- **Sourdough recommendations**: Always specify "traditional long-fermentation sourdough" — commercial sourdough often uses accelerated processes that don't reduce fructans
- **Correlation analysis**: Fully AI-driven — Gemini analyzes meals + symptoms + poop logs holistically, produces plain-English narrative report (no numerical confidence scores). Abnormal poop (Bristol 1-2 or 6-7) counts as a signal even without an explicit symptom log. User selects time period (3, 7, or 10 days) before running — default 7. Past reports stored for history.

## Website (Vercel)
- **Location**: `/website/` — Next.js 14+ App Router, Tailwind CSS
- **URL**: TBD (deploy with `vercel deploy`)
- **Pages**: Landing Page (/), Privacy Policy (/privacy), Terms of Service (/terms), Support (/support)
- **Purpose**: App marketing landing page + App Store required links for privacy policy and terms
- **Design**: Teal/green theme matching the app

## Test Assets
- `test_assets/food/` — salad.jpg, pasta.jpg, burger.jpg (Unsplash photos for AI food scanner testing)
- `test_assets/poop/` — bristol_type_1.jpg through bristol_type_7.jpg (labeled placeholders for classifier testing)
- Push to iOS simulator: `xcrun simctl addmedia <UDID> <files>`
- Push to Android emulator: `adb push <files> /sdcard/Pictures/`
