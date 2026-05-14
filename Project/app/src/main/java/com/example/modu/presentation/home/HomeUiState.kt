package com.example.modu.presentation.home

import com.example.modu.domain.entity.Product

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null
)