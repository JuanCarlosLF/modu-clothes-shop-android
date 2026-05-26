package com.example.modu.data.repository.product

import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.detail.DetailDto
import com.example.modu.data.dataSource.remote.product.dto.detail.ProductVariantDto
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.detail.ProductVariant
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product

internal fun ProductDto.toDomain(): Product {
    return Product(
        id = requireNotNull(this.id),
        image = requireNotNull(this.image)
    )
}

internal fun DetailDto.toDomain(): Detail = Detail(
    id = this.id ?: 0,
    name = this.name.orEmpty(),
    description = this.description.orEmpty(),
    imageUrl = this.imageUrl.orEmpty(),
    price = this.price ?: 0f,
    categoriesSet = this.categories?.map { it.toDomain() } ?: emptyList(),
    productVariantsList = this.productVariants?.map { it.toDomain() } ?: emptyList(),
)

internal fun CategoryDto.toDomain(): Category = Category(
    name = requireNotNull(this.name)
)

internal fun ProductVariantDto.toDomain(): ProductVariant = ProductVariant(
    id = requireNotNull(this.id),
    name = requireNotNull(this.name),
    size = requireNotNull(this.size),
    color = requireNotNull(this.color),
    stock = requireNotNull(this.stock),
    active = requireNotNull(this.active),
    productId = requireNotNull(this.productId)
)