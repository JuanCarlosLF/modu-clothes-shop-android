package com.juancarloslf.modu.presentation.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.juancarloslf.modu.R
import com.juancarloslf.modu.databinding.ItemColorBinding

class ColorAdapter(
    private var colors: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedColor: String? = null

    fun updateData(newColors: List<String>, selected: String?) {
        colors = newColors
        selectedColor = selected
        notifyDataSetChanged()
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
                onSelected(color)
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
        val color = colors[position]
        val isSelected = color == selectedColor
        holder.bind(color, isSelected)
    }

    override fun getItemCount(): Int = colors.size
}
