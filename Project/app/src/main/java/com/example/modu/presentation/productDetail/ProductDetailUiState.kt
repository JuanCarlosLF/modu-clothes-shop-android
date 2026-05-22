package com.example.modu.presentation.productDetail

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val detail: Boolean = false,
    val isExpanded: Boolean = false,
    val error: String? = null
)