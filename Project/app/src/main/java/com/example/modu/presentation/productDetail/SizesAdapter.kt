package com.example.modu.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.modu.R
import com.example.modu.databinding.ItemSizeBinding

class SizesAdapter(
    private var sizes: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<SizesAdapter.SizeViewHolder>() {
    private var selectedPosition = -1
    fun updateData(newSizes: List<String>) {
        sizes = newSizes
        selectedPosition = if (newSizes.isNotEmpty()) 0 else -1
        notifyDataSetChanged()
        if (selectedPosition == 0) onSelected(newSizes[0])
    }

    inner class SizeViewHolder(
        private val binding: ItemSizeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(size: String, isSelected: Boolean) {
            binding.textItemSize.text = size
            if (isSelected) {
                binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                binding.textItemSize.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black_background
                    )
                )
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_item_recycle)
                binding.textItemSize.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            binding.root.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                onSelected(sizes[bindingAdapterPosition])
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
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
        holder.bind(
            sizes[position],
            position == selectedPosition
        )
    }

    override fun getItemCount(): Int = sizes.size
}
