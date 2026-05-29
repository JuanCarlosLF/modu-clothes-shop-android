package com.example.modu.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modu.domain.entity.cart.CartItem
import com.example.modu.domain.exception.AppError
import com.example.modu.domain.usecase.cart.CartUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

private const val JOB_DELAY_MS: Long = 5000

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartUseCase: CartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState = _uiState.asStateFlow()

    private var currentRoomCartItems: List<CartItem> = emptyList()
    private var undoJob: Job? = null

    init {
        observeCart()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartUseCase.getCartFlow().collect { cart ->
                currentRoomCartItems = cart.items

                _uiState.update { state ->
                    val undoItemId = state.itemToUndo?.id
                    val filteredItems = if (undoItemId != null) {
                        cart.items.filter { it.id != undoItemId }
                    } else {
                        cart.items
                    }

                    val filteredSubTotal = filteredItems.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }
                    val filteredTotal = filteredSubTotal + cart.shippingCost

                    state.copy(
                        isLoading = false,
                        items = filteredItems,
                        subTotal = filteredSubTotal,
                        shippingCost = cart.shippingCost,
                        total = filteredTotal,
                        isCartEmpty = filteredItems.isEmpty()
                    )
                }
            }
        }
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            try {
                cartUseCase.updateQuantity(item, newQuantity)
            } catch (error: AppError) {
                _uiState.update { it.copy(error = error) }
            }
        }
    }

    fun onDeleteItemClicked(item: CartItem) {
        undoJob?.cancel()

        _uiState.value.itemToUndo?.let { previousItem ->
            viewModelScope.launch {
                try {
                    cartUseCase.deleteItem(previousItem.id)
                } catch (error: AppError) {
                    _uiState.update { it.copy(error = error) }
                }
            }
        }

        _uiState.update { state ->
            val filteredItems = state.items.filter { it.id != item.id }
            val newSubTotal = filteredItems.fold(BigDecimal.ZERO) { acc, i -> acc + i.totalPrice }

            state.copy(
                itemToUndo = item,
                items = filteredItems,
                subTotal = newSubTotal,
                total = newSubTotal + state.shippingCost,
                isCartEmpty = filteredItems.isEmpty()
            )
        }

        undoJob = viewModelScope.launch {
            delay(JOB_DELAY_MS)
            try {
                cartUseCase.deleteItem(item.id)
            } catch (error: AppError) {
                _uiState.update { it.copy(error = error) }
            } finally {
                _uiState.update { it.copy(itemToUndo = null) }
            }
        }
    }

    fun onUndoDeleteClicked() {
        undoJob?.cancel()

        _uiState.update { state ->
            val restoredItems = currentRoomCartItems
            val restoredSubTotal = restoredItems.fold(BigDecimal.ZERO) { acc, item -> acc + item.totalPrice }

            state.copy(
                itemToUndo = null,
                items = restoredItems,
                subTotal = restoredSubTotal,
                total = restoredSubTotal + state.shippingCost,
                isCartEmpty = restoredItems.isEmpty()
            )
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(itemToUndo = null) }
    }

    fun onClearCartClicked() {
        _uiState.update { it.copy(showClearCartDialog = true) }
    }

    fun onDismissClearCartDialog() {
        _uiState.update { it.copy(showClearCartDialog = false) }
    }

    fun onConfirmClearCart() {
        _uiState.update { it.copy(showClearCartDialog = false) }
        viewModelScope.launch {
            try {
                cartUseCase.clearCart()
            } catch (error: AppError) {
                _uiState.update { it.copy(error = error) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}