package com.juancarloslf.modu.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.juancarloslf.modu.domain.entity.product.Product
import com.juancarloslf.modu.domain.usecase.product.ProductUseCase
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

    fun applyFiltersFromBundle(
        title: String?,
        order: String?,
        maxPrice: Int?,
        categories: List<String>?
    ) {
        _uiState.update {
            it.copy(
                title = title,
                orderByPrice = order,
                maxPrice = maxPrice,
                categories = categories
            )
        }
    }

    fun updateSearchQuery(query: String?) {
        if (query == null) return
        if (_uiState.value.title != query) {
            _uiState.update { it.copy(title = query) }
        }
    }

    sealed interface ActiveFilter {
        data object Title : ActiveFilter
        data object Order : ActiveFilter
        data object MaxPrice : ActiveFilter
        data class Category(val name: String) : ActiveFilter
    }

    fun removeFilter(filter: ActiveFilter) {
        _uiState.update { state ->
            when (filter) {
                ActiveFilter.Title -> state.copy(title = null)
                ActiveFilter.Order -> state.copy(orderByPrice = null)
                ActiveFilter.MaxPrice -> state.copy(maxPrice = null)
                is ActiveFilter.Category -> {
                    val newCategories = state.categories
                        ?.filter { it != filter.name }
                        ?.takeIf { it.isNotEmpty() }
                    state.copy(categories = newCategories)
                }
            }
        }
    }
}