package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.InventoryDisplayItem
import com.example.data.model.ItemLocationEntity
import com.example.data.model.ItemWithLocations
import com.example.data.model.PackageType
import com.example.data.repository.InventoryRepository
import com.example.ocr.BillScannerProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class InventoryFilter(val label: String) {
    ALL("All"),
    FLOOR_1("Floor 1"),
    FLOOR_2("Floor 2"),
    CARTONS("Cartons"),
    LOOSE("Loose")
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Processing : ScanUiState
    data class Success(
        val rawText: String,
        val matchedItems: List<InventoryDisplayItem>,
        val unmatchedLines: List<String>
    ) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InventoryRepository
    private val scannerProcessor = BillScannerProcessor()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = InventoryRepository(database.inventoryDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(InventoryFilter.ALL)
    val selectedFilter: StateFlow<InventoryFilter> = _selectedFilter.asStateFlow()

    private val _isRouteSorted = MutableStateFlow(true)
    val isRouteSorted: StateFlow<Boolean> = _isRouteSorted.asStateFlow()

    private val _checkedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val checkedItemIds: StateFlow<Set<String>> = _checkedItemIds.asStateFlow()

    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    private val _editingItem = MutableStateFlow<InventoryDisplayItem?>(null)
    val editingItem: StateFlow<InventoryDisplayItem?> = _editingItem.asStateFlow()

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()

    // Raw items with locations from database
    val rawInventory: StateFlow<List<ItemWithLocations>> = repository.allItemsWithLocations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Flat display items reactive to search, filter, and sorting
    val displayInventory: StateFlow<List<InventoryDisplayItem>> = combine(
        rawInventory,
        _searchQuery,
        _selectedFilter,
        _isRouteSorted,
        _checkedItemIds
    ) { itemsWithLocs, query, filter, routeSort, checkedSet ->
        val flatList = mutableListOf<InventoryDisplayItem>()

        for (itemWithLoc in itemsWithLocs) {
            val item = itemWithLoc.item
            val locations = itemWithLoc.locations

            if (locations.isEmpty()) {
                flatList.add(
                    InventoryDisplayItem(
                        itemId = item.itemId,
                        itemName = item.itemName,
                        aliases = item.aliases,
                        packageType = PackageType.LOOSE,
                        floor = "1",
                        row = "A",
                        barrack = "01",
                        shelf = "A",
                        locationCode = "1 A 01 A",
                        priority = 1,
                        isPicked = checkedSet.contains("${item.itemId}_LOOSE")
                    )
                )
            } else {
                for (loc in locations) {
                    flatList.add(
                        InventoryDisplayItem(
                            itemId = item.itemId,
                            itemName = item.itemName,
                            aliases = item.aliases,
                            packageType = loc.packageType,
                            floor = loc.floor,
                            row = loc.row,
                            barrack = loc.barrack,
                            shelf = loc.shelf,
                            locationCode = loc.locationCode,
                            priority = loc.priority,
                            isPicked = checkedSet.contains("${item.itemId}_${loc.packageType.name}")
                        )
                    )
                }
            }
        }

        // Apply Search
        var filtered = if (query.isBlank()) {
            flatList
        } else {
            val q = query.trim().lowercase()
            flatList.filter {
                it.itemName.lowercase().contains(q) ||
                    it.aliases.lowercase().contains(q) ||
                    it.locationCode.lowercase().contains(q) ||
                    it.packageType.name.lowercase().contains(q)
            }
        }

        // Apply Filter Chip
        filtered = when (filter) {
            InventoryFilter.ALL -> filtered
            InventoryFilter.FLOOR_1 -> filtered.filter { it.floor == "1" || it.floor == "01" }
            InventoryFilter.FLOOR_2 -> filtered.filter { it.floor == "2" || it.floor == "02" }
            InventoryFilter.CARTONS -> filtered.filter { it.packageType == PackageType.CARTON }
            InventoryFilter.LOOSE -> filtered.filter { it.packageType == PackageType.LOOSE }
        }

        // Apply Routing / Sorting
        if (routeSort) {
            scannerProcessor.sortItemsForWarehouseRoute(filtered)
        } else {
            filtered.sortedBy { it.itemName }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: InventoryFilter) {
        _selectedFilter.value = filter
    }

    fun toggleRouteSorting() {
        _isRouteSorted.value = !_isRouteSorted.value
    }

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun toggleItemChecked(item: InventoryDisplayItem) {
        val key = "${item.itemId}_${item.packageType.name}"
        val current = _checkedItemIds.value.toMutableSet()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }
        _checkedItemIds.value = current
    }

    fun openEditLocation(item: InventoryDisplayItem) {
        _editingItem.value = item
    }

    fun closeEditLocation() {
        _editingItem.value = null
    }

    fun scanBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            _scanUiState.value = ScanUiState.Processing
            try {
                val inventory = repository.getAllItemsWithLocationsList()
                val result = scannerProcessor.processImage(bitmap, inventory)
                _scanUiState.value = ScanUiState.Success(
                    rawText = result.rawRecognizedText,
                    matchedItems = result.matchedItems,
                    unmatchedLines = result.unmatchedLines
                )
            } catch (e: Exception) {
                _scanUiState.value = ScanUiState.Error(e.message ?: "OCR processing error")
            }
        }
    }

    fun scanManualText(rawText: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _scanUiState.value = ScanUiState.Processing
            try {
                val inventory = repository.getAllItemsWithLocationsList()
                val result = scannerProcessor.processRawText(rawText, inventory)
                _scanUiState.value = ScanUiState.Success(
                    rawText = result.rawRecognizedText,
                    matchedItems = result.matchedItems,
                    unmatchedLines = result.unmatchedLines
                )
            } catch (e: Exception) {
                _scanUiState.value = ScanUiState.Error(e.message ?: "Matching error")
            }
        }
    }

    fun dismissScanResult() {
        _scanUiState.value = ScanUiState.Idle
    }

    fun updateLocation(
        itemId: Long,
        packageType: PackageType,
        floor: String,
        row: String,
        barrack: String,
        shelf: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val f = floor.trim().ifBlank { "1" }
            val r = row.trim().uppercase().ifBlank { "A" }
            val b = barrack.trim().ifBlank { "01" }
            val s = shelf.trim().uppercase().ifBlank { "A" }
            val locCode = "$f $r $b $s"

            repository.updateLocation(
                itemId = itemId,
                packageType = packageType,
                floor = f,
                row = r,
                barrack = b,
                shelf = s,
                locationCode = locCode,
                priority = if (packageType == PackageType.LOOSE) 1 else 2
            )
            _editingItem.value = null
        }
    }

    fun addNewProduct(
        name: String,
        aliases: String,
        defaultPackageType: PackageType,
        floor: String,
        row: String,
        barrack: String,
        shelf: String,
        cartonFloor: String = "",
        cartonRow: String = "",
        cartonBarrack: String = "",
        cartonShelf: String = "",
        isHighPriority: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        if (name.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val f = floor.trim().ifBlank { "1" }
            val r = row.trim().uppercase().ifBlank { "A" }
            val b = barrack.trim().ifBlank { "01" }
            val s = shelf.trim().uppercase().ifBlank { "A" }
            val locCode = "$f $r $b $s"
            val priority = if (isHighPriority) 0 else 1

            val locations = mutableListOf<ItemLocationEntity>()
            locations.add(
                ItemLocationEntity(
                    itemId = 0L,
                    packageType = defaultPackageType,
                    floor = f,
                    row = r,
                    barrack = b,
                    shelf = s,
                    locationCode = locCode,
                    priority = priority
                )
            )

            // Add carton location if specified or generate default
            val secondaryType = if (defaultPackageType == PackageType.LOOSE) PackageType.CARTON else PackageType.LOOSE
            val cFloor = cartonFloor.trim().ifBlank { f }
            val cRow = cartonRow.trim().uppercase().ifBlank { r }
            val cBarrack = cartonBarrack.trim().ifBlank { b }
            val cShelf = cartonShelf.trim().uppercase().ifBlank { (s.firstOrNull()?.let { (it + 1).toString() } ?: "B") }
            val cLocCode = "$cFloor $cRow $cBarrack $cShelf"

            locations.add(
                ItemLocationEntity(
                    itemId = 0L,
                    packageType = secondaryType,
                    floor = cFloor,
                    row = cRow,
                    barrack = cBarrack,
                    shelf = cShelf,
                    locationCode = cLocCode,
                    priority = priority + 1
                )
            )

            repository.insertItemWithLocations(
                itemName = name.trim(),
                aliases = aliases.trim(),
                locations = locations
            )

            launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }
}
