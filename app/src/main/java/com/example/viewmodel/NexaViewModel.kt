package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.api.GeminiClient
import com.example.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

// Representing active chat message bubble
data class ChatMessage(
    val sender: String, // "USER" or "NEXA_AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Main screen navigation keys
enum class NexaScreen {
    SPLASH,
    LOGIN,
    HOME,
    CATEGORIES,
    PRODUCT_DETAIL,
    CART,
    CHECKOUT,
    WALLET,
    ORDERS,
    SETTINGS,
    LIVE_SHOPPING,
    SELLER_DASHBOARD,
    ADMIN_PANEL,
    GAMIFICATION,
    AI_ASSISTANT,
    DELIVERY_MAP,
    VIRTUAL_AR
}

class NexaViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "NexaViewModel"

    // Initialize Room Database
    private val db = Room.databaseBuilder(
        application,
        NexaDatabase::class.java,
        "nexacart_database"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.nexaDao()

    // --- Core Jetpack States ---

    // Navigation and UX Flow
    var currentScreen = MutableStateFlow(NexaScreen.SPLASH)
        private set

    var prevScreens = mutableListOf<NexaScreen>() // Navigation Stack for Back button

    var selectedProduct = MutableStateFlow<Product?>(null)
        private set

    var selectedCategory = MutableStateFlow<String>("All")
        private set

    // --- AI Personalization Engine State ---
    val selectedPersona = MutableStateFlow<String>("Tech Enthusiast")
    val browsingHistory = MutableStateFlow<List<String>>(emptyList())

    // --- AR Interactive Visor States ---
    val arScale = MutableStateFlow(1.0f)
    val arRotation = MutableStateFlow(0f)
    val arHeight = MutableStateFlow(0f)
    val arLuminosity = MutableStateFlow(1.0f)
    val arGlowColor = MutableStateFlow("Cyan") // Cyan, Magenta, Emerald, Gold
    val arPointCloudActive = MutableStateFlow(true)
    val arSelectedProduct = MutableStateFlow<Product?>(null)

    fun setPersonaManual(persona: String) {
        selectedPersona.value = persona
    }

    fun resetArSettings() {
        arScale.value = 1.0f
        arRotation.value = 0f
        arHeight.value = 0f
        arLuminosity.value = 1.0f
        arGlowColor.value = "Cyan"
        arPointCloudActive.value = true
    }

    fun autoRecalculatePersona() {
        val historyIds = browsingHistory.value
        val wishlists = wishlistItems.value
        val orders = orderHistory.value

        val products = productsList.value
        if (products.isEmpty()) return

        val categoryScores = mutableMapOf<String, Int>()

        // Weight elements: Browsing history gets 1 point
        for (id in historyIds) {
            val prod = products.find { it.id == id }
            if (prod != null) {
                categoryScores[prod.category] = (categoryScores[prod.category] ?: 0) + 1
            }
        }
        // Wishlists get 3 points
        for (w in wishlists) {
            val prod = products.find { it.id == w.productId }
            if (prod != null) {
                categoryScores[prod.category] = (categoryScores[prod.category] ?: 0) + 3
            }
        }
        // Orders get 5 points
        for (ord in orders) {
            val summary = ord.itemsSummary.lowercase()
            for (p in products) {
                if (summary.contains(p.name.lowercase())) {
                    categoryScores[p.category] = (categoryScores[p.category] ?: 0) + 5
                }
            }
        }

        if (categoryScores.isEmpty()) return
        val topCategory = categoryScores.maxByOrNull { it.value }?.key ?: return

        val newPersona = when (topCategory) {
            "Electronics" -> "Quantum Tech Pioneer"
            "Fashion" -> "Ethereal Fashion Icon"
            "Gaming" -> "Cyber Athlete / Gamer"
            "Health & Beauty" -> "Wellness Explorer"
            "Furniture", "Home Decor" -> "Cosmopolitan Decorator"
            else -> selectedPersona.value
        }
        selectedPersona.value = newPersona
    }

    // Selected Order for Real-Time Live Delivery GPS Screen
    var selectedOrderForTracking = MutableStateFlow<OrderTrack?>(null)
        private set

    // Database reactive streams
    val userProfile: StateFlow<UserProfile> = dao.getUserProfileFlow()
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())

    val productsList: StateFlow<List<Product>> = dao.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = dao.getCartItemsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = dao.getWishlistFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderHistory: StateFlow<List<OrderTrack>> = dao.getOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val walletTransactions: StateFlow<List<WalletTransaction>> = dao.getWalletTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive State Holders ---

    // Chat room values
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("NEXA_AI", "Welcome Samir Khadka to NexaCart! I am your cybernetic AI shopping assistant. How can I help you customize your shopping matrix today?")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    var aiChatLoading = MutableStateFlow(false)
        private set

    // AI Fake review analysis state
    var aiReviewReportResult = MutableStateFlow<String?>(null)
        private set
    var aiReviewReportLoading = MutableStateFlow(false)
        private set

    // AI Fashion analyzer state
    var aiFashionReportResult = MutableStateFlow<String?>(null)
        private set
    var aiFashionLoading = MutableStateFlow(false)
        private set

    // Interactive Spinning Wheel
    var isSpinning = MutableStateFlow(false)
        private set
    var spinResultDegree = MutableStateFlow(0f)
        private set
    var spinPrizeWon = MutableStateFlow<String?>(null)
        private set

    // Admin & Seller control metrics
    var adminProductModerationList = MutableStateFlow<List<Product>>(emptyList())
        private set

    // App system status logs
    var statusText = MutableStateFlow("NexaEcosystem Online")
        private set

    init {
        // Seed default products and user profile on first boot automatically
        viewModelScope.launch(Dispatchers.IO) {
            setupInitialDatabaseSeed()
        }
    }

    // --- Navigation ---

    fun navigateTo(screen: NexaScreen) {
        if (currentScreen.value != screen) {
            prevScreens.add(currentScreen.value)
            currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (prevScreens.isNotEmpty()) {
            val last = prevScreens.removeAt(prevScreens.size - 1)
            currentScreen.value = last
            return true
        }
        return false
    }

    fun selectProduct(product: Product) {
        selectedProduct.value = product
        
        // Track browsing history dynamically
        val currentHistory = browsingHistory.value.toMutableList()
        currentHistory.add(product.id)
        browsingHistory.value = currentHistory
        
        // Compute new persona dynamically
        autoRecalculatePersona()

        navigateTo(NexaScreen.PRODUCT_DETAIL)
    }

    fun filterCategory(category: String) {
        selectedCategory.value = category
    }

    // --- Database Initialization & Seeding ---

    private suspend fun setupInitialDatabaseSeed() {
        try {
            // Check current user profile
            val curProfile = dao.getUserProfileDirect()
            if (curProfile == null) {
                dao.saveUserProfile(UserProfile()) // Default Founder Samir Khadka
            }

            // Seed premium cyber product catalog if database is fresh
            val existing = dao.getAllProductsFlow().first()
            if (existing.isEmpty()) {
                val seedList = listOf(
                    Product(
                        id = "nex_phone_1",
                        name = "NexaPhone Holo X",
                        price = 145000.0,
                        category = "Electronics",
                        rating = 4.9,
                        reviewsCount = 240,
                        sellerName = "EchoByte Labs",
                        imageUrlName = "ic_phone",
                        isFeatured = true,
                        aiScoreRating = 98,
                        predictedDemand = "ULTRA HIGH",
                        description = "Next-generation quantum smartphone with neural holographic sync projection, liquid crystal back-panel, and a built-in highly secure Hardware Digital Wallet transponder."
                    ),
                    Product(
                        id = "echo_shoes_spectral",
                        name = "EchoShoes Spectral",
                        price = 12500.0,
                        category = "Fashion",
                        rating = 4.8,
                        reviewsCount = 132,
                        sellerName = "Driftware Ltd",
                        imageUrlName = "ic_shoes",
                        isFeatured = true,
                        aiScoreRating = 95,
                        predictedDemand = "HIGH",
                        description = "Self-lacing adaptive light sportswear. Cybernetic structural mesh equipped with dynamic, full-spectrum LED color customization pairing instantly to your audio vibes."
                    ),
                    Product(
                        id = "quantum_rig_helm",
                        name = "Quantum HUD Helmet",
                        price = 48000.0,
                        category = "Gaming",
                        rating = 4.7,
                        reviewsCount = 88,
                        sellerName = "EchoByte Technologies",
                        imageUrlName = "ic_gaming",
                        isFeatured = true,
                        aiScoreRating = 97,
                        predictedDemand = "HIGH",
                        description = "Full-immersion augmented reality visor. Delivers localized 3D audio cues, dual-panel nano screens, and responsive biometric eye-tracking controls."
                    ),
                    Product(
                        id = "core_carbon_jacket",
                        name = "CyberCore Shield Jacket",
                        price = 32000.0,
                        category = "Fashion",
                        rating = 4.9,
                        reviewsCount = 110,
                        sellerName = "EchoByte Apparel",
                        imageUrlName = "ic_jacket",
                        isFeatured = true,
                        aiScoreRating = 99,
                        predictedDemand = "EXTREME",
                        description = "Thermoregulating carbon fiber jacket with integrated modular active heating cells, water-shield nano coating, and integrated wireless device charging pockets."
                    ),
                    Product(
                        id = "bioglow_fluid_pack",
                        name = "BioGlow Smart Nectar",
                        price = 1950.0,
                        category = "Health & Beauty",
                        rating = 4.6,
                        reviewsCount = 312,
                        sellerName = "NexaLabs Core",
                        imageUrlName = "ic_beverage",
                        isFeatured = false,
                        aiScoreRating = 91,
                        predictedDemand = "STABLE",
                        description = "Molecular glowing hydration fuel. Promotes bio-energy, cells replenishment, and provides long-lasting focus elements with zero crash or synthetic sugars."
                    ),
                    Product(
                        id = "neon_pod_pro",
                        name = "NexaBuds Holographic",
                        price = 16000.0,
                        category = "Electronics",
                        rating = 4.7,
                        reviewsCount = 94,
                        sellerName = "EchoByte Tech",
                        imageUrlName = "ic_earbuds",
                        isFeatured = false,
                        aiScoreRating = 93,
                        predictedDemand = "STEADY",
                        description = "Acoustic spatial audio earbuds with transparent composite glass housing, hybrid solid-state noise cancel, and automatic translation firmware."
                    ),
                    Product(
                        id = "cyber_edge_desk",
                        name = "Nexa-Edge Cyber Desk",
                        price = 45000.0,
                        category = "Furniture",
                        rating = 4.9,
                        reviewsCount = 42,
                        sellerName = "EchoByte Furniture",
                        imageUrlName = "ic_furniture",
                        isFeatured = true,
                        aiScoreRating = 96,
                        predictedDemand = "HIGH",
                        description = "Next-gen motor-lift height desk fitted with custom structural carbon-alloy framework, integrated wireless charging bays, and edge ambient responsive LED lighting."
                    ),
                    Product(
                        id = "orbit_lamp_lum",
                        name = "Orbit Luminary Floor Lamp",
                        price = 14000.0,
                        category = "Home Decor",
                        rating = 4.7,
                        reviewsCount = 57,
                        sellerName = "AeroShade Design",
                        imageUrlName = "ic_decor",
                        isFeatured = false,
                        aiScoreRating = 94,
                        predictedDemand = "STABLE",
                        description = "Intelligent levitating glow ring floor lamp syncing bio-metrics with adaptive bioluminescent color tones."
                    )
                )
                dao.saveProducts(seedList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database: ${e.message}")
        }
    }


    // --- Core Shopping Actions (Cart / Wishlist) ---

    fun addToCart(product: Product, option: String = "Default") {
        viewModelScope.launch(Dispatchers.IO) {
            val currentCart = cartItems.value
            val match = currentCart.find { it.productId == product.id && it.selectedOption == option }
            if (match != null) {
                dao.updateCartQuantity(match.id, match.quantity + 1)
            } else {
                dao.insertCartItem(
                    CartItem(
                        productId = product.id,
                        productName = product.name,
                        price = product.price,
                        quantity = 1,
                        imageUrlName = product.imageUrlName,
                        selectedOption = option
                    )
                )
            }
            showTemporaryStatus("Added ${product.name} to NexaCart")
        }
    }

    fun changeCartQuantity(item: CartItem, amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val nextQty = item.quantity + amount
            if (nextQty <= 0) {
                dao.deleteCartItem(item)
            } else {
                dao.updateCartQuantity(item.id, nextQty)
            }
        }
    }

    fun removeCartItem(item: CartItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCartItem(item)
        }
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch(Dispatchers.IO) {
            val isFav = wishlistItems.value.any { it.productId == product.id }
            if (isFav) {
                dao.deleteWishlistById(product.id)
                showTemporaryStatus("Removed ${product.name} from Wishlist")
            } else {
                dao.insertWishlist(WishlistItem(productId = product.id))
                showTemporaryStatus("Added ${product.name} to Wishlist")
            }
        }
    }


    // --- Multi-Vendor Product Actions ---

    fun insertCustomProduct(name: String, price: Double, category: String, desc: String, stock: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "vendor_prod_" + System.currentTimeMillis()
            val newProd = Product(
                id = id,
                name = name,
                price = price,
                category = category,
                rating = 5.0,
                reviewsCount = 1,
                sellerName = "Vendor Hub",
                imageUrlName = "ic_launcher_foreground",
                stock = stock,
                description = desc,
                aiScoreRating = 100,
                predictedDemand = "STABLE"
            )
            dao.insertProduct(newProd)
            showTemporaryStatus("Product successfully listed on NexaCart!")
        }
    }

    fun moderateDeleteProduct(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteProductById(productId)
            showTemporaryStatus("Product Moderated and Removed successfully")
        }
    }


    // --- Checkout & Real-time Live Order Dispatch Simulator ---

    fun triggerCheckoutAndPlaceOrder(paymentGateway: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.price * it.quantity }
            val currentProfile = userProfile.value

            if (currentProfile.walletBalance < subtotal && paymentGateway != "COD") {
                withContext(Dispatchers.Main) {
                    showTemporaryStatus("Insufficient Funds in NexaWallet!")
                }
                return@launch
            }

            // Deduct funds from Wallet Balance unless COD
            if (paymentGateway != "COD") {
                val nextBalance = currentProfile.walletBalance - subtotal
                val nextCoins = currentProfile.coins + (subtotal * 0.05).toInt() // Add 5% cash-back coins
                val nextXP = currentProfile.xp + 150 // Gain transaction experience points
                dao.saveUserProfile(
                    currentProfile.copy(
                        walletBalance = nextBalance,
                        coins = nextCoins,
                        xp = nextXP
                    )
                )

                // Log Transaction
                dao.insertWalletTransaction(
                    WalletTransaction(
                        amount = subtotal,
                        type = "DEBIT",
                        description = "NexaCart Checkout Payment for Order",
                        gateway = paymentGateway
                    )
                )
            } else {
                // COD yields standard XP rewards too
                val nextCoins = currentProfile.coins + 25
                val nextXP = currentProfile.xp + 50
                dao.saveUserProfile(
                    currentProfile.copy(
                        coins = nextCoins,
                        xp = nextXP
                    )
                )
            }

            // Build item description summary
            val summary = items.joinToString { "${it.quantity}x ${it.productName}" }
            val oId = "NEX-" + (1000..9999).random() + "-XP"

            // Insert new order: initial state "Ordered"
            val initialOrder = OrderTrack(
                orderId = oId,
                totalAmount = subtotal,
                itemsSummary = summary,
                paymentMethod = paymentGateway,
                status = "Ordered",
                deliveryEtaMinutes = 35,
                deliveryLatitude = 27.7007,
                deliveryLongitude = 85.3123
            )
            dao.insertOrder(initialOrder)

            // Clear Cart
            dao.clearCart()

            // Navigate to Order screen
            withContext(Dispatchers.Main) {
                navigateTo(NexaScreen.ORDERS)
                selectedOrderForTracking.value = initialOrder
                showTemporaryStatus("Order Placed Successfully!")
            }

            // Fire and Forget real-time dispatch map simulation loop
            simulateDeliveryLifecycle(oId)
        }
    }

    /**
     * Spawns a background worker that transitions the order from ordered -> delivered,
     * modifying coordinates, ETA, status reactively on database updates.
     */
    private suspend fun simulateDeliveryLifecycle(orderId: String) {
        val pathCoordinates = listOf(
            Pair(27.7007, 85.3123) to Pair(35, "Ordered"),
            Pair(27.7123, 85.3210) to Pair(28, "Processing"),
            Pair(27.7250, 85.3340) to Pair(20, "With Delivery Partner"),
            Pair(27.7380, 85.3400) to Pair(10, "Out for Delivery"),
            Pair(27.7490, 85.3560) to Pair(0, "Delivered")
        )

        for (step in pathCoordinates) {
            delay(12000) // Transition every 12 seconds
            val currentCoordinates = step.first
            val currentMetadata = step.second
            
            dao.updateOrderStatus(
                orderId = orderId,
                status = currentMetadata.second,
                eta = currentMetadata.first,
                lat = currentCoordinates.first,
                lng = currentCoordinates.second
            )

            // Refresh tracking stream selection
            val curOrder = selectedOrderForTracking.value
            if (curOrder != null && curOrder.orderId == orderId) {
                selectedOrderForTracking.value = curOrder.copy(
                    status = currentMetadata.second,
                    deliveryEtaMinutes = currentMetadata.first,
                    deliveryLatitude = currentCoordinates.first,
                    deliveryLongitude = currentCoordinates.second
                )
            }
        }
    }

    fun startTrackingOrder(order: OrderTrack) {
        selectedOrderForTracking.value = order
        navigateTo(NexaScreen.DELIVERY_MAP)
    }


    // --- Gamification Play Zone (Daily Stamp, Wheel Spin) ---

    fun performDailyCheckIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = dao.getUserProfileDirect() ?: return@launch
            val nowTime = System.currentTimeMillis()
            
            // Check if checked in today (simplified to 1 hour delay limit for easy testing)
            if (nowTime - user.lastCheckIn < 360000) {
                withContext(Dispatchers.Main) {
                    showTemporaryStatus("Already Checked In recently! Come back in an hour.")
                }
                return@launch
            }

            val updatedUser = user.copy(
                coins = user.coins + 150,
                xp = user.xp + 250,
                streakCount = user.streakCount + 1,
                lastCheckIn = nowTime
            )
            dao.saveUserProfile(updatedUser)

            withContext(Dispatchers.Main) {
                showTemporaryStatus("Daily Check-In: +150 NexaCoins, +250 XP!")
            }
        }
    }

    fun spinTheRewardWheel() {
        if (isSpinning.value) return

        viewModelScope.launch(Dispatchers.Main) {
            // Check if user has enough coins to spin
            val userDirect = withContext(Dispatchers.IO) { dao.getUserProfileDirect() } ?: return@launch
            if (userDirect.coins < 50) {
                showTemporaryStatus("Requires 50 NexaCoins to Spin the Wheel!")
                return@launch
            }

            // Deduct spin fee
            withContext(Dispatchers.IO) {
                dao.saveUserProfile(userDirect.copy(coins = userDirect.coins - 50))
            }

            isSpinning.value = true
            spinPrizeWon.value = null

            // Animate spin degree
            val randomAddedRotations = (5..10).random() * 360f
            val prizeIndex = (0..7).random()
            val targetDegree = (prizeIndex * 45) + 22.5f
            val finalDegree = randomAddedRotations + targetDegree

            // Smooth linear visual rotation steps
            var currentDeg = 0f
            while (currentDeg < finalDegree) {
                val step = (finalDegree - currentDeg) / 10f
                currentDeg += if (step < 2f) 2f else step
                spinResultDegree.value = currentDeg
                delay(16)
            }

            // List of potential reward pointers
            val prizes = listOf(
                "+200 NexaCoins",
                "+500 XP Surge",
                "20% VIP Coupon Coupon",
                "+50 NexaCoins Voucher",
                "+1000 Extra XP",
                "Grand VIP Badge Stamp",
                "Free Super-Delivery Pass",
                "Rs. 500 NexaWallet Cash"
            )
            val wonPrize = prizes[prizeIndex]
            spinPrizeWon.value = wonPrize
            isSpinning.value = false

            // Credit Won Rewards directly to Room DB
            withContext(Dispatchers.IO) {
                val freshProfile = dao.getUserProfileDirect() ?: return@withContext
                var coinsIncrement = 0
                var xpIncrement = 0
                var walletIncrement = 0.0

                when {
                    wonPrize.contains("200 NexaCoins") -> coinsIncrement = 200
                    wonPrize.contains("50 NexaCoins") -> coinsIncrement = 50
                    wonPrize.contains("500 XP") -> xpIncrement = 500
                    wonPrize.contains("1000 Extra XP") -> xpIncrement = 1000
                    wonPrize.contains("Rs. 500") -> walletIncrement = 500.0
                }

                dao.saveUserProfile(
                    freshProfile.copy(
                        coins = freshProfile.coins + coinsIncrement,
                        xp = freshProfile.xp + xpIncrement,
                        walletBalance = freshProfile.walletBalance + walletIncrement
                    )
                )

                if (walletIncrement > 0) {
                    dao.insertWalletTransaction(
                        WalletTransaction(
                            amount = walletIncrement,
                            type = "CREDIT",
                            description = "Gamified Wheel Spin Jackpot Win",
                            gateway = "NexaWallet"
                        )
                    )
                }
            }

            showTemporaryStatus("Jackpot Stop! Won: $wonPrize")
        }
    }


    // --- Cognitive AI Shopping Core Integrations (Gemini API Callouts) ---

    fun postChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage("USER", text)
        _chatMessages.value = _chatMessages.value + userMsg
        aiChatLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val systemRule = "You are Nexa AI, the master conversational shopping core of NexaCart super-app. " +
                    "Owned completely by Samir Khadka and EchoByte Technologies. Answer premium cyber questions " +
                    "with high cyber-tech enthusiasm, providing useful techspecs, prices, and suggestions."

            val rawPrompt = "User queries NexaCart console: $text"
            val aiResponse = GeminiClient.queryGemini(rawPrompt, systemRule)

            withContext(Dispatchers.Main) {
                val botMsg = ChatMessage("NEXA_AI", aiResponse)
                _chatMessages.value = _chatMessages.value + botMsg
                aiChatLoading.value = false
            }
        }
    }

    fun analyzeFakeReviews(productName: String, reviews: String) {
        aiReviewReportLoading.value = true
        aiReviewReportResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val instruction = "You are a cyber security AI agent scanning for artificial bot generated feedback."
            val userPrompt = "Perform fake-review detection audit for product: $productName. " +
                    "Analyze these reviews for bot patterns, repetitive wording, and rating clustering:\n\n$reviews"

            val result = GeminiClient.queryGemini(userPrompt, instruction)
            withContext(Dispatchers.Main) {
                aiReviewReportResult.value = result
                aiReviewReportLoading.value = false
            }
        }
    }

    fun consultAIFashionStyling(selectedStyleVibe: String) {
        aiFashionLoading.value = true
        aiFashionReportResult.value = null

        viewModelScope.launch(Dispatchers.IO) {
            val instruction = "You are the Nexa Cybernetic Fashion Lens coordinator, suggesting stunning neon outfit matrices."
            val prompt = "Generate a personalized futuristic outfit matrix and dynamic fashion layering ideas for the style matrix vibe: '$selectedStyleVibe'."

            val result = GeminiClient.queryGemini(prompt, instruction)
            withContext(Dispatchers.Main) {
                aiFashionReportResult.value = result
                aiFashionLoading.value = false
            }
        }
    }


    // --- Wallet Money Add & Refunding ---

    fun depositWalletFunds(amount: Double, gateway: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = dao.getUserProfileDirect() ?: return@launch
            val nextBalance = profile.walletBalance + amount
            val nextXP = profile.xp + 50
            dao.saveUserProfile(profile.copy(walletBalance = nextBalance, xp = nextXP))

            dao.insertWalletTransaction(
                WalletTransaction(
                    amount = amount,
                    type = "CREDIT",
                    description = "Added Secure Net Banking Funds To Wallet",
                    gateway = gateway
                )
            )
            withContext(Dispatchers.Main) {
                showTemporaryStatus("Deposited Rs. ${DecimalFormat("#,##0").format(amount)} successfully via $gateway!")
            }
        }
    }

    fun cancelAndRefundOrder(order: OrderTrack) {
        viewModelScope.launch(Dispatchers.IO) {
            if (order.status == "Delivered") {
                withContext(Dispatchers.Main) {
                    showTemporaryStatus("Delivered orders cannot be refunded directly.")
                }
                return@launch
            }

            // Refund wallet balance
            val profile = dao.getUserProfileDirect() ?: return@launch
            val nextBalance = profile.walletBalance + order.totalAmount
            dao.saveUserProfile(profile.copy(walletBalance = nextBalance))

            // Delete order or set refunded status
            dao.insertOrder(order.copy(status = "Refunded/Cancelled"))

            // Log Transaction receipt
            dao.insertWalletTransaction(
                WalletTransaction(
                    amount = order.totalAmount,
                    type = "CREDIT",
                    description = "Refunded cancelled Order ${order.orderId}",
                    gateway = "NexaWallet"
                )
            )

            // Close tracking panel if closed order
            val trackingOrder = selectedOrderForTracking.value
            if (trackingOrder != null && trackingOrder.orderId == order.orderId) {
                selectedOrderForTracking.value = null
            }

            withContext(Dispatchers.Main) {
                showTemporaryStatus("Order ${order.orderId} Refund Completed!")
            }
        }
    }


    // --- Utility Methods ---

    private suspend fun showTemporaryStatus(message: String) {
        withContext(Dispatchers.Main) {
            statusText.value = message
        }
    }
}
