package com.juancarloslf.modu.presentation.cart

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.juancarloslf.modu.R
import com.juancarloslf.modu.databinding.FragmentCartBinding
import com.juancarloslf.modu.databinding.LayoutSnackbarBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

internal const val COMPONENT_ENABLED_OPACITY = 1f
internal const val COMPONENT_DISABLED_OPACITY = 0.5f

@AndroidEntryPoint
class CartFragment : Fragment(R.layout.fragment_cart) {

    private var cartAdapter: CartAdapter? = null
    private var binding: FragmentCartBinding? = null
    private val viewModel: CartViewModel by viewModels()
    private var customSnackbar: Snackbar? = null
    private var maxStockToast: Toast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCartBinding.bind(view)

        setupListeners()
        setupAdapter()
        setupObservers()
    }

    private fun setupListeners() {
        binding?.btnClearCart?.setOnClickListener {
            viewModel.onClearCartClicked()
        }

        binding?.btnAddCardItem?.setOnClickListener {
            val specialInstructions = binding?.editTextCart?.text?.toString() ?: ""
            viewModel.checkout(specialInstructions)
        }
    }

    private fun setupAdapter() {
        cartAdapter = CartAdapter(
            onDeleteClick = { item ->
                viewModel.onDeleteItemClicked(item)
            },
            onAddQuantityClick = { item ->
                viewModel.updateQuantity(item, item.quantity + 1)
            },
            onRemoveQuantityClick = { item ->
                if (item.quantity > 1) {
                    viewModel.updateQuantity(item, item.quantity - 1)
                } else {
                    viewModel.onDeleteItemClicked(item)
                }
            },
            onMaxStockReached = {
                maxStockToast?.cancel()
                maxStockToast = Toast.makeText(
                    requireContext(),
                    R.string.cart_max_stock_reached,
                    Toast.LENGTH_SHORT
                )
                maxStockToast?.show()
            },
            onImageClick = {
                item ->
                val action = CartFragmentDirections.actionCartToDetail(item.productId)
                findNavController().navigate(action)
            }
        )

        binding?.recyclerItems?.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            itemAnimator = null
            adapter = cartAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        cartAdapter?.submitList(state.items)

                        if (state.itemToUndo != null) {
                            showUndoSnackbar()
                        } else {
                            customSnackbar?.dismiss()
                        }

                        binding?.textValueSubtotal?.text =
                            getString(R.string.text_price_format, state.subTotal)
                        binding?.textValueShipping?.text =
                            getString(R.string.text_price_format, state.shippingCost)
                        binding?.textValueTotal?.text =
                            getString(R.string.text_price_format, state.total)

                        val isNotEmpty = !state.isCartEmpty

                        binding?.btnClearCart?.isEnabled = isNotEmpty
                        binding?.btnClearCart?.alpha = if (isNotEmpty) COMPONENT_ENABLED_OPACITY else COMPONENT_DISABLED_OPACITY

                        binding?.btnAddCardItem?.isEnabled = isNotEmpty
                        binding?.btnAddCardItem?.alpha = if (isNotEmpty) COMPONENT_ENABLED_OPACITY else COMPONENT_DISABLED_OPACITY
                    }
                }

                launch {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is CartUiEvent.CheckoutSuccess -> {
                                binding?.editTextCart?.text?.clear()
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.cart_toast_checkout_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is CartUiEvent.OfflineSyncFailed -> {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_sync_failed_msg),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is CartUiEvent.ShowClearCartDialog -> {
                                showClearCartDialog()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showUndoSnackbar() {
        if (customSnackbar?.isShown == true) return

        val parentView = binding?.root ?: return

        customSnackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_LONG).apply {
            view.setBackgroundColor(Color.TRANSPARENT)
            view.setPadding(0, 0, 0, 0)

            val snackbarBinding = LayoutSnackbarBinding.inflate(layoutInflater)

            snackbarBinding.textUndo.paintFlags = snackbarBinding.textUndo.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            snackbarBinding.textUndo.setTypeface(null, android.graphics.Typeface.BOLD)

            snackbarBinding.textUndo.setOnClickListener {
                viewModel.onUndoDeleteClicked()
                dismiss()
            }

            snackbarBinding.imageClose.setOnClickListener {
                viewModel.onSnackbarDismissed()
                dismiss()
            }

            (view as ViewGroup).addView(snackbarBinding.root, 0)

            addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION && event != DISMISS_EVENT_MANUAL) {
                        viewModel.onSnackbarDismissed()
                    }
                }
            })
        }

        customSnackbar?.show()
    }

    private fun showClearCartDialog() {
        context?.let { ctx ->
            MaterialAlertDialogBuilder(
                ctx,
                R.style.CustomDarkDialog
            ).setTitle(ctx.getString(R.string.cart_tob_bar))
                .setMessage(R.string.alert_delete_all_items_dialog)
                .setPositiveButton(R.string.alert_choice_delete) { _, _ ->
                    viewModel.onConfirmClearCart()
                }
                .setNegativeButton(R.string.alert_choice_cancel, null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.recyclerItems?.adapter = null
        cartAdapter = null
        customSnackbar?.dismiss()
        customSnackbar = null
        maxStockToast?.cancel()
        maxStockToast = null
        binding = null
    }
}