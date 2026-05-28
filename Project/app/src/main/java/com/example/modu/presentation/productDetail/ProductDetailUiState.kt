package com.example.modu.presentation.productDetail

import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import kotlinx.coroutines.flow.Flow

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val detail: Detail? = null,
    val similarProducts: List<Product>? = null,
    val errorMessage: String? = null
)