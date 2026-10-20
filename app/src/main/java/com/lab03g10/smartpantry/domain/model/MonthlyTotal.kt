package com.lab03g10.smartpantry.domain.model


/**
 * @(#) MonthlyTotal.kt 1.0 2026-05-10
 * Copyright (c) 2026 the SmartPantry team.
 *
 * Aggregate result from WasteLogDao.observeMonthlyTotals().
 * yearMonth format: "YYYY-MM" (e.g., "2026-04")
 */
data class MonthlyTotal(
    val yearMonth: String,
    val count: Int,
    val co2eGramsTotal: Float,
)