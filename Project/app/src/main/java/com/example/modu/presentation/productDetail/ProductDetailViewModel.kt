package com.example.modu.presentation.productDetail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ProductDetailViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun toggleExpanded() {
        _uiState.update {
            it.copy(
                isExpanded = !it.isExpanded
            )
        }
    }
}