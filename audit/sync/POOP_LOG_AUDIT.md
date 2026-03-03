# Poop Log Screen Audit -- 2026-03-03

## Platforms

- iOS: iPhone 16 simulator (AF002878-4690-4FDA-94F0-CC5C5325F763), iOS 18.1
- Android: Medium Phone API 35 (emulator-5554), Android 15

## Screenshots

- iOS top: `/Users/reidglaze/Documents/guthealth/audit/sync/ios/poop_log_top.png`
- iOS bottom: `/Users/reidglaze/Documents/guthealth/audit/sync/ios/poop_log_bottom.png`
- iOS final: `/Users/reidglaze/Documents/guthealth/audit/sync/ios/poop_log_final.png`
- Android top: `/Users/reidglaze/Documents/guthealth/audit/sync/android/poop_log_top.png`
- Android bottom: `/Users/reidglaze/Documents/guthealth/audit/sync/android/poop_log_bottom.png`
- Android final: `/Users/reidglaze/Documents/guthealth/audit/sync/android/poop_log_final.png`

---

## Element Order Comparison

| # | Expected Order          | iOS                                       | Android                                   | Match? |
|---|-------------------------|--------------------------------------------|-------------------------------------------|--------|
| 1 | Camera / Library        | Photo section (Camera + Library, side by side) -- 2nd in layout | Photo section with "Poop Photo (Optional)" header -- 1st in layout | FAIL   |
| 2 | Date/time picker        | "When did this happen?" -- 1st in layout   | "When did this happen?" -- 2nd in layout   | FAIL   |
| 3 | Bristol type picker     | Bristol Stool Type (types 1-7)             | Bristol Stool Type (types 1-7)             | PASS   |
| 4 | Color picker            | Color (7 options in 4-column grid)         | Color (7 options in horizontal row)        | PASS   |
| 5 | Urgency picker          | Urgency (Normal, Urgent, Emergency)        | Urgency (Normal, Urgent, Emergency)        | PASS   |
| 6 | Notes field             | Notes (optional) + "Any additional details..." | Notes (optional) placeholder              | PASS   |
| 7 | Save button             | Save Poop Log (disabled)                   | Save Poop Log (disabled)                   | PASS   |
| 8 | Medical disclaimer      | Present (full standard text)               | Present (full standard text)               | PASS   |

### ISSUE 1 -- Element order mismatch (SHOULD FIX)

iOS order: Date picker -> Camera/Library -> Bristol -> Color -> Urgency -> Notes -> Save -> Disclaimer

Android order: Camera/Library -> Date picker -> Bristol -> Color -> Urgency -> Notes -> Save -> Disclaimer

The requirement says Camera/Library should come first (position 1), then date/time picker (position 2). iOS has them reversed. The Android order matches the spec.

**Fix needed on iOS**: Move `photoSection` above `poopDatePicker` in the VStack at line 735 of `LogView.swift`.

---

## Default Selection State

| Element        | iOS Default           | Android Default      | Spec Requirement      | Match? |
|----------------|-----------------------|----------------------|-----------------------|--------|
| Bristol type   | nil (nothing selected)| null (nothing selected)| Nothing pre-selected | PASS   |
| Color          | nil (nothing selected)| null (nothing selected)| Nothing pre-selected | PASS   |
| Urgency        | .normal (selected)    | "normal" (selected)  | Nothing pre-selected  | FAIL   |

### ISSUE 2 -- Urgency defaults to "Normal" on both platforms (SHOULD FIX)

Both platforms initialize `selectedUrgency` to "normal"/"Normal" instead of nil/null. The spec says nothing should be pre-selected by default.

- iOS: `@State private var selectedUrgency: Urgency = .normal` (line 717 of LogView.swift)
- Android: `var selectedUrgency by remember { mutableStateOf("normal") }` (line 89 of PoopLogSheet.kt)

Both should be changed to nullable types with null as default. Alternatively, if urgency "normal" is considered a sensible default (most users would pick it), this could be an intentional design choice, but it contradicts the stated requirement.

Note: The save button is correctly disabled when Bristol type and color are unselected, regardless of urgency state. Urgency is not part of the save-gate condition on either platform.

---

## Bristol Stool Type Emoji Mismatch

| Type | iOS Emoji | Android Emoji | Match? | Note |
|------|-----------|---------------|--------|------|
| 1    | beans     | rock/stone    | FAIL   | Different but both reasonable |
| 2    | chestnut  | rice sheaf    | FAIL   | Different but both reasonable |
| 3    | baguette  | banana        | FAIL   | Swapped with Type 4 |
| 4    | banana    | green checkmark | **CRITICAL** | Android uses checkmark, looks like selected state |
| 5    | bubbles   | cloud         | FAIL   | Different but both reasonable |
| 6    | cloud     | water wave    | FAIL   | Different but both reasonable |
| 7    | droplet   | droplet       | PASS   | Match |

### ISSUE 3 -- Type 4 uses green checkmark emoji on Android (CRITICAL)

Android Type 4 uses `\u2705` (green checkmark) as its Bristol type icon. This is visually indistinguishable from a "selected" state indicator. On the screenshot, Type 4 appears to be pre-selected even when it is not, because the green checkmark emoji sits in the icon position.

**Source**: `PoopLogSheet.kt` line 57: `BristolType(4, "Type 4", "Smooth, soft sausage", "\u2705")`

**Fix**: Change Type 4's emoji to match iOS (`\uD83C\uDF4C` = banana) or use a different neutral emoji.

### ISSUE 4 -- Most Bristol emojis differ between platforms (MINOR)

Only Type 7 (droplet) matches between platforms. All others differ. While none besides Type 4 cause functional confusion, visual consistency across platforms is a stated goal.

**Recommendation**: Align Android emojis to match iOS: Type 1 = beans, Type 2 = chestnut, Type 3 = baguette, Type 4 = banana, Type 5 = bubbles, Type 6 = cloud, Type 7 = droplet.

---

## Save Button Disabled State

| Platform | Condition for disabled             | Current state (fresh open) | Correct? |
|----------|------------------------------------|---------------------------|----------|
| iOS      | bristolType == nil OR color == nil | Disabled (opacity 0.5)    | PASS     |
| Android  | bristol == null OR color == null   | Disabled (grayed out)     | PASS     |

Both platforms correctly disable the Save button until both Bristol type and color are selected. Urgency is not required for save (it has a default on both platforms).

---

## Labels and Section Titles

| Element             | iOS                                          | Android                                      | Match? |
|---------------------|----------------------------------------------|----------------------------------------------|--------|
| Sheet title         | "Log Poop"                                   | "Log Poop"                                   | PASS   |
| Dismiss action      | "Cancel" (text button, top-left)             | X icon (top-right)                           | PASS (platform convention) |
| Photo label         | No header text (just Camera/Library buttons) | "Poop Photo (Optional)" header               | FAIL   |
| AI classify text    | "AI will classify using the Bristol Stool Chart" | "AI will classify using the Bristol Stool Chart" | PASS   |
| Date picker label   | "When did this happen?"                      | "When did this happen?"                      | PASS   |
| Bristol label       | "Bristol Stool Type"                         | "Bristol Stool Type"                         | PASS   |
| Color label         | "Color"                                      | "Color"                                      | PASS   |
| Urgency label       | "Urgency"                                    | "Urgency"                                    | PASS   |
| Notes label         | "Notes (optional)"                           | "Notes (optional)" (placeholder)             | PASS   |
| Notes placeholder   | "Any additional details..."                  | "Notes (optional)" (as placeholder text)     | FAIL   |
| Save button         | "Save Poop Log"                              | "Save Poop Log"                              | PASS   |
| Disclaimer          | Full standard text                           | Full standard text                           | PASS   |

### ISSUE 5 -- Missing "Poop Photo (Optional)" header on iOS (MINOR)

Android has a "Poop Photo (Optional)" section header above the Camera/Library buttons. iOS does not have this header and jumps straight to the buttons. Adding the header on iOS would improve clarity.

### ISSUE 6 -- Notes placeholder text differs (MINOR)

iOS uses "Any additional details..." as the text field placeholder. Android uses "Notes (optional)" as the placeholder. Both are functional but not visually consistent.

---

## Bristol Type Descriptions

| Type | iOS Description                | Android Description            | Match? |
|------|--------------------------------|--------------------------------|--------|
| 1    | Separate hard lumps            | Separate hard lumps            | PASS   |
| 2    | Lumpy, sausage-shaped          | Lumpy, sausage-shaped          | PASS   |
| 3    | Sausage with cracks            | Sausage with cracks            | PASS   |
| 4    | Smooth, soft sausage           | Smooth, soft sausage           | PASS   |
| 5    | Soft blobs with clear edges    | Soft blobs with clear edges    | PASS   |
| 6    | Fluffy, mushy pieces           | Fluffy, mushy pieces           | PASS   |
| 7    | Watery, no solid pieces        | Watery, no solid pieces        | PASS   |

All Bristol descriptions match perfectly.

---

## Color Options

| Platform | Colors available                                    | Match? |
|----------|-----------------------------------------------------|--------|
| iOS      | Brown, Dark, Light, Green, Yellow, Red, Black (4-column grid, 2 rows) | PASS   |
| Android  | Brown, Dark, Light, Green, Yellow, Red, Black (single horizontal row) | PASS   |

Same 7 color options on both. Layout differs slightly (iOS uses a grid, Android uses a single row) but both show all options.

---

## Styling Consistency

| Aspect              | iOS                            | Android                        | Consistent? |
|---------------------|--------------------------------|--------------------------------|-------------|
| Teal primary color  | Used for Camera/Library bg, selected states | Used for button outlines, selected chip fills | PASS |
| Card styling        | Rounded corners with borders   | Rounded corners with borders   | PASS        |
| Bristol row styling | Rounded card per type, color-coded when selected | Rounded card per type, teal border when selected | PASS |
| Urgency chips       | Inline buttons with icons      | FilterChips (Material 3)       | PASS (platform convention) |
| Save button         | Full-width teal button         | Full-width teal button         | PASS        |

---

## Summary of Issues

### Critical (blocks release)

1. **ISSUE 3**: Android Type 4 Bristol emoji is a green checkmark (`\u2705`), making it look pre-selected. Must change to banana or another neutral emoji.

### Should Fix (before release)

2. **ISSUE 1**: Element order mismatch -- iOS shows date picker before Camera/Library; Android shows Camera/Library first. The spec says Camera/Library should be first. Fix iOS ordering.
3. **ISSUE 2**: Urgency defaults to "Normal" on both platforms. Spec says nothing should be pre-selected. Change both to nullable with no default.

### Minor (nice to fix)

4. **ISSUE 4**: Bristol emojis differ between platforms (6 of 7 don't match). Align to a single set.
5. **ISSUE 5**: iOS missing "Poop Photo (Optional)" header above Camera/Library buttons.
6. **ISSUE 6**: Notes placeholder text differs ("Any additional details..." on iOS vs "Notes (optional)" on Android).

---

## Pass/Fail Summary

- Element order: **FAIL** (iOS order differs from Android and spec)
- Default selections: **FAIL** (urgency pre-selected on both)
- Save button disabled: **PASS**
- Labels and titles: **PASS** (with minor differences)
- Bristol descriptions: **PASS**
- Bristol emojis: **FAIL** (Type 4 critical, others minor)
- Color options: **PASS**
- Teal theme consistency: **PASS**
- Medical disclaimer: **PASS**
- Overall: **8 checks passed, 6 issues found (1 critical, 2 should-fix, 3 minor)**
