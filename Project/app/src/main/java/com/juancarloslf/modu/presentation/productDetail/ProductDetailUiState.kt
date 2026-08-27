package com.juancarloslf.modu.presentation.productDetail

import com.juancarloslf.modu.domain.entity.detail.Detail
import com.juancarloslf.modu.domain.entity.product.Product
import com.juancarloslf.modu.presentation.productDetail.model.SizeItemUi
import java.math.BigDecimal

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val detail: Detail? = null,
    val similarProducts: List<Product> = emptyList(),
    val selectedSize: String? = null,
    val selectedColor: String? = null,
    val sizes: List<SizeItemUi>? = null,
    val quantity: Int = 0,
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val errorMessage: String? = null,
) {
    val isAddButtonEnabled: Boolean
        get() = selectedColor != null && selectedSize != null && quantity > 0
}