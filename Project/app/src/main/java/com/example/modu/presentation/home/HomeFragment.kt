package com.example.modu.presentation.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.modu.R
import com.example.modu.databinding.FragmentHomeBinding
import com.example.modu.databinding.ItemHomeFilterChipBinding
import com.example.modu.presentation.filter.FilterFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val DETAIL_NAVIGATION_KEY = "PRODUCT_ID"
private const val LAYOUT_COLUMNS_QUANTITY = 2
private const val CHIP_PREFIX_TITLE = "Nombre: "
private const val CHIP_PREFIX_ORDER = "Orden: "
private const val CHIP_PREFIX_MAX_PRICE = "Máx: "
private const val CHIP_SUFFIX_CURRENCY = "$"

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null
    private val viewModel: HomeViewModel by viewModels()
    private var adapter: HomeProductsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = HomeProductsAdapter { product ->
            val bundle = Bundle().apply {
                putInt(DETAIL_NAVIGATION_KEY, product.id)
            }
            findNavController().navigate(
                R.id.action_home_to_cart,
                bundle
            )
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
                    }
                }
            }
        }
    }

    private fun updateChipsUI(state: HomeUiState) {
        val chipGroup = binding?.chipGroupFilters ?: return
        chipGroup.removeAllViews()

        state.title?.takeIf { it.isNotBlank() }?.let { title ->
            chipGroup.addView(createCloseableChip("$CHIP_PREFIX_TITLE$title") { viewModel.removeTitleFilter() })
        }
        state.orderByPrice?.takeIf { it.isNotBlank() }?.let { order ->
            chipGroup.addView(createCloseableChip("$CHIP_PREFIX_ORDER$order") { viewModel.removeOrderFilter() })
        }
        state.maxPrice?.let { price ->
            chipGroup.addView(createCloseableChip("$CHIP_PREFIX_MAX_PRICE$price$CHIP_SUFFIX_CURRENCY") { viewModel.removeMaxPriceFilter() })
        }
        state.categories?.forEach { category ->
            chipGroup.addView(createCloseableChip(category) {
                viewModel.removeCategoryFilter(category)
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
        chipBinding.root.setOnCloseIconClickListener { onClose() }
        return chipBinding.root
    }

    private fun setupListeners() {
        binding?.buttonHomeFilter?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_filter)
        }

        setFragmentResultListener(FilterFragment.RESULT_KEY) { _, bundle ->
            val title = bundle.getString(FilterFragment.BUNDLE_KEY_TITLE)?.takeIf { it.isNotBlank() }
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