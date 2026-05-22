package com.example.modu.presentation.productDetail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.modu.R
import com.example.modu.databinding.FragmentProductDetailBinding
import com.example.modu.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailFragment : Fragment(R.layout.fragment_product_detail) {

    private val viewModel: ProductDetailViewModel by viewModels()
    private var binding: FragmentProductDetailBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)
        binding = FragmentProductDetailBinding.bind(view)
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setupListeners() {
        binding?.let { binding ->
            binding.icBack.setOnClickListener {
                findNavController().popBackStack()
                (activity as? MainActivity)?.setBottomNavVisible(true)
            }
        }
    }
}