package com.example.modu.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.modu.R
import com.example.modu.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val DETAIL_NAVIGATION_KEY = "PRODUCT_ID"
private const val LAYOUT_COLUMNS_QUANTITY = 2

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
    }

    private fun setupRecyclerView() {
        adapter = HomeProductsAdapter() { product ->
            val bundle = Bundle().apply {
                putInt(DETAIL_NAVIGATION_KEY, product.id)
            }
            findNavController().navigate(
                R.id.action_home_to_cart,
                bundle
            )
        }

        binding?.recyclerHome?.layoutManager =
            StaggeredGridLayoutManager(LAYOUT_COLUMNS_QUANTITY, StaggeredGridLayoutManager.VERTICAL)

        binding?.recyclerHome?.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter?.submitList(state.products)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
