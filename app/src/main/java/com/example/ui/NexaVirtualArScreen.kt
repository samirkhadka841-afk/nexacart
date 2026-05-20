package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.Product
import com.example.ui.theme.*
import com.example.viewmodel.NexaScreen
import com.example.viewmodel.NexaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexaVirtualArScreen(viewModel: NexaViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val products by viewModel.productsList.collectAsState()

    // Filter relevant AR compatible products: Furniture, Home Decor, Apparel, Gaming
    val arCompatibleProducts = remember(products) {
        products.filter {
            it.category.lowercase() in listOf("furniture", "home decor", "fashion", "gaming")
        }
    }

    // AR Scene parameters
    val scale by viewModel.arScale.collectAsState()
    val rotation by viewModel.arRotation.collectAsState()
    val heightOffset by viewModel.arHeight.collectAsState()
    val luminosity by viewModel.arLuminosity.collectAsState()
    val glowColorStr by viewModel.arGlowColor.collectAsState()
    val pointCloudActive by viewModel.arPointCloudActive.collectAsState()
    val selectedArProduct by viewModel.arSelectedProduct.collectAsState()

    // Set initial custom product if none chosen yet
    LaunchedEffect(arCompatibleProducts) {
        if (selectedArProduct == null && arCompatibleProducts.isNotEmpty()) {
            viewModel.arSelectedProduct.value = arCompatibleProducts.firstOrNull()
        }
    }

    // Free dragging offset state
    var dragOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Interactivity feedbacks
    var cameraFlashActive by remember { mutableStateOf(false) }
    var arInfoDialogShowing by remember { mutableStateOf(false) }
    var captureNotification by remember { mutableStateOf<String?>(null) }

    // Laser scanning scanline animation
    val infiniteTransition = rememberInfiniteTransition(label = "AR Laser scan")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    // Spatial cloud points coordinates simulation
    val spacePoints = remember {
        List(40) { i ->
            Offset(
                x = 100f + (i * 23) % 800f,
                y = 150f + (i * 31) % 1000f
            )
        }
    }

    val glowColor = when (glowColorStr) {
        "Cyan" -> CyberPrimary
        "Magenta" -> CyberSecondary
        "Emerald" -> Color(0xFF00FF87)
        "Gold" -> CyberGold
        else -> CyberPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("virtual_ar_screen")
    ) {
        // 1. SIMULATED CAMERA MATRIX BACKGROUND IN CLOUD VIEWPORT
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragOffset = Offset(
                            dragOffset.x + dragAmount.x,
                            dragOffset.y + dragAmount.y
                        )
                    }
                }
        ) {
            // Background environment room shadow with customizable ambient illumination
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF151821).copy(alpha = 0.85f * luminosity),
                        Color(0xFF050505).copy(alpha = 0.98f)
                    ),
                    center = center,
                    radius = size.maxDimension * 0.7f
                )
            )

            // Draw isometric mesh mapping grid representation for depth cue
            val cols = 8
            val rows = 12
            val spacingX = size.width / cols
            val spacingY = size.height / rows

            // Simulated mesh ground tracking lines
            for (i in 0..cols) {
                drawLine(
                    color = glowColor.copy(alpha = 0.08f * luminosity),
                    start = Offset(i * spacingX, 100f),
                    end = Offset(i * spacingX, size.height - 250f),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                )
            }

            for (j in 0..rows) {
                drawLine(
                    color = glowColor.copy(alpha = 0.08f * luminosity),
                    start = Offset(0f, j * spacingY),
                    end = Offset(size.width, j * spacingY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                )
            }

            // Draw active lidar point cloud coordinate dots tracking physical surface
            if (pointCloudActive) {
                spacePoints.forEachIndexed { idx, pt ->
                    val alphaVal = 0.2f + ((idx % 5) * 0.1f)
                    val radiusVal = 3f + (idx % 4)
                    drawCircle(
                        color = glowColor.copy(alpha = alphaVal * luminosity),
                        radius = radiusVal,
                        center = pt
                    )
                }
            }

            // Draw focal center scan lines coordinates crosshair
            drawCircle(
                color = glowColor.copy(alpha = 0.15f),
                radius = 120f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f)
                )
            )
        }

        // Animated laser grid掃描線
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset { IntOffset(0, laserOffsetY.roundToInt()) }
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, glowColor, Color.Transparent)
                    )
                )
        )

        // 2. HUD DEVICE LAYOVER (Viewfinder, metrics system overlay)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header actions line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(NexaScreen.HOME) },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit AR Space",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .border(1.dp, glowColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Green, CircleShape)
                        )
                        Text(
                            text = "ECHOSPATIAL ACTIVE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = { arInfoDialogShowing = true },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AR HUD Specifications Guide",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI real-time tracking metrics HUD Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedArProduct != null) "VIRTUAL MODEL: ${selectedArProduct!!.name.uppercase()}" else "SEARCHING ASSETS...",
                            color = glowColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "CATEGORY: ${selectedArProduct?.category?.uppercase() ?: "NONE"} • COORD: X=${dragOffset.x.toInt()} Y=${dragOffset.y.toInt()}",
                            color = CyberGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(glowColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SCALE: ${(scale * 100).toInt()}%",
                            color = glowColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. PERSISTENT INTERACTIVE PRODUCT VISUALIZER OVERLAY
            // Renders selected products on floating drag anchor
            selectedArProduct?.let { product ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    dragOffset.x.roundToInt(),
                                    dragOffset.y.roundToInt() + heightOffset.roundToInt()
                                )
                            }
                            .size((200 * scale).dp)
                            .rotate(rotation)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // High quality Category vector depiction for simulated placement
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = when (product.imageUrlName) {
                                    "ic_phone" -> Icons.Default.Settings
                                    "ic_shoes" -> Icons.Default.Star
                                    "ic_gaming" -> Icons.Default.PlayArrow
                                    "ic_jacket" -> Icons.Default.Person
                                    "ic_beverage" -> Icons.Default.Favorite
                                    "ic_furniture" -> Icons.Default.Home
                                    "ic_decor" -> Icons.Default.Star
                                    else -> Icons.Default.ShoppingCart
                                },
                                contentDescription = "Interactively Placed Product",
                                tint = glowColor.copy(alpha = luminosity),
                                modifier = Modifier.size((110 * scale).dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = (11 * scale).sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. BOTTOM INTERACTIVE PARAMETERS CONTROLLER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Title index
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPATIAL CALIBRATION CONTROLS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        // Clear resets
                        Text(
                            text = "RESET MATRIX",
                            color = CyberSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable {
                                    viewModel.resetArSettings()
                                    dragOffset = Offset(0f, 0f)
                                }
                        )
                    }

                    // Multi Sliders structure Tab Row
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Slider A: Scale Sizing
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SIZE SCALE",
                                color = CyberGray,
                                fontSize = 9.sp,
                                modifier = Modifier.width(68.dp),
                                fontFamily = FontFamily.Monospace
                            )
                            Slider(
                                value = scale,
                                onValueChange = { viewModel.arScale.value = it },
                                valueRange = 0.4f..2.2f,
                                colors = SliderDefaults.colors(
                                    thumbColor = glowColor,
                                    activeTrackColor = glowColor,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Slider B: Rotation Index
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ROTATION",
                                color = CyberGray,
                                fontSize = 9.sp,
                                modifier = Modifier.width(68.dp),
                                fontFamily = FontFamily.Monospace
                            )
                            Slider(
                                value = rotation,
                                onValueChange = { viewModel.arRotation.value = it },
                                valueRange = -180f..180f,
                                colors = SliderDefaults.colors(
                                    thumbColor = glowColor,
                                    activeTrackColor = glowColor,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Slider C: Luminosity Room Light
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AMBIENT",
                                color = CyberGray,
                                fontSize = 9.sp,
                                modifier = Modifier.width(68.dp),
                                fontFamily = FontFamily.Monospace
                            )
                            Slider(
                                value = luminosity,
                                onValueChange = { viewModel.arLuminosity.value = it },
                                valueRange = 0.2f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = glowColor,
                                    activeTrackColor = glowColor,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Toggle & Hologram Glow Selector Buttons List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // POINT CLOUD TRIGGER
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.arPointCloudActive.value = !pointCloudActive
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(1.dp, glowColor, RoundedCornerShape(4.dp))
                                    .background(
                                        if (pointCloudActive) glowColor else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (pointCloudActive) {
                                    Icon(Icons.Default.Check, "Checked", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SENSOR CLOUD", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }

                        // HOLOGRARM COLORS SELECTION
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colorsList = listOf("Cyan", "Magenta", "Emerald", "Gold")
                            colorsList.forEach { colName ->
                                val active = glowColorStr == colName
                                val itemColor = when (colName) {
                                    "Cyan" -> CyberPrimary
                                    "Magenta" -> CyberSecondary
                                    "Emerald" -> Color(0xFF00FF87)
                                    "Gold" -> CyberGold
                                    else -> CyberPrimary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(itemColor)
                                        .border(
                                            if (active) BorderStroke(1.5.dp, Color.White) else BorderStroke(0.dp, Color.Transparent),
                                            CircleShape
                                        )
                                        .clickable { viewModel.arGlowColor.value = colName }
                                )
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)

                    // 5. HORIZONTAL AR CAPABLE PRODUCTS PICKER ROW
                    Text(
                        text = "SELECT DESIGN MODEL FOR PLACEMENT:",
                        color = CyberGray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(arCompatibleProducts) { prod ->
                            val isSelected = selectedArProduct?.id == prod.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) glowColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        1.dp,
                                        if (isSelected) glowColor else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.arSelectedProduct.value = prod
                                        // Reset offsets on asset swap
                                        dragOffset = Offset(0f, 0f)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (prod.imageUrlName) {
                                            "ic_phone" -> Icons.Default.Settings
                                            "ic_shoes" -> Icons.Default.Star
                                            "ic_gaming" -> Icons.Default.PlayArrow
                                            "ic_jacket" -> Icons.Default.Person
                                            "ic_beverage" -> Icons.Default.Favorite
                                            "ic_furniture" -> Icons.Default.Home
                                            "ic_decor" -> Icons.Default.Star
                                            else -> Icons.Default.ShoppingCart
                                        },
                                        contentDescription = prod.name,
                                        tint = if (isSelected) glowColor else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = prod.name,
                                        color = if (isSelected) Color.White else CyberGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // 6. SNAPSHOT & INSTANT CHECKOUT BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Capture snapshot button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    cameraFlashActive = true
                                    delay(150)
                                    cameraFlashActive = false
                                    captureNotification = "NexaShot AR Capture Saved! Generated +100 Coins Promo!"
                                    delay(2000)
                                    captureNotification = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Add, "Snap Photo AR", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Text("NEXASHOT AI", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Instant Placement Checkout to purchase item
                        Button(
                            onClick = {
                                selectedArProduct?.let { prod ->
                                    viewModel.addToCart(prod)
                                    viewModel.navigateTo(NexaScreen.CART)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = glowColor),
                            shape = RoundedCornerShape(12.dp),
                            enabled = selectedArProduct != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, "Add to cart", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Text("CONFIRM FIT & BUY", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Camera shutter white-out flash visual overlay
        if (cameraFlashActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }

        // Toast confirmation popup
        AnimatedVisibility(
            visible = captureNotification != null,
            enter = fadeIn(tween(150)) + slideInVertically { -it },
            exit = fadeOut(tween(250)) + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xEB0A0A0A)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, glowColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, "Success Reward", tint = glowColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = captureNotification ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Informational Specifications Overlay Dialog
        if (arInfoDialogShowing) {
            AlertDialog(
                onDismissRequest = { arInfoDialogShowing = false },
                containerColor = Color(0xFF0F121F),
                title = {
                    Text(
                        text = "AR HUD Specifications Guide",
                        color = glowColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "NexaCart's built-in EchoSpatial placement engine uses advanced LiDAR tracking simulations. Live diagnostics:",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Place items from categorizations: Furniture, Home Decor, Apparel & Gaming.\n" +
                                    "• Free dragging: Touch-gesture drag anywhere inside viewport to position model.\n" +
                                    "• Rescale scaling triggers responsive metric calculations.\n" +
                                    "• Tap NEXASHOT AI to save snapshots & claim cashback tokens.",
                            color = CyberGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { arInfoDialogShowing = false }) {
                        Text("DISMISS COGNITIVE GUIDELINE", color = glowColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                modifier = Modifier.border(1.dp, glowColor, RoundedCornerShape(28.dp))
            )
        }
    }
}
