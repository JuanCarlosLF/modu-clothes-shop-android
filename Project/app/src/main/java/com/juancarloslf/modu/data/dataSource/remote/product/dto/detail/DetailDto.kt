package com.juancarloslf.modu.data.dataSource.remote.product.dto.detail

import com.juancarloslf.modu.data.dataSource.remote.product.dto.CategoryDto
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class DetailDto(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val price: BigDecimal? = null,
    @SerializedName("categoriesSet")
    val categories: List<CategoryDto>? = null,
    @SerializedName("productVariantsList")
    val productVariants: List<ProductVariantDto>? = null
)