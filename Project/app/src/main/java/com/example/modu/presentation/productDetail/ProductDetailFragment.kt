package com.example.modu.presentation.detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
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
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.presentation.MainActivity
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

    private var productDetail: Detail? = null
    private var unitPrice: Float = 0.0F
    private var selectedSize: String? = null
    private var selectedColor: String? = null
    private var quantity: Int = 0

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
        updateAddButtonState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setupAdapters() {
        sizesAdapter = SizesAdapter(emptyList()) { size ->
            selectedSize = size
            quantity = 0
            updateQuantityUI()
            updateAddButtonState()
        }

        colorsAdapter = ColorAdapter(emptyList()) { color ->
            selectedColor = color
            quantity = 0
            updateQuantityUI()
            updateAddButtonState()
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
                val stock = productDetail?.productVariantsList?.find {
                    it.size == selectedSize && it.color == selectedColor
                }?.stock ?: 0
                if (quantity < stock) {
                    quantity++
                    updateQuantityUI()
                    updateAddButtonState()
                }
            }

            icRemoveQuantityDetail.setOnClickListener {
                if (quantity > 0) {
                    quantity--
                    updateQuantityUI()
                    updateAddButtonState()
                }
            }

            btnAddCardItem.setOnClickListener {
                addToCart()
            }
        }
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

                    state.detail?.let { detail ->
                        renderDetail(detail)
                    }
                    carruselAdapter?.submitList(state.similarProducts)
                }
            }
        }
    }

    private fun renderDetail(detail: Detail) {
        productDetail = detail
        unitPrice = detail.price

        binding?.apply {
            textTitleDetail.text = detail.name
            textDescriptionDetail.text = detail.description
            textPriceDetail.text = "$ ${detail.price}"
            textTotal.text = "$ 0.00"
            imgBackground.load(detail.imageUrl)
        }

        renderSizes(detail)
        renderColors(detail)
    }

    private fun renderSizes(detail: Detail) {
        val sizes = detail.productVariantsList
            .map { it.size }
            .distinct()
        sizesAdapter.updateData(sizes)
    }

    private fun renderColors(detail: Detail) {
        val colors = detail.productVariantsList
            .map { it.color }
            .distinct()
        colorsAdapter.updateData(colors)
    }

    private fun addToCart() {
        val productVariant = productDetail?.productVariantsList
            ?.find {
                it.size == selectedSize && it.color == selectedColor
            }

        val itemId = productVariant?.id
        Log.d("AddCartItem", "cart: $itemId $quantity")
    }

    private fun updateQuantityUI() {
        binding?.apply {
            textQuantityDetail.text = quantity.toString()
            textTotal.text = "$ %.2f".format(unitPrice * quantity)

            icRemoveQuantityDetail.setBackgroundResource(
                if (quantity == 0)
                    R.drawable.ic_remove_quantity
                else
                    R.drawable.ic_remove_quantity_white
            )

        }
    }

    private fun updateAddButtonState() {
        val isEnabled = quantity > 0

        binding?.btnAddCardItem?.apply {
            this.isEnabled = isEnabled

            val backgroundColor = if (isEnabled)
                R.color.orange_primary
            else
                R.color.grey_light_button_background
            val textColor = if (isEnabled)
                R.color.white
            else
                R.color.grey_button_disable

            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), backgroundColor)
            )
            setTextColor(ContextCompat.getColor(requireContext(), textColor))

        }
    }
}