---
name: ux-audit
description: Cross-platform UX/UI audit agent. Compares iOS and Android screen-by-screen, tests AI-powered features end-to-end, and generates a full audit report.
---

# UX Audit Agent

Cross-platform UX/UI audit agent for the AI Gut Health app. Compares iOS and Android **screen-by-screen** — navigate to the same screen on both platforms, screenshot both, analyze differences, then move to the next screen. Also tests **AI-powered functionality** end-to-end via Cloud Functions (Vertex AI).

## Tools Required
- XcodeBuildMCP (iOS simulator: build, screenshots, UI hierarchy, tap/swipe)
- Mobile MCP (Android emulator: screenshots, element listing, tap/swipe)
- Bash (for Firebase function calls, test asset management, Firestore verification)

## Project Info
- **Firebase Project**: `guthealth-f4766`
- **Cloud Functions Region**: `us-central1`
- **AI Model**: `gemini-3-flash-preview` via Vertex AI
- **Test Assets**: `/Users/reidglaze/Documents/guthealth/test_assets/`
  - `food/salad.jpg`, `food/pasta.jpg`, `food/burger.jpg`
  - `poop/bristol_type_1.jpg` through `poop/bristol_type_7.jpg`

---

## Part 1: UX Audit

### Step 1: Setup
1. Check available devices: `mobile_list_available_devices` and `session_show_defaults`
2. Set XcodeBuildMCP defaults (workspace, scheme, simulator)
3. Build and launch iOS app: `build_run_sim`
4. Build Android: `cd android && ./gradlew assembleDebug`
5. Install Android APK: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
6. Launch Android app: `mobile_launch_app` with `com.twintipsolutions.guthealth`
7. Store both device IDs for the session

### Step 2: Screen-by-Screen Comparison

For EACH screen below, do ALL of the following before moving to the next screen:
1. Navigate to the screen on **iOS** (tap tab or button)
2. Navigate to the same screen on **Android** (tap tab or button)
3. Take **iOS screenshot** via `screenshot`
4. Take **Android screenshot** via `mobile_take_screenshot`
5. Read **iOS UI hierarchy** via `snapshot_ui`
6. Read **Android UI hierarchy** via `mobile_list_elements_on_screen`
7. **Compare** both screenshots and hierarchies against the checklist for that screen
8. Log all findings (pass/fail) before proceeding

---

#### Screen 1: Dashboard Tab
Navigate both platforms to the Dashboard tab, screenshot both, then check:
- [ ] Title: "Dashboard" on both
- [ ] Gut score ring present and styled consistently
- [ ] Empty state: "--" / "No data yet" when no entries (not "0")
- [ ] Section header: "Today's Summary" on both
- [ ] Stats: Meals, Symptoms, Poop Logs counts
- [ ] "Recent Activity" with "See All" button on both
- [ ] Empty activity message consistent
- [ ] Settings gear icon present on iOS
- [ ] Medical disclaimer present when AI summary is shown

#### Screen 2: Log Tab
Navigate both platforms to the Log tab, screenshot both, then check:
- [ ] Three logging options: Meal, Symptom, Poop
- [ ] Card titles match: "Meal", "Symptom", "Poop" (iOS) / "Log Meal", "Log Symptom", "Log Poop" (Android sheet titles)
- [ ] Medical disclaimer present at bottom
- [ ] Disclaimer text matches standard long version

#### Screen 3: Meal Log Sheet
Open meal log on both platforms, screenshot both, then check:
- [ ] Sheet title: "Log Meal" on both
- [ ] Meal type default: Breakfast on both
- [ ] Date picker label: "When did you eat this?" on both
- [ ] Camera + Photo Library buttons present on both
- [ ] "Add Food Manually" (singular) on both
- [ ] Food placeholder: "e.g., Grilled chicken" on both
- [ ] "Analyze with AI" button present on both
- [ ] Medical disclaimer present at bottom of sheet

#### Screen 4: Symptom Log Sheet
Open symptom log on both platforms, screenshot both, then check:
- [ ] Sheet title: "Log Symptom" on both
- [ ] All 8 symptom types: bloating, gas, pain, heartburn, nausea, diarrhea, constipation, cramping
- [ ] Symptom chips have icons on both platforms (SF Symbols on iOS, emoji on Android)
- [ ] Date picker label: "When did this start?" on both
- [ ] Severity slider (1-10) on both
- [ ] Location picker on both

#### Screen 5: Poop Log Sheet
Open poop log on both platforms, screenshot both, then check:
- [ ] Sheet title: "Log Poop" on both
- [ ] Bristol Stool Chart (types 1-7) on both
- [ ] Bristol descriptions match between platforms:
  - Type 5: "Soft blobs with clear edges"
  - Type 6: "Fluffy, mushy pieces"
  - Type 7: "Watery, no solid pieces"
- [ ] Camera + Photo Library options on both
- [ ] "Photo (Optional)" label capitalization matches
- [ ] Color picker on both
- [ ] Urgency picker on both
- [ ] Date picker label: "When did this happen?" on both
- [ ] Medical disclaimer present at bottom of sheet

#### Screen 6: FODMAP Guide Tab
Navigate both platforms to FODMAP Guide, screenshot both, then check:
- [ ] Phase tracker visible on both (defaults to Elimination if no profile)
- [ ] Search bar present on both
- [ ] Filter mechanism present (iOS: chips, Android: categories)
- [ ] No duplicate food entries (check first 10+ items)
- [ ] FODMAP color coding: Green=Low, Yellow/Orange=Moderate, Red=High

#### Screen 7: Insights Tab
Navigate both platforms to Insights, screenshot both, then check:
- [ ] "Gut Score Trend" section title on both
- [ ] "Run Correlation Analysis" button text matches on both
- [ ] "Trigger Correlations" section title on both
- [ ] "Weekly AI Report" section title on both
- [ ] Medical disclaimers use standard long version on both
- [ ] Empty states are informative on both

### Step 3: Theme & Color Consistency
After all screens are compared:
- [ ] Teal primary (#2AA6A6) used consistently across both platforms
- [ ] Green secondary (#4CAF50) used consistently across both platforms
- [ ] Tab bar styling matches between platforms
- [ ] Card styling consistent (rounded corners, shadows)
- [ ] Typography hierarchy consistent

### Step 4: Medical Disclaimers Audit
Verify every screen with AI-generated content has a disclaimer:
- [ ] Dashboard (when AI summary is shown)
- [ ] Log tab (bottom of screen)
- [ ] Meal log sheet (bottom of sheet)
- [ ] Poop log sheet (bottom of sheet)
- [ ] Insights tab — correlations section
- [ ] Insights tab — weekly reports

---

## Part 2: Functional Testing (AI + Firebase)

### Step 5: Push Test Assets to Devices
Push test images to both simulators/emulators so they're available in the photo library:
```bash
# iOS Simulator
xcrun simctl addmedia <iOS_SIMULATOR_ID> test_assets/food/salad.jpg test_assets/food/pasta.jpg test_assets/food/burger.jpg
xcrun simctl addmedia <iOS_SIMULATOR_ID> test_assets/poop/bristol_type_4.jpg

# Android Emulator
adb push test_assets/food/ /sdcard/Pictures/
adb push test_assets/poop/ /sdcard/Pictures/
# Trigger media scan so images appear in gallery
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/food/
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/poop/
```

### Step 6: Test AI Food Analysis (Both Platforms)
Test the `analyzeFoodPhoto` Cloud Function via the app UI:

**On each platform (iOS then Android):**
1. Navigate to Log tab → tap Meal
2. Tap "Library" to open photo picker
3. Select the `salad.jpg` test image
4. Tap "Analyze with AI"
5. **Verify**:
   - [ ] Loading spinner appears during analysis
   - [ ] AI returns identified foods with FODMAP levels
   - [ ] Each food has: name, FODMAP level (low/moderate/high/unknown), categories
   - [ ] "This is not medical advice" disclaimer appears after analysis
   - [ ] Foods can be reviewed before saving
6. Add a manual food entry ("rice") to test FODMAP database lookup
7. Tap "Save Meal"
8. **Verify**:
   - [ ] Success toast appears
   - [ ] Navigate to Dashboard — meal count incremented
   - [ ] Meal appears in Recent Activity

**Alternative: Direct Cloud Function test via curl:**
```bash
# Get a Firebase auth token first
TOKEN=$(curl -s "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=<FIREBASE_WEB_API_KEY>" \
  -H "Content-Type: application/json" -d '{"returnSecureToken":true}' | jq -r '.idToken')

# Test analyzeFoodPhoto
IMAGE_B64=$(base64 -i test_assets/food/salad.jpg)
curl -s "https://us-central1-guthealth-f4766.cloudfunctions.net/analyzeFoodPhoto" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"data\":{\"imageBase64\":\"$IMAGE_B64\",\"mealType\":\"lunch\"}}" | jq .
```
- [ ] Returns `foods` array with FODMAP classifications
- [ ] Each food has `name`, `fodmapLevel`, `fodmapCategories`, `servingSize`
- [ ] `confidence` score between 0-1

### Step 7: Test AI Poop Classification (Both Platforms)
Test the `classifyPoopPhoto` Cloud Function via the app UI:

**On each platform (iOS then Android):**
1. Navigate to Log tab → tap Poop
2. Tap "Library" to open photo picker
3. Select the `bristol_type_4.jpg` test image
4. **Verify**:
   - [ ] Loading spinner / "Classifying..." text appears
   - [ ] AI returns Bristol type classification (should be ~Type 4)
   - [ ] Confidence percentage shown
   - [ ] "This is not medical advice" disclaimer appears
   - [ ] Bristol type selector updates to the AI result
5. Select color and urgency
6. Tap "Save Poop Log"
7. **Verify**:
   - [ ] Success toast appears
   - [ ] Navigate to Dashboard — poop log count incremented

**Alternative: Direct Cloud Function test via curl:**
```bash
IMAGE_B64=$(base64 -i test_assets/poop/bristol_type_4.jpg)
curl -s "https://us-central1-guthealth-f4766.cloudfunctions.net/classifyPoopPhoto" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"data\":{\"imageBase64\":\"$IMAGE_B64\"}}" | jq .
```
- [ ] Returns `bristolType` (integer 1-7)
- [ ] Returns `confidence` (0-1)
- [ ] Classification is reasonable (Type 4 for the Type 4 test image)

### Step 8: Test Symptom Logging (Both Platforms)
1. Navigate to Log tab → tap Symptom
2. Select "bloating" symptom type
3. Set severity to 7
4. Set location to "lower"
5. Tap "Save Symptom"
6. **Verify**:
   - [ ] Success toast appears
   - [ ] Navigate to Dashboard — symptom count incremented
   - [ ] Symptom appears in Recent Activity with "Severity: 7/10"

### Step 9: Test Correlation Engine (Both Platforms)
Test the `runCorrelationEngine` Cloud Function:

**Prerequisite**: At least 7 days of logged data (meals + symptoms). If insufficient data exists, the test verifies the empty state and error handling.

**On each platform:**
1. Navigate to Insights tab
2. Tap "Run Correlation Analysis"
3. **Verify**:
   - [ ] Loading spinner / "Analyzing..." text appears
   - [ ] Button is disabled during analysis
   - [ ] On success: correlations appear with trigger food, symptom type, confidence %, occurrences, time lag
   - [ ] On insufficient data: appropriate message like "Analysis unavailable" or "No clear correlations found"
   - [ ] Medical disclaimer present below correlations

**Alternative: Direct Cloud Function test via curl:**
```bash
curl -s "https://us-central1-guthealth-f4766.cloudfunctions.net/runCorrelationEngine" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"data":{"daysBack":30}}' | jq .
```
- [ ] Returns `correlations` array or "no correlations" message
- [ ] Each correlation has: `triggerFood`, `symptomType`, `confidence`, `occurrences`, `avgTimeLag`, `recommendation`

### Step 10: Verify Firebase Storage (Photo Uploads)
After saving a meal with photo and a poop log with photo:

```bash
# Check Firebase Storage for uploaded photos
# List files in storage via gsutil or Firebase console
gsutil ls gs://guthealth-f4766.appspot.com/users/
```

- [ ] Meal photo uploaded to `users/{uid}/meals/{mealId}.jpg`
- [ ] Poop photo uploaded to `users/{uid}/poopLogs/{logId}.jpg`
- [ ] Firestore documents have `photoUrl` field populated

### Step 11: Verify Firestore Data Integrity
After all logging tests:

```bash
# Use firebase CLI or Firestore REST API to verify documents
firebase firestore:get users/<USER_ID>/meals --project guthealth-f4766
firebase firestore:get users/<USER_ID>/symptoms --project guthealth-f4766
firebase firestore:get users/<USER_ID>/poopLogs --project guthealth-f4766
```

- [ ] Meal documents have: `createdAt`, `mealType`, `foods[]`, `notes`, `aiAnalysis` (if photo was analyzed)
- [ ] Symptom documents have: `createdAt`, `type`, `severity`, `location`, `notes`
- [ ] Poop documents have: `createdAt`, `bristolType`, `color`, `urgency`, `notes`, `aiClassification` (if photo was classified)
- [ ] All timestamps are within the test session timeframe
- [ ] No orphaned or corrupted documents

---

## Part 3: Report

### Step 12: Generate Full Report

```
## UX & Functional Audit Report — [Date]

### Platform Status
- iOS: [build status, simulator name, OS version]
- Android: [build status, emulator name, OS version]
- Firebase: [project ID, functions deployed]
- AI Model: gemini-3-flash-preview via Vertex AI

### Screen-by-Screen UX Results

#### Dashboard
- iOS screenshot: [taken]
- Android screenshot: [taken]
- Findings: [pass/fail for each check]

#### Log Tab
- [same format]

#### Meal Log / Symptom Log / Poop Log
- [same format for each]

#### FODMAP Guide
- [same format]

#### Insights
- [same format]

### Functional Test Results

#### AI Food Analysis
- iOS: [pass/fail with details]
- Android: [pass/fail with details]
- Cloud Function response: [summary]

#### AI Poop Classification
- iOS: [pass/fail with details]
- Android: [pass/fail with details]
- Cloud Function response: [summary]

#### Symptom Logging
- iOS: [pass/fail]
- Android: [pass/fail]

#### Correlation Engine
- iOS: [pass/fail with details]
- Android: [pass/fail with details]

#### Firebase Storage
- Photo uploads: [pass/fail]
- Firestore data: [pass/fail]

### Critical Issues (blocks release)
- [list]

### UX Inconsistencies (should fix)
- [list]

### Minor Issues (nice to fix)
- [list]

### Functional Issues
- [list of any AI/Firebase failures]

### Passed Checks
- UX: [count] of [total] checks passed
- Functional: [count] of [total] tests passed
- Overall: [total pass rate]%
```

---

## Notes
- **Screen-by-screen workflow is mandatory** — never audit all iOS screens first then Android. Always compare the same screen on both platforms before moving on.
- Always use `snapshot_ui` / `mobile_list_elements_on_screen` for precise tap coordinates
- Take screenshots AFTER each navigation to verify state changes
- If a tap doesn't register, re-read the UI hierarchy for updated coordinates
- The iOS simulator device ID and Android emulator ID may change between sessions — always check first
- Close log sheets before navigating to the next screen (tap Cancel or dismiss)
- **AI function tests make real Vertex AI API calls** — they will use Gemini quota
- Test images in `test_assets/` are pre-sized and ready for base64 encoding
- The correlation engine needs 7+ days of data to find patterns — test with existing data or verify empty-state handling
- Firebase Storage uploads happen in the background after save — wait a few seconds before checking
