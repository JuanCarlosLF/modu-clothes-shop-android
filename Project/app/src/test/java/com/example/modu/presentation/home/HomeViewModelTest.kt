package com.example.modu.presentation.home

import com.example.modu.domain.usecase.product.ProductUseCase
import com.example.modu.MainDispatcherRule
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var productUseCase: ProductUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        productUseCase = mockk()
        viewModel = HomeViewModel(productUseCase)
    }

    @Test
    fun `updateSearchQuery given null query when called then doesn't update title`() {
        // GIVEN
        viewModel.updateSearchQuery("shirt")

        // WHEN
        viewModel.updateSearchQuery(null)

        // THEN
        assertEquals("shirt", viewModel.uiState.value.title)
    }

    @Test
    fun `updateSearchQuery given valid value when called then updates title`() {
        // GIVEN
        viewModel.updateSearchQuery("shoes")

        // WHEN
        viewModel.updateSearchQuery("shoesS")

        // THEN
        assertEquals("shoesS", viewModel.uiState.value.title)
    }
}
