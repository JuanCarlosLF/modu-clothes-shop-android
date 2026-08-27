package com.juancarloslf.modu.presentation.cart

sealed interface CartUiEvent {
    object CheckoutSuccess : CartUiEvent
    object OfflineSyncFailed : CartUiEvent
    object ShowClearCartDialog : CartUiEvent
}