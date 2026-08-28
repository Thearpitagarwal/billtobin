package com.example.ui.navigation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AddProductScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.OfflineSyncScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.MinimalBluePrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.viewmodel.WarehouseViewModel

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    SCANNER("scanner", "SCAN", Icons.Default.QrCodeScanner),
    INVENTORY("inventory", "INVENTORY", Icons.Default.ListAlt),
    SYNC("sync", "SYNC", Icons.Default.Sync),
    ADD_PRODUCT("add_product", "Add Product", Icons.Default.ListAlt)
}

@Composable
fun AppNavigation(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.SCANNER,
        Screen.INVENTORY,
        Screen.SYNC
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute != Screen.ADD_PRODUCT.route) {
                WarehouseBottomNavigation(
                    items = bottomNavItems,
                    currentRoute = currentRoute ?: Screen.SCANNER.route,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    isDarkTheme = currentRoute == Screen.SCANNER.route
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.SCANNER.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.SCANNER.route) {
                ScannerScreen(
                    viewModel = viewModel,
                    onNavigateToInventory = {
                        navController.navigate(Screen.INVENTORY.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToAddProduct = {
                        navController.navigate(Screen.ADD_PRODUCT.route)
                    }
                )
            }

            composable(Screen.INVENTORY.route) {
                InventoryScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = {
                        navController.navigate(Screen.ADD_PRODUCT.route)
                    }
                )
            }

            composable(Screen.SYNC.route) {
                OfflineSyncScreen(viewModel = viewModel)
            }

            composable(Screen.ADD_PRODUCT.route) {
                AddProductScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun WarehouseBottomNavigation(
    items: List<Screen>,
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else MinimalSurface
    val borderCol = if (isDarkTheme) Color(0xFF1E293B) else Slate100

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Screen 1: Inventory / Home
            val isInventorySelected = currentRoute == Screen.INVENTORY.route
            val inventoryColor = when {
                isInventorySelected -> MinimalBluePrimary
                isDarkTheme -> Color.White.copy(alpha = 0.5f)
                else -> Slate400
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.INVENTORY) }
                    .padding(vertical = 8.dp)
                    .testTag("nav_tab_inventory"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "Inventory",
                    tint = inventoryColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "HOME",
                    color = inventoryColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }

            // Center Floating Camera / Scan Button
            val isScannerSelected = currentRoute == Screen.SCANNER.route
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MinimalBluePrimary)
                        .clickable { onNavigate(Screen.SCANNER) }
                        .testTag("nav_tab_scanner"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Screen 3: Routes / Sync
            val isSyncSelected = currentRoute == Screen.SYNC.route
            val syncColor = when {
                isSyncSelected -> MinimalBluePrimary
                isDarkTheme -> Color.White.copy(alpha = 0.5f)
                else -> Slate400
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.SYNC) }
                    .padding(vertical = 8.dp)
                    .testTag("nav_tab_sync"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Routes & Sync",
                    tint = syncColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ROUTES",
                    color = syncColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}
