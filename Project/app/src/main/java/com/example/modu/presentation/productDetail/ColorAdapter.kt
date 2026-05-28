package com.example.modu.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.modu.R
import com.example.modu.databinding.ItemColorBinding

class ColorAdapter(
    private var colors: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = -1

    fun updateData(newColors: List<String>) {
        colors = newColors
        selectedPosition = if (newColors.isNotEmpty()) 0 else -1
        notifyDataSetChanged()

        if (selectedPosition == 0) onSelected(newColors[0])
    }

    inner class ColorViewHolder(
        private val binding: ItemColorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(color: String, isSelected: Boolean) {
            val context = binding.root.context
            binding.viewColor.setBackgroundColor(mapColor(context, color))
            if (isSelected)
                binding.itemColor.setBackgroundResource(R.drawable.bg_item_recycle)
            else binding.itemColor.setBackgroundResource(0)
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                val oldPosition = selectedPosition
                selectedPosition = position
                onSelected(colors[bindingAdapterPosition])
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size
}
