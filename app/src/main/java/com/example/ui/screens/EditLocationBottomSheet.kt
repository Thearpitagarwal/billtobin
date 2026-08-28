package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryDisplayItem
import com.example.data.model.PackageType
import com.example.ui.theme.MinimalBluePrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLocationBottomSheet(
    item: InventoryDisplayItem,
    onDismiss: () -> Unit,
    onUpdateLocation: (itemId: Long, packageType: PackageType, floor: String, row: String, barrack: String, shelf: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedPackageType by remember(item) { mutableStateOf(item.packageType) }
    var floor by remember(item) { mutableStateOf(item.floor) }
    var row by remember(item) { mutableStateOf(item.row) }
    var barrack by remember(item) { mutableStateOf(item.barrack) }
    var shelf by remember(item) { mutableStateOf(item.shelf) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MinimalSurface,
        contentColor = Slate900,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Slate300)
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header with Item Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = item.itemName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "SKU-99${item.itemId * 110 + 10} • Alias: ${item.aliases.ifBlank { "None" }}",
                        fontSize = 13.sp,
                        color = Slate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_edit_bottom_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Segmented Control: [ Loose Items | Carton Packs ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate100)
                    .padding(4.dp)
            ) {
                // Loose Items Segment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedPackageType == PackageType.LOOSE) MinimalSurface else Color.Transparent)
                        .clickable { selectedPackageType = PackageType.LOOSE }
                        .testTag("segment_loose_items"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loose Items",
                        color = if (selectedPackageType == PackageType.LOOSE) MinimalBluePrimary else Slate600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Carton Packs Segment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedPackageType == PackageType.CARTON) MinimalSurface else Color.Transparent)
                        .clickable { selectedPackageType = PackageType.CARTON }
                        .testTag("segment_carton_packs"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Carton Packs",
                        color = if (selectedPackageType == PackageType.CARTON) MinimalBluePrimary else Slate600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2x2 Grid of Location Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Floor Input
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Floor",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = floor,
                        onValueChange = { floor = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_floor"),
                        placeholder = { Text("e.g. 02", fontSize = 14.sp, color = Slate400) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalBluePrimary,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface
                        ),
                        singleLine = true
                    )
                }

                // Row Input
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Row",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = row,
                        onValueChange = { row = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_row"),
                        placeholder = { Text("e.g. B", fontSize = 14.sp, color = Slate400) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalBluePrimary,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Barrack Input
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Barrack",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = barrack,
                        onValueChange = { barrack = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_barrack"),
                        placeholder = { Text("e.g. 12", fontSize = 14.sp, color = Slate400) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalBluePrimary,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface
                        ),
                        singleLine = true
                    )
                }

                // Shelf Input
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shelf",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = shelf,
                        onValueChange = { shelf = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_shelf"),
                        placeholder = { Text("e.g. 04", fontSize = 14.sp, color = Slate400) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MinimalBluePrimary,
                            unfocusedBorderColor = Slate200,
                            focusedContainerColor = MinimalSurface,
                            unfocusedContainerColor = MinimalSurface
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Update Location Action Button
            Button(
                onClick = {
                    onUpdateLocation(
                        item.itemId,
                        selectedPackageType,
                        floor,
                        row,
                        barrack,
                        shelf
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("update_location_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MinimalBluePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Update Location",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

