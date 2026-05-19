package com.example.modu.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.modu.databinding.ItemGalleryBinding
import com.example.modu.domain.entity.product.Product

private const val FADE_DURATION_MS = 500

class HomeProductsAdapter(
    val onItemClick: (Product) -> Unit
) : ListAdapter<Product, HomeProductsAdapter.ProductsViewHolder>(ProductsDiffCallback) {

    inner class ProductsViewHolder(private val binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Product) {
            binding.itemImage.load(item.image) {
                crossfade(true)
                crossfade(FADE_DURATION_MS)
                binding.root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val binding = ItemGalleryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) =
        holder.bind(getItem(position))

    private object ProductsDiffCallback : DiffUtil.ItemCallback<Product>() {

        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}