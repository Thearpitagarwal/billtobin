package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LocationBadge
import com.example.ui.components.OfflineStatusHeader
import com.example.ui.components.PackageTypeBadge
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.MinimalBluePrimary
import com.example.ui.theme.MinimalSurface
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.WarehouseViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: WarehouseViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val scanUiState by viewModel.scanUiState.collectAsStateWithLifecycle()
    val isFlashOn by viewModel.isFlashOn.collectAsStateWithLifecycle()

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var showManualDialog by remember { mutableStateOf(false) }
    var manualInputText by remember {
        mutableStateOf("Parle-G 50g 2x\nDetergent 1kg CTN\nApple iPhone 15 128GB\nBleach Cartons BOX")
    }

    // Toggle Torch on hardware
    LaunchedEffect(isFlashOn, camera) {
        camera?.cameraControl?.enableTorch(isFlashOn)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
    ) {
        // Camera Preview Layer
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCapture = capture

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera Permission Required",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Allow camera access to scan receipts and bills offline.",
                    color = Slate400,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalBluePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Grant Permission")
                }
            }
        }

        // Minimalist Scanner Viewfinder Overlay
        ScannerViewfinderOverlay()

        // Top Status Header (Dark Theme)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            OfflineStatusHeader(
                isDarkTheme = true,
                title = "BillToBin"
            )

            // Guidance Subtitle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Align physical receipt or bill within frame",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Controls Bar (Flash, Capture, Manual Entry)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xD90F172A))
                .padding(horizontal = 28.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate800)
                        .clickable { viewModel.toggleFlash() }
                        .testTag("flash_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash Toggle",
                        tint = if (isFlashOn) EmeraldText else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Large Main Scanner Capture Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MinimalBluePrimary)
                        .clickable {
                            val capture = imageCapture
                            if (capture != null) {
                                capture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val bitmap = imageProxyToBitmap(image)
                                            image.close()
                                            if (bitmap != null) {
                                                viewModel.scanBitmap(bitmap)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                            // Fallback to sample parse if simulator has no physical lens
                                            viewModel.scanManualText(manualInputText)
                                        }
                                    }
                                )
                            } else {
                                // Fallback for emulator without hardware camera
                                viewModel.scanManualText(manualInputText)
                            }
                        }
                        .testTag("scan_capture_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    )
                }

                // Manual Entry Action Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate800)
                        .clickable { showManualDialog = true }
                        .testTag("manual_entry_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "Manual Entry",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "MANUAL",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Processing Loading Indicator
        if (scanUiState is ScanUiState.Processing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Slate800)
                        .padding(28.dp)
                ) {
                    CircularProgressIndicator(
                        color = MinimalBluePrimary,
                        modifier = Modifier.size(44.dp),
                        strokeWidth = 3.5.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ON-DEVICE OCR SCANNING",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Fuzzy matching items & bin routing",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Scanned Results Modal Bottom Sheet
        if (scanUiState is ScanUiState.Success) {
            val success = scanUiState as ScanUiState.Success
            ScanResultBottomSheet(
                result = success,
                onDismiss = { viewModel.dismissScanResult() },
                onViewInRoute = {
                    viewModel.dismissScanResult()
                    onNavigateToInventory()
                }
            )
        }

        // Manual Text Entry Dialog
        if (showManualDialog) {
            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                containerColor = Slate900,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Manual Receipt / Bill Input",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter raw bill lines (with packaging indicators like CTN, BOX, pcs, etc.):",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = manualInputText,
                            onValueChange = { manualInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .testTag("manual_text_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MinimalBluePrimary,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate800,
                                unfocusedContainerColor = Slate800
                            ),
                            placeholder = { Text("e.g. Parle-G 50g 2x\nDetergent 1kg CTN", color = Slate500) }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showManualDialog = false
                            viewModel.scanManualText(manualInputText)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalBluePrimary)
                    ) {
                        Text("Process & Route", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showManualDialog = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            )
        }
    }
}

@Composable
fun ScannerViewfinderOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 64.dp, bottom = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Frame Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.8f)
        ) {
            val w = size.width
            val h = size.height
            val cornerLen = 36.dp.toPx()
            val strokeW = 4.dp.toPx()
            val innerStrokeW = 2.5.dp.toPx()

            // Outer brackets (White)
            val outerPath = Path().apply {
                // Top-Left
                moveTo(0f, cornerLen)
                lineTo(0f, 0f)
                lineTo(cornerLen, 0f)

                // Top-Right
                moveTo(w - cornerLen, 0f)
                lineTo(w, 0f)
                lineTo(w, cornerLen)

                // Bottom-Right
                moveTo(w, h - cornerLen)
                lineTo(w, h)
                lineTo(w - cornerLen, h)

                // Bottom-Left
                moveTo(cornerLen, h)
                lineTo(0f, h)
                lineTo(0f, h - cornerLen)
            }

            drawPath(
                path = outerPath,
                color = Color.White.copy(alpha = 0.8f),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // Inner Target brackets (Minimal Blue)
            val innerInset = 24.dp.toPx()
            val innerCornerLen = 28.dp.toPx()
            val innerPath = Path().apply {
                // Top-Left
                moveTo(innerInset, innerInset + innerCornerLen)
                lineTo(innerInset, innerInset)
                lineTo(innerInset + innerCornerLen, innerInset)

                // Top-Right
                moveTo(w - innerInset - innerCornerLen, innerInset)
                lineTo(w - innerInset, innerInset)
                lineTo(w - innerInset, innerInset + innerCornerLen)

                // Bottom-Right
                moveTo(w - innerInset, h - innerInset - innerCornerLen)
                lineTo(w - innerInset, h - innerInset)
                lineTo(w - innerInset - innerCornerLen, h - innerInset)

                // Bottom-Left
                moveTo(innerInset + innerCornerLen, h - innerInset)
                lineTo(innerInset, h - innerInset)
                lineTo(innerInset, h - innerInset - innerCornerLen)
            }

            drawPath(
                path = innerPath,
                color = MinimalBluePrimary,
                style = Stroke(width = innerStrokeW, cap = StrokeCap.Round)
            )

            // Horizontal Scanning Laser line
            val currentLaserY = h * laserYRatio
            drawLine(
                color = EmeraldText,
                start = Offset(innerInset - 10f, currentLaserY),
                end = Offset(w - innerInset + 10f, currentLaserY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // HUD Warehouse Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "READY TO SCAN",
                color = EmeraldText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ON-DEVICE OCR ENGINE ACTIVE",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultBottomSheet(
    result: ScanUiState.Success,
    onDismiss: () -> Unit,
    onViewInRoute: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MinimalSurface,
        contentColor = Slate900,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = EmeraldText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scanned Receipt Bins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Slate500
                    )
                }
            }

            Text(
                text = "Extracted ${result.matchedItems.size} items matched to warehouse bins:",
                fontSize = 13.sp,
                color = Slate500,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // List of matched items & bin routing
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(result.matchedItems) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MinimalSurface)
                            .border(1.dp, Slate100, RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.itemName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PackageTypeBadge(packageType = item.packageType)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Floor ${item.floor} • Row ${item.row}",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        LocationBadge(
                            locationCode = item.locationCode,
                            isCarton = item.packageType == com.example.data.model.PackageType.CARTON
                        )
                    }
                }

                if (result.unmatchedLines.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Unmatched",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Unmatched Lines (${result.unmatchedLines.size}):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                            result.unmatchedLines.take(3).forEach { line ->
                                Text(
                                    text = "• $line",
                                    fontSize = 11.sp,
                                    color = Slate600,
                                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button to view in inventory route
            Button(
                onClick = onViewInRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("view_in_inventory_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MinimalBluePrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Route",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OPTIMIZE WAREHOUSE PICKING ROUTE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Helper to convert CameraX ImageProxy to Bitmap with correct orientation.
 */
private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    val rotation = image.imageInfo.rotationDegrees
    if (rotation != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    return bitmap
}

