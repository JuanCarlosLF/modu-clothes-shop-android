package com.juancarloslf.modu.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.juancarloslf.modu.R
import com.juancarloslf.modu.databinding.ItemSizeBinding
import com.juancarloslf.modu.presentation.productDetail.model.SizeItemUi

private const val HALF_OPACITY = 0.5F
private const val FULL_OPACITY = 1F

class SizesAdapter(
    private val onSelected: (String) -> Unit
) : ListAdapter<SizeItemUi, SizesAdapter.SizeViewHolder>(DiffCallback()) {

    private var selectedSize: String? = null

    inner class SizeViewHolder(
        private val binding: ItemSizeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SizeItemUi) {
            val context = binding.root.context

            val isSelected = item.size == selectedSize

            binding.textItemSize.text = item.size

            if (!item.enabled) {
                binding.root.setBackgroundResource(R.drawable.bg_item_size_disable)
                binding.textItemSize.setTextColor(
                    ContextCompat.getColor(context, R.color.white)
                )
                binding.textItemSize.alpha = HALF_OPACITY
                binding.root.isEnabled = false
                return
            } else binding.textItemSize.alpha = FULL_OPACITY

            binding.root.isEnabled = true

            if (isSelected) {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.white)
                )
                binding.textItemSize.setTextColor(
                    ContextCompat.getColor(context, R.color.black_background)
                )
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_item_recycle)
                binding.textItemSize.setTextColor(
                    ContextCompat.getColor(context, R.color.white)
                )
            }

            binding.root.setOnClickListener {
                onSelected(item.size)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SizeViewHolder {
        val binding = ItemSizeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SizeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SizeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSelectedSize(size: String) {
        selectedSize = size
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<SizeItemUi>() {

        override fun areItemsTheSame(
            oldItem: SizeItemUi,
            newItem: SizeItemUi
        ): Boolean {
            return oldItem.size == newItem.size
        }

        override fun areContentsTheSame(
            oldItem: SizeItemUi,
            newItem: SizeItemUi
        ): Boolean {
            return oldItem == newItem
        }
    }
}