package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ItemEntity
import com.example.data.model.ItemLocationEntity
import com.example.data.model.ItemWithLocations
import com.example.data.model.PackageType
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Transaction
    @Query("SELECT * FROM items ORDER BY itemName ASC")
    fun getAllItemsWithLocationsFlow(): Flow<List<ItemWithLocations>>

    @Transaction
    @Query("SELECT * FROM items")
    suspend fun getAllItemsWithLocations(): List<ItemWithLocations>

    @Query("SELECT * FROM items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItemById(itemId: Long): ItemEntity?

    @Query("SELECT * FROM item_locations WHERE itemId = :itemId")
    suspend fun getLocationsForItem(itemId: Long): List<ItemLocationEntity>

    @Query("SELECT * FROM item_locations WHERE itemId = :itemId AND packageType = :packageType LIMIT 1")
    suspend fun getLocationForItemAndType(itemId: Long, packageType: PackageType): ItemLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: ItemLocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<ItemLocationEntity>)

    @Update
    suspend fun updateLocation(location: ItemLocationEntity)

    @Query("UPDATE item_locations SET floor = :floor, row = :row, barrack = :barrack, shelf = :shelf, locationCode = :locationCode, priority = :priority WHERE itemId = :itemId AND packageType = :packageType")
    suspend fun updateLocationDetails(
        itemId: Long,
        packageType: PackageType,
        floor: String,
        row: String,
        barrack: String,
        shelf: String,
        locationCode: String,
        priority: Int
    )

    @Query("DELETE FROM items WHERE itemId = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int
}
