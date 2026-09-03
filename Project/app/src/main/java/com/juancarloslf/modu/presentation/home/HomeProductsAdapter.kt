package com.juancarloslf.modu.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.juancarloslf.modu.R
import com.juancarloslf.modu.databinding.ItemGalleryBinding
import com.juancarloslf.modu.domain.entity.product.Product

private const val FADE_DURATION_MS = 500

class HomeProductsAdapter(
    private val onItemClick: (Product) -> Unit
) : PagingDataAdapter<Product, HomeProductsAdapter.ProductsViewHolder>(ProductsDiffCallback) {

    inner class ProductsViewHolder(private val binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Product) {
            binding.itemImage.load(item.image) {
                crossfade(true)
                crossfade(FADE_DURATION_MS)

                error(R.drawable.ic_placeholder)
                fallback(R.drawable.ic_placeholder)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
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

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    private object ProductsDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}