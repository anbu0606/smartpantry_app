package com.lab03g10.smartpantry.domain.model


/**
 * @(#) EnvironmentType.kt 1.0 2026-05-10
 * Copyright (c) 2026 the SmartPantry team.
 */
enum class EnvironmentType (
    val label: String,
    val emoji: String,
    val description: String,
    val suggestedThresholdC: Float,
    val suggestedHumidityThreshold: Float,
    val suggestedMaxLux: Float,
    ) {
    REFRIGERATOR("Refrigerator", "🧊", "Sealed cooling unit, 1–7 °C",           7f,  70f,  50f),
    FREEZER     ("Freezer",      "❄️", "Sealed freezing, −20 to −15 °C",        -15f, 70f,  50f),
    PANTRY      ("Pantry",       "🌡", "Enclosed room-temp storage, 15–25 °C",  28f,  65f, 300f),
    CUPBOARD    ("Cupboard",     "🚪", "Enclosed cabinet, 18–28 °C",            25f,  60f, 100f),
    CELLAR      ("Cellar",       "🏚", "Cool underground space, 8–15 °C",       18f,  75f,  50f),
    OPEN_SHELF  ("Open Shelf",   "📦", "Exposed ambient storage",               35f,  70f, 800f),
    GARAGE      ("Garage",       "🏠", "Unregulated outdoor-adjacent space",    40f,  80f, 600f),
    CUSTOM      ("Custom",       "⚙️", "Define your own environment type",      30f,  70f, 500f),
}