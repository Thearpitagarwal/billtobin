package com.example.data.repository

import com.example.data.dao.InventoryDao
import com.example.data.model.ItemEntity
import com.example.data.model.ItemLocationEntity
import com.example.data.model.ItemWithLocations
import com.example.data.model.PackageType
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val inventoryDao: InventoryDao) {

    val allItemsWithLocations: Flow<List<ItemWithLocations>> =
        inventoryDao.getAllItemsWithLocationsFlow()

    suspend fun getAllItemsWithLocationsList(): List<ItemWithLocations> {
        return inventoryDao.getAllItemsWithLocations()
    }

    suspend fun getItemById(itemId: Long): ItemEntity? {
        return inventoryDao.getItemById(itemId)
    }

    suspend fun getLocationsForItem(itemId: Long): List<ItemLocationEntity> {
        return inventoryDao.getLocationsForItem(itemId)
    }

    suspend fun insertItemWithLocations(
        itemName: String,
        aliases: String,
        locations: List<ItemLocationEntity>
    ): Long {
        val itemId = inventoryDao.insertItem(
            ItemEntity(
                itemName = itemName,
                aliases = aliases
            )
        )
        val locationsWithId = locations.map { it.copy(itemId = itemId) }
        inventoryDao.insertLocations(locationsWithId)
        return itemId
    }

    suspend fun updateLocation(
        itemId: Long,
        packageType: PackageType,
        floor: String,
        row: String,
        barrack: String,
        shelf: String,
        locationCode: String,
        priority: Int
    ) {
        val existing = inventoryDao.getLocationForItemAndType(itemId, packageType)
        if (existing != null) {
            inventoryDao.updateLocationDetails(
                itemId = itemId,
                packageType = packageType,
                floor = floor,
                row = row,
                barrack = barrack,
                shelf = shelf,
                locationCode = locationCode,
                priority = priority
            )
        } else {
            inventoryDao.insertLocation(
                ItemLocationEntity(
                    itemId = itemId,
                    packageType = packageType,
                    floor = floor,
                    row = row,
                    barrack = barrack,
                    shelf = shelf,
                    locationCode = locationCode,
                    priority = priority
                )
            )
        }
    }

    suspend fun deleteItem(itemId: Long) {
        inventoryDao.deleteItem(itemId)
    }
}
