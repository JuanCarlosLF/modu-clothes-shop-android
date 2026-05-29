package com.example.modu.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modu.domain.usecase.product.ProductUseCase
import com.example.modu.presentation.productDetail.ProductDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
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
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            try {
                val detail = useCase.getDetailById(id)
                val firstSize = detail.productVariantsList.firstOrNull()?.size
                val firstColor = detail.productVariantsList.firstOrNull()?.color
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        detail = detail,
                        selectedSize = firstSize,
                        selectedColor = firstColor
                    )
                }
                loadRelatedProducts()
            } catch (error: Exception) {
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
                _uiState.update { state ->
                    state.copy(suggestionProducts = products)
                }
            } catch (error: Exception) {
                _uiState.update { state ->
                    state.copy(errorMessage = error.message)
                }
            }
        }
    }

    fun onSizeSelected(selectedSize: String) = _uiState.update { size ->
        size.copy(
            selectedSize = selectedSize,
            quantity = 0
        )
    }

    fun onColorSelected(selectedColor: String) = _uiState.update { color ->
        color.copy(
            selectedColor = selectedColor,
            quantity = 0
        )
    }

    fun increaseQuantity() {
        _uiState.update { state ->
            val stock = state.detail?.productVariantsList?.find { productVariant ->
                productVariant.size == state.selectedSize &&
                        productVariant.color == state.selectedColor
            }?.stock ?: 0
            val newQuantity = (state.quantity + 1).coerceAtMost(stock)
            state.copy(
                quantity = newQuantity
            )
        }
    }

    fun decreaseQuantity() {
        _uiState.update { state ->
            val newQuantity = (state.quantity - 1).coerceAtLeast(0)
            state.copy(
                quantity = newQuantity
            )
        }
    }
}