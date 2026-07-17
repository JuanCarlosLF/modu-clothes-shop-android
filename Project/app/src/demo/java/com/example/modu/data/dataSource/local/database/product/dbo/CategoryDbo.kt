package com.example.modu.data.dataSource.local.database.product.dbo

import androidx.room.ColumnInfo
import androidx.room.ColumnInfo.Companion.NOCASE
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories", indices = [
        Index(
            value = ["name"],
            unique = true
        )]
)
data class CategoryDbo(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(collate = NOCASE)
    val name: String
)