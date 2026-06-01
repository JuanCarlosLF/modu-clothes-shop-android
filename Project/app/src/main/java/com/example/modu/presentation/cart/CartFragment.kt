package com.example.modu.presentation.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.modu.R
import com.example.modu.databinding.FragmentCartBinding
import com.example.modu.domain.entity.cart.CartItem

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var cartAdapter: CartAdapter
    private var binding: FragmentCartBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCartBinding.bind(view)
        setupAdapter()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setupAdapter() {
        cartAdapter = CartAdapter(
            onDeleteClick = { item ->
            },
            onAddQuantityClick = { item -> },
            onRemoveQuantityClick = { item -> }
        )

        binding?.recyclerItems?.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = cartAdapter
        }
    }

    private fun loadData() {

        val items = listOf(
            CartItem(
                id = 1,
                imageUrl = "https://images.unsplash.com/photo-1608063615781-e2ef8c73d114?auto=format&fit=crop&w=800&q=80",
                title = "Camiseta UN Limited",
                size = "L",
                color = "Negro",
                unitPrice = 39.99.toBigDecimal(),
                totalPrice = 79.98.toBigDecimal(),
                quantity = 2,
                currentStock = 20,
                productId = 12,
                productVariantId = 5
            )
        )

        cartAdapter.submitList(items)
    }
}