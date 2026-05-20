package com.example.modu.presentation.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.modu.databinding.ItemFilterCategoryBinding
import com.example.modu.domain.entity.product.Category
import com.google.android.flexbox.FlexboxLayoutManager

class FilterCategoriesAdapter(
    private val onCategoryChecked: (Category, Boolean) -> Unit
) : ListAdapter<Category, FilterCategoriesAdapter.CategoryViewHolder>(CategoryDiffCallback) {

    private val selectedCategoryNames = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemFilterCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))

        val layoutParams = holder.itemView.layoutParams
        if (layoutParams is FlexboxLayoutManager.LayoutParams) {
            layoutParams.isWrapBefore = (position > 0 && position % 2 == 0)
            holder.itemView.layoutParams = layoutParams
        }
    }

    fun updateSelectedCategories(newSelectedCategories: List<Category>) {
        val newNames = newSelectedCategories.map { it.name }.toSet()
        val changedNames =
            (selectedCategoryNames union newNames) - (selectedCategoryNames intersect newNames)

        selectedCategoryNames.clear()
        selectedCategoryNames.addAll(newNames)

        currentList.forEachIndexed { index, category ->
            if (category.name in changedNames) {
                notifyItemChanged(index)
            }
        }
    }

    inner class CategoryViewHolder(private val binding: ItemFilterCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            with(binding.root) {
                text = category.name
                setOnCheckedChangeListener(null)
                isChecked = category.name in selectedCategoryNames
                setOnCheckedChangeListener { _, isChecked ->
                    onCategoryChecked(category, isChecked)
                }
            }
        }
    }

    private object CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem
    }
}