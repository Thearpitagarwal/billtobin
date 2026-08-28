package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InventoryDao
import com.example.data.model.ItemEntity
import com.example.data.model.ItemLocationEntity
import com.example.data.model.PackageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
    entities = [ItemEntity::class, ItemLocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "billtobin_encrypted.db"
        // Passphrase for SQLCipher on-device local database encryption
        private val DB_PASSPHRASE = "B1llT0B1n_W4r3h0us3_Encrypted_Secur3_2026".toByteArray()

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphraseFactory = try {
                    SQLiteDatabase.loadLibs(context)
                    SupportFactory(DB_PASSPHRASE)
                } catch (e: Throwable) {
                    null
                }

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).addCallback(DatabasePrepopulationCallback(context.applicationContext, scope))

                if (passphraseFactory != null) {
                    builder.openHelperFactory(passphraseFactory)
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabasePrepopulationCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        prepopulateDatabase(context, database.inventoryDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        if (database.inventoryDao().getItemCount() == 0) {
                            prepopulateDatabase(context, database.inventoryDao())
                        }
                    }
                }
            }
        }

        suspend fun prepopulateDatabase(context: Context, dao: InventoryDao) {
            try {
                val inputStream = context.assets.open("inventory.json")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }
                val jsonArray = JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    val itemObj = jsonArray.getJSONObject(i)
                    val itemName = itemObj.getString("itemName")
                    val aliases = itemObj.optString("aliases", "")

                    val itemId = dao.insertItem(
                        ItemEntity(
                            itemName = itemName,
                            aliases = aliases
                        )
                    )

                    val locationsArray = itemObj.optJSONArray("locations")
                    if (locationsArray != null) {
                        for (j in 0 until locationsArray.length()) {
                            val locObj = locationsArray.getJSONObject(j)
                            val packageTypeStr = locObj.getString("packageType")
                            val packageType = if (packageTypeStr.equals("CARTON", ignoreCase = true)) {
                                PackageType.CARTON
                            } else {
                                PackageType.LOOSE
                            }
                            val floor = locObj.getString("floor")
                            val row = locObj.getString("row")
                            val barrack = locObj.getString("barrack")
                            val shelf = locObj.getString("shelf")
                            val locationCode = locObj.optString("locationCode", "$floor $row $barrack $shelf")
                            val priority = locObj.optInt("priority", 1)

                            dao.insertLocation(
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
