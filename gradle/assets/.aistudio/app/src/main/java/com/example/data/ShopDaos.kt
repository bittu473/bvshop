package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isApproved = 1")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsDirect(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM products WHERE sellerName = :sellerName")
    fun getProductsBySellerFlow(sellerName: String): Flow<List<Product>>
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItemsFlow(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Update
    suspend fun updateCartItem(cartItem: CartItem)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items")
    fun getWishlistItemsFlow(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItem)

    @Delete
    suspend fun deleteWishlist(item: WishlistItem)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE productId = :productId)")
    fun isWishlistedFlow(productId: String): Flow<Boolean>
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses ORDER BY isDefault DESC")
    fun getAddressesFlow(): Flow<List<UserAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: UserAddress)

    @Delete
    suspend fun deleteAddress(address: UserAddress)

    @Query("UPDATE addresses SET isDefault = 0")
    suspend fun clearDefaultAddresses()

    @Transaction
    suspend fun setDefaultAddress(id: Int) {
        clearDefaultAddresses()
        updateDefaultAddress(id, true)
    }

    @Query("UPDATE addresses SET isDefault = :isDefault WHERE id = :id")
    suspend fun updateDefaultAddress(id: Int, isDefault: Boolean)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: String, status: String)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getProfileFlow(id: String = "current_user"): Flow<UserProfile?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileDirect(id: String = "current_user"): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("UPDATE profiles SET walletBalance = :balance WHERE id = 'current_user'")
    suspend fun updateWalletBalance(balance: Double)

    @Query("UPDATE profiles SET isSeller = :isSeller WHERE id = 'current_user'")
    suspend fun setSellerStatus(isSeller: Boolean)

    @Query("UPDATE profiles SET isAdmin = :isAdmin WHERE id = 'current_user'")
    suspend fun setAdminStatus(isAdmin: Boolean)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getTransactionsFlow(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
