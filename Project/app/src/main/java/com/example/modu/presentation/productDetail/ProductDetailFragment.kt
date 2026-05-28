package com.example.modu.presentation.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.modu.R
import com.example.modu.databinding.FragmentProductDetailBinding
import com.example.modu.presentation.MainActivity
import com.example.modu.presentation.productDetail.ProductDetailUiState
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val viewModel: DetailViewModel by viewModels()
    private var binding: FragmentProductDetailBinding? = null
    private val args: ProductDetailFragmentArgs by navArgs()
    private var carruselAdapter: CarruselAdapter? = null
    private lateinit var sizesAdapter: SizesAdapter
    private lateinit var colorsAdapter: ColorAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)
        binding = FragmentProductDetailBinding.bind(view)
        (activity as? MainActivity)?.setBottomNavVisible(false)
        setupAdapters()
        setupRecyclerViews()
        setupListeners()
        observeUiState()
        viewModel.loadDetail(args.productId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding?.apply {
                        shimmerDetail.isVisible = state.isLoading
                        contentDetail.isVisible = !state.isLoading

                        if (state.isLoading) shimmerDetail.startShimmer()
                        else shimmerDetail.stopShimmer()
                    }

                    renderDetail(state)

                }
            }
        }
    }

    private fun renderDetail(state: ProductDetailUiState) {

        binding?.apply {
            state.detail?.let { detail ->
                textTitleDetail.text = detail.name
                textDescriptionDetail.text = detail.description
                textPriceDetail.text = "$ ${detail.price}"
                imgBackground.load(detail.imageUrl)

                renderSizes(state)
                renderColors(state)
            }
            textQuantityDetail.text = state.quantity.toString()
            icRemoveQuantityDetail.setBackgroundResource(
                if (state.quantity == 0)
                    R.drawable.ic_remove_quantity
                else
                    R.drawable.ic_remove_quantity_white
            )
            val price = state.detail?.price ?: 0.0F
            textTotal.text = String.format("$ %.2f", price * state.quantity)
            btnAddCardItem.isEnabled = state.isAddButtonEnabled
            val bgColor = if (state.isAddButtonEnabled)
                R.color.orange_primary
            else
                R.color.grey_light_button_background

            val textColor = if (state.isAddButtonEnabled)
                R.color.white
            else
                R.color.grey_button_disable

            btnAddCardItem.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), bgColor)
            )

            btnAddCardItem.setTextColor(
                ContextCompat.getColor(requireContext(), textColor)
            )
            carruselAdapter?.submitList(state.similarProducts)
        }
    }

    private fun setupAdapters() {
        sizesAdapter = SizesAdapter(emptyList()) { size ->
            viewModel.onSizeSelected(size)
        }

        colorsAdapter = ColorAdapter(emptyList()) { color ->
            viewModel.onColorSelected(color)
        }
    }

    private fun setupRecyclerViews() {
        binding?.recyclerSizes?.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = sizesAdapter
        }

        binding?.recyclerColors?.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = colorsAdapter
        }

        setupCarrusel()
    }

    private fun setupCarrusel() {

        carruselAdapter = CarruselAdapter { product ->

            val action =
                ProductDetailFragmentDirections.actionFragmentProductDetailSelf(product.id)

            findNavController().navigate(action)
        }

        binding?.recyclerCarruselDetail?.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

            adapter = carruselAdapter
        }
    }

    private fun setupListeners() {
        binding?.apply {
            icBack.setOnClickListener {
                findNavController().popBackStack(R.id.fragment_home, false)
                (requireActivity() as? MainActivity)?.setBottomNavVisible(true)
            }
            icAddQuantityDetail.setOnClickListener {
                viewModel.increaseQuantity()
            }
            icRemoveQuantityDetail.setOnClickListener {
                viewModel.decreaseQuantity()
            }
            btnAddCardItem.setOnClickListener {
            }
        }
    }

    private fun renderSizes(state: ProductDetailUiState) {
        binding?.apply {
            state.detail?.let { detail ->
                val sizes = detail.productVariantsList
                    .map { it.size }
                    .distinct()
                sizesAdapter.updateData(sizes, state.selectedSize)
            }
        }
    }

    private fun renderColors(state: ProductDetailUiState) {
        binding?.apply {
            state.detail?.let { detail ->
                val colors = detail.productVariantsList
                    .map { it.color }
                    .distinct()
                colorsAdapter.updateData(colors, state.selectedColor)
            }
        }
    }
}