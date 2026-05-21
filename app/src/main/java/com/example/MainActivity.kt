package com.example

import android.os.Bundle
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.database.CartItem
import com.example.database.OrderTrack
import com.example.database.Product
import com.example.database.WalletTransaction
import com.example.database.UserProfile
import com.example.ui.NexaVirtualArScreen
import com.example.ui.theme.*
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.NexaScreen
import com.example.viewmodel.NexaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NexaCartSuperAppShell()
            }
        }
    }
}

val currencyFormat = DecimalFormat("'Rs.' #,##0")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NexaCartSuperAppShell(viewModel: NexaViewModel = viewModel()) {
    val currentScreenState = viewModel.currentScreen.collectAsState()
    val currentScreen = currentScreenState.value

    val statusTextState = viewModel.statusText.collectAsState()
    val statusText = statusTextState.value

    val userProfileState = viewModel.userProfile.collectAsState()
    val userProfile = userProfileState.value

    val cartState = viewModel.cartItems.collectAsState()
    val cart = cartState.value

    var flashStatus by remember { mutableStateOf(false) }
    LaunchedEffect(statusText) {
        if (statusText.isNotEmpty() && statusText != "NexaEcosystem Online") {
            flashStatus = true
            delay(3000)
            flashStatus = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg)
    ) {
        // Starry matrix vector wire grid background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val gridSpacing = 40.dp.toPx()
                    val lineAlpha = 0.04f
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = CyberPrimary,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f,
                            alpha = lineAlpha
                        )
                        x += gridSpacing
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = CyberSecondary,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            alpha = lineAlpha
                        )
                        y += gridSpacing
                    }
                }
        )

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "ScreenSwitch"
        ) { screen ->
            when (screen) {
                NexaScreen.SPLASH -> NexaSplashScreen(viewModel)
                NexaScreen.LOGIN -> NexaLoginScreen(viewModel)
                else -> {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = { NexaHeaderBar(viewModel, userProfile, cart.size) },
                        bottomBar = { NexaBottomNavigationBar(viewModel, viewModel.currentScreen) },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            when (screen) {
                                NexaScreen.HOME -> NexaHomeScreen(viewModel)
                                NexaScreen.CATEGORIES -> NexaCategoriesScreen(viewModel)
                                NexaScreen.PRODUCT_DETAIL -> NexaProductDetailScreen(viewModel)
                                NexaScreen.CART -> NexaCartScreen(viewModel, cart)
                                NexaScreen.CHECKOUT -> NexaCheckoutScreen(viewModel, cart)
                                NexaScreen.WALLET -> NexaWalletScreen(viewModel)
                                NexaScreen.ORDERS -> NexaOrdersScreen(viewModel)
                                NexaScreen.SETTINGS -> NexaSettingsScreen(viewModel, userProfile)
                                NexaScreen.LIVE_SHOPPING -> NexaLiveShoppingStreamScreen(viewModel)
                                NexaScreen.SELLER_DASHBOARD -> NexaSellerDashboardScreen(viewModel)
                                NexaScreen.ADMIN_PANEL -> NexaAdminPanelScreen(viewModel)
                                NexaScreen.GAMIFICATION -> NexaGamificationScreen(viewModel, userProfile)
                                NexaScreen.AI_ASSISTANT -> NexaAIAssistantScreen(viewModel)
                                NexaScreen.DELIVERY_MAP -> NexaDeliveryTrackingScreen(viewModel)
                                NexaScreen.VIRTUAL_AR -> NexaVirtualArScreen(viewModel)
                                else -> NexaHomeScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = flashStatus,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xCC10131E)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cyber_status_toast")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Alert notification logo",
                        tint = CyberHighlight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


// ==========================================
// SCREEN 1: SPLASH SCREEN (Animated boot loader)
// ==========================================
@Composable
fun NexaSplashScreen(viewModel: NexaViewModel) {
    val scale = remember { Animatable(0.6f) }
    val rotate = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        rotate.animateTo(
            targetValue = 360f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
        delay(1200)
        viewModel.navigateTo(NexaScreen.LOGIN)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(CyberDarkBg, Color(0xFF0C0F22))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "NexaCart Premium Cyber Logo",
                tint = CyberPrimary,
                modifier = Modifier
                    .size(120.dp)
                    .rotate(rotate.value)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, CyberPrimary), CircleShape)
                    .padding(24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NEXACART",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberPrimary,
                fontFamily = FontFamily.Monospace,
                style = TextStyle(
                    shadow = Shadow(
                        color = CyberSecondary,
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Text(
                text = "AI-POWERED COGNITIVE SUPER APP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "CEO, FOUNDER, COPYRIGHT HOLDER:\nSAMIR KHADKA",
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ECHOBYTE TECHNOLOGIES INC.\n© 2026 NexaUniverse Network. All rights reserved.",
                textAlign = TextAlign.Center,
                color = CyberSecondary.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal
            )
        }
    }
}


// ==========================================
// SCREEN 2: MULTI-AUTHENTICATOR SIGNIN GATEWAY
// ==========================================
@Composable
fun NexaLoginScreen(viewModel: NexaViewModel) {
    var userIdInput by remember { mutableStateOf("samir_khadka") }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var scanHoverState by remember { mutableStateOf(false) }
    var otpStage by remember { mutableStateOf(false) }
    var otpValue by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Brand logo",
                tint = CyberSecondary,
                modifier = Modifier
                    .size(80.dp)
                    .border(BorderStroke(1.dp, CyberSecondary), CircleShape)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NexaCore Auth Gateway",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "EchoByte Protocol Level 4 Encryption",
                fontSize = 11.sp,
                color = CyberGray,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!otpStage) {
                OutlinedTextField(
                    value = userIdInput,
                    onValueChange = { userIdInput = it },
                    label = { Text("Identity Alias") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPrimary,
                        unfocusedBorderColor = CyberGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Security Access Code") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberSecondary,
                        unfocusedBorderColor = CyberGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scanHoverState = true
                            coroutineScope.launch {
                                delay(1200)
                                otpStage = true
                                scanHoverState = false
                            }
                        }
                        .testTag("biometric_scanner"),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, if (scanHoverState) CyberHighlight else CyberPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Biometric code secure",
                            tint = if (scanHoverState) CyberHighlight else CyberPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (scanHoverState) "VERIFYING BIOMETRICS SHELL..." else "TAP TO SCAN SECURE BIOMETRIC ID",
                            fontSize = 10.sp,
                            color = if (scanHoverState) CyberHighlight else CyberGray,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { otpStage = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("manual_login_button")
                ) {
                    Text("Manual Password Override Access", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            } else {
                val displayName = if (userIdInput == "samir_khadka") "Founder Samir Khadka" else userIdInput
                Text(
                    text = "A dual-authentication OTP was requested for: $displayName",
                    fontSize = 13.sp,
                    color = CyberHighlight,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = otpValue,
                    onValueChange = { otpValue = it },
                    label = { Text("6-Digit OTP Protocol") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberHighlight,
                        unfocusedBorderColor = CyberGray,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.navigateTo(NexaScreen.HOME)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("otp_verify_btn")
                ) {
                    Text("AUTHORIZE CORE CONNECTION", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { otpStage = false }) {
                    Text("Decrypt Gateway Back", color = CyberSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}


// ==========================================
// CENTRAL SYSTEM HEADER & STATS BAR
// ==========================================
@Composable
fun NexaHeaderBar(viewModel: NexaViewModel, userProfile: UserProfile, cartCount: Int) {
    Surface(
        color = CyberDarkBg,
        border = BorderStroke(0.0.dp, Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Mini logo launchpad",
                tint = CyberPrimary,
                modifier = Modifier
                    .size(34.dp)
                    .border(1.dp, CyberPrimary, CircleShape)
                    .padding(4.dp)
                    .clickable { viewModel.navigateTo(NexaScreen.HOME) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ECHOBYTE PRESENTS",
                    color = CyberPrimary.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nexa",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                    Text(
                        text = "Cart",
                        color = CyberPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    )
                }
            }

            // Wallet Shortcut Credit Label
            Row(
                modifier = Modifier
                    .background(Color(0x2200FF87), RoundedCornerShape(12.dp))
                    .border(BorderStroke(0.5.dp, CyberHighlight.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(NexaScreen.WALLET) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Wallet logo icon placeholder",
                    tint = CyberHighlight,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = currencyFormat.format(userProfile.walletBalance),
                    color = CyberHighlight,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Game rewards token dashboard
            Row(
                modifier = Modifier
                    .background(Color(0x22FFCC00), RoundedCornerShape(12.dp))
                    .border(BorderStroke(0.5.dp, CyberGold.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(NexaScreen.GAMIFICATION) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Token coin logo icon placeholder",
                    tint = CyberGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${userProfile.coins} NC",
                    color = CyberGold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Cart icon Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { viewModel.navigateTo(NexaScreen.CART) }
                    .testTag("topbar_cart_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Shopping Bag icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                if (cartCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(CyberSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cartCount.toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}


// ==========================================
// DYNAMIC NAVIGATION LOWER PILL DOCK
// ==========================================
@Composable
fun NexaBottomNavigationBar(viewModel: NexaViewModel, currentScreen: StateFlow<NexaScreen>) {
    val activeScreenState = currentScreen.collectAsState()
    val activeScreen = activeScreenState.value

    val activePersonaState = viewModel.selectedPersona.collectAsState()
    val activePersona = activePersonaState.value

    val personaPrimary = when (activePersona) {
        "Quantum Tech Pioneer" -> CyberPrimary
        "Ethereal Fashion Icon" -> Color(0xFFEC4899)
        "Cyber Athlete / Gamer" -> Color(0xFFEAB308)
        "Wellness Explorer" -> Color(0xFF10B981)
        "Cosmopolitan Decorator" -> Color(0xFFF97316)
        else -> CyberPrimary
    }

    val personaSecondary = when (activePersona) {
        "Quantum Tech Pioneer" -> CyberSecondary
        "Ethereal Fashion Icon" -> Color(0xFF8B5CF6)
        "Cyber Athlete / Gamer" -> CyberPrimary
        "Wellness Explorer" -> Color(0xFF84CC16)
        "Cosmopolitan Decorator" -> Color(0xFF3B82F6)
        else -> CyberSecondary
    }

    val customLiveLabel = when (activePersona) {
        "Quantum Tech Pioneer" -> "Specs Live"
        "Ethereal Fashion Icon" -> "Trends Live"
        "Cyber Athlete / Gamer" -> "Stream Live"
        "Wellness Explorer" -> "Hydra Live"
        "Cosmopolitan Decorator" -> "Space Live"
        else -> "Live"
    }

    Surface(
        color = Color(0xFF0A0A0A),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NexaNavBarItem(
                label = "Core Store",
                iconSelected = Icons.Default.Home,
                iconUnselected = Icons.Default.Home,
                isActive = activeScreen == NexaScreen.HOME || activeScreen == NexaScreen.CATEGORIES,
                onClick = { viewModel.navigateTo(NexaScreen.HOME) },
                activeColor = personaPrimary
            )

            // Dynamic Gradient Core for Central Nexa AI
            val aiActive = activeScreen == NexaScreen.AI_ASSISTANT
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .clickable { viewModel.navigateTo(NexaScreen.AI_ASSISTANT) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .size(38.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(personaPrimary, personaSecondary)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Nexa AI core button",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "NEXA AI",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (aiActive) personaPrimary else CyberGray,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }
            }

            NexaNavBarItem(
                label = customLiveLabel,
                iconSelected = Icons.Default.PlayArrow,
                iconUnselected = Icons.Default.PlayArrow,
                isActive = activeScreen == NexaScreen.LIVE_SHOPPING,
                onClick = { viewModel.navigateTo(NexaScreen.LIVE_SHOPPING) },
                activeColor = personaPrimary
            )

            NexaNavBarItem(
                label = "Rewards",
                iconSelected = Icons.Default.Refresh,
                iconUnselected = Icons.Default.Refresh,
                isActive = activeScreen == NexaScreen.GAMIFICATION,
                onClick = { viewModel.navigateTo(NexaScreen.GAMIFICATION) },
                activeColor = CyberGold
            )

            NexaNavBarItem(
                label = "System",
                iconSelected = Icons.Default.Menu,
                iconUnselected = Icons.Default.Menu,
                isActive = activeScreen == NexaScreen.SELLER_DASHBOARD || activeScreen == NexaScreen.ADMIN_PANEL || activeScreen == NexaScreen.SETTINGS || activeScreen == NexaScreen.ORDERS,
                onClick = { viewModel.navigateTo(NexaScreen.SETTINGS) },
                activeColor = personaSecondary
            )
        }
    }
}

@Composable
fun RowScope.NexaNavBarItem(
    label: String,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isActive) activeColor.copy(alpha = 0.12f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) iconSelected else iconUnselected,
                    contentDescription = label,
                    tint = if (isActive) activeColor else CyberGray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                color = if (isActive) activeColor else CyberGray
            )
        }
    }
}


// ==========================================
// SCREEN 3: HOME DASHBOARD (Personalized product grids)
// ==========================================
@Composable
fun NexaHomeScreen(viewModel: NexaViewModel) {
    val productsState = viewModel.productsList.collectAsState()
    val products = productsState.value

    val selectedCatState = viewModel.selectedCategory.collectAsState()
    val selectedCat = selectedCatState.value

    val activePersonaState = viewModel.selectedPersona.collectAsState()
    val activePersona = activePersonaState.value

    var searchQuery by remember { mutableStateOf("") }
    var voiceLoadingSimulated by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Determine color schemes & messaging based on computed user persona
    val personaPrimary = when (activePersona) {
        "Quantum Tech Pioneer" -> CyberPrimary
        "Ethereal Fashion Icon" -> Color(0xFFEC4899)
        "Cyber Athlete / Gamer" -> Color(0xFFEAB308)
        "Wellness Explorer" -> Color(0xFF10B981)
        "Cosmopolitan Decorator" -> Color(0xFFF97316)
        else -> CyberPrimary
    }

    val personaSecondary = when (activePersona) {
        "Quantum Tech Pioneer" -> CyberSecondary
        "Ethereal Fashion Icon" -> Color(0xFF8B5CF6)
        "Cyber Athlete / Gamer" -> CyberPrimary
        "Wellness Explorer" -> Color(0xFF84CC16)
        "Cosmopolitan Decorator" -> Color(0xFF3B82F6)
        else -> CyberSecondary
    }

    val personaMessage = when (activePersona) {
        "Quantum Tech Pioneer" -> "Underpinned by Samir Khadka's core labs. Quantum hardware protocols and secure nodes active."
        "Ethereal Fashion Icon" -> "Self-lacing couture diagnostics loaded. Dynamic style mesh coordinates verified."
        "Cyber Athlete / Gamer" -> "Low-latency esports stream sync and biometric hardware optimization complete."
        "Wellness Explorer" -> "Holographic bio-nectar formulations and recovery parameters initialized."
        "Cosmopolitan Decorator" -> "Responsive smart desks and levitating lamps mapped using local LiDAR."
        else -> "NexaCart AI engine mapping user telemetry layers securely."
    }

    val banners = when (activePersona) {
        "Quantum Tech Pioneer" -> listOf(
            Triple("CYBER FLASH ACCEL", "Claim +500 XP and up to 50% discount on NexaPhone!", personaPrimary),
            Triple("HARDWARE STREAK BOOSTER", "Tech node logins boost wallet balances instantly.", personaSecondary)
        )
        "Ethereal Fashion Icon" -> listOf(
            Triple("COUTURE SPECTRAL RELEASE", "Redeem rare style threads with +300 EcoBytes XP!", personaPrimary),
            Triple("SELF-LACING LAUNCH", "Claim 20% cashback on dynamic LED smart shoes.", personaSecondary)
        )
        "Cyber Athlete / Gamer" -> listOf(
            Triple("ESPORTS CHAIR DROP", "Claim ultra posture rig elements with level-up multipliers!", personaPrimary),
            Triple("HUD INTEGRATION UPGRADE", "Claim +400 XP on holographic headsets today.", personaSecondary)
        )
        "Cosmopolitan Decorator" -> listOf(
            Triple("LIDAR ROOM ADJUSTMENT", "Claim 25% off height-motor adaptive smart desks!", personaPrimary),
            Triple("LEVITATING EMBELLISHMENTS", "Custom glow floor lamps. Level up your spaces.", personaSecondary)
        )
        else -> listOf(
            Triple("CYBER FLASH SALE", "Claim +500 XP and up to 50% discount on NexaPhone!", CyberPrimary),
            Triple("ECHOON STREAK BOOSTER", "Daily login reward multiplies. claim free cash now!", CyberSecondary)
        )
    }

    var activeBannerIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(banners) {
        while (true) {
            delay(5000)
            activeBannerIdx = (activeBannerIdx + 1) % banners.size
        }
    }

    val filteredProducts = products.filter {
        (selectedCat == "All" || it.category.equals(selectedCat, ignoreCase = true)) &&
                it.name.contains(searchQuery, ignoreCase = true)
    }

    // Dynamic Personalized recommendation sorting: Put category associated with selected persona at the top!
    val preferredCategory = when (activePersona) {
        "Quantum Tech Pioneer" -> "Electronics"
        "Ethereal Fashion Icon" -> "Fashion"
        "Cyber Athlete / Gamer" -> "Gaming"
        "Wellness Explorer" -> "Health & Beauty"
        "Cosmopolitan Decorator" -> "Furniture" // displays Furniture & Home Decor
        else -> "All"
    }

    val sortedProducts = remember(filteredProducts, preferredCategory) {
        if (preferredCategory == "All") {
            filteredProducts
        } else {
            filteredProducts.sortedWith { p1, p2 ->
                val match1 = p1.category.lowercase() == preferredCategory.lowercase() || 
                             (preferredCategory == "Furniture" && p1.category.lowercase() in listOf("furniture", "home decor"))
                val match2 = p2.category.lowercase() == preferredCategory.lowercase() || 
                             (preferredCategory == "Furniture" && p2.category.lowercase() in listOf("furniture", "home decor"))
                when {
                    match1 && !match2 -> -1
                    !match1 && match2 -> 1
                    else -> 0
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Voice & text search bar unit
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨",
                    fontSize = 15.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Ask AI to find products...", color = CyberGray, fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Elegant VOICE pill matching HTML spec
                Box(
                    modifier = Modifier
                        .background(personaPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .border(0.5.dp, personaPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .clickable {
                            coroutineScope.launch {
                                voiceLoadingSimulated = true
                                searchQuery = "Holo"
                                delay(1200)
                                voiceLoadingSimulated = false
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (voiceLoadingSimulated) "..." else "VOICE",
                        color = personaPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // --- FEATURE 1: AI COGNITIVE JOURNEY DIAGNOSTICS & OVERRIDE PANEL ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(
                        BorderStroke(1.dp, Brush.linearGradient(listOf(personaPrimary.copy(alpha = 0.4f), Color.White.copy(alpha = 0.05f)))),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title index
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✨", fontSize = 14.sp)
                            Text(
                                text = "NEXA AI COGNITIVE MATRIX",
                                color = personaPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(personaPrimary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SAMIR SIGNATURE EDGE",
                                color = personaPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Persona display
                    Text(
                        text = activePersona.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = personaMessage,
                        color = CyberGray,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Radar diagnostics tracking items
                    Text(
                        text = "NEURAL COGNITIVE VECTORS INFERRED:",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val categories = listOf("Electronics", "Fashion", "Gaming", "Health & Beauty", "Furniture / Decor")
                        categories.forEach { cat ->
                            val catVal = when (cat) {
                                "Electronics" -> if (activePersona == "Quantum Tech Pioneer") 0.95f else 0.3f
                                "Fashion" -> if (activePersona == "Ethereal Fashion Icon") 0.95f else 0.25f
                                "Gaming" -> if (activePersona == "Cyber Athlete / Gamer") 0.95f else 0.2f
                                "Health & Beauty" -> if (activePersona == "Wellness Explorer") 0.95f else 0.15f
                                "Furniture / Decor" -> if (activePersona == "Cosmopolitan Decorator") 0.95f else 0.1f
                                else -> 0.2f
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    modifier = Modifier.width(110.dp),
                                    fontFamily = FontFamily.Monospace
                                )
                                LinearProgressIndicator(
                                    progress = catVal,
                                    color = if (catVal > 0.5f) personaPrimary else Color.White.copy(alpha = 0.1f),
                                    trackColor = Color.White.copy(alpha = 0.05f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${(catVal * 100).toInt()}%",
                                    color = if (catVal > 0.5f) personaPrimary else CyberGray,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive diagnostic manual configuration override tabs
                    Text(
                        text = "FORCE CALIBRATE COGNITIVE VECTOR:",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val personasList = listOf(
                        "Quantum Tech Pioneer",
                        "Ethereal Fashion Icon",
                        "Cyber Athlete / Gamer",
                        "Wellness Explorer",
                        "Cosmopolitan Decorator"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(personasList) { p ->
                            val active = activePersona == p
                            val pColor = when (p) {
                                "Quantum Tech Pioneer" -> CyberPrimary
                                "Ethereal Fashion Icon" -> Color(0xFFEC4899)
                                "Cyber Athlete / Gamer" -> Color(0xFFEAB308)
                                "Wellness Explorer" -> Color(0xFF10B981)
                                "Cosmopolitan Decorator" -> Color(0xFFF97316)
                                else -> CyberPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) pColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                    .border(
                                        1.dp,
                                        if (active) pColor else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setPersonaManual(p) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = when (p) {
                                        "Quantum Tech Pioneer" -> "TECH"
                                        "Ethereal Fashion Icon" -> "COUTURE"
                                        "Cyber Athlete / Gamer" -> "ESPORTS"
                                        "Wellness Explorer" -> "VITALITY"
                                        "Cosmopolitan Decorator" -> "SPACES"
                                        else -> "DEFAULT"
                                    },
                                    color = if (active) pColor else Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Banner display (Hero Section)
        item {
            val currentBanner = banners.getOrNull(activeBannerIdx) ?: banners[0]
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .clickable { viewModel.navigateTo(NexaScreen.GAMIFICATION) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF121212), Color(0xFF050505))
                            )
                        )
                ) {
                    // Radial glow in Top-Right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(160.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(personaSecondary.copy(alpha = 0.15f), Color.Transparent),
                                    radius = 240f
                                ),
                                CircleShape
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEF4444), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE NOW",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "SAMIR COGNITION CORE",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentBanner.first,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Claim Cash Reward",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Active +2.4k inside sphere",
                                color = personaPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // --- TAILORED PROMOTIONAL OFFERS ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalized Coupons",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = "COUPON TUNER",
                    color = personaPrimary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val coupons = listOf(
                Triple("TECH-ACCEL-15", "15% off Elektron Devices", "Quantum Tech Pioneer"),
                Triple("AESTHETE-STYLE-20", "20% off Cyber Couture", "Ethereal Fashion Icon"),
                Triple("ESPORTS-LEAGUE-10", "10% match cash back", "Cyber Athlete / Gamer"),
                Triple("BIO-REPLENISH-12", "12% off glowing foods", "Wellness Explorer"),
                Triple("NEON-SPACES-25", "25% off futuristic rooms", "Cosmopolitan Decorator")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(coupons) { coupon ->
                    val isPersonaMatched = activePersona == coupon.third
                    val borderAccent = when (coupon.third) {
                        "Quantum Tech Pioneer" -> CyberPrimary
                        "Ethereal Fashion Icon" -> Color(0xFFEC4899)
                        "Cyber Athlete / Gamer" -> Color(0xFFEAB308)
                        "Wellness Explorer" -> Color(0xFF10B981)
                        "Cosmopolitan Decorator" -> Color(0xFFF97316)
                        else -> CyberPrimary
                    }
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isPersonaMatched) borderAccent.copy(alpha = 0.08f) else Color.White.copy(
                                    alpha = 0.02f
                                )
                            )
                            .border(
                                1.dp,
                                if (isPersonaMatched) borderAccent else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = coupon.first,
                                    color = if (isPersonaMatched) borderAccent else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isPersonaMatched) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "RECOMMENDED",
                                            color = Color.Red,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = coupon.second,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "TAP TO SECURE PROMO CODE",
                                color = CyberHighlight,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.clickable {
                                    viewModel.statusText.value = "Applied Coupon: ${coupon.first} to current session Ledger"
                                }
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Spheres Filter bar
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Shopping Spheres",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val categoriesList = listOf("All", "Electronics", "Fashion", "Gaming", "Health & Beauty", "Furniture", "Home Decor")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(categoriesList) { cat ->
                    val isSelected = selectedCat.lowercase() == cat.lowercase()
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) personaPrimary else CyberSurface,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                BorderStroke(1.dp, if (isSelected) personaPrimary else CyberGray.copy(alpha = 0.3f)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.filterCategory(cat) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personalized Materials (${sortedProducts.size})",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                TextButton(onClick = { viewModel.navigateTo(NexaScreen.CATEGORIES) }) {
                    Text("Expanded View", color = personaPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Commodities listing grid rows
        if (sortedProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Empty list icon",
                            tint = CyberSecondary,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No compatible commodities match search index.",
                            color = CyberGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val chunked = sortedProducts.chunked(2)
            items(chunked) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (prod in pair) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            ProductGridCard(prod, viewModel)
                        }
                    }
                    if (pair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Procedural commodities visualizer
@Composable
fun ProductGridCard(product: Product, viewModel: NexaViewModel) {
    val activePersonaState = viewModel.selectedPersona.collectAsState()
    val activePersona = activePersonaState.value

    val isArCapable = product.category.lowercase() in listOf("furniture", "home decor", "fashion", "gaming")

    val personaPrimary = when (activePersona) {
        "Quantum Tech Pioneer" -> CyberPrimary
        "Ethereal Fashion Icon" -> Color(0xFFEC4899)
        "Cyber Athlete / Gamer" -> Color(0xFFEAB308)
        "Wellness Explorer" -> Color(0xFF10B981)
        "Cosmopolitan Decorator" -> Color(0xFFF97316)
        else -> CyberPrimary
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        border = BorderStroke(1.dp, CyberGray.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.selectProduct(product) }
            .testTag("product_card_${product.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(CyberPrimary.copy(alpha = 0.05f), CyberSecondary.copy(alpha = 0.05f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(CyberPrimary.copy(alpha = 0.4f), CyberSecondary.copy(alpha = 0.4f))
                            ), CircleShape
                        )
                )
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
                    contentDescription = product.name,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(
                            if (product.predictedDemand == "ULTRA HIGH" || product.predictedDemand == "EXTREME") CyberSecondary else CyberHighlight,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AI: ${product.predictedDemand}",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TRUST: ${product.aiScoreRating}%",
                        color = personaPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    color = CyberGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currencyFormat.format(product.price),
                        color = personaPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating star",
                            tint = CyberGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = product.rating.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { viewModel.addToCart(product) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(30.dp)
                            .testTag("add_cart_btn_${product.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add symbol",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                "INSTALL",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    if (isArCapable) {
                        Button(
                            onClick = {
                                viewModel.arSelectedProduct.value = product
                                viewModel.navigateTo(NexaScreen.VIRTUAL_AR)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, personaPrimary.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(30.dp)
                                .testTag("ar_view_btn_${product.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "AR Visualizer Icon",
                                    tint = personaPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    "AR FIT",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 4: DETAILED SPHERES CATALOG
// ==========================================
@Composable
fun NexaCategoriesScreen(viewModel: NexaViewModel) {
    val productsState = viewModel.productsList.collectAsState()
    val products = productsState.value

    val activeCatState = viewModel.selectedCategory.collectAsState()
    val activeCat = activeCatState.value

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Expanded Catalog Node: $activeCat",
            color = CyberPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = "Select from EchoByte vendor registry databases",
            color = CyberGray,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            val categorised = products.filter { activeCat == "All" || it.category.equals(activeCat, ignoreCase = true) }
            items(categorised) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF0C0F22), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (item.id) {
                                    "nex_phone_1" -> Icons.Default.Settings
                                    "echo_shoes_spectral" -> Icons.Default.Star
                                    "quantum_rig_helm" -> Icons.Default.PlayArrow
                                    else -> Icons.Default.ShoppingCart
                                },
                                contentDescription = item.name,
                                tint = CyberPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Marketed by: ${item.sellerName}",
                                fontSize = 9.sp,
                                color = CyberGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormat.format(item.price),
                                color = CyberSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { viewModel.selectProduct(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                        ) {
                            Text("VIEW", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 5: SPECIFICATIONS HUD & GEMINI AI SCANNERS
// ==========================================
@Composable
fun NexaProductDetailScreen(viewModel: NexaViewModel) {
    val productState = viewModel.selectedProduct.collectAsState()
    val product = productState.value

    val wishlistState = viewModel.wishlistItems.collectAsState()
    val wishlist = wishlistState.value

    val aiLoadingState = viewModel.aiReviewReportLoading.collectAsState()
    val aiLoading = aiLoadingState.value

    val aiResultState = viewModel.aiReviewReportResult.collectAsState()
    val aiResult = aiResultState.value

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No product chosen.", color = Color.White)
        }
        return
    }

    val prod = product!!
    val isFav = wishlist.any { it.productId == prod.id }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "SPECIFICATIONS HUD",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary
                )
                IconButton(onClick = { viewModel.toggleWishlist(prod) }) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite logo",
                        tint = if (isFav) CyberSecondary else Color.White
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberPrimary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF0F121F)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (prod.imageUrlName) {
                            "ic_phone" -> Icons.Default.Settings
                            "ic_shoes" -> Icons.Default.Star
                            "ic_gaming" -> Icons.Default.PlayArrow
                            "ic_jacket" -> Icons.Default.Person
                            "ic_beverage" -> Icons.Default.Favorite
                            else -> Icons.Default.ShoppingCart
                        },
                        contentDescription = prod.name,
                        tint = CyberHighlight,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = prod.name,
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "SUPPLIER HUB: ${prod.sellerName}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGray,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormat.format(prod.price),
                    color = CyberSecondary,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )

                Box(
                    modifier = Modifier
                        .background(Color(0x3300FF87), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "STOCK: ${prod.stock} PCS",
                        color = CyberHighlight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Description Meta-Vault:",
                fontSize = 12.sp,
                color = CyberPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = prod.description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        // Live Nexa Fake-Review Analyzer
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x771E2235)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "AI Audit Checkmark Logo", tint = CyberPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NexaShield Review Authenticity Guard",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Active AI scans customer keywords for artificial bots, rating manipulation, and repetitive sentiment anomalies.",
                        fontSize = 10.sp,
                        color = CyberGray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Active Scanner: Double-blind checks algorithms backed by AI models.",
                        fontSize = 10.sp,
                        color = CyberGray,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val reviewsMock = "Review 1: Very good product super amazing!\nReview 2: Very good product buy this now!\nReview 3: Excellent premium holographic quality recommend."
                            viewModel.analyzeFakeReviews(prod.name, reviewsMock)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ai_audit_reviews_btn")
                    ) {
                        if (aiLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RUNNING DEEP AUDIT SCANS...", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            Text("RUN NEXATRON REVIEW AUDIT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (aiResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F121F), RoundedCornerShape(8.dp))
                                .border(BorderStroke(0.5.dp, CyberHighlight), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = aiResult!!,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.addToCart(prod) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberHighlight),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_buy_now_btn")
            ) {
                Text(
                    text = "DEPLOY COMMODITY TO NEXACART",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}


// ==========================================
// SCREEN 6: SHOPPING CART MANIFEST
// ==========================================
@Composable
fun NexaCartScreen(viewModel: NexaViewModel, cart: List<CartItem>) {
    val totalAmount = cart.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Active Deployment Manifest (NexaCart)",
            color = CyberPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Review selected inventory nodes prior to delivery dispatch.",
            color = CyberGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty shopping",
                        tint = CyberSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Manifest currently empty. Add materials to cart.",
                        color = CyberGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(NexaScreen.HOME) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary)
                    ) {
                        Text("EXPLORE MATERIALS", color = Color.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cart) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSurface),
                        border = BorderStroke(0.5.dp, CyberGray.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color(0xFF0C0F22), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (item.productId) {
                                        "nex_phone_1" -> Icons.Default.Settings
                                        "echo_shoes_spectral" -> Icons.Default.Star
                                        "quantum_rig_helm" -> Icons.Default.PlayArrow
                                        else -> Icons.Default.ShoppingCart
                                    },
                                    contentDescription = item.productName,
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currencyFormat.format(item.price),
                                    color = CyberSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }

                            // Dynamic quantity modifiers
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.changeCartQuantity(item, -1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceLighter),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(text = "−", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = item.quantity.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Button(
                                    onClick = { viewModel.changeCartQuantity(item, 1) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceLighter),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(text = "+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                border = BorderStroke(1.dp, CyberSecondary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Dockets Count", color = CyberGray, fontSize = 12.sp)
                        Text(text = "${cart.sumOf { it.quantity }} Nodes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Core Price", color = CyberGray, fontSize = 12.sp)
                        Text(
                            text = currencyFormat.format(totalAmount),
                            color = CyberPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.navigateTo(NexaScreen.CHECKOUT) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("checkout_trigger_btn")
                    ) {
                        Text("PROCEED TO TRANSACTION HUD", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}


// ==========================================
// SCREEN 7: PREMIUM CHECKOUT GATEWAY
// ==========================================
@Composable
fun NexaCheckoutScreen(viewModel: NexaViewModel, cart: List<CartItem>) {
    var selectedMethod by remember { mutableStateOf("NexaWallet") }
    val totalAmount = cart.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "NexaPay Transaction Gateway",
            color = CyberHighlight,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "COMMODITY METADATA", color = CyberPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                for (item in cart) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${item.quantity}x ${item.productName}", color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(text = currencyFormat.format(item.price * item.quantity), color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Payment Ledger Integration:",
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val paymentGateways = listOf("NexaWallet", "eSewa", "Khalti", "IME Pay", "Stripe", "COD")
        for (gway in paymentGateways) {
            val isChosen = selectedMethod == gway
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(if (isChosen) Color(0x3300F2FE) else CyberSurface, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, if (isChosen) CyberPrimary else CyberGray.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                    .clickable { selectedMethod = gway }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isChosen,
                    onClick = { selectedMethod = gway },
                    colors = RadioButtonDefaults.colors(selectedColor = CyberPrimary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = gway,
                    color = if (isChosen) CyberPrimary else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (gway == "NexaWallet") {
                    Text(text = "Pre-Funded Ledger", fontSize = 9.sp, color = CyberHighlight, modifier = Modifier.background(Color(0x3300FF87)).padding(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.triggerCheckoutAndPlaceOrder(selectedMethod)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberHighlight),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("confirm_payment_btn")
        ) {
            Text(
                "AUTHORIZE PAYLOAD TRANSACTION (${currencyFormat.format(totalAmount)})",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}


// ==========================================
// SCREEN 8: COGNITIVE REAL-TIME CHATBOT & FASHION LENS
// ==========================================
@Composable
fun NexaAIAssistantScreen(viewModel: NexaViewModel) {
    val messagesState = viewModel.chatMessages.collectAsState()
    val messages = messagesState.value

    val loadingState = viewModel.aiChatLoading.collectAsState()
    val loading = loadingState.value

    val fashionLoadingState = viewModel.aiFashionLoading.collectAsState()
    val fashionLoading = fashionLoadingState.value

    val fashionResultState = viewModel.aiFashionReportResult.collectAsState()
    val fashionResult = fashionResultState.value

    var userPromptText by remember { mutableStateOf("") }
    var activeAiTab by remember { mutableStateOf("CHAT") }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .background(CyberSurface, RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { activeAiTab = "CHAT" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeAiTab == "CHAT") CyberPrimary else Color.Transparent),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Nexa Chat AI", color = if (activeAiTab == "CHAT") Color.Black else Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Button(
                onClick = { activeAiTab = "FASHION_LENS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeAiTab == "FASHION_LENS") CyberPrimary else Color.Transparent),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("AI Fashion Lens", color = if (activeAiTab == "FASHION_LENS") Color.Black else Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (activeAiTab == "CHAT") {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isBot = msg.sender == "NEXA_AI"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBot) CyberSurface else Color(0x3300F2FE)
                            ),
                            border = BorderStroke(0.5.dp, if (isBot) CyberPrimary.copy(alpha = 0.5f) else CyberSecondary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBot) "🧠 NEXA AI CORES" else "👤 COMMAND NODE",
                                    color = if (isBot) CyberPrimary else CyberSecondary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.text,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                if (loading) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Start) {
                            CircularProgressIndicator(color = CyberPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Core synchronizing quantum arrays...", color = CyberGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .background(CyberSurface, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userPromptText,
                    onValueChange = { userPromptText = it },
                    placeholder = { Text("Ask Nexa AI chat anything...", color = CyberGray, fontSize = 12.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (userPromptText.isNotBlank()) {
                            viewModel.postChatMessage(userPromptText)
                            userPromptText = ""
                        }
                    },
                    modifier = Modifier.testTag("ai_send_message_btn")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Send text prompt to brain", tint = CyberPrimary)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Personalized Cyber Fashion Visor",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Select your cybernetic style aesthetic, allowing our Nexa AI to curate layered accessories.",
                    color = CyberGray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val vibeOptions = listOf(
                    "Neon Hacker Matrix (Cyan & Slate-Carbon)",
                    "Orbital Executive Premium (Platinum & Dark-Silk)",
                    "Sub-Zero Neon Explorer (Violet & Cryo-Padded Leather)",
                    "Retro Arcade Cyber-Synth (Hot Magento & Radiant Brass)"
                )
                var chosenVibe by remember { mutableStateOf(vibeOptions[0]) }

                for (vibe in vibeOptions) {
                    val matching = chosenVibe == vibe
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(if (matching) Color(0x2200FF87) else CyberSurface, RoundedCornerShape(8.dp))
                            .border(BorderStroke(0.5.dp, if (matching) CyberHighlight else CyberGray.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                            .clickable { chosenVibe = vibe }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = matching, onClick = { chosenVibe = vibe }, colors = RadioButtonDefaults.colors(selectedColor = CyberHighlight))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = vibe, color = if (matching) CyberHighlight else Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.consultAIFashionStyling(chosenVibe) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                    modifier = Modifier.fillMaxWidth().testTag("ai_fashion_btn")
                ) {
                    if (fashionLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GENERATING FASHION ALIAS...", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    } else {
                        Text("QUERY EYE-LENS CODES", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                if (fashionResult != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberSurface, RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, CyberHighlight), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = fashionResult!!,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}


// ==========================================
// SCREEN 9: DEEP LIVE SHOPPING FEED OVERLAY
// ==========================================
@Composable
fun NexaLiveShoppingStreamScreen(viewModel: NexaViewModel) {
    var heartsTriggerCount by remember { mutableIntStateOf(0) }
    val simulatedAudienceJoined = remember {
        mutableStateListOf(
            "Samir Khadka entered live matrix node",
            "EchoByte Developer logged in",
            "Rita joined orbital stream",
            "Prashant tapped 10 hearts!"
        )
    }

    LaunchedEffect(Unit) {
        val simulatedGuestNicknames = listOf("kazi_boy", "nepal_punk_88", "hacker_samir", "ceo_khadka", "rita_nep")
        while (true) {
            delay(4000)
            simulatedAudienceJoined.add(0, "${simulatedGuestNicknames.random()} registered stream viewport")
            if (simulatedAudienceJoined.size > 5) {
                simulatedAudienceJoined.removeLast()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF1B0314), Color(0xFF02091A), CyberDarkBg)
                        )
                    )
                }
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Live video indicator logo",
                tint = CyberSecondary,
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ORBITAL LIVESTREAM COM-HUB ONLINE",
                color = CyberSecondary,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
            Text(
                text = "Streaming holographic feed • 1,420 viewers synchronised",
                color = CyberGray,
                fontSize = 10.sp
            )
        }

        LazyColumn(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.65f)
                .height(130.dp)
                .padding(start = 14.dp, bottom = 10.dp)
        ) {
            items(simulatedAudienceJoined) { msg ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = msg,
                        color = CyberHighlight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceLighter),
                border = BorderStroke(1.dp, CyberSecondary),
                modifier = Modifier
                    .size(90.dp, 120.dp)
                    .clickable {
                        viewModel.insertCustomProduct("NexaPhone Holo X", 145000.0, "Electronics", "Featured Live Feed Item", 12)
                        viewModel.navigateTo(NexaScreen.CART)
                    }
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE OFFER",
                        color = CyberSecondary,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Active promo product", tint = CyberPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Holo X", color = Color.White, fontSize = 8.sp, maxLines = 1)
                    Text(text = "Rs. 145K", color = CyberHighlight, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.background(CyberSecondary, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                        Text(text = "BUY NOW", fontSize = 6.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = { heartsTriggerCount++ },
                containerColor = CyberSecondary,
                shape = CircleShape,
                modifier = Modifier.size(46.dp).testTag("live_pulse_heart_btn")
            ) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Love tap", tint = Color.White)
            }
        }

        if (heartsTriggerCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        for (i in 1..heartsTriggerCount) {
                            val yOffset = size.height - (i * 24f % size.height)
                            val xOffset = size.width - 150f + (sin(yOffset / 100f) * 60f)
                            drawCircle(
                                color = CyberSecondary,
                                radius = 8f,
                                center = Offset(xOffset, yOffset),
                                alpha = 1f - (yOffset / size.height)
                            )
                        }
                    }
            )
        }
    }
}


// ==========================================
// SCREEN 10: GAMIFICATION ZONE Circular Wheel
// ==========================================
@Composable
fun NexaGamificationScreen(viewModel: NexaViewModel, userProfile: UserProfile) {
    val currentRotationState = viewModel.spinResultDegree.collectAsState()
    val currentRotation = currentRotationState.value

    val isSpinningState = viewModel.isSpinning.collectAsState()
    val isSpinning = isSpinningState.value

    val prizeWonState = viewModel.spinPrizeWon.collectAsState()
    val prizeWon = prizeWonState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Cyber Play-to-Earn Vault",
                color = CyberGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Play quests, spin the reactor core, earn tokens and checkout cash discounts.",
                color = CyberGray,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "DAILY MATRIX STREAK", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(text = "Streak Multiplier: ${userProfile.streakCount}x days", color = CyberGray, fontSize = 10.sp)
                        }
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Cal icon placeholder", tint = CyberGold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 1..5) {
                            val activeStamp = i <= userProfile.streakCount
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(
                                        if (activeStamp) Color(0x33FFCC00) else Color(0x11FFFFFF),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        BorderStroke(1.dp, if (activeStamp) CyberGold else CyberGray.copy(alpha = 0.2f)),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Day $i", color = if (activeStamp) CyberGold else CyberGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    if (activeStamp) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Stamp check", tint = CyberGold, modifier = Modifier.size(16.dp))
                                    } else {
                                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Stamp open", tint = CyberGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.performDailyCheckIn()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                        modifier = Modifier.fillMaxWidth().testTag("claim_daily_rewards_btn")
                    ) {
                        Text("AUTHENTICATE DAILY CHECK-IN CHECKPOINT (+150 NC)", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceLighter),
                border = BorderStroke(1.dp, CyberPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quantum Node Reactor Spinner",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Cost: 50 NexaCoins per fusion spin",
                        color = CyberGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .rotate(currentRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val sectorsCount = 8
                            val prizeDegree = 360f / sectorsCount
                            val innerColors = listOf(
                                Color(0xFFFF2E93), Color(0xFF00F2FE),
                                Color(0xFFFFCC00), Color(0xFF00FF87),
                                Color(0xFF1D2235), Color(0xFF07090E),
                                Color(0xFF8E9AAA), Color(0xFF10131E)
                            )
                            for (i in 0 until sectorsCount) {
                                drawArc(
                                    color = innerColors[i],
                                    startAngle = i * prizeDegree,
                                    sweepAngle = prizeDegree,
                                    useCenter = true,
                                    size = size
                                )
                            }
                            drawCircle(color = Color.White, radius = 24f)
                        }

                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Pin indicator placeholder symbol",
                            tint = Color.Black,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (prizeWon != null) {
                        Text(
                            text = "FUSION RESULT CONVERGED:\n$prizeWon",
                            color = CyberHighlight,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.spinTheRewardWheel() },
                        enabled = !isSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("reactor_spin_btn")
                    ) {
                        Text("INITIATE REACTOR SPIN (-50 COINS)", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}


// ==========================================
// SCREEN 11: MULTI VENDOR SELLER PANEL
// ==========================================
@Composable
fun NexaSellerDashboardScreen(viewModel: NexaViewModel) {
    var upName by remember { mutableStateOf("") }
    var upPrice by remember { mutableStateOf("") }
    var upCat by remember { mutableStateOf("Fashion") }
    var upStock by remember { mutableStateOf("45") }
    var upDesc by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Vendor Hub Console",
                color = CyberPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Register multi-vendor items and verify real-time income statistics.",
                color = CyberGray,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberHighlight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "REVENUE LEDGER ANALYTICS", color = CyberGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Rs. 928,400", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Box(modifier = Modifier.background(Color(0x3300FF87)).padding(4.dp)) {
                            Text(text = "+14.2% PROGRESS", color = CyberHighlight, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val heights = listOf(0.4f, 0.6f, 0.5f, 0.9f, 0.7f, 0.8f, 1f)
                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
                        heights.forEachIndexed { idx, h ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(18.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(h)
                                            .fillMaxWidth()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(CyberPrimary, CyberSecondary)
                                                ), RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = days[idx], color = CyberGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceLighter),
                border = BorderStroke(0.5.dp, CyberGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "REGISTER NEW MATERIAL FLUX", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = upName,
                        onValueChange = { upName = it },
                        label = { Text("Material Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = upPrice,
                        onValueChange = { upPrice = it },
                        label = { Text("Flux Price (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = upCat,
                            onValueChange = { upCat = it },
                            label = { Text("Sphere") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = upStock,
                            onValueChange = { upStock = it },
                            label = { Text("Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = upDesc,
                        onValueChange = { upDesc = it },
                        label = { Text("Specifications Details") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPrimary, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val priceVal = upPrice.toDoubleOrNull() ?: 100.0
                            val stockVal = upStock.toIntOrNull() ?: 10
                            viewModel.insertCustomProduct(upName, priceVal, upCat, upDesc, stockVal)
                            upName = ""
                            upPrice = ""
                            upDesc = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("seller_upload_product_btn")
                    ) {
                        Text("LAUNCH MATERIAL IN CATALOG", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}


// ==========================================
// SCREEN 12: ADMIN CONTROL MODULE
// ==========================================
@Composable
fun NexaAdminPanelScreen(viewModel: NexaViewModel) {
    val productsState = viewModel.productsList.collectAsState()
    val products = productsState.value

    val transactionsState = viewModel.walletTransactions.collectAsState()
    val transactions = transactionsState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "System Admin Control Core",
                color = CyberSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Control databases, moderate live user listings, scan financial caches.",
                color = CyberGray,
                fontSize = 11.sp
            )
        }

        item {
            Text(text = "CONCURRENT PRODUCT LISTINGS (${products.size})", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        items(products) { prod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = prod.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Vendor alias: ${prod.sellerName}", color = CyberGray, fontSize = 9.sp)
                    }
                    IconButton(
                        onClick = { viewModel.moderateDeleteProduct(prod.id) },
                        modifier = Modifier.testTag("admin_delete_${prod.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Moderate delete", tint = CyberSecondary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "SECURE WALLET PAYMENTS LEDGER", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        if (transactions.isEmpty()) {
            item {
                Text(text = "No recorded secure transactions.", color = CyberGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            items(transactions) { tx ->
                Card(colors = CardDefaults.cardColors(containerColor = CyberSurfaceLighter)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = tx.description, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Gateway: ${tx.gateway}", color = CyberGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = (if (tx.type == "CREDIT") "+" else "−") + " Rs. " + DecimalFormat("#,##0").format(tx.amount),
                            color = if (tx.type == "CREDIT") CyberHighlight else CyberSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


// ==========================================
// SCREEN 13: DIGITAL WALLET LEDGER
// ==========================================
@Composable
fun NexaWalletScreen(viewModel: NexaViewModel) {
    val userProfileState = viewModel.userProfile.collectAsState()
    val userProfile = userProfileState.value

    val transactionsState = viewModel.walletTransactions.collectAsState()
    val transactions = transactionsState.value

    var depositText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Secure Digital Ledger",
                color = CyberHighlight,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Deposit, withdraw, encrypt financial nodes using eSewa, Khalti, IME Pay.",
                color = CyberGray,
                fontSize = 11.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberHighlight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "NexaWallet Active Liquid Balance", color = CyberGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormat.format(userProfile.walletBalance),
                        color = CyberHighlight,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = CyberSurfaceLighter)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "CREDIT CHANNELS", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it },
                        label = { Text("Transfer Cash Amount (Rs.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberHighlight, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val dAmount = depositText.toDoubleOrNull() ?: 0.0
                                if (dAmount > 0) {
                                    viewModel.depositWalletFunds(dAmount, "eSewa")
                                    depositText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60BB46)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("dep_esewa_btn")
                        ) {
                            Text("eSewa", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val dAmount = depositText.toDoubleOrNull() ?: 0.0
                                if (dAmount > 0) {
                                    viewModel.depositWalletFunds(dAmount, "Khalti")
                                    depositText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C2D91)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("dep_khalti_btn")
                        ) {
                            Text("Khalti", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val dAmount = depositText.toDoubleOrNull() ?: 0.0
                                if (dAmount > 0) {
                                    viewModel.depositWalletFunds(dAmount, "Stripe")
                                    depositText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("dep_stripe_btn")
                        ) {
                            Text("Stripe", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "PERSONAL ACCOUNT BALANCE TRACES", color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        if (transactions.isEmpty()) {
            item {
                Text(text = "No history recorded on wallet ledger.", color = CyberGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        } else {
            items(transactions) { tx ->
                Card(colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = tx.description, color = Color.White, fontSize = 12.sp)
                            Text(text = "Gateway: ${tx.gateway}", color = CyberGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = (if (tx.type == "CREDIT") "+" else "−") + " Rs. " + DecimalFormat("#,##0").format(tx.amount),
                            color = if (tx.type == "CREDIT") CyberHighlight else CyberSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


// ==========================================
// SCREEN 14: CONSOLE SETTINGS & PROFILE DETAILS
// ==========================================
@Composable
fun NexaSettingsScreen(viewModel: NexaViewModel, userProfile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "System Console Config",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "View core hardware configuration and Samir Khadka licenses.",
            color = CyberGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            border = BorderStroke(1.dp, CyberPrimary)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Profile icon log screen", tint = CyberPrimary, modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = userProfile.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = userProfile.email, color = CyberGray, fontSize = 11.sp)
                        Text(text = "Access node: ${userProfile.role}", color = CyberHighlight, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "ADMINISTRATIVE ACCESS SHELLS", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

        Button(
            onClick = { viewModel.navigateTo(NexaScreen.ADMIN_PANEL) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_admin_panel_btn")
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Admin key icon placeholder")
            Spacer(modifier = Modifier.width(8.dp))
            Text("LAUNCH ROOT ADMIN TERMINAL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        Button(
            onClick = { viewModel.navigateTo(NexaScreen.SELLER_DASHBOARD) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_seller_panel_btn")
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Merchant dashboard key icon placeholder", tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SWITCH TO MERCHANT TERMINAL", color = Color.Black, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        Button(
            onClick = { viewModel.navigateTo(NexaScreen.ORDERS) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
            border = BorderStroke(0.5.dp, CyberGray),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_orders_history_btn")
        ) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "Orders checklist placeholder logo")
            Spacer(modifier = Modifier.width(8.dp))
            Text("VIEW PURCHASE INVOICES", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121F))) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "LICENSING PROTOCOL REGISTRY", color = CyberSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                val descLicensing = "This NexaEcosystem code is copyrighted and belongs exclusively to:\n" +
                        "Owner & CEO: Samir Khadka\n" +
                        "Consortium: EchoByte Technologies Inc.\n\n" +
                        "Any synthetic duplication, redistribution, or replication of visual assets, circular spinners, or system interface nodes without strict signature consent will trigger automatic microservice protocol compliance blocks."
                Text(
                    text = descLicensing,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}


// ==========================================
// SCREEN 15: INVOICES LOG STATISTICS
// ==========================================
@Composable
fun NexaOrdersScreen(viewModel: NexaViewModel) {
    val ordersState = viewModel.orderHistory.collectAsState()
    val orders = ordersState.value

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Active Logistics Stream",
            color = CyberHighlight,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Track ongoing orbital payload deliveries and invoice cancellations.",
            color = CyberGray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No previous transactions registered.", color = CyberGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(orders) { ord ->
                    Card(colors = CardDefaults.cardColors(containerColor = CyberSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "ID: ${ord.orderId}", color = CyberPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (ord.status) {
                                                "Delivered" -> Color(0x3300FF87)
                                                "Cancelled" -> Color(0x33FF4D4D)
                                                else -> Color(0x33FFCC00)
                                            }, RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = ord.status.uppercase(), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text = "Summary: ${ord.itemsSummary}", color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currencyFormat.format(ord.totalAmount),
                                    color = CyberSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { viewModel.startTrackingOrder(ord) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.height(28.dp).testTag("track_btn_${ord.orderId}")
                                    ) {
                                        Text("GPS HUD", color = Color.Black, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }

                                    if (ord.status != "Delivered" && !ord.status.contains("Refund")) {
                                        Button(
                                            onClick = { viewModel.cancelAndRefundOrder(ord) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FF4D4D)),
                                            border = BorderStroke(0.5.dp, Color(0xFFFF4D4D)),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(28.dp).testTag("refund_btn_${ord.orderId}")
                                        ) {
                                            Text("CANCEL", color = Color(0xFFFF4D4D), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// SCREEN 16: PHYSICAL GPS TRANSPONDER MAP SIMULATOR
// ==========================================
@Composable
fun NexaDeliveryTrackingScreen(viewModel: NexaViewModel) {
    val orderState = viewModel.selectedOrderForTracking.collectAsState()
    val order = orderState.value

    if (order == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active transponder selected.", color = Color.White)
        }
        return
    }

    val activeOrd = order!!

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(NexaScreen.ORDERS) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = Color.White)
            }
            Text(
                text = "Live Courier Transponder",
                color = CyberPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(44.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .background(Color(0xFF07090E), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, CyberPrimary), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val margin = 50f
                val streetAlpha = 0.15f
                drawLine(color = CyberPrimary, start = Offset(100f, margin), end = Offset(100f, size.height - margin), strokeWidth = 3f, alpha = streetAlpha)
                drawLine(color = CyberPrimary, start = Offset(400f, margin), end = Offset(400f, size.height - margin), strokeWidth = 3f, alpha = streetAlpha)
                drawLine(color = CyberSecondary, start = Offset(margin, 200f), end = Offset(size.width - margin, 200f), strokeWidth = 3f, alpha = streetAlpha)
                drawLine(color = CyberSecondary, start = Offset(margin, 500f), end = Offset(size.width - margin, 500f), strokeWidth = 3f, alpha = streetAlpha)

                drawCircle(color = CyberSecondary, radius = 60f, center = Offset(250f, 350f), alpha = 0.08f)
                drawCircle(color = CyberPrimary, radius = 90f, center = Offset(500f, 600f), alpha = 0.05f)

                val latMin = 27.7000
                val latMax = 27.7500
                val lngMin = 85.3000
                val lngMax = 85.3600

                val scaleX = size.width / (lngMax - lngMin).toFloat()
                val scaleY = size.height / (latMax - latMin).toFloat()

                val courierX = ((activeOrd.deliveryLongitude - lngMin) * scaleX).toFloat()
                val courierY = size.height - ((activeOrd.deliveryLatitude - latMin) * scaleY).toFloat()

                drawLine(
                    color = CyberHighlight,
                    start = Offset(100f, size.height - 100f),
                    end = Offset(courierX, courierY),
                    strokeWidth = 4f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                drawRect(
                    color = CyberPrimary,
                    topLeft = Offset(100f - 15f, size.height - 100f - 15f),
                    size = androidx.compose.ui.geometry.Size(30f, 30f)
                )

                drawCircle(
                    color = CyberSecondary,
                    radius = 16f,
                    center = Offset(courierX, courierY)
                )
                drawCircle(
                    color = CyberPrimary,
                    radius = 28f,
                    center = Offset(courierX, courierY),
                    alpha = 0.4f
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(CyberPrimary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Central Depot Hub", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp, 2.dp).background(CyberHighlight))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Optimized Route Vector", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(CyberSecondary, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Courier Transponder Active", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "ESTIMATED ARRIVAL (ETA)", color = CyberGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        val etaText = if (activeOrd.status == "Delivered") "DELIVERED REACHED!" else "${activeOrd.deliveryEtaMinutes} MINUTES TO DOCK"
                        Text(
                            text = etaText,
                            color = CyberPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(CyberSecondary, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(text = activeOrd.status.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GPS Coordinates: " + String.format("%.4f", activeOrd.deliveryLatitude) + " N, " + String.format("%.4f", activeOrd.deliveryLongitude) + " E",
                    color = CyberGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
