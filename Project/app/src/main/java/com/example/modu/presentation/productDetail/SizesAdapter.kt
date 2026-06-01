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

    private var selectedSize: String? = null
    fun updateData(newSizes: List<String>, selected: String?) {
        sizes = newSizes
        selectedSize = selected
        notifyDataSetChanged()
    }

    inner class SizeViewHolder(
        private val binding: ItemSizeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(size: String, isSelected: Boolean) {
            with(binding) {
                textItemSize.text = size
                if (isSelected) {
                    root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    textItemSize.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black_background
                        )
                    )
                } else {
                    root.setBackgroundResource(R.drawable.bg_item_recycle)
                    textItemSize.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                }
                root.setOnClickListener {
                    onSelected(size)
                }
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
        val size = sizes[position]
        val isSelected = size == selectedSize
        holder.bind(size, isSelected)
    }

    override fun getItemCount(): Int = sizes.size
}
