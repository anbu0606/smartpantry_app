package com.lab03g10.smartpantry.domain.model


/**
 * @(#) CategoryTotal.kt 1.0 2026-05-10
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * Aggregate result from WasteLogDao.observeByCategory().
 */
data class CategoryTotal(
    val category: String,
    val count: Int,
    val co2eGramsTotal: Float,
)