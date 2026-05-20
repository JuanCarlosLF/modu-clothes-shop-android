package com.example.modu.presentation.filter

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.modu.R
import com.example.modu.databinding.FragmentFilterBinding
import com.example.modu.presentation.utils.setupAccordion
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val PRICE_SORT_DESC = "highest"
private const val PRICE_SORT_ASC = "lowest"

@AndroidEntryPoint
class FilterFragment : Fragment(R.layout.fragment_filter) {

    private var binding: FragmentFilterBinding? = null
    private val viewModel: FilterViewModel by viewModels()
    private lateinit var categoryAdapter: FilterCategoriesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFilterBinding.bind(view)

        setupAdapter()
        setupListeners()
        setupObservers()
    }

    private fun setupAdapter() {
        categoryAdapter = FilterCategoriesAdapter { category, isChecked ->
            viewModel.updateCategory(category, isChecked)
        }

        val flexLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }

        binding?.rvFilterCategory?.apply {
            layoutManager = flexLayoutManager
            adapter = categoryAdapter
        }
    }

    private fun setupListeners() {
        binding?.let { safeBinding ->
            with(safeBinding) {
                buttonClose.setOnClickListener { findNavController().popBackStack() }

                headerFilterTitle.setupAccordion(imageArrowTitle, contentFilterTitle)
                headerFilterPrice.setupAccordion(imageArrowPrice, contentFilterPrice)
                headerFilterRange.setupAccordion(imageArrowRange, contentFilterRange)
                headerFilterCategory.setupAccordion(imageArrowCategory, rvFilterCategory)

                contentFilterTitle.editText?.doAfterTextChanged { text ->
                    viewModel.updateTitleText(
                        text?.toString()?.takeIf { it.isNotBlank() }
                    )
                }

                buttonFilterLowest.setOnClickListener {
                    viewModel.updatePriceOrder(PRICE_SORT_ASC)
                }

                buttonFilterHighest.setOnClickListener {
                    viewModel.updatePriceOrder(PRICE_SORT_DESC)
                }

                sliderPriceRange.setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textCurrentPrice.text =
                            getString(R.string.filter_slider_current_value, progress)
                        if (fromUser) {
                            viewModel.updateMaxPrice(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                buttonReset.setOnClickListener {
                    viewModel.resetFilters()
                }

                buttonSaveFilters.setOnClickListener {
                    val state = viewModel.uiState.value

                    val resultBundle = Bundle().apply {
                        putString("title", state.selectedTitle)
                        putString("orderByPrice", state.selectedPriceOrder)

                        state.selectedMaxPrice?.let { putInt("maxPrice", it) }

                        val categoryNamesArray = state.selectedCategories.map { it.name }.toTypedArray()
                        putStringArray("categoryNames", categoryNamesArray)
                    }
                    setFragmentResult("filter_request", resultBundle)
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    state.categories?.let { categories ->
                        categoryAdapter.submitList(categories)
                    }
                    categoryAdapter.updateSelectedCategories(state.selectedCategories)

                    binding?.apply {
                        if (contentFilterTitle.editText?.text?.toString() != state.selectedTitle) {
                            contentFilterTitle.editText?.setText(state.selectedTitle ?: "")
                        }

                        buttonFilterLowest.isChecked = state.selectedPriceOrder == PRICE_SORT_ASC
                        buttonFilterHighest.isChecked = state.selectedPriceOrder == PRICE_SORT_DESC

                        val maxPrice = state.selectedMaxPrice ?: sliderPriceRange.max
                        if (sliderPriceRange.progress != maxPrice) {
                            sliderPriceRange.progress = maxPrice
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}