package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InventoryDisplayItem
import com.example.data.model.PackageType
import com.example.ui.components.LocationBadge
import com.example.ui.components.OfflineStatusHeader
import com.example.ui.components.PackageTypeBadge
import com.example.ui.theme.MinimalBg
import com.example.ui.theme.MinimalBluePrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.InventoryFilter
import com.example.ui.viewmodel.WarehouseViewModel

@Composable
fun InventoryScreen(
    viewModel: WarehouseViewModel,
    onNavigateToAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isRouteSorted by viewModel.isRouteSorted.collectAsStateWithLifecycle()
    val items by viewModel.displayInventory.collectAsStateWithLifecycle()
    val editingItem by viewModel.editingItem.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MinimalBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MinimalBg)
            ) {
                OfflineStatusHeader(
                    isDarkTheme = false,
                    title = "BillToBin"
                )

                // Sticky Search Field - Clean Minimalism Style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = "Search inventory...",
                                fontSize = 14.sp,
                                color = Slate400
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(1.dp, RoundedCornerShape(16.dp))
                            .testTag("search_inventory_input"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalBluePrimary,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface,
                            cursorColor = MinimalBluePrimary
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Chips Row - Clean Minimalism Pill Style
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(InventoryFilter.entries.toTypedArray()) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) MinimalBluePrimary else MinimalSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) MinimalBluePrimary else Slate200,
                                    RoundedCornerShape(50)
                                )
                                .clickable { viewModel.onFilterSelected(filter) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("filter_chip_${filter.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) Color.White else Slate600,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Route Sorting Banner / Walking Path Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = "Route",
                            tint = if (isRouteSorted) MinimalBluePrimary else Slate500,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRouteSorted) "Optimal Walking Path (Floor → Row → Barrack)" else "Alphabetical Order",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRouteSorted) MinimalBluePrimary else Slate500
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewModel.toggleRouteSorting() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Toggle Sort",
                            tint = Slate500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = MinimalBluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(8.dp, CircleShape)
                    .testTag("add_product_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Products Found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting your search or add a new product.",
                        fontSize = 13.sp,
                        color = Slate600
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = items,
                    key = { "${it.itemId}_${it.packageType.name}" }
                ) { item ->
                    ProductInventoryCard(
                        item = item,
                        onCheckedChange = { viewModel.toggleItemChecked(item) },
                        onCardClick = { viewModel.openEditLocation(item) }
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Showing ${items.size} of 1,248 offline records",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Editing Location
    editingItem?.let { itemToEdit ->
        EditLocationBottomSheet(
            item = itemToEdit,
            onDismiss = { viewModel.closeEditLocation() },
            onUpdateLocation = { itemId, packageType, floor, row, barrack, shelf ->
                viewModel.updateLocation(
                    itemId = itemId,
                    packageType = packageType,
                    floor = floor,
                    row = row,
                    barrack = barrack,
                    shelf = shelf
                )
            }
        )
    }
}

@Composable
fun ProductInventoryCard(
    item: InventoryDisplayItem,
    onCheckedChange: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardAlpha = if (item.isPicked) 0.6f else 1f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .shadow(2.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onCardClick() }
            .testTag("inventory_item_${item.itemId}"),
        color = MinimalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Custom Styled Checkbox + Product Name & Metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Clean Minimalist Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.isPicked) MinimalBluePrimary else Color.Transparent)
                        .border(
                            2.dp,
                            if (item.isPicked) MinimalBluePrimary else Slate300,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onCheckedChange(!item.isPicked) }
                        .testTag("checkbox_${item.itemId}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isPicked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.itemName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPicked) Slate500 else Slate800,
                        textDecoration = if (item.isPicked) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PackageTypeBadge(packageType = item.packageType)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stock: ${item.stockQty}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Clean Minimalism Sleek Location Pill
            LocationBadge(
                locationCode = item.locationCode,
                isCarton = item.packageType == PackageType.CARTON,
                isPicked = item.isPicked
            )
        }
    }
}

