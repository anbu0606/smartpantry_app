package com.lab03g10.smartpantry.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity

/**
 * Firestore-shaped twin of FoodItemEntity. Defaults are required because
 * Firestore's deserialiser needs a no-arg constructor.
 */
data class FoodItemDto(
    @get:PropertyName("id")                  val id: String = "",
    @get:PropertyName("name")                val name: String = "",
    @get:PropertyName("category")            val category: String = "",
    @get:PropertyName("quantity")            val quantity: String = "",
    @get:PropertyName("unit")                val unit: String = "g",
    @get:PropertyName("barcode")             val barcode: String? = null,
    @get:PropertyName("imageUrl")            val imageUrl: String? = null,
    @get:PropertyName("purchaseDate")        val purchaseDate: Long = 0L,
    @get:PropertyName("nominalExpiryDate")   val nominalExpiryDate: Long = 0L,
    @get:PropertyName("effectiveExpiryDate") val effectiveExpiryDate: Long = 0L,
    @get:PropertyName("storageLocationId")   val storageLocationId: String? = null,
    @get:PropertyName("storageChangedAt")    val storageChangedAt: Long = 0L,
    @get:PropertyName("notes")               val notes: String? = null,
    @get:PropertyName("createdAt")           val createdAt: Long = 0L,
    @get:PropertyName("updatedAt")           val updatedAt: Long = 0L,
    @get:PropertyName("consumed")            val consumed: Boolean = false,
    @get:PropertyName("wasted")              val wasted: Boolean = false,
)

fun FoodItemEntity.toDto() = FoodItemDto(
    id, name, category, quantity, unit, barcode, imageUrl,
    purchaseDate, nominalExpiryDate, effectiveExpiryDate,
    storageLocationId, storageChangedAt, notes,
    createdAt, updatedAt, consumed, wasted,
)

fun FoodItemDto.toEntity() = FoodItemEntity(
    id, name, category, quantity, unit, barcode, imageUrl,
    purchaseDate, nominalExpiryDate, effectiveExpiryDate,
    storageLocationId, storageChangedAt, notes,
    createdAt, updatedAt, consumed, wasted,
)
