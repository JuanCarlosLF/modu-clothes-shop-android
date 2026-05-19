package com.example.modu.data.repository.product

import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.data.dataSource.remote.product.dto.CategoryDto
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.entity.product.Category

internal fun ProductDto.toDomain() : Product {
    return Product(
        id = requireNotNull(this.id),
        image = requireNotNull(this.image)
    )
}

internal fun CategoryDto.toDomain() : Category {
    return Category(
        name = requireNotNull(this.name)
    )
}