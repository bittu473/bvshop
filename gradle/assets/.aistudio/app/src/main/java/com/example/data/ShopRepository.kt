package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class ShopRepository(private val db: ShopDatabase) {

    val products: Flow<List<Product>> = db.productDao().getAllProductsFlow()
    val cartItems: Flow<List<CartItem>> = db.cartDao().getCartItemsFlow()
    val wishlistItems: Flow<List<WishlistItem>> = db.wishlistDao().getWishlistItemsFlow()
    val addresses: Flow<List<UserAddress>> = db.addressDao().getAddressesFlow()
    val orders: Flow<List<Order>> = db.orderDao().getAllOrdersFlow()
    val userProfile: Flow<UserProfile?> = db.profileDao().getProfileFlow()
    val walletTransactions: Flow<List<WalletTransaction>> = db.walletDao().getTransactionsFlow()
    val notifications: Flow<List<Notification>> = db.notificationDao().getNotificationsFlow()

    // Seeds initial catalog and profile if empty
    suspend fun seedDatabaseIfEmpty() {
        // 1. Seed Profile
        val existingProfile = db.profileDao().getProfileDirect()
        if (existingProfile == null) {
            val defaultProfile = UserProfile(
                id = "current_user",
                name = "Aria Sterling",
                email = "aria.sterling@luxcart.com",
                phone = "+1 (555) 019-2831",
                walletBalance = 1500.0, // Starter wallet balance for premium experience
                referralCode = "LUX777",
                isSeller = false,
                isAdmin = false
            )
            db.profileDao().insertProfile(defaultProfile)

            // Add starter transaction log
            db.walletDao().insertTransaction(
                WalletTransaction(
                    timestamp = System.currentTimeMillis(),
                    amount = 1500.0,
                    type = "CREDIT",
                    description = "Welcome Signup Bonus + Wallet Activation"
                )
            )

            // Seed initial notifications
            db.notificationDao().insertNotification(
                Notification(
                    timestamp = System.currentTimeMillis(),
                    title = "Welcome to ShopNest",
                    message = "Experience premium curated shopping. We've credited $1,500.00 to your wallet as a sign-up bonus!"
                )
            )
            db.notificationDao().insertNotification(
                Notification(
                    timestamp = System.currentTimeMillis() - 3600000,
                    title = "Flash Sale Alert!",
                    message = "Get 20% off on all luxury catalog items today. Use coupon code: LUX20"
                )
            )
        }

        // 2. Seed Products
        val existingProducts = db.productDao().getAllProductsDirect()
        if (existingProducts.isEmpty()) {
            val defaultProducts = listOf(
                Product(
                    id = "lux_watch_1",
                    title = "Cosmic Gold Chronograph",
                    description = "Sleek obsidian gold bezel with authentic premium leather straps, sapphire glass protection, and precision automatic quartz movement. Water-resistant up to 50 meters. A timeless classic designed for the discerning individual.",
                    price = 299.00,
                    oldPrice = 450.00,
                    rating = 4.8f,
                    category = "Luxury",
                    imageResName = "img_shop_logo",
                    sizeVariants = "One Size",
                    colorVariants = "Amber Gold,Champagne,Titanium",
                    stock = 14,
                    reviewCount = 124,
                    sellerName = "ShopNest Curated"
                ),
                Product(
                    id = "audio_1",
                    title = "Symphony ANC Headphones",
                    description = "Escape into pure sound with high-fidelity acoustic performance, class-leading active noise cancellation (ANC), memory foam cushioned earcups, and up to 45 hours of immersive playtime on a single charge.",
                    price = 199.00,
                    oldPrice = 299.00,
                    rating = 4.7f,
                    category = "Electronics",
                    imageResName = "img_hero_banner",
                    sizeVariants = "Standard",
                    colorVariants = "Silver Slate,Matte Black,Earthy Sand",
                    stock = 25,
                    reviewCount = 98,
                    sellerName = "Acoustic Labs"
                ),
                Product(
                    id = "footwear_1",
                    title = "Stratus Air Sneakers",
                    description = "Engineered with breathable knit mesh, reactive lightweight foam midsoles, and signature impact-absorbing air bubbles. Merges athletic power with ultimate everyday street fashion styling.",
                    price = 129.00,
                    oldPrice = 189.00,
                    rating = 4.5f,
                    category = "Fashion",
                    imageResName = "img_onboarding_bg",
                    sizeVariants = "US 8,US 9,US 10,US 11",
                    colorVariants = "Scarlet Red,Midnight Cobalt,Stealth Grey",
                    stock = 30,
                    reviewCount = 215,
                    sellerName = "Apex Wear"
                ),
                Product(
                    id = "lifestyle_1",
                    title = "Velvet Crimson Handbag",
                    description = "Handcrafted designer clutch in plush luxurious velvet with rich heavy gold-toned hardware, modular shoulder drop chain, and spacious compartment layout for modern essentials.",
                    price = 349.00,
                    oldPrice = 499.00,
                    rating = 4.9f,
                    category = "Luxury",
                    imageResName = "img_shop_logo",
                    sizeVariants = "Medium",
                    colorVariants = "Crimson Ruby,Ivory Cream,Midnight Onyx",
                    stock = 8,
                    reviewCount = 64,
                    sellerName = "ShopNest Curated"
                ),
                Product(
                    id = "audio_2",
                    title = "Echo Pro Wireless Earbuds",
                    description = "Ultra-compact ergonomic true-wireless earbuds with deep powerful bass, custom EQ controls via companion app, crystal-clear quad microphone calling, and water-resistant nano coating.",
                    price = 89.00,
                    oldPrice = 149.00,
                    rating = 4.4f,
                    category = "Electronics",
                    imageResName = "img_hero_banner",
                    sizeVariants = "One Size",
                    colorVariants = "Chalk White,Graphite Black",
                    stock = 45,
                    reviewCount = 182,
                    sellerName = "Acoustic Labs"
                ),
                Product(
                    id = "lifestyle_2",
                    title = "Satin Silk Loungewear",
                    description = "Drift into sheer relaxation. Breathable, 100% premium mulberry silk pajamas. Includes elegant button-up top and relaxed drawstring pants with a beautiful lustrous drape.",
                    price = 159.00,
                    oldPrice = 229.00,
                    rating = 4.6f,
                    category = "Fashion",
                    imageResName = "img_onboarding_bg",
                    sizeVariants = "XS,S,M,L,XL",
                    colorVariants = "Emerald Silk,Rose Quartz,Champagne",
                    stock = 18,
                    reviewCount = 76,
                    sellerName = "Mulberry Silk Co."
                ),
                Product(
                    id = "home_1",
                    title = "Aura Smart Ambient Light",
                    description = "Transform your living space with customizable ambient flows. Supports 16 million colors, voice assistant control, sound-responsive reactive modes, and automated sunrise/sunset routing.",
                    price = 79.00,
                    oldPrice = 119.00,
                    rating = 4.3f,
                    category = "Lifestyle",
                    imageResName = "img_hero_banner",
                    sizeVariants = "One Size",
                    colorVariants = "Minimalist White",
                    stock = 50,
                    reviewCount = 142,
                    sellerName = "Aura Home"
                ),
                Product(
                    id = "lux_perfume_1",
                    title = "Oud Eclipse Eau de Parfum",
                    description = "An enigmatic blend of rich agarwood, warm black pepper, and earthy cedar wood, lightened with sweet damask rose and vanilla bean. Exudes ultimate luxury, depth, and sophistication.",
                    price = 189.00,
                    oldPrice = 250.00,
                    rating = 4.9f,
                    category = "Luxury",
                    imageResName = "img_shop_logo",
                    sizeVariants = "50ml,100ml",
                    colorVariants = "Eclipse Obsidian",
                    stock = 12,
                    reviewCount = 89,
                    sellerName = "Oud Atelier"
                )
            )
            for (p in defaultProducts) {
                db.productDao().insertProduct(p)
            }
        }
    }

    // --- Product Actions ---
    suspend fun getProductById(id: String): Product? = db.productDao().getProductById(id)
    suspend fun insertProduct(product: Product) = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: Product) = db.productDao().updateProduct(product)
    suspend fun deleteProduct(product: Product) = db.productDao().deleteProduct(product)
    fun getProductsBySeller(sellerName: String): Flow<List<Product>> = db.productDao().getProductsBySellerFlow(sellerName)

    // --- Cart Actions ---
    suspend fun insertCartItem(cartItem: CartItem) = db.cartDao().insertCartItem(cartItem)
    suspend fun updateCartItem(cartItem: CartItem) = db.cartDao().updateCartItem(cartItem)
    suspend fun deleteCartItem(cartItem: CartItem) = db.cartDao().deleteCartItem(cartItem)
    suspend fun clearCart() = db.cartDao().clearCart()

    // --- Wishlist Actions ---
    suspend fun toggleWishlist(productId: String) {
        val currentWishlist = db.wishlistDao().getWishlistItemsFlow().first()
        val exists = currentWishlist.any { it.productId == productId }
        if (exists) {
            db.wishlistDao().deleteWishlist(WishlistItem(productId))
        } else {
            db.wishlistDao().insertWishlist(WishlistItem(productId))
        }
    }
    fun isWishlisted(productId: String): Flow<Boolean> = db.wishlistDao().isWishlistedFlow(productId)

    // --- Address Actions ---
    suspend fun insertAddress(address: UserAddress) {
        if (address.isDefault) {
            db.addressDao().clearDefaultAddresses()
        }
        db.addressDao().insertAddress(address)
    }
    suspend fun deleteAddress(address: UserAddress) = db.addressDao().deleteAddress(address)
    suspend fun setDefaultAddress(id: Int) = db.addressDao().setDefaultAddress(id)

    // --- Profile Actions ---
    suspend fun updateProfile(profile: UserProfile) = db.profileDao().insertProfile(profile)
    suspend fun setSellerStatus(isSeller: Boolean) = db.profileDao().setSellerStatus(isSeller)
    suspend fun setAdminStatus(isAdmin: Boolean) = db.profileDao().setAdminStatus(isAdmin)
    
    suspend fun addWalletBalance(amount: Double, source: String) {
        val profile = db.profileDao().getProfileDirect() ?: return
        val newBalance = profile.walletBalance + amount
        db.profileDao().updateWalletBalance(newBalance)
        db.walletDao().insertTransaction(
            WalletTransaction(
                timestamp = System.currentTimeMillis(),
                amount = amount,
                type = "CREDIT",
                description = "Wallet Recharge via $source"
            )
        )
        db.notificationDao().insertNotification(
            Notification(
                timestamp = System.currentTimeMillis(),
                title = "Wallet Recharged",
                message = "Successfully credited $${String.format("%.2f", amount)} via ${source}. New balance: $${String.format("%.2f", newBalance)}"
            )
        )
    }

    // --- Order / Checkout Actions ---
    suspend fun placeOrder(
        items: List<CartProduct>,
        totalPrice: Double,
        address: UserAddress,
        paymentMethod: String,
        couponApplied: String?
    ): Boolean {
        val profile = db.profileDao().getProfileDirect() ?: return false
        
        // If payment is via wallet, verify balance
        if (paymentMethod == "Wallet") {
            if (profile.walletBalance < totalPrice) {
                return false // Insufficient balance
            }
            // Deduct from wallet
            val newBalance = profile.walletBalance - totalPrice
            db.profileDao().updateWalletBalance(newBalance)
            db.walletDao().insertTransaction(
                WalletTransaction(
                    timestamp = System.currentTimeMillis(),
                    amount = totalPrice,
                    type = "DEBIT",
                    description = "Purchase Payment (Order #${System.currentTimeMillis().toString().takeLast(6)})"
                )
            )
        }

        // Apply coupon cashback if applicable
        if (couponApplied != null) {
            val cashbackAmount = when (couponApplied) {
                "LUX20" -> totalPrice * 0.10 // 10% cashback
                "WELCOME50" -> 50.0
                else -> 0.0
            }
            if (cashbackAmount > 0) {
                val currentProfile = db.profileDao().getProfileDirect()!!
                db.profileDao().updateWalletBalance(currentProfile.walletBalance + cashbackAmount)
                db.walletDao().insertTransaction(
                    WalletTransaction(
                        timestamp = System.currentTimeMillis(),
                        amount = cashbackAmount,
                        type = "CREDIT",
                        description = "Cashback for coupon $couponApplied"
                    )
                )
            }
        }

        // Construct serializable JSON array of items
        val jArray = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("productId", item.product.id)
            obj.put("title", item.product.title)
            obj.put("price", item.product.price)
            obj.put("quantity", item.cartItem.quantity)
            obj.put("size", item.cartItem.selectedSize)
            obj.put("color", item.cartItem.selectedColor)
            jArray.put(obj)

            // Adjust stock
            val updatedProduct = item.product.copy(
                stock = (item.product.stock - item.cartItem.quantity).coerceAtLeast(0)
            )
            db.productDao().insertProduct(updatedProduct)
        }

        val orderId = "ORD" + (100000 + (Math.random() * 900000).toInt()).toString()
        val addressText = "${address.name}, ${address.street}, ${address.city}, ${address.state} - ${address.zipCode}"

        // Set tracking logs
        val trackingLogs = JSONArray()
        val log1 = JSONObject().apply {
            put("status", "Order Placed")
            put("timestamp", System.currentTimeMillis())
            put("desc", "Your order has been received and is being processed by the merchant.")
        }
        val log2 = JSONObject().apply {
            put("status", "Confirmed")
            put("timestamp", System.currentTimeMillis() + 120000)
            put("desc", "Order validated and confirmed. Items are being packed.")
        }
        trackingLogs.put(log1)
        trackingLogs.put(log2)

        val newOrder = Order(
            id = orderId,
            timestamp = System.currentTimeMillis(),
            itemsJson = jArray.toString(),
            totalPrice = totalPrice,
            shippingAddress = addressText,
            status = "Processing",
            paymentMethod = paymentMethod,
            trackingLogsJson = trackingLogs.toString()
        )

        db.orderDao().insertOrder(newOrder)
        db.cartDao().clearCart()

        // Send confirmation notification
        db.notificationDao().insertNotification(
            Notification(
                timestamp = System.currentTimeMillis(),
                title = "Order Confirmed!",
                message = "Your order ${orderId} for $${String.format("%.2f", totalPrice)} has been successfully placed via ${paymentMethod}."
            )
        )

        return true
    }

    suspend fun updateOrderStatus(orderId: String, status: String) {
        val order = db.orderDao().getOrderById(orderId) ?: return
        db.orderDao().updateOrderStatus(orderId, status)
        
        // Append tracking log
        val logs = JSONArray(order.trackingLogsJson)
        val newLog = JSONObject().apply {
            put("status", status)
            put("timestamp", System.currentTimeMillis())
            put("desc", when(status) {
                "Shipped" -> "The carrier has received the shipment and it is on its way."
                "Out for Delivery" -> "The local delivery expert is bringing your package today."
                "Delivered" -> "Delivered safely! Thank you for choosing ShopNest."
                else -> "Your order state has changed to $status"
            })
        }
        logs.put(newLog)
        
        val updatedOrder = order.copy(
            status = status,
            trackingLogsJson = logs.toString()
        )
        db.orderDao().insertOrder(updatedOrder)

        db.notificationDao().insertNotification(
            Notification(
                timestamp = System.currentTimeMillis(),
                title = "Order Status Update",
                message = "Your order ${orderId} is now: ${status}."
            )
        )
    }

    suspend fun addNotification(title: String, message: String) {
        db.notificationDao().insertNotification(
            Notification(
                timestamp = System.currentTimeMillis(),
                title = title,
                message = message
            )
        )
    }

    suspend fun markNotificationsAsRead() = db.notificationDao().markAllAsRead()
}
