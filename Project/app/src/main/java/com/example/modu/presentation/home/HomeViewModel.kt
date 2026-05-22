package com.example.modu.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.usecase.product.ProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCase: ProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val productsFlow: Flow<PagingData<Product>> = _uiState.flatMapLatest { state ->
        useCase.getProductsBy(
            title = state.title,
            orderByPrice = state.orderByPrice,
            maxPrice = state.maxPrice,
            categories = state.categories
        )
    }.cachedIn(viewModelScope)

    fun applyFiltersFromBundle(title: String?, order: String?, maxPrice: Int?, categories: List<String>?) {
        _uiState.update {
            it.copy(title = title, orderByPrice = order, maxPrice = maxPrice, categories = categories)
        }
    }

    fun removeTitleFilter() = _uiState.update { it.copy(title = null) }

    fun removeOrderFilter() = _uiState.update { it.copy(orderByPrice = null) }

    fun removeMaxPriceFilter() = _uiState.update { it.copy(maxPrice = null) }

    fun removeCategoryFilter(categoryToRemove: String) {
        _uiState.update { state ->
            val newCategories = state.categories?.filter { it != categoryToRemove }?.takeIf { it.isNotEmpty() }
            state.copy(categories = newCategories)
        }
    }

    fun updateSearchQuery(query: String?) {
        if (_uiState.value.title != query) {
            _uiState.update { it.copy(title = query) }
        }
    }
}