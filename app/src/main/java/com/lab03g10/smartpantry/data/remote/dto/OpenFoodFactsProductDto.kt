package com.lab03g10.smartpantry.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductResponse(
    @Json(name = "code") val code: String?,
    @Json(name = "status") val status: Int?,
    @Json(name = "product") val product: OpenFoodFactsProductDto?,
)

@JsonClass(generateAdapter = true)
data class OpenFoodFactsProductDto(
    @Json(name = "product_name") val productName: String?,
    @Json(name = "product_name_en") val productNameEn: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "categories") val categories: String?,
    @Json(name = "categories_tags") val categoryTags: List<String>?,
    @Json(name = "image_url") val imageUrl: String?,
)
