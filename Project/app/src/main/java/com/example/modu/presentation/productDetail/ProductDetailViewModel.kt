package com.example.modu.presentation.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modu.domain.usecase.cart.CartUseCase
import com.example.modu.domain.usecase.product.ProductUseCase
import com.example.modu.presentation.productDetail.ProductDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun loadDetail(id: Int) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                errorMessage = null
            )
        }
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
                val products = useCaseProduct.getRelatedProducts(categories)
                _uiState.update { state ->
                    state.copy(suggestedProducts = products)
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

    fun addItemToCart() {
        viewModelScope.launch {
            try {

                val detail = _uiState.value.detail
                val productVariant = detail?.productVariantsList?.find {
                    it.size == _uiState.value.selectedSize && it.color == _uiState.value.selectedColor
                }

                Log.d(
                    "CART_REQUEST", """
    productId=${detail?.id}
    productVariantId=${productVariant?.id}
    currentStock=${productVariant?.stock}
    quantity=${_uiState.value.quantity}
    unitPrice=${detail?.price ?: BigDecimal.ZERO}
    title=${productVariant?.name}
    imageUrl=${detail?.imageUrl}
    size=${_uiState.value.selectedSize}
    color=${_uiState.value.selectedColor}
""".trimIndent()
                )
                useCaseCart.addItem(
                    productId = detail?.id ?: 0,
                    productVariantId = productVariant?.id ?: 0,
                    currentStock = productVariant?.stock ?: 0,
                    quantity = _uiState.value.quantity,
                    unitPrice = detail?.price ?: BigDecimal.ZERO,
                    title = productVariant?.name ?: "",
                    imageUrl = detail?.imageUrl ?: "",
                    size = _uiState.value.selectedSize ?: "",
                    color = _uiState.value.selectedColor ?: ""
                )

                Log.d("VMADD", "OK")

            } catch (error: Exception) {
                Log.d("VMADDERROR", "REAL ERROR", error)


                _uiState.update { state ->
                    state.copy(errorMessage = error.message)
                }
            }
        }
    }
}