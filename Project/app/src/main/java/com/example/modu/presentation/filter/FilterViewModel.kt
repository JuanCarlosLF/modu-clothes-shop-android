package com.example.modu.presentation.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.usecase.product.ProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val useCase: ProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState
        .onStart {
            if (_uiState.value.categories == null) {
                loadCategories()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = _uiState.value
        )

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = useCase.getCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (error: Exception) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun updateTitleText(title: String?) {
        _uiState.update { it.copy(selectedTitle = title) }
    }

    fun updatePriceOrder(order: String?) {
        _uiState.update { it.copy(selectedPriceOrder = order) }
    }

    fun updateMaxPrice(price: Int?) {
        _uiState.update { it.copy(selectedMaxPrice = price) }
    }

    fun updateCategory(category: Category, isChecked: Boolean) {
        _uiState.update { currentState ->
            val currentSelected = currentState.selectedCategories.toMutableList()
            if (isChecked) {
                if (!currentSelected.any { it.name == category.name }) {
                    currentSelected.add(category)
                }
            } else {
                currentSelected.removeAll { it.name == category.name }
            }
            currentState.copy(selectedCategories = currentSelected)
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedTitle = null,
                selectedPriceOrder = null,
                selectedMaxPrice = null,
                selectedCategories = emptyList()
            )
        }
    }
}