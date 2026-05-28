package com.example.modu.domain.entity.cart

sealed class CartAlertEvent {
    data class PriceChanged(val alert: PriceChangedAlert) : CartAlertEvent()
    data class StockInsufficient(val alert: InsufficientStockAlert) : CartAlertEvent()
    data class VariantUnavailable(val alert: VariantAvailabilityAlert) : CartAlertEvent()
}