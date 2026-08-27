package com.juancarloslf.modu.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.juancarloslf.modu.R
import com.juancarloslf.modu.databinding.FragmentHomeBinding
import com.juancarloslf.modu.databinding.ItemHomeFilterChipBinding
import com.juancarloslf.modu.presentation.filter.FilterFragment
import com.juancarloslf.modu.presentation.filter.FilterFragment.Companion.PRICE_SORT_ASC
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val LAYOUT_COLUMNS_QUANTITY = 2
private const val WAIT_BEFORE_SEARCH_TIME: Long = 400

private const val NOT_FOUND_CODE = "400"
private const val NOT_FOUND_MSG = "not found"

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null
    private val viewModel: HomeViewModel by viewModels()
    private var adapter: HomeProductsAdapter? = null
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = HomeProductsAdapter { product ->
            val action =
                HomeFragmentDirections.actionHomeToDetail(product.id)

            findNavController().navigate(action)
        }

        binding?.recyclerHome?.layoutManager = StaggeredGridLayoutManager(
            LAYOUT_COLUMNS_QUANTITY,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding?.recyclerHome?.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.productsFlow.collectLatest { pagingData ->
                        adapter?.submitData(pagingData)
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        binding?.containerFilters?.isVisible = state.hasActiveFilters
                        updateChipsUI(state)
                        val currentSearchText = binding?.editTextHomeSearch?.text?.toString() ?: ""
                        val newSearchText = state.title ?: ""

                        if (currentSearchText != newSearchText) {
                            binding?.editTextHomeSearch?.setText(newSearchText)
                            binding?.editTextHomeSearch?.setSelection(newSearchText.length)
                        }
                    }
                }

                launch {
                    adapter?.loadStateFlow?.collectLatest { loadState ->
                        val refreshState = loadState.source.refresh
                        val isError = refreshState is LoadState.Error
                        val isLoading = refreshState is LoadState.Loading
                        val isListEmpty = refreshState is LoadState.NotLoading && adapter?.itemCount == 0

                        binding?.progressHome?.isVisible = isLoading

                        binding?.layoutError?.isVisible = isError || isListEmpty

                        binding?.recyclerHome?.isVisible = !isError && !isLoading && !isListEmpty

                        if (isListEmpty) {
                            binding?.textError?.text = getString(R.string.home_error_not_found)
                            binding?.buttonRetry?.isVisible = false
                        } else if (isError) {
                            val errorState = refreshState as? LoadState.Error
                            val errorMessage = errorState?.error?.message ?: ""

                            val isNotFoundCode = errorMessage.contains(NOT_FOUND_CODE, ignoreCase = true) ||
                                    errorMessage.contains(NOT_FOUND_MSG, ignoreCase = true)

                            if (isNotFoundCode) {
                                binding?.textError?.text = getString(R.string.home_error_not_found)
                                binding?.buttonRetry?.isVisible = false
                            } else {
                                binding?.textError?.text = getString(R.string.error_generic_oops)
                                binding?.buttonRetry?.isVisible = true
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateChipsUI(state: HomeUiState) {
        val chipGroup = binding?.chipGroupFilters ?: return
        chipGroup.removeAllViews()

        state.title?.takeIf { it.isNotBlank() }?.let { title ->
            val prefix = getString(R.string.home_filter_prefix_title)
            chipGroup.addView(createCloseableChip("$prefix$title") {
                viewModel.removeFilter(HomeViewModel.ActiveFilter.Title)
            })
        }

        state.orderByPrice?.takeIf { it.isNotBlank() }?.let { order ->
            val displayOrderResId = if (order == PRICE_SORT_ASC) R.string.home_filter_order_lowest else R.string.home_filter_order_highest

            val prefix = getString(R.string.home_filter_prefix_order)
            val displayOrderText = getString(displayOrderResId)

            chipGroup.addView(createCloseableChip("$prefix$displayOrderText") {
                viewModel.removeFilter(HomeViewModel.ActiveFilter.Order)
            })
        }

        state.maxPrice?.let { price ->
            val prefix = getString(R.string.home_filter_prefix_max_price)
            val suffix = getString(R.string.home_filter_suffix_currency)
            chipGroup.addView(createCloseableChip("$prefix$price$suffix") {
                viewModel.removeFilter(HomeViewModel.ActiveFilter.MaxPrice)
            })
        }

        state.categories?.forEach { category ->
            chipGroup.addView(createCloseableChip(category) {
                viewModel.removeFilter(HomeViewModel.ActiveFilter.Category(category))
            })
        }
    }

    private fun createCloseableChip(text: String, onClose: () -> Unit): Chip {
        val chipBinding = ItemHomeFilterChipBinding.inflate(
            layoutInflater,
            binding?.chipGroupFilters,
            false
        )
        chipBinding.root.text = text
        chipBinding.root.isCheckable = false
        chipBinding.root.setOnCloseIconClickListener { onClose() }
        return chipBinding.root
    }

    private fun setupListeners() {
        binding?.buttonHomeFilter?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_filter)
        }

        binding?.buttonRetry?.setOnClickListener {
            adapter?.retry()
        }

        binding?.editTextHomeSearch?.doAfterTextChanged { editable ->
            searchJob?.cancel()
            searchJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(WAIT_BEFORE_SEARCH_TIME)
                val query = editable?.toString()?.trim()

                if (query.isNullOrEmpty()) {
                    viewModel.updateSearchQuery(null)
                } else if (query.length >= 3) {
                    viewModel.updateSearchQuery(query)
                }
            }
        }

        setFragmentResultListener(FilterFragment.RESULT_KEY) { _, bundle ->
            val title =
                bundle.getString(FilterFragment.BUNDLE_KEY_TITLE)?.takeIf { it.isNotBlank() }
            val order = bundle.getString(FilterFragment.BUNDLE_KEY_ORDER_BY_PRICE)
            val maxPrice = if (bundle.containsKey(FilterFragment.BUNDLE_KEY_MAX_PRICE)) {
                bundle.getInt(FilterFragment.BUNDLE_KEY_MAX_PRICE)
            } else null
            val categories =
                bundle.getStringArray(FilterFragment.BUNDLE_KEY_CATEGORY_NAMES)?.toList()

            viewModel.applyFiltersFromBundle(title, order, maxPrice, categories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}