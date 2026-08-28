package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "item_locations",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["itemId"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["itemId", "packageType"], unique = true)
    ]
)
data class ItemLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val locationId: Long = 0L,
    val itemId: Long,
    val packageType: PackageType,
    val floor: String,
    val row: String,
    val barrack: String,
    val shelf: String,
    val locationCode: String,
    val priority: Int = 1
)
