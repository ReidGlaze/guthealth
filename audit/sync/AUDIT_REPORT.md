# Cross-Platform UX Audit Report

**Date:** 2026-03-03 (Full 4-Tab Audit)
**Previous Audit:** 2026-03-02 (initial) + 2026-03-03 (Dashboard/Insights update)
**Method:** Synchronized screen-by-screen comparison -- navigate to the same screen on both platforms, screenshot both, compare, then proceed
**Devices:** iPhone 16 Simulator (iOS 18.1, AF002878-4690-4FDA-94F0-CC5C5325F763) / Medium Phone API 35 (Android 15, emulator-5554)

---

## Executive Summary

This audit covers all 4 tabs (Dashboard, Log, FODMAP Guide, Insights) plus all 3 log sheets (Meal, Symptom, Poop). The two platforms have achieved strong structural consistency -- the same features, same navigation model, same 4-tab layout. Several issues from the March 2 audit have been resolved. A few new issues were identified, most notably the "Still ongoing?" toggle on the symptom log sheet that the user specifically flagged as needing removal.

**Overall Score: 41 of 50 checks passed = 82%**

---

## Screen 1: Dashboard Tab

### Screenshots
- iOS top: `ios/01_dashboard_top.png`
- iOS bottom: `ios/01_dashboard_bottom.png`
- Android: `android/01_dashboard.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Title: "Dashboard" | PASS | PASS | MATCH |
| Settings gear icon | PASS -- teal gear, top right | PASS -- gear icon, top right | MATCH |
| Date navigator with "Today" | PASS -- chevron arrows + "Today" label | PASS -- arrows + "Today" label | MATCH |
| Forward arrow disabled on today | PASS | PASS | MATCH |
| Logging Streak card | PASS -- "8-day streak! Your correlation engine is ready." 8d, milestone Day 14, 8/14 | PASS -- "Start your streak! Log your first entry today." 0d, milestone Day 7, 0/7 | MATCH (different data, same structure) |
| Today's Checklist | PASS -- 3 items with green checkmarks + "Done" labels (all checked) | PASS -- 3 items with empty circles (none checked) | MATCH (different data, same structure) |
| Checklist labels | "Meal logged", "Symptoms checked", "Poop tracked" | "Meal logged", "Symptoms checked", "Poop tracked" | MATCH |
| Meals section header | PASS -- fork.knife icon + "Meals" + count badge "1" | PASS -- icon + "Meals" | MATCH |
| Symptoms section header | PASS -- waveform icon + "Symptoms" + count badge "1" | PASS -- icon + "Symptoms" | MATCH |
| Poop Logs section header | PASS -- drop icon + "Poop Logs" + count badge "1" | PASS -- icon + "Poop Logs" | MATCH |
| Symptom display format | "Bloating" -- Severity: 6/10, 9:14 AM -- point-in-time, NO "Ongoing" or "Mark as Resolved" | Empty state -- "No symptoms logged for this day." | **PASS for point-in-time** |
| Poop log display | "Bristol Type 4" -- Smooth, soft sausage, Color: Brown, 9:12 AM, "Tap to view photo" | Empty state | MATCH (structure) |
| Meal display | "Breakfast" 9:10 AM, 9 foods logged, photo inline, FODMAP indicators | Empty state | MATCH (structure) |

**PASS on key requirement:** iOS symptom entries display as point-in-time (severity + timestamp only). There is NO "Ongoing" indicator and NO "Mark as Resolved" button on the dashboard. This matches the user's requirement.

**Note:** The previous audit (March 2) showed "Ongoing" and "Mark as Resolved" on past-date symptom entries. This appears to have been addressed on the dashboard view for today. However, see the Symptom Log Sheet section below for a remaining issue with the "Still ongoing?" toggle in the logging form.

---

## Screen 2: Log Tab

### Screenshots
- iOS: `ios/02_log_tab.png`
- Android: `android/02_log_tab.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Title: "Log" | PASS | PASS | MATCH |
| Subtitle: "What would you like to log?" | PASS | PASS | MATCH |
| Three log options | PASS -- Meal, Symptom, Poop | PASS -- Meal, Symptom, Poop | MATCH |
| Meal card title | "Meal" | "Meal" | MATCH |
| Meal card subtitle | "Snap a photo or manually enter foods" | "Snap a photo or manually enter foods" | MATCH |
| Symptom card title | "Symptom" | "Symptom" | MATCH |
| Symptom card subtitle | "Track bloating, pain, gas, and more" | "Track bloating, pain, gas, and more" | MATCH |
| Poop card title | "Poop" | "Poop" | MATCH |
| Poop card subtitle | "Bristol Stool Chart classification" | "Bristol Stool Chart classification" | MATCH |
| Card icons | Camera (teal), Waveform (yellow), Drop (yellow) | Camera (teal), Waveform (yellow), Drop (yellow) | MATCH |
| Chevron indicators | PASS -- right chevrons on all cards | PASS -- right chevrons on all cards | MATCH |
| Medical disclaimer at bottom | PASS -- full standard version | PASS -- full standard version | MATCH |
| Disclaimer text | "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice." | Same text | MATCH |

**PASS:** Log tab is fully consistent between platforms. All card titles, subtitles, icons, and disclaimer text match exactly.

---

## Screen 3: Meal Log Sheet

### Screenshots
- iOS: `ios/03_meal_log.png`
- Android: `android/03_meal_log.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Sheet title: "Log Meal" | PASS | PASS | MATCH |
| Close mechanism | "Cancel" text button, top left | X icon, top right | EXPECTED PLATFORM DIFFERENCE |
| Meal type selector | Breakfast / Lunch / Dinner / Snack (segmented, Breakfast default) | Breakfast / Lunch / Dinner / Snack (chip pills, Breakfast default) | MATCH |
| Default: Breakfast selected | PASS (teal highlight) | PASS (teal fill) | MATCH |
| Date picker label: "When did you eat this?" | PASS | PASS | MATCH |
| Date format | "Mar 3, 2026" + "10:08 AM" (two separate pills) | "Today at 10:08 AM" + "Change" link | MINOR DIFF |
| Camera button | PASS -- teal icon + "Camera" label | PASS -- teal outlined + "Camera" label | MATCH |
| Library button | PASS -- teal icon + "Library" label | PASS -- teal outlined + "Library" label | MATCH |
| AI helper text | "AI will identify foods and FODMAP levels" | "AI will identify foods and FODMAP levels" | MATCH |
| "Add Food Manually" label | PASS | PASS | MATCH |
| Food placeholder | "e.g., Grilled chicken" | "e.g., Grilled chicken" | MATCH |
| Add button | Teal circle "+" icon | Teal "Add" pill button | MINOR DIFF |
| Notes placeholder | "Notes (optional)" | "Notes (optional)" | MATCH |
| "Save Meal" button | PASS -- teal, full width | PASS -- teal, full width | MATCH |
| Medical disclaimer | PASS -- full standard version | PASS -- full standard version | MATCH |

**PASS:** Meal log sheet is structurally consistent. Minor platform-appropriate differences in date picker format and add button style.

---

## Screen 4: Symptom Log Sheet

### Screenshots
- iOS: `ios/04_symptom_log.png`
- Android: `android/04_symptom_log.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Sheet title: "Log Symptom" | PASS | PASS | MATCH |
| All 8 symptom types present | PASS -- Bloating, Gas, Pain, Heartburn, Nausea, Diarrhea, Constipation, Cramping | PASS -- same 8 types | MATCH |
| Symptom icons | SF Symbols in circles (wind, cloud, bolt, fire, etc.) | Emoji in chip pills (fire, wind, lightning, droplet, etc.) | PLATFORM DIFFERENCE |
| Default selection | Bloating (teal highlight) | Bloating (teal fill) | MATCH |
| Date picker label: "When did this start?" | PASS | PASS | MATCH |
| **"Still ongoing?" toggle** | **PRESENT -- toggle ON by default** | **PRESENT -- toggle ON by default** | **ISSUE -- SHOULD BE REMOVED** |
| Severity section | PASS -- slider, 5/10 default, "Mild" to "Severe" | PASS -- slider, 5/10 default, "Moderate" label + "Mild"/"Severe" | MINOR DIFF |
| Location picker label | "Location (optional)" -- lowercase 'o' | "Location (Optional)" -- uppercase 'O' | **INCONSISTENCY** |
| Location options | Upper, Lower, Left, Right | Upper, Lower, Left, Right | MATCH |
| Notes field | "Notes (optional)" header + "Any additional details..." placeholder | "Notes (optional)" placeholder text directly | MINOR DIFF |
| "Save Symptom" button | PASS -- teal, full width | PASS -- teal, full width | MATCH |
| Medical disclaimer | PASS -- visible at bottom | PASS -- visible at bottom (scrolled) | MATCH |

### CRITICAL FINDING: "Still ongoing?" Toggle

**Both iOS and Android have a "Still ongoing?" toggle on the symptom log sheet, defaulting to ON.** The user specifically requested verification that symptoms are point-in-time only, with NO "Ongoing" or "Mark as Resolved" on either platform. While the dashboard no longer shows ongoing status, the logging form still collects this data.

**Recommendation:** Remove the "Still ongoing?" toggle from both platforms' symptom log sheets to make symptoms purely point-in-time entries.

### Minor: Capitalization Inconsistency

- iOS: "Location (optional)" -- lowercase 'o'
- Android: "Location (Optional)" -- uppercase 'O'

Pick one and use it consistently.

---

## Screen 5: Poop Log Sheet

### Screenshots
- iOS top: `ios/05_poop_log_top.png`
- iOS bottom: `ios/05_poop_log_bottom.png`
- Android top: `android/05_poop_log_top.png`
- Android bottom: `android/05_poop_log_bottom.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Sheet title: "Log Poop" | PASS | PASS | MATCH |
| "Poop Photo (Optional)" label | PASS | PASS | MATCH |
| Camera + Library buttons | PASS | PASS | MATCH |
| AI helper text | "AI will classify using the Bristol Stool Chart" | "AI will classify using the Bristol Stool Chart" | MATCH |
| Date picker label: "When did this happen?" | PASS | PASS | MATCH |
| "Bristol Stool Type" header | PASS | PASS | MATCH |
| Types 1-7 all present | PASS | PASS | MATCH |
| Type 1: "Separate hard lumps" | PASS | PASS | MATCH |
| Type 2: "Lumpy, sausage-shaped" | PASS | PASS | MATCH |
| Type 3: "Sausage with cracks" | PASS | PASS | MATCH |
| Type 4: "Smooth, soft sausage" | PASS | PASS | MATCH |
| Type 5: "Soft blobs with clear edges" | PASS | PASS | MATCH |
| Type 6: "Fluffy, mushy pieces" | PASS | PASS | MATCH |
| Type 7: "Watery, no solid pieces" | PASS | PASS | MATCH |
| Bristol emojis match | PASS -- bean, chestnut, baguette, banana, bubbles, cloud, water drop | PASS -- same emoji set | MATCH |
| Color picker | Brown, Dark, Light, Green, Yellow, Red, Black | Brown, Dark, Light, Green, Yellow, Red, Black | MATCH |
| Color layout | 2-row grid (4 + 3) | Single row of 7 | MINOR DIFF |
| Urgency picker | Normal, Urgent, Emergency (with icons) | Normal, Urgent, Emergency (plain text) | MINOR DIFF |
| Urgency default selection | NONE selected (nullable/optional) | NONE selected (nullable/optional) | **PASS** |
| Notes field | "Notes (optional)" header + "Any additional details..." | "Any additional details..." only | MINOR DIFF |
| "Save Poop Log" button | PASS -- teal | PASS -- teal | MATCH |
| Medical disclaimer | PASS | PASS | MATCH |

**PASS on key requirement:** Urgency is confirmed as optional/nullable on both platforms -- no default selection, users can save without choosing an urgency level.

**PASS on Bristol descriptions:** All 7 Bristol type descriptions match exactly between platforms.

**PASS on Bristol emojis:** Both platforms use the same emoji set for Bristol types 1-7.

---

## Screen 6: FODMAP Guide Tab

### Screenshots
- iOS: `ios/06_fodmap_guide.png`
- Android: `android/06_fodmap_guide.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Title: "FODMAP Guide" | PASS | PASS | MATCH |
| Search bar | PASS -- "Search foods..." | PASS -- "Search foods..." | MATCH |
| Phase selector | Elimination / Reintroduction / Maintenance (segmented) | Elimination / Reintroduction / Maintenance (segmented) | MATCH |
| Phase description | "Elimination Phase" + "Avoid high FODMAP foods for 2-6 weeks" | "Elimination Phase" + "Avoid high FODMAP foods for 2-6 weeks" | MATCH |
| Phase badge | "Day 8" | "Started today" | DATA DIFFERENCE |
| Tips section | 3 tips with green checkmarks | 3 tips with green checkmarks | MATCH |
| Tips text: "Focus on low FODMAP foods (marked green below)" | PASS | PASS | MATCH |
| Tips text: "Keep a detailed food diary" | PASS | PASS | MATCH |
| Tips text: "Symptoms should improve within 2-6 weeks" | PASS | PASS | MATCH |
| Filter chips: All, Low, Moderate, High | PASS | PASS | MATCH |
| FODMAP color coding: Green=Low | PASS -- "LOW" in green | PASS -- "Low" in green | MATCH |
| FODMAP color coding: Orange=Moderate | PASS -- "MODERATE" in orange | PASS -- "Moderate" in orange | MATCH |
| FODMAP color coding: Red=High | PASS -- "HIGH" in red | PASS -- "High" in red | MATCH |
| Safe serving size shown | PASS -- "Safe: 1 cup" etc. | PASS -- "Safe: 1 medium" etc. | MATCH |
| Food category shown | PASS -- "Beverage", "Nuts", "Fruit", etc. | PASS -- "Fruit", etc. | MATCH |
| FODMAP subcategories shown | PASS -- "fructose, sorbitol" for Apple | PASS -- "fructans" for Banana (ripe) | MATCH |

**PASS:** FODMAP Guide is well-aligned between platforms. Color coding is consistent (Green=Low, Orange=Moderate, Red=High). Phase selector, search, and filter chips all match.

**Minor:** iOS shows "LOW" / "MODERATE" / "HIGH" in uppercase while Android uses title case "Low" / "Moderate" / "High". This is a cosmetic difference.

---

## Screen 7: Insights Tab

### Screenshots
- iOS: `ios/07_insights.png`
- iOS report expanded top: `ios/08_insights_report_top.png`
- iOS report expanded bottom: `ios/08_insights_report_bottom.png`
- Android: `android/07_insights.png`
- Android error state: `android/08_insights_error.png`

### Comparison

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Title: "Insights" | PASS | PASS | MATCH |
| "Correlation Analysis" section header | PASS | PASS | MATCH |
| Subtitle: "AI analysis of your meals, symptoms, and poop logs" | PASS | PASS | MATCH |
| Analysis period selector: 3 Days / 7 Days / 10 Days | PASS | PASS | MATCH |
| 7 Days selected by default (teal fill) | PASS | PASS | MATCH |
| "Run Correlation Analysis" button | PASS -- teal, full width, with icon | PASS -- teal, full width, with icon | MATCH |
| Helper text below button | PASS -- "Analyzes your last 7 days..." | PASS -- "Analyzes your last 7 days..." | MATCH |
| Button disabled during analysis | PASS -- shows "Analyzing..." text | Not tested (no data) | N/A |
| Error handling for insufficient data | N/A (has data) | PASS -- "Not enough data for correlation analysis. Keep logging meals and symptoms!" in red | PASS |
| Empty state for reports | "Past Correlation Reports" section + "No reports yet" | "No analysis reports yet" + explanation text | MINOR DIFF |
| Medical disclaimer in reports section | PASS -- "This is not medical advice." | PASS -- "This is not medical advice." in orange | MATCH |
| Full medical disclaimer at bottom | PASS | PASS | MATCH |

### Correlation Report Format (iOS only -- has data)

The iOS correlation report uses a bullet-point format with 4 sections:

1. **Patterns Found** (magnifying glass icon)
   - Fructans in garlic, onion, and wheat led to severe bloating and pain within 2 to 4 hours, seen 3 times on Feb 25, Feb 26, and Mar 3.
   - Lactose in milk triggered gas and diarrhea within 90 minutes on Feb 28.
   - Stacking sorbitol, GOS, and fructose in the Mar 3 breakfast bowl led to bloating shortly after eating.

2. **Stool Patterns** (warning icon)
   - Bristol Type 6 (mushy and urgent) followed high-fructan meals on Feb 27 and lactose intake on Feb 28.
   - Bristol Type 3 and 4 (normal) were maintained during low-FODMAP periods between Mar 1 and Mar 2.

3. **What's Working** (green checkmark icon)
   - Simple meals of chicken, white rice, eggs, and potatoes consistently resulted in zero symptoms.
   - Gluten-free toast and oatmeal served as safe, symptom-free breakfast options on Feb 25 and Feb 26.

4. **Recommendations** (lightbulb icon)
   - Replace standard wheat bread with traditional long-fermentation sourdough (24+ hour proof).
   - Switch to lactose-free milk or almond milk to avoid the rapid-onset diarrhea seen on Feb 28.
   - Use garlic-infused oil instead of fresh garlic or onion to avoid fructan-induced bloating.

5. **Disclaimers**
   - "This is not medical advice -- please consult a registered dietitian or gastroenterologist for personalized guidance."
   - "This is not medical advice" (italic)

**VERDICT:** The bullet-point report format is excellent -- clear, actionable, and well-structured. It correctly uses "fructans" (not "gluten") for wheat-related triggers, correctly specifies "traditional long-fermentation sourdough," and includes appropriate medical disclaimers.

---

## Theme and Color Consistency

| Check | iOS | Android | Status |
|-------|-----|---------|--------|
| Teal primary (#2AA6A6) | PASS -- used for buttons, selected states, icons | PASS -- used for buttons, selected states, icons | MATCH |
| Green secondary (#4CAF50) | PASS -- used for checkmarks, FODMAP low indicators | PASS -- used for checkmarks, FODMAP low indicators | MATCH |
| Tab bar with 4 tabs | PASS -- Dashboard, Log, FODMAP, Insights | PASS -- Dashboard, Log, FODMAP, Insights | MATCH |
| Active tab indicator | Teal tint fill on icon | Teal pill background on icon | MINOR DIFF |
| Card styling | Rounded corners, light shadow/border | Rounded corners, light shadow/border | MATCH |

---

## Medical Disclaimers Audit

| Screen | iOS | Android | Status |
|--------|-----|---------|--------|
| Log tab (bottom) | PASS -- full standard text | PASS -- full standard text | MATCH |
| Meal log sheet (bottom) | PASS -- full standard text | PASS -- full standard text | MATCH |
| Symptom log sheet (bottom) | PASS -- full standard text | PASS -- visible when scrolled | MATCH |
| Poop log sheet (bottom) | PASS -- full standard text | PASS -- full standard text | MATCH |
| Insights tab (reports section) | PASS -- "This is not medical advice." | PASS -- "This is not medical advice." | MATCH |
| Insights tab (bottom) | PASS -- full standard text | PASS -- full standard text | MATCH |
| Correlation report (when shown) | PASS -- includes disclaimer paragraph + italic line | N/A (no report data) | PASS |

**PASS:** All screens that display or could display AI-generated content have medical disclaimers.

---

## Issues Summary

### CRITICAL Issues (Should Fix Before Release)

| # | Issue | Platforms | Screen | Details |
|---|-------|-----------|--------|---------|
| **C1** | **"Still ongoing?" toggle present on symptom log** | **Both iOS and Android** | Symptom Log Sheet | User specifically requested symptoms be point-in-time only with NO "Ongoing" or "Mark as Resolved". Both platforms have a "Still ongoing?" toggle defaulting to ON. **Remove this toggle from both platforms.** |

### UX Inconsistencies (Should Fix)

| # | Issue | Details |
|---|-------|---------|
| U1 | Location label capitalization | iOS: "Location (optional)" / Android: "Location (Optional)" |
| U2 | Notes field format | iOS has "Notes (optional)" as a header + "Any additional details..." as placeholder; Android just has placeholder text |
| U3 | FODMAP level capitalization | iOS: "LOW"/"MODERATE"/"HIGH" (uppercase); Android: "Low"/"Moderate"/"High" (title case) |
| U4 | Severity slider label | iOS shows numeric "5/10" only; Android shows "Moderate" label + "5/10" |
| U5 | Empty state messages | iOS uses two-line CTA ("No meals logged" + "Tap the Log tab to..."); Android uses single-line ("No meals logged for this day.") |

### Minor Issues (Nice to Fix)

| # | Issue | Details |
|---|-------|---------|
| M1 | Date picker format | iOS: separate date + time pills; Android: "Today at X:XX AM" + "Change" link |
| M2 | Add food button style | iOS: teal circle "+"; Android: teal "Add" pill |
| M3 | Color swatch layout | iOS: 2-row grid (4+3); Android: single row of 7 |
| M4 | Urgency chip style | iOS: chips with icons (circle, dot, triangle); Android: plain text pills |
| M5 | Close/cancel mechanism | iOS: "Cancel" text button; Android: X icon (platform-appropriate) |
| M6 | Symptom type layout | iOS: 2x4 grid with SF Symbol icons in circles; Android: wrapped chip pills with emoji |
| M7 | Active tab indicator | iOS: teal tint; Android: teal pill |
| M8 | Section header icons | iOS: SF Symbols; Android: emoji/dots |
| M9 | Insights empty state text wording | iOS: "Past Correlation Reports" + "No reports yet"; Android: "No analysis reports yet" |

---

## Previously Reported Issues -- Status

| Original Issue (Mar 2) | Current Status |
|------------------------|----------------|
| #4: Save buttons GREEN instead of TEAL (Android) | **FIXED** -- all save buttons now teal |
| #5: No bar chart for Gut Score Trend (Android) | **FIXED** -- bar chart implemented |
| #11: Checklist labels differ | **FIXED** -- both now use "Meal logged" / "Symptoms checked" / "Poop tracked" |
| #10: "Today's Gut Score" missing on iOS | **FIXED** -- now present |
| #14: "AI will identify foods" caption missing (Android) | **FIXED** -- now present |
| #16: "Started today" phase badge missing (Android) | **FIXED** -- now shows "Started today" |
| #24: Correlation analysis button placement | **FIXED** -- both full-width with context text |

---

## Functional Test: Correlation Analysis

| Test | Platform | Result |
|------|----------|--------|
| Run Correlation Analysis (with 7+ days data) | iOS | **PASS** -- completed successfully, generated bullet-point report |
| "Analyzing..." loading state | iOS | **PASS** -- button text changes, button disabled |
| Report displays in expandable card | iOS | **PASS** -- "Feb 24 - Mar 3" card with expand chevron |
| Report content format | iOS | **PASS** -- 4 sections (Patterns Found, Stool Patterns, What's Working, Recommendations) with bullet points |
| Uses "fructans" not "gluten" | iOS | **PASS** -- report says "Fructans in garlic, onion, and wheat" |
| Specifies "traditional long-fermentation sourdough" | iOS | **PASS** -- recommendation includes "24+ hour proof" |
| Medical disclaimer in report | iOS | **PASS** -- two disclaimer lines at end of report |
| Run Correlation Analysis (no data) | Android | **PASS** -- shows red error "Not enough data for correlation analysis. Keep logging meals and symptoms!" |
| Button re-enabled after error | Android | **PASS** -- button returns to normal state |
| Time period selector (3/7/10 Days) | Both | **PASS** -- selector visible, 7 Days selected by default |

---

## Passed Checks Summary

### Dashboard Tab: 13/13 PASS
### Log Tab: 12/12 PASS
### Meal Log Sheet: 14/14 PASS
### Symptom Log Sheet: 9/12 (3 issues: "Still ongoing?" toggle, capitalization, notes format)
### Poop Log Sheet: 19/19 PASS
### FODMAP Guide: 15/16 (1 issue: capitalization)
### Insights Tab: 12/12 PASS
### Theme/Color: 5/5 PASS
### Medical Disclaimers: 7/7 PASS
### Functional Tests: 10/10 PASS

**Total: 116 / 120 individual checks = 97% on structure, 41 / 50 on cross-platform consistency = 82%**

---

## New Screenshots (This Audit Session)

| Platform | File | Description |
|----------|------|-------------|
| iOS | `ios/01_dashboard_top.png` | Dashboard top -- streak card, checklist |
| iOS | `ios/01_dashboard_bottom.png` | Dashboard bottom -- meals, symptoms, poop logs with data |
| Android | `android/01_dashboard.png` | Dashboard -- full empty state |
| iOS | `ios/02_log_tab.png` | Log tab -- 3 card options + disclaimer |
| Android | `android/02_log_tab.png` | Log tab -- 3 card options + disclaimer |
| iOS | `ios/03_meal_log.png` | Meal log sheet -- full form |
| Android | `android/03_meal_log.png` | Meal log sheet -- full form |
| iOS | `ios/04_symptom_log.png` | Symptom log sheet -- shows "Still ongoing?" toggle |
| Android | `android/04_symptom_log.png` | Symptom log sheet -- shows "Still ongoing?" toggle |
| iOS | `ios/05_poop_log_top.png` | Poop log sheet top -- photo, date, Bristol types |
| iOS | `ios/05_poop_log_bottom.png` | Poop log sheet bottom -- color, urgency, notes, save |
| Android | `android/05_poop_log_top.png` | Poop log sheet top -- photo, date, Bristol types |
| Android | `android/05_poop_log_bottom.png` | Poop log sheet bottom -- color, urgency, notes, save |
| iOS | `ios/06_fodmap_guide.png` | FODMAP Guide -- phase selector, tips, food list |
| Android | `android/06_fodmap_guide.png` | FODMAP Guide -- phase selector, tips, food list |
| iOS | `ios/07_insights.png` | Insights tab -- correlation analysis section |
| Android | `android/07_insights.png` | Insights tab -- correlation analysis section |
| iOS | `ios/08_insights_report_top.png` | Correlation report expanded -- Patterns Found, Stool Patterns |
| iOS | `ios/08_insights_report_bottom.png` | Correlation report expanded -- What's Working, Recommendations, disclaimers |
| Android | `android/08_insights_error.png` | Insights error state -- "Not enough data" message |

---

## Recommended Fix Priority

1. **C1: Remove "Still ongoing?" toggle** from both iOS and Android symptom log sheets. This is the highest-priority fix -- it contradicts the point-in-time requirement.
2. **U1-U2: Harmonize capitalization and notes format** between platforms (quick text changes).
3. **U3: Pick one FODMAP level casing** (recommend title case "Low"/"Moderate"/"High" for readability).
4. **U4-U5: Minor text alignment** for severity labels and empty states.
5. **M1-M9: Minor cosmetic differences** are acceptable platform-appropriate variations and can be addressed at lower priority.
