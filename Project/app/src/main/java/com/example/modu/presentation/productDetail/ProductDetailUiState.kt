package com.example.modu.presentation.productDetail

import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Product

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val detail: Detail? = null,
    val suggestedProducts: List<Product> = emptyList(),
    val selectedSize: String? = null,
    val selectedColor: String? = null,
    val quantity: Int = 0,
    val errorMessage: String? = null
) {
    val isAddButtonEnabled: Boolean
        get() = selectedColor != null && selectedSize != null && quantity > 0

    val isButtonQuantityEnabled: Boolean
        get() = quantity > 0
}