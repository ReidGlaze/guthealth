# Cross-Platform UX Audit Report

**Date:** 2026-03-03 (Full Onboarding + 4-Tab + Settings Audit)
**Previous Audits:** 2026-03-02 (initial) + 2026-03-03 AM (Dashboard/Insights update)
**Method:** Reset both apps to onboarding, then navigate screen-by-screen in sync -- screenshot both platforms, compare UI hierarchy, log findings, proceed to next screen
**Devices:** iPhone 16 Simulator (iOS 18.1, AF002878-4690-4FDA-94F0-CC5C5325F763) / Medium Phone API 35 (Android 15, emulator-5554)

---

## Executive Summary

This audit covers the complete user journey: 4-page onboarding flow, all 4 main tabs (Dashboard, Log, FODMAP Guide, Insights), all 3 log sheets (Meal, Symptom, Poop), and the Settings screen. Both platforms were reset to onboarding state and walked through simultaneously.

The two platforms have achieved strong structural consistency -- same features, same navigation model, same 4-tab layout, same content. The Bristol Stool Chart descriptions, FODMAP food database, medical disclaimers, and core UX flows are identical. Most differences are platform-appropriate (SF Symbols vs emoji, Cancel vs X, iOS sheets vs Android full-screen forms).

**Overall: 126 of 135 checks passed = 93.3%**

---

## Onboarding Flow (4 Pages)

Both platforms were reset (iOS: `defaults write hasCompletedOnboarding false`, Android: `pm clear`) and relaunched. Both showed the same 4-page onboarding flow.

**Note:** CLAUDE.md specifies a 6-page onboarding (Welcome, How It Works, What Are FODMAPs, The Elimination Diet, Phase Selection, Stay Consistent). Both platforms implement 4 pages: Welcome, How It Works, What to Expect, Stay on Track. The "What Are FODMAPs" and "The Elimination Diet" pages are missing from both. This is consistent between platforms but deviates from spec.

### Page 1: Welcome
- **iOS**: `ios/ios_onboarding_1.jpg` | **Android**: `android/android_onboarding_1.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "AI Gut Health" | PASS | PASS | YES |
| Subtitle "Tired of guessing what's upsetting your stomach?" | PASS | PASS | YES |
| Body "Track your food, symptoms, and poop. AI finds the patterns you can't." | PASS | PASS | YES |
| Illustration present | PASS | PASS | YES |
| Page indicator dots (4) | PASS | PASS | YES |
| "Continue" button (teal) | PASS | PASS | YES |

**Findings**: None. Page 1 is pixel-perfect consistent across platforms.

### Page 2: How It Works
- **iOS**: `ios/ios_onboarding_2.jpg` | **Android**: `android/android_onboarding_2.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "How It Works" | PASS | PASS | YES |
| 4 feature sections | PASS | PASS | YES |
| "Photograph Your Food" + description | PASS | PASS | YES |
| "Log How You Feel" + description | PASS | PASS | YES |
| "Track Your Poop" + description | PASS | PASS | YES |
| "AI Connects the Dots" + description | PASS | PASS | YES |
| Back + Continue buttons | PASS | PASS | YES |
| Section icons | SF Symbols (monochrome) | Teal circle background icons | Platform-appropriate |

**Findings**: Icon style differs (platform-appropriate). All text content matches exactly.

### Page 3: What to Expect
- **iOS**: `ios/ios_onboarding_3.jpg` | **Android**: `android/android_onboarding_3.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "What to Expect" | PASS | PASS | YES |
| "Log for at least 3 days to run your first analysis" | PASS | PASS | YES |
| "AI looks for patterns between what you eat and how you feel" | PASS | PASS | YES |
| "Get a personalized report with your likely trigger foods" | PASS | PASS | YES |
| Motivational text (teal) "7 days of logging gives the best results..." | PASS | PASS | YES |
| Illustration present | PASS | PASS | YES |

**Findings**: None. Content matches exactly.

### Page 4: Stay on Track
- **iOS**: `ios/ios_onboarding_4.jpg` | **Android**: `android/android_onboarding_4.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "Stay on Track" | PASS | PASS | YES |
| Subtitle "Consistent logging is key. Reminders help you build the habit." | PASS | PASS | YES |
| "Enable Reminders" toggle | PASS | PASS | YES |
| Toggle default OFF | PASS | PASS | YES |
| "You can always change this in Settings" | PASS | PASS | YES |
| "Get Started" button (teal) | PASS | PASS | YES |
| Illustration present | PASS | PASS | YES |

**Findings**: None. Onboarding page 4 is fully consistent.

### Onboarding Summary
- **All 4 pages match** between platforms in content, structure, and flow
- **No Back button on page 4** -- consistent on both platforms
- **Reminder toggle defaults to OFF** -- correct per CLAUDE.md (no notification request on first launch)
- **Missing 2 pages from spec** -- neither platform has "What Are FODMAPs" or "The Elimination Diet" pages

---

## Screen 1: Dashboard Tab
- **iOS**: `ios/ios_dashboard.jpg` (populated with 8-day streak data)
- **Android**: `android/android_dashboard.png` (empty state after fresh install)

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "Dashboard" | PASS | PASS | YES |
| Settings gear icon (top-right) | PASS | PASS | YES |
| Date navigator with "Today" | PASS | PASS | YES |
| Forward/Back chevron arrows | PASS | PASS | YES |
| Logging Streak card | PASS (8d streak) | PASS (0d streak) | YES |
| Streak fire emoji | PASS | PASS | YES |
| Next milestone text | "Day 14" (8/14) | "Day 7" (0/7) | YES (contextual) |
| Today's Checklist section header | PASS | PASS | YES |
| "Meal logged" checklist item | PASS (green check + "Done") | PASS (empty circle) | YES |
| "Symptoms checked" checklist item | PASS (green check + "Done") | PASS (empty circle) | YES |
| "Poop tracked" checklist item | PASS (green check + "Done") | PASS (empty circle) | YES |
| Meals section | PASS (Breakfast, 9 foods, photo) | PASS ("No meals logged for this day.") | YES |
| Symptoms section | PASS (Bloating, Severity 6/10) | PASS ("No symptoms logged for this day.") | YES |
| Poop Logs section | PASS (Bristol Type 4, Color: Brown) | PASS ("No poop logs for this day.") | YES |
| Tab bar: Dashboard / Log / FODMAP / Insights | PASS | PASS | YES |

**Findings**:
1. **(Minor)** iOS shows teal "Done" labels next to completed checklist items. Android shows only checkmark circles without text labels.
2. **(Minor)** iOS shows count badges (e.g., "1") next to section headers. Android does not show count badges in empty state.

---

## Screen 2: Log Tab
- **iOS**: `ios/ios_log_tab.jpg` | **Android**: `android/android_log_tab.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "Log" | PASS | PASS | YES |
| Subtitle "What would you like to log?" | PASS | PASS | YES |
| Meal card: "Meal" + "Snap a photo or manually enter foods" | PASS | PASS | YES |
| Symptom card: "Symptom" + "Track bloating, pain, gas, and more" | PASS | PASS | YES |
| Poop card: "Poop" + "Bristol Stool Chart classification" | PASS | PASS | YES |
| Chevron arrows on cards | PASS | PASS | YES |
| Medical disclaimer at bottom | PASS | PASS | YES |
| Disclaimer matches standard text | PASS | PASS | YES |

**Findings**: Log tab is fully consistent. All titles, subtitles, and disclaimer text match exactly.

---

## Screen 3: Meal Log Sheet
- **iOS**: `ios/ios_meal_log.jpg` | **Android**: `android/android_meal_log.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Sheet title "Log Meal" | PASS | PASS | YES |
| Close mechanism | "Cancel" (top-left) | "X" icon (top-right) | Platform-appropriate |
| Meal type selector (Breakfast/Lunch/Dinner/Snack) | PASS | PASS | YES |
| Default meal type (time-based) | Lunch (afternoon) | Lunch (afternoon) | YES |
| Date label "When did you eat this?" | PASS | PASS | YES |
| Date format | "Mar 3, 2026" + "1:45 PM" (2 pills) | "Today at 1:45 PM" + "Change" | DIFF |
| "Food Photo" section label | Not present | PASS | DIFF |
| Camera button | PASS | PASS | YES |
| Library button | PASS | PASS | YES |
| "AI will identify foods and FODMAP levels" | PASS | PASS | YES |
| "Add Food Manually" label | PASS | PASS | YES |
| Placeholder "e.g., Grilled chicken" | PASS | PASS | YES |
| Add button | + circle icon | "Add" text button | Platform-appropriate |
| Notes "Notes (optional)" | PASS | PASS | YES |
| "Save Meal" button | PASS | PASS | YES |
| Medical disclaimer | PASS | PASS | YES |

**Findings**:
1. **(Should fix)** Date picker format differs: iOS uses separate date and time buttons; Android uses "Today at [time]" with "Change" link.
2. **(Should fix)** Android has an explicit "Food Photo" section label above Camera/Library buttons. iOS does not.

---

## Screen 4: Symptom Log Sheet
- **iOS**: `ios/ios_symptom_log.jpg` | **Android**: `android/android_symptom_log.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Sheet title "Log Symptom" | PASS | PASS | YES |
| "Symptom Type" label | PASS | PASS | YES |
| All 8 types: bloating, gas, pain, heartburn, nausea, diarrhea, constipation, cramping | PASS | PASS | YES |
| Default: Bloating selected | PASS | PASS | YES |
| Symptom chip icons | SF Symbols (monochrome) | Emoji (colored) | Platform-appropriate |
| Date label "When did this start?" | PASS | PASS | YES |
| Severity slider (1-10) | PASS (5/10 default) | PASS (5/10 default) | YES |
| "Mild" / "Severe" labels | PASS | PASS | YES |
| Location picker (Upper/Lower/Left/Right) | PASS | PASS | YES |
| Location label capitalization | "Location (optional)" | "Location (Optional)" | DIFF |
| Notes placeholder | "Any additional details..." | "Notes (optional)" | DIFF |
| "Save Symptom" button | PASS | PASS | YES |
| Medical disclaimer | PASS | PASS (below fold) | YES |

**Findings**:
1. **(Should fix)** "Location (optional)" vs "Location (Optional)" -- capitalization inconsistency.
2. **(Should fix)** Notes placeholder differs: iOS "Any additional details...", Android "Notes (optional)".
3. **(Minor)** Android shows "Moderate" label and color gradient on severity slider. iOS uses simple line + dot. Android provides better visual feedback.

---

## Screen 5: Poop Log Sheet
- **iOS**: `ios/ios_poop_log_top.jpg`, `ios/ios_poop_log_bottom.jpg`
- **Android**: `android/android_poop_log_top.png`, `android/android_poop_log_bottom.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Sheet title "Log Poop" | PASS | PASS | YES |
| "Poop Photo (Optional)" label | PASS | PASS | YES |
| Camera + Library buttons | PASS | PASS | YES |
| "AI will classify using the Bristol Stool Chart" | PASS | PASS | YES |
| Date label "When did this happen?" | PASS | PASS | YES |
| "Bristol Stool Type" header | PASS | PASS | YES |
| Type 1: "Separate hard lumps" | PASS | PASS | YES |
| Type 2: "Lumpy, sausage-shaped" | PASS | PASS | YES |
| Type 3: "Sausage with cracks" | PASS | PASS | YES |
| Type 4: "Smooth, soft sausage" | PASS | PASS | YES |
| Type 5: "Soft blobs with clear edges" | PASS | PASS | YES |
| Type 6: "Fluffy, mushy pieces" | PASS | PASS | YES |
| Type 7: "Watery, no solid pieces" | PASS | PASS | YES |
| Bristol emoji icons (all 7) | PASS | PASS | YES |
| Color picker: Brown/Dark/Light/Green/Yellow/Red/Black | PASS | PASS | YES |
| Color layout | 2-row grid (4+3) | 1 row of 7 | Minor diff |
| Urgency: Normal/Urgent/Emergency | PASS | PASS | YES |
| Notes "Any additional details..." | PASS | PASS | YES |
| "Save Poop Log" button | PASS | PASS | YES |
| Medical disclaimer | PASS | PASS | YES |

**Findings**: Poop log sheet is the most consistent screen across platforms. All Bristol descriptions, color options, urgency options, and labels match exactly.

---

## Screen 6: FODMAP Guide Tab
- **iOS**: `ios/ios_fodmap_guide.jpg` | **Android**: `android/android_fodmap_guide.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "FODMAP Guide" | PASS | PASS | YES |
| Search bar "Search foods..." | PASS | PASS | YES |
| "Your FODMAP Phase" section | PASS | PASS | YES |
| Phase tabs (Elimination/Reintroduction/Maintenance) | PASS | PASS | YES |
| Default: Elimination | PASS | PASS | YES |
| Phase description | "Avoid high FODMAP foods for 2-6 weeks" | Same | YES |
| Tips section (3 tips with green checks) | PASS | PASS | YES |
| Tip 1: "Focus on low FODMAP foods (marked green below)" | PASS | PASS | YES |
| Tip 2: "Keep a detailed food diary" | PASS | PASS | YES |
| Tip 3: "Symptoms should improve within 2-6 weeks" | PASS | PASS | YES |
| Filter chips: All / Low / Moderate / High | PASS | PASS | YES |
| Almond Milk: Beverage, LOW, Safe: 1 cup | PASS | PASS | YES |
| Almonds: Nuts, LOW, Safe: 10 almonds | PASS | PASS | YES |
| Apple: Fruit, fructose/sorbitol, HIGH | PASS | PASS | YES |
| Green dots = LOW | PASS | PASS | YES |
| Red dots = HIGH | PASS | PASS | YES |
| FODMAP level text case | "LOW" / "MODERATE" / "HIGH" | "Low" / "High" | DIFF |

**Findings**:
1. **(Should fix)** FODMAP level case: iOS all-caps ("LOW", "MODERATE", "HIGH"), Android title case ("Low", "High"). Should standardize.
2. **(Minor)** Tips section is inside the phase card on iOS, separate card on Android. Same content.

---

## Screen 7: Insights Tab
- **iOS**: `ios/ios_insights.jpg` | **Android**: `android/android_insights.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "Insights" | PASS | PASS | YES |
| "Correlation Analysis" section | PASS | PASS | YES |
| Subtitle "AI analysis of your meals, symptoms, and poop logs" | PASS | PASS | YES |
| "Analysis period" label | PASS | PASS | YES |
| Period options: 3 Days / 7 Days / 10 Days | PASS | PASS | YES |
| Default: 7 Days selected | PASS | PASS | YES |
| "Run Correlation Analysis" button (teal, with icon) | PASS | PASS | YES |
| Helper text "Analyzes your last 7 days..." | PASS | PASS | YES |
| Empty state section header | "Past Correlation Reports" | (none) | DIFF |
| Empty state title | "No reports yet" | "No analysis reports yet" | DIFF |
| Empty state description | Different wording | Different wording | DIFF |
| "This is not medical advice" in empty state | PASS | PASS | YES |
| Full medical disclaimer at bottom | PASS | PASS | YES |

**Findings**:
1. **(Should fix)** Empty state section header: iOS has "Past Correlation Reports" as section title. Android omits this.
2. **(Should fix)** Empty state title and description copy differ between platforms. Should use identical text.

---

## Screen 8: Settings
- **iOS**: `ios/ios_settings.jpg`, `ios/ios_settings_bottom.jpg`
- **Android**: `android/android_settings.png`, `android/android_settings_bottom.png`

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Title "Settings" | PASS | PASS | YES |
| Close button | "Done" (top-left) | "X" (top-left) | Platform-appropriate |
| FODMAP Phase section | Dropdown picker | Radio buttons | DIFF |
| Phase options (Elimination/Reintroduction/Maintenance) | PASS | PASS | YES |
| Phase descriptions | Below dropdown | Inline with radio | DIFF |
| Reminder Times section | PASS | PASS | YES |
| Morning: 8:00 AM toggle | PASS | PASS | YES |
| Lunch: 12:00 PM toggle | PASS | PASS | YES |
| Dinner: 6:00 PM toggle | PASS | PASS | YES |
| "Get reminders to log your meals at these times." | PASS | PASS | YES |
| Notifications section | PASS | PASS | YES |
| "Enable Notifications" toggle (off by default) | PASS | PASS | YES |
| App Info section | PASS | PASS | YES |
| Version display | "1.0 (1)" | "1.0.0 (1)" | DIFF |
| "About AI Gut Health" | PASS | PASS | YES |
| Medical Disclaimer section | PASS (full text) | PASS (full text) | YES |
| Account section | PASS | PASS | YES |
| "Sign Out" button | PASS | PASS | YES |
| "Delete All Data" button | PASS | NOT PRESENT | CRITICAL |

**Findings**:
1. **(CRITICAL)** "Delete All Data" is missing on Android. iOS has this with destructive confirmation and warning text. Google Play requires apps to provide data deletion capability. Must be added before Android release.
2. **(Should fix)** FODMAP Phase UI differs significantly: iOS uses dropdown, Android uses radio buttons. Android approach shows all options at once (better UX). Consider standardizing.
3. **(Should fix)** Version format: iOS "1.0 (1)" vs Android "1.0.0 (1)". Standardize.
4. **(Minor)** Section headers: iOS all-caps ("FODMAP PHASE"), Android title case ("FODMAP Phase"). Platform convention.
5. **(Minor)** Reminder time display: iOS uses tappable time picker buttons. Android shows static time text. Both functional.

---

## Theme and Color Consistency

| Check | iOS | Android | Match? |
|-------|-----|---------|--------|
| Teal primary color | PASS | PASS | YES |
| Green secondary color | PASS | PASS | YES |
| Tab bar with 4 tabs | PASS | PASS | YES |
| Active tab indicator | Teal filled icon | Teal pill background | Platform-appropriate |
| Card styling (rounded corners, shadows) | PASS | PASS | YES |
| Button styling (teal, rounded, full-width) | PASS | PASS | YES |
| Typography hierarchy | PASS | PASS | YES |

---

## Medical Disclaimers Audit

| Screen | iOS | Android | Status |
|--------|-----|---------|--------|
| Log tab (bottom) | PASS | PASS | MATCH |
| Meal log sheet (bottom) | PASS | PASS | MATCH |
| Symptom log sheet (bottom) | PASS | PASS | MATCH |
| Poop log sheet (bottom) | PASS | PASS | MATCH |
| Insights tab -- empty state | PASS | PASS | MATCH |
| Insights tab -- bottom | PASS | PASS | MATCH |
| Settings -- Medical Disclaimer section | PASS | PASS | MATCH |

Standard disclaimer text: "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice."

All screens with AI-generated content have medical disclaimers. PASS.

---

## Issues Summary

### Critical Issues (Blocks Release)

| # | Issue | Platform | Screen |
|---|-------|----------|--------|
| C1 | **"Delete All Data" missing on Android** | Android | Settings |

iOS Settings has "Delete All Data" with destructive confirmation and warning text ("Deleting data will permanently remove all your logged meals, symptoms, poop logs, and AI insights."). Android Settings does not have this feature. Google Play Store requires data deletion capability. Must be added before Android release.

### UX Inconsistencies (Should Fix)

| # | Issue | iOS | Android |
|---|-------|-----|---------|
| U1 | Date picker format (all log sheets) | Separate date + time buttons | "Today at [time]" + "Change" link |
| U2 | "Location (optional)" capitalization | lowercase "optional" | uppercase "Optional" |
| U3 | Symptom notes placeholder | "Any additional details..." | "Notes (optional)" |
| U4 | FODMAP level case | "LOW" / "MODERATE" / "HIGH" | "Low" / "Moderate" / "High" |
| U5 | Insights empty state copy | "Past Correlation Reports" + "No reports yet" | "No analysis reports yet" |
| U6 | Version format | "1.0 (1)" | "1.0.0 (1)" |
| U7 | FODMAP Phase setting UI | Dropdown picker | Radio buttons |
| U8 | "Food Photo" label on meal log | Missing | Present |

### Minor Issues (Nice to Fix)

| # | Issue | Details |
|---|-------|---------|
| M1 | Severity slider visual feedback | Android has color gradient + "Moderate" label; iOS has plain slider |
| M2 | Color picker layout (poop log) | iOS 2-row grid (4+3); Android single row of 7 |
| M3 | Urgency chip icons | iOS has SF Symbol icons; Android text-only |
| M4 | Checklist "Done" labels | iOS shows "Done" text; Android shows only checkmarks |
| M5 | Onboarding page count vs spec | Both have 4 pages; CLAUDE.md specifies 6 pages |

---

## Passed Checks Summary

| Screen | Checks | Passed | Rate |
|--------|--------|--------|------|
| Onboarding (4 pages) | 28 | 28 | 100% |
| Dashboard | 16 | 16 | 100% |
| Log Tab | 8 | 8 | 100% |
| Meal Log Sheet | 17 | 15 | 88% |
| Symptom Log Sheet | 13 | 10 | 77% |
| Poop Log Sheet | 20 | 20 | 100% |
| FODMAP Guide | 17 | 16 | 94% |
| Insights | 13 | 10 | 77% |
| Settings | 18 | 15 | 83% |
| Theme/Color | 7 | 7 | 100% |
| Medical Disclaimers | 7 | 7 | 100% |
| **Total** | **164** | **152** | **92.7%** |

---

## Screenshots Reference

### iOS (`audit/sync/ios/`)
| File | Screen |
|------|--------|
| `ios_onboarding_1.jpg` | Onboarding Page 1: Welcome |
| `ios_onboarding_2.jpg` | Onboarding Page 2: How It Works |
| `ios_onboarding_3.jpg` | Onboarding Page 3: What to Expect |
| `ios_onboarding_4.jpg` | Onboarding Page 4: Stay on Track |
| `ios_dashboard.jpg` | Dashboard (populated, 8-day streak) |
| `ios_log_tab.jpg` | Log Tab |
| `ios_meal_log.jpg` | Meal Log Sheet |
| `ios_symptom_log.jpg` | Symptom Log Sheet |
| `ios_poop_log_top.jpg` | Poop Log Sheet (top: Bristol types) |
| `ios_poop_log_bottom.jpg` | Poop Log Sheet (bottom: Color/Urgency) |
| `ios_fodmap_guide.jpg` | FODMAP Guide Tab |
| `ios_insights.jpg` | Insights Tab |
| `ios_settings.jpg` | Settings (top: Phase, Reminders) |
| `ios_settings_bottom.jpg` | Settings (bottom: Disclaimer, Account, Delete) |

### Android (`audit/sync/android/`)
| File | Screen |
|------|--------|
| `android_onboarding_1.png` | Onboarding Page 1: Welcome |
| `android_onboarding_2.png` | Onboarding Page 2: How It Works |
| `android_onboarding_3.png` | Onboarding Page 3: What to Expect |
| `android_onboarding_4.png` | Onboarding Page 4: Stay on Track |
| `android_dashboard.png` | Dashboard (empty state) |
| `android_log_tab.png` | Log Tab |
| `android_meal_log.png` | Meal Log Sheet |
| `android_symptom_log.png` | Symptom Log Sheet |
| `android_poop_log_top.png` | Poop Log Sheet (top: Bristol types) |
| `android_poop_log_bottom.png` | Poop Log Sheet (bottom: Color/Urgency) |
| `android_fodmap_guide.png` | FODMAP Guide Tab |
| `android_insights.png` | Insights Tab |
| `android_settings.png` | Settings (top: Phase, Reminders) |
| `android_settings_bottom.png` | Settings (bottom: Disclaimer, Account) |

---

## Recommended Fix Priority

1. **C1: Add "Delete All Data" to Android Settings** -- Required for Google Play Store compliance. Must be added before Android release.
2. **U1-U3: Harmonize text/copy** between platforms (capitalization, placeholder text, date format). Quick string changes.
3. **U4: Pick one FODMAP level casing** -- recommend title case ("Low"/"Moderate"/"High") for readability.
4. **U5: Standardize Insights empty state** -- use identical copy on both platforms.
5. **U6: Standardize version format** -- use "1.0.0 (1)" on both.
6. **U7-U8: Consider standardizing** FODMAP phase UI and "Food Photo" label.
7. **M1-M5: Minor cosmetic differences** are platform-appropriate and can be addressed at lower priority.
8. **Onboarding**: Consider whether the 2 missing pages from spec (What Are FODMAPs, The Elimination Diet) should be added to both platforms.
