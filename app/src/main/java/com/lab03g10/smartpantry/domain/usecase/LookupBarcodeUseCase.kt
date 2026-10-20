package com.lab03g10.smartpantry.domain.usecase


import com.lab03g10.smartpantry.data.local.entity.FoodItemEntity
import com.lab03g10.smartpantry.data.remote.OpenFoodFactsApi
import com.lab03g10.smartpantry.data.remote.dto.OpenFoodFactsProductDto
import com.lab03g10.smartpantry.domain.model.BarcodeProduct
import com.lab03g10.smartpantry.domain.repo.FoodRepository
import javax.inject.Inject

class LookupBarcodeUseCase @Inject constructor(
    private val foodRepository: FoodRepository,
    private val openFoodFactsApi: OpenFoodFactsApi,
) {
    suspend operator fun invoke(rawBarcode: String): Result<BarcodeProduct> {
        val barcode = rawBarcode.filter(Char::isDigit)
        if (barcode.length < 8) {
            return Result.failure(IllegalArgumentException("Enter a valid barcode."))
        }

        foodRepository.findByBarcode(barcode)?.let { existing ->
            return Result.success(existing.toBarcodeProduct())
        }

        return runCatching {
            val response = openFoodFactsApi.getProduct(barcode)
            val product = response.product
            if (response.status != 1 || product == null) {
                error("No product found for this barcode.")
            }
            product.toBarcodeProduct(barcode)
        }
    }
}

private fun FoodItemEntity.toBarcodeProduct(): BarcodeProduct =
    BarcodeProduct(
        barcode = barcode.orEmpty(),
        name = name,
        brand = null,
        category = category,
        imageUrl = imageUrl,
        fromLocalInventory = true,
    )

private fun OpenFoodFactsProductDto.toBarcodeProduct(barcode: String): BarcodeProduct {
    val name = productName?.takeIf { it.isNotBlank() }
        ?: productNameEn?.takeIf { it.isNotBlank() }
        ?: "Unnamed product"

    return BarcodeProduct(
        barcode = barcode,
        name = name,
        brand = brands
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() },
        category = inferSmartPantryCategory(),
        imageUrl = imageUrl,
    )
}

private fun OpenFoodFactsProductDto.inferSmartPantryCategory(): String {
    val text = buildString {
        append(categories.orEmpty())
        append(' ')
        append(categoryTags.orEmpty().joinToString(" "))
    }.lowercase()

    return when {
        listOf("milk", "cheese", "yogurt", "dairy").any { it in text } -> "Dairy"
        listOf("meat", "chicken", "beef", "pork", "fish", "seafood").any { it in text } -> "Meat"
        listOf("fruit", "vegetable", "produce").any { it in text } -> "Produce"
        listOf("frozen", "ice-cream").any { it in text } -> "Frozen"
        listOf("rice", "pasta", "cereal", "bread", "grain", "snack").any { it in text } -> "Dry Goods"
        else -> "Other"
    }
}
