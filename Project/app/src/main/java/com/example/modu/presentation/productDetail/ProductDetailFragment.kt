package com.example.modu.presentation.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import java.math.BigDecimal

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val viewModel: DetailViewModel by viewModels()
    private var binding: FragmentProductDetailBinding? = null
    private val args: ProductDetailFragmentArgs by navArgs()

    private var carrouselAdapter: CarruselAdapter? = null
    private var sizesAdapter: SizesAdapter? = null
    private var colorsAdapter: ColorAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)
        binding = FragmentProductDetailBinding.bind(view)

        setupAdapters()
        setupRecyclerViews()
        setupListeners()
        observeUiState()
        observeEvents()

        viewModel.loadDetail(args.productId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerSizes?.adapter = null
        binding?.recyclerColors?.adapter = null
        binding?.recyclerCarruselDetail?.adapter = null
        sizesAdapter = null
        colorsAdapter = null
        carrouselAdapter = null
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

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    Toast.makeText(
                        requireContext(),
                        event.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun renderDetail(state: ProductDetailUiState) {
        binding?.apply {
            state.detail?.let { detail ->
                textTitleDetail.text = detail.name
                textDescriptionDetail.text = detail.description
                textPriceDetail.text = getString(R.string.text_price_format, detail.price)
                imgBackground.load(detail.imageUrl)

                val sizes = detail.productVariantsList.mapNotNull { it.size }.distinct()
                sizesAdapter?.updateData(sizes, state.selectedSize)

                val colors = detail.productVariantsList.mapNotNull { it.color }.distinct()
                colorsAdapter?.updateData(colors, state.selectedColor)
            }

            textQuantityDetail.text = state.quantity.toString()

            icRemoveQuantityDetail.setBackgroundResource(
                state.isButtonQuantityEnabled.toColorRes(
                    enabled = R.drawable.ic_remove_quantity_white,
                    disabled = R.drawable.ic_remove_quantity
                )
            )

            val price = state.detail?.price ?: BigDecimal.ZERO
            val total = price.multiply(state.quantity.toBigDecimal())
            textTotal.text = getString(R.string.text_price_format, total)

            btnAddCardItem.isEnabled = state.isAddButtonEnabled

            val bgColor = state.isAddButtonEnabled.toColorRes(
                enabled = R.color.orange_primary,
                disabled = R.color.grey_light_button_background
            )

            val textColor = state.isAddButtonEnabled.toColorRes(
                enabled = R.color.white,
                disabled = R.color.grey_button_disable
            )

            btnAddCardItem.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), bgColor)
            )

            btnAddCardItem.setTextColor(
                ContextCompat.getColor(requireContext(), textColor)
            )

            carrouselAdapter?.submitList(state.suggestedProducts)
        }
    }

    private fun Boolean.toColorRes(enabled: Int, disabled: Int) = if (this) enabled else disabled

    private fun setupAdapters() {
        sizesAdapter = SizesAdapter(emptyList()) { size ->
            viewModel.onSizeSelected(size)
        }
        colorsAdapter = ColorAdapter(emptyList()) { color ->
            viewModel.onColorSelected(color)
        }
        carrouselAdapter = CarruselAdapter { product ->
            val action = ProductDetailFragmentDirections.actionFragmentProductDetailSelf(product.id)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerViews() {
        binding?.apply {
            recyclerSizes.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
                adapter = sizesAdapter
            }

            recyclerColors.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                }
                adapter = colorsAdapter
            }

            recyclerCarruselDetail.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = carrouselAdapter
            }
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
                viewModel.addItemToCart()
            }
        }
    }
}