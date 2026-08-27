package com.juancarloslf.modu.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juancarloslf.modu.R
import com.juancarloslf.modu.domain.entity.detail.ProductVariant
import com.juancarloslf.modu.domain.usecase.cart.CartUseCase
import com.juancarloslf.modu.domain.usecase.product.ProductUseCase
import com.juancarloslf.modu.presentation.productDetail.ProductDetailUiEvent
import com.juancarloslf.modu.presentation.productDetail.ProductDetailUiState
import com.juancarloslf.modu.presentation.productDetail.model.SizeItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private var isAdding = false

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
                val detail = useCaseProduct.getDetailById(id)
                val firstAvailableVariant = detail.productVariantsList.firstOrNull {
                    it.active && it.stock > 0
                }
                val firstAvailableSize = firstAvailableVariant?.size
                val firstAvailableColor = firstAvailableVariant?.color

                val sizes = buildSizes(detail.productVariantsList, firstAvailableColor)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail,
                        selectedSize = firstAvailableSize,
                        selectedColor = firstAvailableColor,
                        sizes = sizes
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

    fun onSizeSelected(size: String) {
        _uiState.update { it ->
            it.copy(
                selectedSize = size,
                quantity = 0
            )
        }
        recomputeOptions()
    }

    fun onColorSelected(color: String) {
        _uiState.update { it ->
            it.copy(
                selectedColor = color,
                quantity = 0
            )
        }
        recomputeOptions()
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
                        message = R.string.alert_out_of_stock_detail
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
            state.copy(
                quantity = newQuantity
            )
        }
    }

    fun addItemToCart() {
        if (isAdding) return
        isAdding = true

        viewModelScope.launch {
            try {
                val state = _uiState.value
                val detail = state.detail ?: return@launch
                val productVariant = detail.productVariantsList.find {
                    it.size == state.selectedSize && it.color == state.selectedColor
                } ?: return@launch

                useCaseCart.addItem(
                    productId = detail.id,
                    productVariantId = productVariant.id,
                    currentStock = productVariant.stock,
                    quantity = state.quantity,
                    unitPrice = detail.price,
                    title = detail.name,
                    imageUrl = detail.imageUrl,
                    size = state.selectedSize.orEmpty(),
                    color = state.selectedColor.orEmpty()
                )

                _uiEvent.emit(
                    ProductDetailUiEvent(
                        message = R.string.text_add_to_cart
                    )
                )

            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
                _uiEvent.emit(
                    ProductDetailUiEvent(
                        message = R.string.error_add_item_to_cart
                    )
                )
            } finally {
                isAdding = false
            }
        }
    }

    private fun buildSizes(
        variants: List<ProductVariant>,
        selectedColor: String?
    ): List<SizeItemUi> {
        val valid = variants.filter {
            it.active && it.stock > 0
        }
        val sizes = valid.map { it.size }.distinct()
        return sizes.map { size ->
            val enabled = valid.any {
                it.size == size && it.color == selectedColor
            }
            SizeItemUi(
                size = size,
                enabled = enabled
            )
        }
    }

    private fun loadRelatedProducts() {
        viewModelScope.launch {
            try {
                val categories = uiState.value.detail
                    ?.categoriesSet
                    ?.map { it.name }
                    ?: emptyList()
                val products = useCaseProduct.getRelatedProducts(categories)
                _uiState.update { it.copy(similarProducts = products) }
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun recomputeOptions() {
        val state = _uiState.value
        val variants = state.detail?.productVariantsList ?: return

        val sizes = buildSizes(variants, _uiState.value.selectedColor)

        val selectedSize =
            if (sizes.any { it.size == state.selectedSize && it.enabled }) {
                state.selectedSize
            } else {
                sizes.firstOrNull { it.enabled }?.size
            }

        _uiState.update {
            it.copy(
                sizes = sizes,
                selectedSize = selectedSize
            )
        }
    }
}