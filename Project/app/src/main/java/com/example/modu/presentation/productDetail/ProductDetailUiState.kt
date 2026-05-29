package com.example.modu.presentation.productDetail

import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Product
import java.math.BigDecimal

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val detail: Detail? = null,
    val relatedProducts: List<Product> = emptyList(),
    val selectedSize: String? = null,
    val selectedColor: String? = null,
    val quantity: Int = 0,
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val errorMessage: String? = null
) {
    val isAddButtonEnabled: Boolean
        get() = selectedColor != null && selectedSize != null && quantity > 0

    val isButtonQuantityEnabled: Boolean
        get() = quantity > 0
}