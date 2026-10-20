package com.lab03g10.smartpantry.ui.util

import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * @(#) DateUtilsTest.kt 1.0 2026-05-17
 * Copyright (c) 2026 the SmartPantry team.
 */
class DateUtilsTest {

    // Fix "now" at May 17 2026 in AEST (UTC+10) — matches the bug-report scenario.
    private val zone = ZoneId.of("Australia/Melbourne")
    private val today = LocalDate.of(2026, 5, 17)

    @Test
    fun `same calendar day returns 0`() {
        val expiry = ZonedDateTime.of(2026, 5, 17, 23, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(0, daysUntil(expiry, zone, today))
    }

    @Test
    fun `tomorrow returns 1 even when timezone offset gives less than 24h delta`() {
        // DatePicker returns midnight UTC for May 18 = 10:00 AEST May 18.
        // Diff from May 17 04:00 UTC (= 14:00 AEST) is only 20 hours.
        // Old impl returned 0 → "TODAY" badge. New impl must return 1.
        val pickerOutputMillis = ZonedDateTime.of(2026, 5, 18, 0, 0, 0, 0, ZoneId.of("UTC"))
            .toInstant().toEpochMilli()
        assertEquals(1, daysUntil(pickerOutputMillis, zone, today))
    }

    @Test
    fun `day after tomorrow returns 2`() {
        val pickerOutputMillis = ZonedDateTime.of(2026, 5, 19, 0, 0, 0, 0, ZoneId.of("UTC"))
            .toInstant().toEpochMilli()
        assertEquals(2, daysUntil(pickerOutputMillis, zone, today))
    }

    @Test
    fun `past date clamps to 0`() {
        val pastMillis = ZonedDateTime.of(2026, 5, 10, 0, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(0, daysUntil(pastMillis, zone, today))
    }

    @Test
    fun `one week away returns 7`() {
        val pickerOutputMillis = ZonedDateTime.of(2026, 5, 24, 0, 0, 0, 0, ZoneId.of("UTC"))
            .toInstant().toEpochMilli()
        assertEquals(7, daysUntil(pickerOutputMillis, zone, today))
    }
}