package com.lab03g10.smartpantry.domain.usecase

import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.sensor.StressSummary
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test


/**
 * @(#) ShelfLifeCalculatorTest.kt 1.0 2026-05-15
 * Copyright (c) 2026, Steve Zheng. ALL right reserved.
 * Steve Zheng PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
class ShelfLifeCalculatorTest {

    private val calculator = ShelfLifeCalculator()

    // Fixed timestamps for deterministic tests (no System.currentTimeMillis())
    private val purchaseDate = 1_715_299_200_000L   // May 10 2024 00:00 UTC
    private val nominalExpiry = 1_715_904_000_000L  // May 17 2024 00:00 UTC — 7-day shelf life
    private val now = 1_715_558_400_000L            // May 13 2024 00:00 UTC — 3 days after purchase

    // Model constants — kept local to make the arithmetic in each test obvious.
    private val tickMs = ShelfLifeCalculator.TICK_INTERVAL_MS   // 900,000 ms (15 min)
    private val breachRate = ShelfLifeCalculator.BREACH_RATE    // 2.0
    private val cleanRate = ShelfLifeCalculator.CLEAN_RATE      // 0.9
    private val nominalShelfLifeMs = nominalExpiry - purchaseDate   // 604,800,000 ms (7 d)

    private fun food(
        nominalExpiryDate: Long = nominalExpiry,
        purchaseDate: Long = this.purchaseDate,
        effectiveExpiryDate: Long = nominalExpiryDate,
    ) = FoodItemEntity(
        id = "food-1",
        name = "Milk",
        category = "Dairy",
        quantity = "1",
        unit = "L",
        barcode = null,
        imageUrl = null,
        purchaseDate = purchaseDate,
        nominalExpiryDate = nominalExpiryDate,
        effectiveExpiryDate = effectiveExpiryDate,
        storageLocationId = "f1",
        storageChangedAt = purchaseDate,
        notes = null,
        createdAt = purchaseDate,
        updatedAt = purchaseDate,
    )

    private fun stress(breach: Int, clean: Int) = StressSummary(
        breachCount = breach,
        cleanCount = clean,
        totalCount = breach + clean,
        windowStartMs = purchaseDate,
        windowEndMs = now,
    )

    @Test
    fun `no readings returns nominal expiry unchanged`() {
        val result = calculator.recalculate(food(), stress(0, 0), now)
        assertEquals(nominalExpiry, result)
    }

    @Test
    fun `all clean readings still consume shelf-life`() {
        // Checklist §6.2 row 2 claimed effective > nominal, but the implementation
        // (see ShelfLifeCalculator.kt:36-41) charges 0.9 × TICK per clean tick
        // without crediting real-time elapsed, so effective < nominal.
        // consumed = 100 * 0.9 * TICK = 81,000,000 ms
        val expectedConsumed = (100 * cleanRate * tickMs).toLong()
        val expected = purchaseDate + (nominalShelfLifeMs - expectedConsumed)
        val result = calculator.recalculate(food(), stress(breach = 0, clean = 100), now)
        assertEquals(expected, result)
        assertTrue("effective should be earlier than nominal under current formula",
            result < nominalExpiry)
    }

    @Test
    fun `all breach readings shorten shelf-life`() {
        // consumed = 100 * 2.0 * TICK = 180,000,000 ms
        val expectedConsumed = (100 * breachRate * tickMs).toLong()
        val expected = purchaseDate + (nominalShelfLifeMs - expectedConsumed)
        val result = calculator.recalculate(food(), stress(breach = 100, clean = 0), now)
        assertEquals(expected, result)
        assertTrue("effective should be earlier than nominal", result < nominalExpiry)
    }

    @Test
    fun `mixed readings - worked example from checklist 3 dot 5`() {
        // 20 breach, 180 clean → consumed = (20*2 + 180*0.9) * 900_000
        //                                  = 202 * 900_000 = 181,800,000 ms
        // remaining = 604,800,000 - 181,800,000 = 423,000,000 ms (~4.89 d)
        val expectedConsumed = (20 * breachRate * tickMs + 180 * cleanRate * tickMs).toLong()
        val expected = purchaseDate + (nominalShelfLifeMs - expectedConsumed)
        val result = calculator.recalculate(food(), stress(breach = 20, clean = 180), now)
        assertEquals(expected, result)
        // Sanity: matches the "~2 days earlier than nominal" claim in §3.5
        val daysEarlier = (nominalExpiry - result).toDouble() / (24 * 60 * 60 * 1000)
        assertTrue("should be roughly 2 days earlier than nominal (got $daysEarlier d)",
            daysEarlier in 1.9..2.2)
    }

    @Test
    fun `expired food clamps to now`() {
        // 500 breach ticks = 900,000,000 ms consumed >> 604,800,000 ms nominal life
        // remaining clamps to 0, effective = purchaseDate, which is < now → clamp to now
        val result = calculator.recalculate(food(), stress(breach = 500, clean = 0), now)
        assertEquals(now, result)
    }

    @Test
    fun `nonsensical dates returns nominal`() {
        // nominalExpiryDate <= purchaseDate triggers the bad-data guard at line 33
        val brokenFood = food(nominalExpiryDate = purchaseDate)  // shelf life = 0
        val result = calculator.recalculate(brokenFood, stress(breach = 10, clean = 10), now)
        assertEquals(purchaseDate, result)   // == brokenFood.nominalExpiryDate
    }

    @Test
    fun `single breach tick penalty`() {
        // Checklist §6.2 row 7 expected nominal − TICK, but the implementation
        // applies the full 2.0 multiplier with no real-time credit, so the
        // actual delta is 2 × TICK = 1,800,000 ms.
        val expected = nominalExpiry - (breachRate * tickMs).toLong()   // nominal − 1,800,000
        val result = calculator.recalculate(food(), stress(breach = 1, clean = 0), now)
        assertEquals(expected, result)
    }

    @Test
    fun `single clean tick penalty`() {
        // Checklist §6.2 row 8 expected a bonus (nominal + 0.1 × TICK), but the
        // implementation still charges 0.9 × TICK with no real-time credit, so
        // a single clean tick is a small penalty.
        val expected = nominalExpiry - (cleanRate * tickMs).toLong()    // nominal − 810,000
        val result = calculator.recalculate(food(), stress(breach = 0, clean = 1), now)
        assertEquals(expected, result)
    }
}