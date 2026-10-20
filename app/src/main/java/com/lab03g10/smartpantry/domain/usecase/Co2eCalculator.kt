package com.lab03g10.smartpantry.domain.usecase

import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @(#) Co2eCalculator.kt 1.0 2026-05-17
 * Copyright (c) 2026 the SmartPantry team.
 */

/**
 * Single source of truth for CO2-equivalent estimates on a wasted food item.
 *
 * Coefficients are from Poore & Nemecek (2018) "Reducing food's environmental
 * impacts through producers and consumers" (Science 360), expressed as
 * kg CO2e per kg of food.
 *
 * Previously duplicated as a private helper inside [com.lab03g10.smartpantry
 * .ui.viewmodel.FoodDetailViewModel]; consolidated here so per-category
 * coefficients can't drift between ViewModels.
 *
 * Analytics totals are read off persisted [com.lab03g10.smartpantry.data.local
 * .entity.WasteLogEntity.co2eGrams] rather than recomputed, so historical rows
 * don't shift when coefficients change.
 */
@Singleton
class Co2eCalculator @Inject constructor() {

    /** Returns CO2-equivalent grams for [item], rounded to a Float. */
    fun estimateGrams(item: FoodItemEntity): Float {
        val kg = when (item.unit.lowercase()) {
            "kg", "l"   -> item.quantity.toFloatOrNull() ?: 0f
            "g", "ml"   -> (item.quantity.toFloatOrNull() ?: 0f) / 1_000f
            // ~150g average per piece; rough fallback for countable items
            "pcs"       -> (item.quantity.toFloatOrNull() ?: 1f) * 0.15f
            else        -> item.quantity.toFloatOrNull() ?: 0f
        }
        val kgCo2ePerKg = CO2E_KG_PER_KG[item.category] ?: CO2E_KG_PER_KG.getValue("Other")
        return kg * kgCo2ePerKg * 1_000f
    }

    companion object {
        /**
         * kg CO2e per kg of food, indexed by [FoodItemEntity.category] string.
         * Exposed read-only for analytics labels / report copy; keep in sync
         * with the lookup in [estimateGrams].
         */
        val CO2E_KG_PER_KG: Map<String, Float> = mapOf(
            "Dairy"     to 18f,
            "Meat"      to 27f,
            "Produce"   to 2f,
            "Dry Goods" to 1.5f,
            "Grains"    to 1.5f,
            "Frozen"    to 5f,
            "Other"     to 5f,
        )
    }
}
