package com.example.modu.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modu.domain.usecase.product.ProductUseCase
import com.example.modu.presentation.productDetail.ProductDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val useCase: ProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadDetail(id: Int) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            try {
                delay(500)
                val detail = useCase.getDetailById(id)
                Log.d("DETAIL_VMDetail", "Respuesta API: $detail")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail
                    )
                }
                loadRelatedProducts()
            } catch (error: Exception) {
                Log.e("DETAIL_VMError", "Error: ${error.message}", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }

    fun loadRelatedProducts() {
        viewModelScope.launch {
            try {
                val categories = uiState.value.detail
                    ?.categoriesSet
                    ?.map { it.name }
                    ?: emptyList()
                val products = useCase.getRelatedProducts(categories)
                Log.d("DETAIL_VMCategories", "Respuesta API: $products")
                _uiState.update { it.copy(similarProducts = products) }
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }
}