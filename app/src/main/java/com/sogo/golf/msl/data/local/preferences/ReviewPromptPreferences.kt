package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Play Store rating prompt bookkeeping.
 *
 * The native in-app review sheet (stars only, comment optional) is requested
 * after the golfer's THIRD clean round submission (success only — partial
 * successes and failures never count), at the moment they tap Done on the
 * submit-success dialog: the round is finished, nothing is interrupted, and
 * they just had the app's best moment.
 *
 * Google Play is the final arbiter of whether the sheet actually appears (it
 * enforces its own quota and gives no signal), so we keep our own bookkeeping:
 * prompt from the 3rd success onward, but at most once every 60 days — giving
 * Play later chances if it suppressed one.
 */
@Singleton
class ReviewPromptPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("review_prompt_preferences", Context.MODE_PRIVATE)
    }

    /** Call on a CLEAN round submission only. */
    fun recordSuccessfulSubmission() {
        val count = prefs.getInt(KEY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_COUNT, count).apply()
        Log.d(TAG, "⭐️ successful submissions = $count")
    }

    fun shouldRequestReview(): Boolean {
        // DEV-ONLY: prompt after EVERY submission so the flow can be exercised
        // without playing three rounds. Note: sideloaded debug builds won't
        // RENDER the Play sheet (Play-installed builds only) — watch the
        // "ReviewPrompt" logs to confirm the trigger fires. Release builds use
        // the real threshold below.
        if (com.sogo.golf.msl.BuildConfig.DEBUG) return true

        val count = prefs.getInt(KEY_COUNT, 0)
        if (count < MIN_SUBMISSIONS) return false
        val last = prefs.getLong(KEY_LAST_REQUEST, 0L)
        return last == 0L || System.currentTimeMillis() - last >= MIN_MILLIS_BETWEEN_REQUESTS
    }

    fun recordReviewRequested() {
        prefs.edit().putLong(KEY_LAST_REQUEST, System.currentTimeMillis()).apply()
    }

    companion object {
        private const val TAG = "ReviewPrompt"
        private const val KEY_COUNT = "successful_submission_count"
        private const val KEY_LAST_REQUEST = "last_request_millis"
        private const val MIN_SUBMISSIONS = 3
        private const val MIN_MILLIS_BETWEEN_REQUESTS = 60L * 24 * 60 * 60 * 1000
    }
}
