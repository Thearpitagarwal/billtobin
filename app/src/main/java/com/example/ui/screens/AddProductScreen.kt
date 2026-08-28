package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PackageType
import com.example.ui.theme.MinimalBg
import com.example.ui.theme.MinimalBluePrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.WarehouseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: WarehouseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var productName by remember { mutableStateOf("") }
    var productAliases by remember { mutableStateOf("") }
    var defaultPackageType by remember { mutableStateOf(PackageType.LOOSE) }

    var floor by remember { mutableStateOf("1") }
    var row by remember { mutableStateOf("A") }
    var barrack by remember { mutableStateOf("01") }
    var shelf by remember { mutableStateOf("A") }

    var cartonFloor by remember { mutableStateOf("1") }
    var cartonRow by remember { mutableStateOf("A") }
    var cartonBarrack by remember { mutableStateOf("01") }
    var cartonShelf by remember { mutableStateOf("B") }

    var isHighPriority by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MinimalBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Product",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_product_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate900
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (productName.isBlank()) {
                                isNameError = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter a product name")
                                }
                            } else {
                                viewModel.addNewProduct(
                                    name = productName,
                                    aliases = productAliases,
                                    defaultPackageType = defaultPackageType,
                                    floor = floor,
                                    row = row,
                                    barrack = barrack,
                                    shelf = shelf,
                                    cartonFloor = cartonFloor,
                                    cartonRow = cartonRow,
                                    cartonBarrack = cartonBarrack,
                                    cartonShelf = cartonShelf,
                                    isHighPriority = isHighPriority,
                                    onSuccess = {
                                        onNavigateBack()
                                    }
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_product_top_button")
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalBluePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MinimalSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MinimalBg)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Section 1: Product Details
            Text(
                text = "Product Details",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Product Name
            OutlinedTextField(
                value = productName,
                onValueChange = {
                    productName = it
                    if (it.isNotBlank()) isNameError = false
                },
                label = { Text("Product Name *") },
                placeholder = { Text("e.g. Parle-G 50g, iPhone 15 128GB", color = Slate400) },
                isError = isNameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_product_name"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MinimalBluePrimary,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = MinimalSurface,
                    unfocusedContainerColor = MinimalSurface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Product Aliases
            OutlinedTextField(
                value = productAliases,
                onValueChange = { productAliases = it },
                label = { Text("Product Aliases (Comma separated)") },
                placeholder = { Text("e.g. SKU, Internal Code, Brand Alias", color = Slate400) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_product_aliases"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MinimalBluePrimary,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = MinimalSurface,
                    unfocusedContainerColor = MinimalSurface
                ),
                supportingText = {
                    Text(
                        text = "Used for on-device OCR fuzzy matching against receipt lines",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Packaging Configuration
            Text(
                text = "Packaging Configuration",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Loose Settings Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (defaultPackageType == PackageType.LOOSE) MinimalSurface else MinimalSurface.copy(alpha = 0.6f))
                        .border(
                            1.5.dp,
                            if (defaultPackageType == PackageType.LOOSE) MinimalBluePrimary else Slate200,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { defaultPackageType = PackageType.LOOSE }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = if (defaultPackageType == PackageType.LOOSE) MinimalBluePrimary else Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Loose Settings",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Unit: PCS (Pieces)",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                // Carton Settings Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (defaultPackageType == PackageType.CARTON) MinimalSurface else MinimalSurface.copy(alpha = 0.6f))
                        .border(
                            1.5.dp,
                            if (defaultPackageType == PackageType.CARTON) MinimalBluePrimary else Slate200,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { defaultPackageType = PackageType.CARTON }
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = if (defaultPackageType == PackageType.CARTON) MinimalBluePrimary else Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Carton Settings",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bulk / Master Box",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Location Mapping
            Text(
                text = "Primary Location Mapping (Loose / Standard)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_product_floor"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalBluePrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = MinimalSurface,
                        unfocusedContainerColor = MinimalSurface
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = row,
                    onValueChange = { row = it },
                    label = { Text("Row") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_product_row"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalBluePrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = MinimalSurface,
                        unfocusedContainerColor = MinimalSurface
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = barrack,
                    onValueChange = { barrack = it },
                    label = { Text("Barrack") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_product_barrack"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalBluePrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = MinimalSurface,
                        unfocusedContainerColor = MinimalSurface
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = shelf,
                    onValueChange = { shelf = it },
                    label = { Text("Shelf") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_product_shelf"),
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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Carton Bulk Location Mapping",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cartonFloor,
                    onValueChange = { cartonFloor = it },
                    label = { Text("Floor") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalBluePrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = MinimalSurface,
                        unfocusedContainerColor = MinimalSurface
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cartonRow,
                    onValueChange = { cartonRow = it },
                    label = { Text("Row") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalBluePrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = MinimalSurface,
                        unfocusedContainerColor = MinimalSurface
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cartonBarrack,
                    onValueChange = { cartonBarrack = it },
                    label = { Text("Barrack") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MinimalBluePrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = MinimalSurface,
                        unfocusedContainerColor = MinimalSurface
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cartonShelf,
                    onValueChange = { cartonShelf = it },
                    label = { Text("Shelf") },
                    modifier = Modifier.weight(1f),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: High Priority Picking Location Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MinimalSurface)
                    .border(1.dp, Slate100, RoundedCornerShape(18.dp))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Set as High Priority Picking Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Text(
                        text = "Items placed here will be prioritized first in warehouse pick lists.",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Switch(
                    checked = isHighPriority,
                    onCheckedChange = { isHighPriority = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MinimalBluePrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Slate300
                    ),
                    modifier = Modifier.testTag("priority_toggle")
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Action Button
            Button(
                onClick = {
                    if (productName.isBlank()) {
                        isNameError = true
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a product name")
                        }
                    } else {
                        viewModel.addNewProduct(
                            name = productName,
                            aliases = productAliases,
                            defaultPackageType = defaultPackageType,
                            floor = floor,
                            row = row,
                            barrack = barrack,
                            shelf = shelf,
                            cartonFloor = cartonFloor,
                            cartonRow = cartonRow,
                            cartonBarrack = cartonBarrack,
                            cartonShelf = cartonShelf,
                            isHighPriority = isHighPriority,
                            onSuccess = {
                                onNavigateBack()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_product_save_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MinimalBluePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Save Product to Inventory",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

