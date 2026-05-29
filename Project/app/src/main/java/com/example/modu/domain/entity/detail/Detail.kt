package com.example.modu.domain.entity.detail

import com.example.modu.domain.entity.product.Category
import java.math.BigDecimal

data class Detail(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: BigDecimal,
    val categoriesSet: List<Category>,
    val productVariantsList: List<ProductVariant>
)