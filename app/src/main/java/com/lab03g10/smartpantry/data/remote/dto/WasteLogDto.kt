package com.lab03g10.smartpantry.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lab03g10.smartpantry.data.local.entity.WasteLogEntity

/**
 * Firestore-shaped twin of WasteLogEntity. Append-only on both sides —
 * the backup pull treats `id` as the dedupe key and never overwrites.
 *
 * `co2eGrams` is Double here because Firestore stores all decimals as
 * Double; `.toFloat()` reconstitutes the entity's Float.
 */
data class WasteLogDto(
    @get:PropertyName("id")         val id: String = "",
    @get:PropertyName("foodItemId") val foodItemId: String? = null,
    @get:PropertyName("name")       val name: String = "",
    @get:PropertyName("category")   val category: String = "",
    @get:PropertyName("quantity")   val quantity: String = "",
    @get:PropertyName("wastedAt")   val wastedAt: Long = 0L,
    @get:PropertyName("reason")     val reason: String = "",
    @get:PropertyName("co2eGrams")  val co2eGrams: Double = 0.0,
)

fun WasteLogEntity.toDto() = WasteLogDto(
    id, foodItemId, name, category, quantity, wastedAt, reason, co2eGrams.toDouble(),
)

fun WasteLogDto.toEntity() = WasteLogEntity(
    id, foodItemId, name, category, quantity, wastedAt, reason, co2eGrams.toFloat(),
)
