# GI Accuracy Review: Correlation Engine & AI Recommendations

**Reviewer Role:** Medical Knowledge Reviewer (Gastroenterology, IBS, FODMAP Protocol)
**Date:** 2026-03-02
**App:** AI Gut Health & IBS Tracker
**Test:** 7-day simulated IBS patient with known trigger foods
**Artifacts Reviewed:**
- Correlation results screenshots (02-04)
- Dashboard screenshot (01)
- Symptom logging screenshot (00)
- Cloud Function source: `functions/src/index.ts` (all 6 functions)
- Cross-platform audit report

---

## Executive Summary

The correlation engine produces results that are **directionally correct** and align with established FODMAP science from Monash University. The identified triggers (garlic, onion, wheat bread) are among the most well-documented high-FODMAP foods, the time-lag estimates fall within physiologically plausible ranges, and the low-FODMAP substitution recommendations are evidence-based. However, the system exhibits **overconfident scoring** given the minimal data (only 2 occurrences per trigger), lacks critical contextual factors (cumulative FODMAP load, stool form correlation, stress/menstrual cycle), and delegates too much clinical reasoning to an LLM without deterministic guardrails. The app is a promising educational tool but needs calibration improvements before the correlation data should be presented to users with the authority that high-confidence percentages imply.

---

## 1. FODMAP Science Accuracy

**Rating: Accurate**

### Garlic -> Bloating / Gas at ~2h delay
- **Assessment: Correct.** Garlic is one of the highest fructan-containing foods per Monash University data. Fructans are fermented by colonic bacteria, and the 2-hour time lag is consistent with oro-cecal transit time in the fed state (typically 1.5-4 hours depending on meal composition). Published research (Halmos et al., 2014; Shepherd et al., 2008) demonstrates that fructan ingestion reliably provokes gas and bloating in IBS patients, with symptom onset typically between 2 and 6 hours post-ingestion. A ~2h lag is at the faster end of this range, which is plausible if consumed on a relatively empty stomach or with a low-fat meal that accelerates gastric emptying.

### Onion -> Bloating at ~2h delay
- **Assessment: Correct.** Onion (particularly bulb onion) is a major fructan source. The Monash app rates common onion as HIGH FODMAP at typical serving sizes. The ~2h delay mirrors the garlic finding, which is expected since both contain fructans and would undergo similar fermentation kinetics. The engine correctly associates onion with bloating, which is the most commonly reported symptom from fructan malabsorption.

### Wheat bread -> Bloating at ~3h delay
- **Assessment: Correct and nuanced.** Wheat contains fructans (not gluten, as is commonly misbelieved in IBS). The slightly longer ~3h delay compared to garlic/onion is physiologically sensible: bread is a more complex food matrix that requires longer gastric processing, slowing transit to the colon where fermentation occurs. The distinction between a 2h and 3h lag for different food matrices demonstrates that the engine is producing physiologically plausible (not merely templated) results.

### Milk miss (only 1 occurrence)
- **Assessment: Reasonable threshold behavior.** The engine requires >= 2 occurrences to report a correlation (as specified in the prompt). With only 1 milk exposure in the test data, correctly excluding it avoids a false positive from a single data point. However, this is a notable gap -- lactose is one of the "big 6" FODMAP groups, and a single high-severity symptom after dairy should at minimum flag it as a "watch" item even if below the correlation threshold.

### FODMAP category assignment
- **Assessment: Correct for the foods observed.** Garlic and onion are correctly categorized under fructans. Wheat bread is correctly identified as a fructan source. The system does not appear to confuse fructans with gluten (a common error in consumer health apps), which is a positive sign.

**Specific concerns:**
- The system does not distinguish between raw vs. cooked garlic/onion (cooking does not significantly reduce fructan content, so this is minor, but it would be informative).
- No mention of FODMAP stacking/cumulative load (discussed further in Section 5).

---

## 2. Confidence Score Calibration

**Rating: Needs Improvement**

### Observed scores:
| Trigger | Confidence | Occurrences | Symptoms |
|---------|-----------|-------------|----------|
| Garlic -> Gas | 90% | 2 | Gas |
| Garlic -> Bloating | 90% | 2 | Bloating |
| Onion -> Bloating | 85% | 2 | Bloating |
| Wheat bread -> Bloating | 80% | 2 | Bloating |

### Analysis:
These confidence scores are **too high for a 2-occurrence sample**. In clinical research, a pattern observed only twice would never be assigned 90% confidence. Consider:

- **Statistical power:** With n=2, you cannot distinguish a true trigger from coincidence. There may have been confounding variables (stress, other foods in the same meal, cumulative FODMAP load from earlier in the day) that the engine cannot control for.
- **Base rate bias:** Bloating is the most commonly reported IBS symptom, occurring in 75-90% of IBS patients regardless of food triggers. With only 7 days of data and frequent symptom logging, any food eaten before bloating has a high probability of being temporally correlated by chance alone.
- **Clinical standard:** In FODMAP reintroduction protocols, dietitians typically require 3 separate controlled challenge tests on different days to confirm a trigger. Two co-occurrences over a 7-day period with uncontrolled diet is far below this standard.

### Recommended calibration:
| Occurrences | Max Confidence | Label |
|-------------|---------------|-------|
| 2 | 40-50% | "Possible trigger" |
| 3-4 | 50-65% | "Likely trigger" |
| 5-7 | 65-80% | "Probable trigger" |
| 8+ | 80-95% | "Strong correlation" |

The current system lets the LLM assign whatever confidence it deems appropriate, which means the scores are essentially the model's "gut feeling" rather than a statistically grounded metric. This is the single most important improvement area. Confidence should be computed deterministically (or at minimum, constrained) based on occurrence count, not delegated to the LLM.

---

## 3. Recommendation Quality

**Rating: Accurate**

### Garlic: "Switch to garlic-infused oils, which provide flavor without the water-soluble fructans that cause gas."
- **Assessment: Excellent.** This is textbook Monash advice. Fructans are water-soluble but not fat-soluble, so garlic-infused oil provides the flavor compounds (allicin derivatives) without the fermentable carbohydrates. The recommendation even correctly explains the mechanism ("water-soluble fructans"), which is educational without being diagnostic.

### Garlic: "Monitor portion sizes of high-FODMAP aromatics; even small amounts in sauces can trigger significant bloating."
- **Assessment: Accurate and practical.** Hidden garlic in sauces, dressings, and marinades is one of the most common pitfalls on a low-FODMAP diet. This advice is clinically relevant and actionable.

### Onion: "Try using the green parts of spring onions or chives as a low-FODMAP alternative to bulb onions."
- **Assessment: Correct.** The green (hollow) part of spring onions (scallions) is low FODMAP per Monash data. The white bulb portion is high FODMAP. Chives are also a verified low-FODMAP alternative. This recommendation demonstrates good FODMAP knowledge.

### Wheat bread: "Consider swapping wheat bread for true sourdough or certified gluten-free options to reduce fructan load."
- **Assessment: Mostly correct with a caveat.** Traditional long-fermentation sourdough (24+ hours) does have significantly reduced fructan content because the yeast/bacteria consume the fructans during fermentation. However, most commercially labeled "sourdough" bread uses accelerated processes and retains high fructan levels. The recommendation should ideally specify "traditionally fermented sourdough with long proof times" to avoid users purchasing standard supermarket sourdough and experiencing no improvement. The gluten-free alternative is correct -- GF breads use rice/corn/potato flour which are low-FODMAP, though they may contain other triggers (e.g., inulin, chicory root fiber added for texture).

### Harmful or misleading recommendations:
- **None detected.** All recommendations are consistent with Monash University FODMAP protocol and standard dietetic advice for IBS management.
- The inclusion of "This is not medical advice" after every recommendation is appropriate and legally protective.

---

## 4. Bristol Stool Correlation

**Rating: Needs Improvement**

### What the screenshots show:
The correlation engine results display food -> symptom (bloating, gas) correlations only. There is no visible integration of Bristol Stool type data into the correlation analysis.

### What the code confirms:
Looking at the `runCorrelationEngine` function (lines 286-435), the engine queries **only meals and symptoms**. It does **not** query `poopLogs` at all:

```typescript
const [mealsSnap, symptomsSnap] = await Promise.all([
  db.collection(`users/${userId}/meals`)...
  db.collection(`users/${userId}/symptoms`)...
]);
```

Poop logs are collected in the app and used in `generateWeeklyInsights` and `generateDailySummary`, but the core correlation engine ignores them entirely.

### Clinical significance:
This is a meaningful gap. Bristol Stool type is one of the most objective measures available in gut health tracking:
- **Type 4** (smooth, soft sausage) on safe-eating days is a strong positive signal confirming baseline health.
- **Type 5-6** (soft blobs / mushy) following trigger food exposure would significantly strengthen a correlation finding and is consistent with FODMAP-driven osmotic effects and fermentation-induced motility changes.
- Changes in stool form are often more clinically informative than subjective symptom severity ratings, which are highly variable between individuals.

### Recommendation:
Add `poopLogs` to the correlation engine query and include the data in the prompt. The AI should be instructed to look for patterns like: "On days with garlic consumption, Bristol type shifts from 4 to 5-6 within 6-24 hours." This would provide a more complete picture and stronger evidence for trigger identification. Stool form changes are what gastroenterologists actually use for IBS subtyping (IBS-D, IBS-C, IBS-M) per Rome IV criteria.

---

## 5. Missing Correlations / Gaps

**Rating: Needs Improvement**

### 5a. Cumulative FODMAP Load (FODMAP Stacking)
The engine treats each food-symptom pair independently. In clinical practice, FODMAP effects are **dose-dependent and cumulative**. A patient may tolerate a small amount of garlic OR a small amount of onion individually, but eating both in the same meal creates a fructan load that exceeds the individual's tolerance threshold. The current engine cannot detect this pattern. This is one of the most common real-world triggers that dietitians counsel patients about.

**Recommendation:** Add cumulative analysis: when multiple moderate/high-FODMAP foods from the same FODMAP subcategory appear in the same meal, flag this as a potential stacking trigger.

### 5b. Time-of-Day Patterns
The engine does not analyze whether symptoms are more severe after evening meals (common in IBS due to cumulative daily FODMAP load and circadian gut motility patterns). This would be a valuable insight.

### 5c. Meal Composition Context
A high-FODMAP food consumed with fat and protein (which slows gastric emptying) may produce different symptom timing/severity than the same food consumed alone. The engine does not account for meal composition beyond individual food items.

### 5d. False Positive Risk
With only 7 days of data, the engine reported 4 correlations (garlic x2, onion, wheat) all pointing to bloating. Bloating was likely logged frequently throughout the week (it is the most common IBS symptom, often reported daily). The risk of false positive correlation is high when:
- The symptom has a high base rate (bloating occurs 70-90% of days in IBS patients)
- The food has a high exposure rate (garlic/onion are in most prepared foods)
- The sample size is small (7 days)

The engine should ideally compare trigger-day symptom rates against non-trigger-day symptom rates to establish whether the food actually increases symptom probability above baseline.

### 5e. Symptom Severity Differentiation
The screenshots show the engine detected garlic -> Gas and garlic -> Bloating as separate correlations at 90% each. However, it does not appear to weight by symptom severity. A severity-7 bloating episode after garlic is clinically much more significant than a severity-2 episode. The prompt does pass severity data, but the output structure does not distinguish severity patterns.

### 5f. Stress and Lifestyle Factors
The app collects no data on stress, sleep, physical activity, or menstrual cycle -- all of which are major confounders in IBS symptom patterns. While this is understandable for an MVP, the correlation engine should note this limitation in its output rather than presenting food-symptom correlations as if they are the complete picture.

---

## 6. AI Prompt Review

**Rating: Mostly Accurate**

### analyzeFoodPhoto (lines 151-163)
**Strengths:**
- Correctly references "Monash University FODMAP data" as the standard
- Includes all 6 FODMAP subcategories (fructose, lactose, fructans, GOS, mannitol, sorbitol)
- Asks for `lowFodmapServing` which is the correct clinical metric
- States "educational wellness purposes only"

**Concerns:**
- The prompt says "triggers: common gut symptom triggers for this food (e.g., 'high fiber', 'dairy', 'gluten')." Including "gluten" as a trigger category is problematic. In the FODMAP framework, gluten is NOT the trigger in wheat -- fructans are. Listing "gluten" as a trigger could reinforce the common misconception that IBS is related to gluten sensitivity (celiac disease) rather than FODMAP malabsorption. This should be changed to "fructans" or the gluten example should be removed.
- Temperature 0.2 is appropriate for factual food identification.

### classifyPoopPhoto (lines 225-237)
**Strengths:**
- Correctly lists all 7 Bristol Stool types with accurate descriptions
- Temperature 0.1 is appropriately low for classification
- States "educational wellness tracking purposes only"

**Concerns:**
- Type 5 description says "lacking fiber" -- this is an oversimplification. Bristol Type 5 can indicate rapid transit, osmotic effects from unabsorbed carbohydrates (relevant to FODMAP), or insufficient fiber. Saying "lacking fiber" as the sole descriptor could mislead users.
- The prompt does not ask for any dietary advice or symptom interpretation (good -- classification only is the right scope for this function).

### runCorrelationEngine (lines 342-362)
**Strengths:**
- Specifies the 2-24 hour window for food-symptom correlation, which is physiologically appropriate
- Requires minimum 2 occurrences (prevents single-data-point conclusions)
- Uses HIGH thinking level (appropriate for complex analytical task)
- Temperature 0.3 is reasonable for analytical output
- Explicitly states "educational, not medical advice"

**Concerns:**
- The prompt does not instruct the model on HOW to calculate confidence. It simply asks for a 0.0-1.0 score, leaving calibration entirely to the LLM. This is the root cause of the overconfident scores discussed in Section 2. The prompt should include calibration guidance, e.g., "Confidence should reflect statistical strength: 2 occurrences = max 0.5, 5+ occurrences = max 0.8, 10+ with consistent timing = max 0.95."
- The prompt does not mention FODMAP stacking or cumulative load.
- The prompt does not include poop log data (code does not query it).
- The prompt does not ask the model to consider confounding factors or alternative explanations.
- The prompt says "Find correlations between specific foods and symptoms" which may bias toward finding correlations even when the evidence is weak. Consider adding: "If the data is insufficient to draw meaningful conclusions, say so."

### generateWeeklyInsights (lines 497-520)
**Strengths:**
- Includes meal, symptom, poop, and gut score data (broader than correlation engine)
- Temperature 0.7 is appropriate for creative/narrative content
- Uses MEDIUM thinking level

**Concerns:**
- No specific FODMAP terminology or Monash references in the prompt
- The "one actionable tip" instruction has no guardrails against medical advice

### Regulatory Language Review
**Strengths:**
- Consistent "This is not medical advice" disclaimer on every AI-generated output
- Uses "educational wellness" framing throughout
- Bottom-of-screen disclaimer visible in screenshot 04: "This is an educational wellness tool. It is not intended to diagnose, treat, or cure any medical condition. This is not medical advice."

**Concerns:**
- The word "trigger" is used extensively. While common in the FODMAP community, in a regulatory context, identifying "trigger foods" could be interpreted as a diagnostic function. Consider using "potential sensitivity" or "possible association" language in user-facing text.
- The correlation "match" percentages (90%, 85%, 80%) presented in the UI could be interpreted by users as diagnostic certainty. Labels like "possible pattern" would be safer than a precise percentage.

---

## 7. Overall Clinical Utility

**Rating: Mostly Accurate**

### Would a GI doctor find this data presentation useful?
**Partially.** A gastroenterologist reviewing a patient's app data would find:
- **Useful:** The food diary with FODMAP annotations, symptom log with severity and timing, Bristol Stool tracking, and the temporal correlation between meals and symptoms. This is substantially more structured data than what patients typically bring to a GI appointment (if they bring anything at all).
- **Concerning:** The high confidence percentages (90%) on 2 data points would undermine credibility with a clinician. A GI doctor would immediately question the statistical basis. The presentation implies more certainty than the data supports.
- **Missing:** Stool form integration in correlations, FODMAP stacking analysis, medication/supplement tracking, stress and menstrual cycle data, water intake, and a food frequency summary (not just individual meals).

### What is the app capturing well?
1. Temporal data (timestamps on every entry) -- essential for correlation analysis
2. FODMAP subcategory annotation on foods -- goes beyond most food diary apps
3. Bristol Stool Chart classification -- the gold standard for stool assessment
4. Symptom severity on a 1-10 scale with type categorization
5. Photo evidence for meals and stool (enables AI analysis and patient recall)

### What is missing that a GI would want?
1. **Medication tracking** -- PPIs, antispasmodics, laxatives, and probiotics all affect symptoms independently
2. **Menstrual cycle tracking** -- IBS symptoms fluctuate significantly with hormonal cycles; this is a major confounder for female patients (who represent 60-70% of IBS patients)
3. **Stress/anxiety score** -- the gut-brain axis is a primary driver of IBS; food triggers cannot be accurately isolated without accounting for psychological state
4. **Water and fluid intake** -- dehydration and caffeine affect stool form and motility
5. **Physical activity** -- exercise affects GI transit time
6. **FODMAP stacking visualization** -- show cumulative FODMAP load per day/meal
7. **Negative correlation data** -- "safe days" analysis showing what the patient ate on symptom-free days (equally valuable for dietetic counseling)
8. **Elimination phase compliance tracking** -- if the user is in the elimination phase, show how strictly they adhered

---

## Summary Scorecard

| Dimension | Rating | Key Issue |
|-----------|--------|-----------|
| 1. FODMAP Science Accuracy | **Accurate** | Garlic/onion/wheat identification, fructan attribution, and time lags are all consistent with Monash data and published literature |
| 2. Confidence Score Calibration | **Needs Improvement** | 90% confidence on 2 data points is statistically unjustifiable; needs deterministic calibration based on occurrence count |
| 3. Recommendation Quality | **Accurate** | Garlic oil, spring onion greens, sourdough are all evidence-based Monash-aligned recommendations; sourdough caveat about commercial vs. traditional |
| 4. Bristol Stool Correlation | **Needs Improvement** | Poop log data is collected but not used in the correlation engine at all; a significant missed opportunity |
| 5. Missing Correlations / Gaps | **Needs Improvement** | No FODMAP stacking analysis, no severity weighting, no baseline symptom rate comparison, no confounding factor consideration |
| 6. AI Prompt Review | **Mostly Accurate** | FODMAP terminology is correct; "gluten" example in food prompt should be removed; correlation prompt needs confidence calibration instructions |
| 7. Overall Clinical Utility | **Mostly Accurate** | Good data capture foundation; missing medication, stress, cycle, and hydration tracking; high confidence scores could undermine clinical credibility |

---

## Top 5 Recommendations (Priority Order)

### 1. Implement Deterministic Confidence Scoring
Do not let the LLM assign confidence freely. Compute a base confidence from occurrence count, timing consistency, and severity correlation, then pass it as a constraint or post-process the LLM output. Formula suggestion:
```
base = min(0.95, 0.25 + (occurrences * 0.08) + (timing_consistency * 0.1) + (severity_delta * 0.05))
```
Where `timing_consistency` measures how similar the time lags are across occurrences, and `severity_delta` measures how much worse symptoms are on trigger days vs. non-trigger days.

### 2. Integrate Bristol Stool Data into Correlation Engine
Add `poopLogs` to the `runCorrelationEngine` query. Include stool form shifts (e.g., Type 4 baseline -> Type 5-6 after trigger) as supporting evidence for correlations. This provides objective corroboration for subjective symptom reports.

### 3. Add FODMAP Stacking Detection
When multiple high-FODMAP foods from the same subcategory (e.g., garlic + onion = double fructan load) appear in the same meal or same day, flag cumulative load as a potential trigger pattern distinct from individual food triggers.

### 4. Remove "Gluten" from Food Analysis Trigger Examples
In the `analyzeFoodPhoto` prompt, replace the "gluten" trigger example with "fructans" to avoid reinforcing the common misconception that wheat-related IBS symptoms are gluten-mediated. This is both scientifically more accurate and avoids unnecessary dietary restriction (celiac-free IBS patients do not need to avoid all gluten).

### 5. Add Baseline Symptom Rate Comparison
Before reporting a food-symptom correlation, the engine should compare: "How often does bloating occur on days with garlic vs. days without garlic?" If bloating occurs 80% of all days regardless, a food appearing on 2 of those days has weak evidential value. This comparison would dramatically reduce false positives.

---

## Conclusion

The AI Gut Health & IBS Tracker's correlation engine demonstrates solid foundational knowledge of FODMAP science. The trigger foods identified (garlic, onion, wheat) are legitimate high-FODMAP items, the time-lag estimates are physiologically plausible, and the low-FODMAP substitution recommendations are evidence-based and align with Monash University guidance. The app avoids the common pitfall of blaming gluten rather than fructans, and the disclaimer infrastructure is appropriate for a wellness tool.

The primary weakness is confidence score inflation -- presenting 90% confidence on 2 data points risks both misleading users and undermining credibility with healthcare providers who may review the data. Secondary gaps include the unused Bristol Stool data in correlations, absent FODMAP stacking analysis, and missing confounding factors (stress, medications, menstrual cycle). Addressing the deterministic confidence scoring and stool data integration would significantly improve the clinical utility of the tool while maintaining its appropriate position as an educational wellness aid rather than a diagnostic device.

The legal positioning as an "educational wellness tool" is well-maintained throughout the codebase, with consistent disclaimers and careful avoidance of diagnostic language. The one area to watch is the use of precise "match" percentages, which could be perceived as diagnostic confidence by unsophisticated users.
