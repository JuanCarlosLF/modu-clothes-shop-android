package com.example.modu.data.repository.clothes

import com.example.modu.data.dataSource.remote.clothes.dto.ClothesItemDto
import com.example.modu.domain.entity.ClothesItem

internal fun ClothesItemDto.toDomain() : ClothesItem {
    return ClothesItem(
        id = requireNotNull(this.id),
        name = requireNotNull(this.name),
        image = requireNotNull(this.image)
    )
}