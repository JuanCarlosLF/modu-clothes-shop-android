package com.example.modu.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modu.domain.usecase.cart.CartUseCase
import com.example.modu.domain.usecase.product.ProductUseCase
import com.example.modu.presentation.productDetail.ProductDetailUiEvent
import com.example.modu.presentation.productDetail.ProductDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val useCaseProduct: ProductUseCase,
    private val useCaseCart: CartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEvent = MutableSharedFlow<ProductDetailUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun loadDetail(id: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val detail = useCaseProduct.getDetailById(id)
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
                    it.copy(isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    private fun loadRelatedProducts() {
        viewModelScope.launch {
            try {
                val categories =
                    _uiState.value.detail?.categoriesSet?.mapNotNull { it.name } ?: emptyList()
                val products = useCaseProduct.getRelatedProducts(categories)
                _uiState.update { it.copy(suggestedProducts = products) }
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun onSizeSelected(selectedSize: String) = _uiState.update {
        it.copy(selectedSize = selectedSize, quantity = 0)
    }

    fun onColorSelected(selectedColor: String) = _uiState.update {
        it.copy(selectedColor = selectedColor, quantity = 0)
    }

    fun increaseQuantity() {
        val state = _uiState.value
        val stock = state.detail?.productVariantsList?.find {
            it.size == state.selectedSize &&
                    it.color == state.selectedColor
        }?.stock ?: 0

        if (state.quantity >= stock) {
            viewModelScope.launch {
                _uiEvent.emit(
                    ProductDetailUiEvent(
                        message = "No hay más stock disponible"
                    )
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                quantity = it.quantity + 1
            )
        }
    }

    fun decreaseQuantity() {
        _uiState.update { state ->
            val newQuantity = (state.quantity - 1).coerceAtLeast(0)
            state.copy(quantity = newQuantity)
        }
    }

    fun addItemToCart() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val detail = state.detail ?: return@launch
                val productVariant = detail.productVariantsList.find {
                    it.size == state.selectedSize && it.color == state.selectedColor
                } ?: return@launch

                useCaseCart.addItem(
                    productId = detail.id ?: 0,
                    productVariantId = productVariant.id ?: 0,
                    currentStock = productVariant.stock ?: 0,
                    quantity = state.quantity,
                    unitPrice = detail.price ?: BigDecimal.ZERO,
                    title = detail.name ?: "",
                    imageUrl = detail.imageUrl ?: "",
                    size = state.selectedSize ?: "",
                    color = state.selectedColor ?: ""
                )
                viewModelScope.launch {
                    _uiEvent.emit(
                        ProductDetailUiEvent(
                            message = "Tu producto se ha añadido al carrito"
                        )
                    )
                }
            } catch
                (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
                viewModelScope.launch {
                    _uiEvent.emit(
                        ProductDetailUiEvent(
                            message = _uiState.value.errorMessage.toString()
                        )
                    )
                }
            }
        }
    }
}