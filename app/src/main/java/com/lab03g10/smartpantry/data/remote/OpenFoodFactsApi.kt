package com.lab03g10.smartpantry.data.remote

import com.lab03g10.smartpantry.data.remote.dto.OpenFoodFactsProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "code,status,product_name,product_name_en,brands,categories,categories_tags,image_url",
    ): OpenFoodFactsProductResponse
}
