package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Database Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "samir_khadka_user",
    val name: String = "Samir Khadka",
    val email: String = "samirkhadka841@gmail.com",
    val company: String = "EchoByte Technologies",
    val role: String = "CEO & Copyright Holder",
    val coins: Int = 500,               // NexaCoins
    val xp: Int = 1200,                 // Gamified XP levels
    val walletBalance: Double = 25000.0, // Pre-funded Rs. 25,000 for e-payment testing
    val streakCount: Int = 3,
    val lastCheckIn: Long = 0L,
    val isVIP: Boolean = true
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val rating: Double,
    val reviewsCount: Int,
    val sellerName: String,
    val imageUrlName: String, // String reference for dynamic generation or local icon
    val isFeatured: Boolean = false,
    val stock: Int = 99,
    // AI Trust metrics
    val aiScoreRating: Int = 94,        // AI computed genuine review accuracy
    val predictedDemand: String = "HIGH", // AI market forecast demand (Low, Normal, High)
    val isFakeReviewDetected: Boolean = false,
    val description: String = "Futuristic premium product designed for NexaCart super-ecosystem."
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrlName: String,
    val selectedOption: String = "Default"
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderTrack(
    @PrimaryKey val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val itemsSummary: String,
    val paymentMethod: String,
    val status: String, // "Ordered", "Processing", "With Delivery Partner", "Out for Delivery", "Delivered"
    val deliveryEtaMinutes: Int = 45,
    val deliveryLatitude: Double = 27.7007,
    val deliveryLongitude: Double = 85.3123
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "CREDIT", "DEBIT"
    val description: String,
    val gateway: String, // "eSewa", "Khalti", "IME Pay", "Stripe", "NexaWallet"
    val timestamp: Long = System.currentTimeMillis()
)


// --- Data Access Object (DAO) ---

@Dao
interface NexaDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProducts(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartQuantity(id: Int, quantity: Int)

    @Delete
    suspend fun deleteCartItem(item: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT * FROM wishlist_items")
    fun getWishlistFlow(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItem)

    @Query("DELETE FROM wishlist_items WHERE productId = :productId")
    suspend fun deleteWishlistById(productId: String)

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getOrdersFlow(): Flow<List<OrderTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderTrack)

    @Query("UPDATE orders SET status = :status, deliveryEtaMinutes = :eta, deliveryLatitude = :lat, deliveryLongitude = :lng WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, eta: Int, lat: Double, lng: Double)

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getWalletTransactionsFlow(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransaction(transaction: WalletTransaction)
}


// --- App Database Configuration ---

@Database(
    entities = [
        UserProfile::class,
        Product::class,
        CartItem::class,
        WishlistItem::class,
        OrderTrack::class,
        WalletTransaction::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexaDatabase : RoomDatabase() {
    abstract fun nexaDao(): NexaDao
}
