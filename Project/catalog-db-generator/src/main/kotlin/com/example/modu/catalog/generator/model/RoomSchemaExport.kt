package com.example.modu.catalog.generator.model

import kotlinx.serialization.Serializable

@Serializable
internal data class RoomSchemaExport(
    val formatVersion: Int,
    val database: RoomSchema
)

@Serializable
internal data class RoomSchema(
    val version: Int,
    val identityHash: String,
    val entities: List<RoomEntity>,
    val views: List<RoomView> = emptyList(),
    val setupQueries: List<String>
)

@Serializable
internal data class RoomView(
    val viewName: String,
    val createSql: String
)

@Serializable
internal data class RoomEntity(
    val tableName: String,
    val createSql: String,
    val indices: List<RoomIndex> = emptyList()
)

@Serializable
internal data class RoomIndex(val createSql: String)
