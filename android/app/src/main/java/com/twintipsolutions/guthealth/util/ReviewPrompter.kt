package com.twintipsolutions.guthealth.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Decides when to ask for a Play Store rating. Mirrors iOS ReviewPrompter.
 *
 * - Engagement: after a positive log (meal, or a normal Bristol 3–4 stool) once the user has
 *   logged at least [MIN_LOGS] entries across [MIN_DISTINCT_DAYS] days. Once per app version.
 * - Correlation report: after the 1st and every 3rd successful report.
 *
 * Google quotas the actual dialog and never reports whether it showed, so
 * `review_prompt_requested` counts requests, not displays.
 */
object ReviewPrompter {
    private const val MIN_LOGS = 5
    private const val MIN_DISTINCT_DAYS = 3
    private const val COOLDOWN_DAYS = 14L

    private const val PREFS = "gut_health_prefs"
    private const val KEY_LOG_COUNT = "reviewLogCount"
    private const val KEY_LOG_DAYS = "reviewLogDays"
    private const val KEY_LAST_PROMPT = "reviewLastPromptMillis"
    private const val KEY_ENGAGEMENT_VERSION = "reviewEngagementPromptVersion"
    private const val KEY_CORRELATION_COUNT = "correlationReportCount"

    /** Call after any log saves. Symptoms and abnormal stools count but never prompt. */
    fun recordLog(context: Context, isPositiveMoment: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_LOG_COUNT, 0) + 1
        val days = prefs.getStringSet(KEY_LOG_DAYS, emptySet())!!.toMutableSet()
        if (days.size < MIN_DISTINCT_DAYS) {
            days.add(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
        }
        prefs.edit().putInt(KEY_LOG_COUNT, count).putStringSet(KEY_LOG_DAYS, days).apply()

        val version = appVersion(context)
        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT, 0L)
        val inCooldown = lastPrompt > 0 &&
            System.currentTimeMillis() - lastPrompt < TimeUnit.DAYS.toMillis(COOLDOWN_DAYS)

        if (!isPositiveMoment || count < MIN_LOGS || days.size < MIN_DISTINCT_DAYS ||
            prefs.getString(KEY_ENGAGEMENT_VERSION, null) == version || inCooldown
        ) return

        prefs.edit().putString(KEY_ENGAGEMENT_VERSION, version).apply()
        launch(context, trigger = "engagement")
    }

    /** Call after a successful correlation report. */
    fun recordCorrelationReport(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_CORRELATION_COUNT, 0) + 1
        prefs.edit().putInt(KEY_CORRELATION_COUNT, count).apply()
        if (count == 1 || count % 3 == 0) launch(context, trigger = "correlation_report")
    }

    private fun launch(context: Context, trigger: String) {
        val activity = context.findActivity() ?: return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_PROMPT, System.currentTimeMillis()).apply()
        FirebaseAnalytics.getInstance(context).logEvent("review_prompt_requested", Bundle().apply {
            putString("trigger", trigger)
            putLong("log_count", prefs.getInt(KEY_LOG_COUNT, 0).toLong())
        })
        val reviewManager = ReviewManagerFactory.create(activity)
        reviewManager.requestReviewFlow().addOnSuccessListener { reviewInfo ->
            reviewManager.launchReviewFlow(activity, reviewInfo)
        }
    }

    private fun appVersion(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    } catch (_: Exception) {
        "unknown"
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
