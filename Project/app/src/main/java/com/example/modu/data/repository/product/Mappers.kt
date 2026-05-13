package com.example.modu.data.repository.product

import com.example.modu.data.dataSource.remote.product.dto.ProductDto
import com.example.modu.domain.entity.Product

internal fun ProductDto.toDomain() : Product {
    return Product(
        id = requireNotNull(this.id),
        name = requireNotNull(this.name),
        image = requireNotNull(this.image)
    )
}