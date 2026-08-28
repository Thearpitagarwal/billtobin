package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithLocations(
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "itemId"
    )
    val locations: List<ItemLocationEntity>
)

data class InventoryDisplayItem(
    val itemId: Long,
    val itemName: String,
    val aliases: String,
    val packageType: PackageType,
    val floor: String,
    val row: String,
    val barrack: String,
    val shelf: String,
    val locationCode: String,
    val priority: Int,
    val stockQty: Int = 100,
    val isPicked: Boolean = false,
    val matchConfidence: Float? = null,
    val matchedLine: String? = null
)
