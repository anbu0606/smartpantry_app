package com.lab03g10.smartpantry.domain.model


data class BarcodeProduct(
    val barcode: String,
    val name: String,
    val brand: String?,
    val category: String,
    val imageUrl: String?,
    val fromLocalInventory: Boolean = false,
)
