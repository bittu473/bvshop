package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val oldPrice: Double,
    val rating: Float,
    val category: String,
    val imageResName: String, // e.g. "img_hero_banner" or custom or "img_shop_logo"
    val imageUrl: String? = null, // for seller uploaded pictures
    val sizeVariants: String, // Comma-separated, e.g. "S,M,L"
    val colorVariants: String, // Comma-separated, e.g. "Obsidian,Amber,Bronze"
    val stock: Int,
    val reviewCount: Int,
    val sellerName: String,
    val isApproved: Boolean = true
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val quantity: Int,
    val selectedSize: String,
    val selectedColor: String
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey val productId: String
)

@Entity(tableName = "addresses")
data class UserAddress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val itemsJson: String, // Serialized list of purchased items (id, title, qty, price, size, color)
    val totalPrice: Double,
    val shippingAddress: String,
    val status: String, // "Processing", "Shipped", "Out for Delivery", "Delivered"
    val paymentMethod: String,
    val trackingLogsJson: String // Serialized list of status logs with timestamps
)

@Entity(tableName = "profiles")
data class UserProfile(
    @PrimaryKey val id: String, // "current_user"
    val name: String,
    val email: String,
    val phone: String,
    val walletBalance: Double,
    val referralCode: String,
    val isSeller: Boolean = false,
    val isAdmin: Boolean = false
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val amount: Double,
    val type: String, // "CREDIT", "DEBIT"
    val description: String
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val title: String,
    val message: String,
    val isRead: Boolean = false
)

// Simple helper class for items inside Cart UI and Order History
data class CartProduct(
    val cartItem: CartItem,
    val product: Product
)
