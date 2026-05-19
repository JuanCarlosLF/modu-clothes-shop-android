package com.example.modu.presentation.detail

data class DetailUiState(
    val isLoading: Boolean = false,
    val detail: Boolean = false,
    val isExpanded: Boolean = false,
    val error: String? = null
)