package com.example.modu.presentation.home

data class HomeUiState(
    val title: String? = null,
    val orderByPrice: String? = null,
    val maxPrice: Int? = null,
    val categories: List<String>? = null,
    val errorMessage: String? = null
) {
    val hasActiveFilters: Boolean
        get() = title != null || orderByPrice != null || maxPrice != null || !categories.isNullOrEmpty()
}