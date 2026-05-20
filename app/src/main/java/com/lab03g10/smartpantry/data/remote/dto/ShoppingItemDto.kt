package com.lab03g10.smartpantry.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.lab03g10.smartpantry.data.local.entity.ShoppingItemEntity

/**
 * Firestore-shaped twin of ShoppingItemEntity (v5 schema, 10 fields).
 * No `updatedAt` exists on the entity — the merge step uses `addedAt`
 * as the LWW proxy. Edge case: a checked/unchecked toggle does NOT
 * bump addedAt, so cross-device toggle collisions could lose state.
 * Accepted given how short-lived shopping rows are (added → checked
 * → cleared within a single shopping trip).
 */
data class ShoppingItemDto(
    @get:PropertyName("id")                val id: String = "",
    @get:PropertyName("name")              val name: String = "",
    @get:PropertyName("quantity")          val quantity: String = "",
    @get:PropertyName("unit")              val unit: String = "pcs",
    @get:PropertyName("category")          val category: String = "",
    @get:PropertyName("checked")           val checked: Boolean = false,
    @get:PropertyName("addedAt")           val addedAt: Long = 0L,
    @get:PropertyName("nominalExpiryDate") val nominalExpiryDate: Long = 0L,
    @get:PropertyName("storageLocationId") val storageLocationId: String? = null,
    @get:PropertyName("autoSuggested")     val autoSuggested: Boolean = false,
)

fun ShoppingItemEntity.toDto() = ShoppingItemDto(
    id, name, quantity, unit, category, checked, addedAt,
    nominalExpiryDate, storageLocationId, autoSuggested,
)

fun ShoppingItemDto.toEntity() = ShoppingItemEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    category = category,
    checked = checked,
    addedAt = addedAt,
    nominalExpiryDate = nominalExpiryDate,
    storageLocationId = storageLocationId,
    autoSuggested = autoSuggested,
)
