package com.mononote.app.ui.archive

/**
 * How long ago a note was archived, bucketed for display.
 */
internal sealed interface RelativeTimeLabel {
    /** Within the last minute (or a timestamp in the future). */
    data object JustNow : RelativeTimeLabel

    data class MinutesAgo(
        val minutes: Int,
    ) : RelativeTimeLabel

    data class HoursAgo(
        val hours: Int,
    ) : RelativeTimeLabel

    data class DaysAgo(
        val days: Int,
    ) : RelativeTimeLabel

    /** A week or more ago; show a plain date instead of a relative label. */
    data class OnDate(
        val epochMillis: Long,
    ) : RelativeTimeLabel
}

/**
 * Buckets [epochMillis] into a [RelativeTimeLabel] relative to [now], both in
 * epoch millis. Labels never round down to zero: any elapsed time inside a
 * bucket is reported as at least one unit.
 */
internal fun relativeTimeLabel(
    epochMillis: Long,
    now: Long,
): RelativeTimeLabel {
    val elapsed = now - epochMillis
    return when {
        elapsed < MINUTE_MILLIS -> RelativeTimeLabel.JustNow
        elapsed < HOUR_MILLIS -> RelativeTimeLabel.MinutesAgo((elapsed / MINUTE_MILLIS).toInt())
        elapsed < DAY_MILLIS -> RelativeTimeLabel.HoursAgo((elapsed / HOUR_MILLIS).toInt())
        elapsed < WEEK_MILLIS -> RelativeTimeLabel.DaysAgo((elapsed / DAY_MILLIS).toInt())
        else -> RelativeTimeLabel.OnDate(epochMillis)
    }
}

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS
private const val WEEK_MILLIS = 7 * DAY_MILLIS
