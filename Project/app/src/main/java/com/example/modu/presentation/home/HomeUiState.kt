package com.example.modu.presentation.home

import com.example.modu.domain.entity.product.Product

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null
)