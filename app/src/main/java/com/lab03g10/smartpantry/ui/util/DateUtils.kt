package com.lab03g10.smartpantry.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * @(#) DateUtils.kt 1.0 2026-05-17
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 * Whole calendar days between *today* (device local timezone) and the
 * date represented by [expiryMillis].
 *
 * - Returns 0 if [expiryMillis] is today OR earlier (badge shows "TODAY").
 * - Returns 1 for tomorrow, 2 for the day after, etc.
 * - Uses calendar-day comparison via [LocalDate], NOT millis subtraction —
 *   `TimeUnit.MILLISECONDS.toDays(diff)` truncates any sub-24-hour
 *   remainder to 0 and silently misreports "tomorrow" as "today" when
 *   the user's local timezone offset shifts the UTC midnight boundary.
 *
 * @param zone defaults to device local; explicit param simplifies unit tests
 * @param today defaults to LocalDate.now(zone); explicit param for testing
 */
fun daysUntil(
    expiryMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
): Int {
    val expiryDate = Instant.ofEpochMilli(expiryMillis).atZone(zone).toLocalDate()
    return ChronoUnit.DAYS.between(today, expiryDate)
        .toInt()
        .coerceAtLeast(0)
}