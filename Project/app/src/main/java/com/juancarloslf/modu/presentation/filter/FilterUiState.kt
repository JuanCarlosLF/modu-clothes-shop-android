package com.juancarloslf.modu.presentation.filter

import com.juancarloslf.modu.domain.entity.product.Category

data class FilterUiState(
    val categories: List<Category>? = null,
    val errorMessage: String? = null,
    val selectedTitle: String? = null,
    val selectedPriceOrder: String? = null,
    val selectedMaxPrice: Int? = null,
    val selectedCategories: List<Category> = emptyList()
)