package com.example.ocr

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.example.data.model.InventoryDisplayItem
import com.example.data.model.ItemWithLocations
import com.example.data.model.PackageType
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BillScannerProcessor {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class ScanProcessResult(
        val rawRecognizedText: String,
        val matchedItems: List<InventoryDisplayItem>,
        val unmatchedLines: List<String>
    )

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }

    /**
     * Step 1: Process Bitmap from CameraX photo capture or manual image.
     */
    suspend fun processImage(
        bitmap: Bitmap,
        inventory: List<ItemWithLocations>
    ): ScanProcessResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val visionText = textRecognizer.process(inputImage).awaitTask()
        return extractAndRoute(visionText, inventory)
    }

    /**
     * Process ImageProxy from CameraX Analyzer.
     */
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    suspend fun processImageProxy(
        imageProxy: ImageProxy,
        inventory: List<ItemWithLocations>
    ): ScanProcessResult? {
        val mediaImage = imageProxy.image ?: return null
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val visionText = textRecognizer.process(inputImage).awaitTask()
        return extractAndRoute(visionText, inventory)
    }

    /**
     * Process raw text directly (for Manual Bill text entry / barcode paste).
     */
    fun processRawText(
        text: String,
        inventory: List<ItemWithLocations>
    ): ScanProcessResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val matchedList = mutableListOf<InventoryDisplayItem>()
        val unmatchedList = mutableListOf<String>()
        val processedItemIds = mutableSetOf<Pair<Long, PackageType>>()

        for (line in lines) {
            val packageType = LevenshteinMatcher.extractPackageType(line)
            val match = LevenshteinMatcher.findBestMatch(line, inventory, maxDistanceThreshold = 3)

            if (match != null) {
                val itemKey = Pair(match.item.item.itemId, packageType)
                if (!processedItemIds.contains(itemKey)) {
                    processedItemIds.add(itemKey)

                    // Find location for item matching packageType
                    val location = match.item.locations.find { it.packageType == packageType }
                        ?: match.item.locations.firstOrNull()

                    val floor = location?.floor ?: "1"
                    val row = location?.row ?: "A"
                    val barrack = location?.barrack ?: "01"
                    val shelf = location?.shelf ?: "A"
                    val locationCode = location?.locationCode ?: "$floor $row $barrack $shelf"
                    val priority = location?.priority ?: 1

                    matchedList.add(
                        InventoryDisplayItem(
                            itemId = match.item.item.itemId,
                            itemName = match.item.item.itemName,
                            aliases = match.item.item.aliases,
                            packageType = packageType,
                            floor = floor,
                            row = row,
                            barrack = barrack,
                            shelf = shelf,
                            locationCode = locationCode,
                            priority = priority,
                            stockQty = 100,
                            matchConfidence = match.confidence,
                            matchedLine = line
                        )
                    )
                }
            } else {
                if (line.length > 2) {
                    unmatchedList.add(line)
                }
            }
        }

        // Step 4: Route & Sort logically by Floor -> Row -> Barrack -> Priority
        val sortedList = sortItemsForWarehouseRoute(matchedList)

        return ScanProcessResult(
            rawRecognizedText = text,
            matchedItems = sortedList,
            unmatchedLines = unmatchedList
        )
    }

    private fun extractAndRoute(
        visionText: Text,
        inventory: List<ItemWithLocations>
    ): ScanProcessResult {
        val matchedList = mutableListOf<InventoryDisplayItem>()
        val unmatchedList = mutableListOf<String>()
        val processedItemIds = mutableSetOf<Pair<Long, PackageType>>()

        // Extract lines across all blocks
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val rawLine = line.text.trim()
                if (rawLine.isBlank()) continue

                val packageType = LevenshteinMatcher.extractPackageType(rawLine)
                val match = LevenshteinMatcher.findBestMatch(rawLine, inventory, maxDistanceThreshold = 3)

                if (match != null) {
                    val itemKey = Pair(match.item.item.itemId, packageType)
                    if (!processedItemIds.contains(itemKey)) {
                        processedItemIds.add(itemKey)

                        val location = match.item.locations.find { it.packageType == packageType }
                            ?: match.item.locations.firstOrNull()

                        val floor = location?.floor ?: "1"
                        val row = location?.row ?: "A"
                        val barrack = location?.barrack ?: "01"
                        val shelf = location?.shelf ?: "A"
                        val locationCode = location?.locationCode ?: "$floor $row $barrack $shelf"
                        val priority = location?.priority ?: 1

                        matchedList.add(
                            InventoryDisplayItem(
                                itemId = match.item.item.itemId,
                                itemName = match.item.item.itemName,
                                aliases = match.item.item.aliases,
                                packageType = packageType,
                                floor = floor,
                                row = row,
                                barrack = barrack,
                                shelf = shelf,
                                locationCode = locationCode,
                                priority = priority,
                                stockQty = 100,
                                matchConfidence = match.confidence,
                                matchedLine = rawLine
                            )
                        )
                    }
                } else {
                    if (rawLine.length > 2) {
                        unmatchedList.add(rawLine)
                    }
                }
            }
        }

        // Step 4: Route & Sort logically by Floor -> Row -> Barrack -> Priority
        val sortedList = sortItemsForWarehouseRoute(matchedList)

        return ScanProcessResult(
            rawRecognizedText = visionText.text,
            matchedItems = sortedList,
            unmatchedLines = unmatchedList
        )
    }

    /**
     * Step 4: Logical sorting for picking routing optimization
     * Floor (asc) -> Row (asc) -> Barrack (asc) -> Priority (asc)
     */
    fun sortItemsForWarehouseRoute(items: List<InventoryDisplayItem>): List<InventoryDisplayItem> {
        return items.sortedWith(
            compareBy<InventoryDisplayItem> { item ->
                item.floor.toIntOrNull() ?: 999
            }.thenBy { item ->
                item.row
            }.thenBy { item ->
                item.barrack.toIntOrNull() ?: 999
            }.thenBy { item ->
                item.priority
            }.thenBy { item ->
                item.itemName
            }
        )
    }
}
