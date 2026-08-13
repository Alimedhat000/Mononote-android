package com.mononote.app.ui.archive

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RelativeTimeTest {
    private val now = 1_000_000_000_000L

    @Test
    fun `timestamps within a minute are just now`() {
        assertEquals(RelativeTimeLabel.JustNow, relativeTimeLabel(now - 59_000, now))
        assertEquals(RelativeTimeLabel.JustNow, relativeTimeLabel(now, now))
    }

    @Test
    fun `timestamps in the future are just now`() {
        assertEquals(RelativeTimeLabel.JustNow, relativeTimeLabel(now + 60_000, now))
    }

    @Test
    fun `minutes bucket starts at one minute`() {
        assertEquals(RelativeTimeLabel.MinutesAgo(1), relativeTimeLabel(now - 60_000, now))
        assertEquals(RelativeTimeLabel.MinutesAgo(59), relativeTimeLabel(now - 59 * 60_000, now))
    }

    @Test
    fun `hours bucket starts at one hour`() {
        assertEquals(RelativeTimeLabel.HoursAgo(1), relativeTimeLabel(now - 3_600_000, now))
        assertEquals(RelativeTimeLabel.HoursAgo(23), relativeTimeLabel(now - 23 * 3_600_000, now))
    }

    @Test
    fun `days bucket starts at one day`() {
        assertEquals(RelativeTimeLabel.DaysAgo(1), relativeTimeLabel(now - 86_400_000, now))
        assertEquals(RelativeTimeLabel.DaysAgo(6), relativeTimeLabel(now - 6 * 86_400_000, now))
    }

    @Test
    fun `a week or more falls back to a date`() {
        assertEquals(RelativeTimeLabel.OnDate(now - 7L * 86_400_000), relativeTimeLabel(now - 7L * 86_400_000, now))
        assertEquals(RelativeTimeLabel.OnDate(now - 30L * 86_400_000), relativeTimeLabel(now - 30L * 86_400_000, now))
    }
}
