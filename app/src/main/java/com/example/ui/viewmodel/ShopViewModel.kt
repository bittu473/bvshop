package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopRepository

    init {
        val database = ShopDatabase.getDatabase(application)
        repository = ShopRepository(database)
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- State Observables ---
    val products: StateFlow<List<Product>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addresses: StateFlow<List<UserAddress>> = repository.addresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val walletTransactions: StateFlow<List<WalletTransaction>> = repository.walletTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<Notification>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combine Cart items with Products reactively
    val cartProducts: StateFlow<List<CartProduct>> = combine(repository.cartItems, repository.products) { cartItems, products ->
        cartItems.mapNotNull { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            if (product != null) CartProduct(cartItem, product) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication State ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // --- Active Selection State ---
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    // --- Checkout Helper States ---
    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    private val _couponDiscount = MutableStateFlow(0.0)
    val couponDiscount: StateFlow<Double> = _couponDiscount.asStateFlow()

    // --- App Settings (Dark Mode & Language) ---
    private val _isDarkMode = MutableStateFlow(true) // Premium Obsidian default
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("en") // "en", "hi", "es"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // --- AI/Gemini States ---
    private val _isGeminiLoading = MutableStateFlow(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    private val _geminiRecommendation = MutableStateFlow<String?>(null)
    val geminiRecommendation: StateFlow<String?> = _geminiRecommendation.asStateFlow()

    private val _geminiChat = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("Hello! I am your ShopNest Concierge. Ask me for recommendations, styling tips, or any product questions!" to false)
    )
    val geminiChat: StateFlow<List<Pair<String, Boolean>>> = _geminiChat.asStateFlow()

    // --- Authentication Actions ---
    fun loginWithEmail(email: String, pin: String) {
        if (email.contains("@") && pin.length >= 4) {
            _isLoggedIn.value = true
            _authError.value = null
        } else {
            _authError.value = "Please enter a valid email and 4-digit PIN."
        }
    }

    fun loginWithPhone(phone: String, otp: String) {
        if (phone.length >= 10 && otp.length >= 4) {
            _isLoggedIn.value = true
            _authError.value = null
        } else {
            _authError.value = "Please enter a valid 10-digit phone number and 4-digit OTP."
        }
    }

    fun loginWithSocial(platform: String) {
        _isLoggedIn.value = true
        _authError.value = null
        viewModelScope.launch {
            repository.addNotification(
                title = "Signed in with $platform",
                message = "Welcome back, Aria! Successfully authenticated via secure $platform login."
            )
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _appliedCoupon.value = null
        _couponDiscount.value = 0.0
    }

    // --- Product / Catalog Actions ---
    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _geminiRecommendation.value = null // reset recommendations for the new product
    }

    fun selectOrder(order: Order) {
        _selectedOrder.value = order
    }

    fun insertProductBySeller(
        title: String,
        desc: String,
        price: Double,
        category: String,
        sizes: String,
        colors: String,
        stock: Int,
        imageResName: String = "img_shop_logo"
    ) {
        viewModelScope.launch {
            val sellerName = userProfile.value?.name ?: "Seller Team"
            val newProd = Product(
                id = "sel_" + System.currentTimeMillis().toString().takeLast(6),
                title = title,
                description = desc,
                price = price,
                oldPrice = price * 1.25,
                rating = 5.0f,
                category = category,
                imageResName = imageResName,
                sizeVariants = sizes.ifEmpty { "One Size" },
                colorVariants = colors.ifEmpty { "Default" },
                stock = stock,
                reviewCount = 0,
                sellerName = sellerName,
                isApproved = false // Pending admin approval!
            )
            repository.insertProduct(newProd)
            repository.addNotification(
                title = "Product Submitted",
                message = "Your product '${title}' was successfully submitted and is awaiting Admin review."
            )
        }
    }

    fun approveProductByAdmin(productId: String) {
        viewModelScope.launch {
            val prodList = products.value
            val match = prodList.find { it.id == productId }
            if (match != null) {
                val approvedProd = match.copy(isApproved = true)
                repository.insertProduct(approvedProd)
                repository.addNotification(
                    title = "Product Approved",
                    message = "Admin approved seller product: '${match.title}'"
                )
            }
        }
    }

    // --- Cart Actions ---
    fun addToCart(product: Product, size: String, color: String) {
        viewModelScope.launch {
            val existing = cartProducts.value.find { 
                it.cartItem.productId == product.id && 
                it.cartItem.selectedSize == size && 
                it.cartItem.selectedColor == color 
            }
            if (existing != null) {
                val updatedItem = existing.cartItem.copy(quantity = existing.cartItem.quantity + 1)
                repository.updateCartItem(updatedItem)
            } else {
                repository.insertCartItem(CartItem(productId = product.id, quantity = 1, selectedSize = size, selectedColor = color))
            }
        }
    }

    fun incrementCartQty(cartItem: CartItem) {
        viewModelScope.launch {
            repository.updateCartItem(cartItem.copy(quantity = cartItem.quantity + 1))
        }
    }

    fun decrementCartQty(cartItem: CartItem) {
        viewModelScope.launch {
            if (cartItem.quantity > 1) {
                repository.updateCartItem(cartItem.copy(quantity = cartItem.quantity - 1))
            } else {
                repository.deleteCartItem(cartItem)
            }
        }
    }

    fun removeCartItem(cartItem: CartItem) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItem)
        }
    }

    // --- Wishlist Actions ---
    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // --- Address Actions ---
    fun saveAddress(
        name: String,
        phone: String,
        street: String,
        city: String,
        state: String,
        zip: String,
        isDefault: Boolean,
        firstName: String = "",
        middleName: String = "",
        lastName: String = "",
        addressLine1: String = "",
        addressLine2: String = "",
        landmark: String = "",
        policeStation: String = "",
        postOffice: String = "",
        district: String = "",
        pinCode: String = "",
        sign: String = ""
    ) {
        viewModelScope.launch {
            repository.insertAddress(
                UserAddress(
                    name = name,
                    phone = phone,
                    street = street,
                    city = city,
                    state = state,
                    zipCode = zip,
                    isDefault = isDefault,
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    addressLine1 = addressLine1,
                    addressLine2 = addressLine2,
                    landmark = landmark,
                    policeStation = policeStation,
                    postOffice = postOffice,
                    district = district,
                    pinCode = pinCode,
                    sign = sign
                )
            )
        }
    }

    fun deleteAddress(address: UserAddress) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    // --- Profile Actions ---
    fun setSellerRole(isSeller: Boolean) {
        viewModelScope.launch {
            repository.setSellerStatus(isSeller)
        }
    }

    fun setAdminRole(isAdmin: Boolean) {
        viewModelScope.launch {
            repository.setAdminStatus(isAdmin)
        }
    }

    fun rechargeWallet(amount: Double, source: String) {
        viewModelScope.launch {
            repository.addWalletBalance(amount, source)
        }
    }

    // --- Coupon Management ---
    fun applyCoupon(code: String): String {
        val uppercaseCode = code.uppercase()
        return when (uppercaseCode) {
            "LUX20" -> {
                _appliedCoupon.value = "LUX20"
                _couponDiscount.value = 20.0 // 20% off
                "Success: Coupon 'LUX20' applied! 20% Discount with 10% Wallet Cashback on checkout."
            }
            "WELCOME50" -> {
                _appliedCoupon.value = "WELCOME50"
                _couponDiscount.value = 50.0 // Flat $50 off
                "Success: Coupon 'WELCOME50' applied! Flat $50.00 off on your luxury order."
            }
            else -> {
                _appliedCoupon.value = null
                _couponDiscount.value = 0.0
                "Error: Coupon code '$code' is invalid or expired."
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponDiscount.value = 0.0
    }

    // --- Order / Checkout Actions ---
    fun placeOrder(address: UserAddress, paymentMethod: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val items = cartProducts.value
            if (items.isEmpty()) {
                onComplete(false)
                return@launch
            }

            // Calculate Subtotal and Total Price
            var subtotal = 0.0
            for (item in items) {
                subtotal += item.product.price * item.cartItem.quantity
            }

            val discount = if (_appliedCoupon.value == "LUX20") {
                subtotal * 0.20
            } else if (_appliedCoupon.value == "WELCOME50") {
                50.0
            } else {
                0.0
            }

            val finalTotal = (subtotal - discount).coerceAtLeast(0.0)

            val success = repository.placeOrder(
                items = items,
                totalPrice = finalTotal,
                address = address,
                paymentMethod = paymentMethod,
                couponApplied = _appliedCoupon.value
            )

            if (success) {
                _appliedCoupon.value = null
                _couponDiscount.value = 0.0
            }
            onComplete(success)
        }
    }

    fun updateOrderStatusByAdmin(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            // Refresh detail view if current order is viewed
            if (_selectedOrder.value?.id == orderId) {
                val ordersList = orders.value
                val updated = ordersList.find { it.id == orderId }
                if (updated != null) {
                    _selectedOrder.value = updated
                }
            }
        }
    }

    fun addLocalNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.addNotification(title, message)
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsAsRead()
        }
    }

    // --- UI State Toggles ---
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun changeLanguage(langCode: String) {
        _selectedLanguage.value = langCode
    }

    // --- AI/Gemini Actions ---
    fun fetchGeminiProductRecommendations(product: Product) {
        viewModelScope.launch {
            _isGeminiLoading.value = true
            val prompt = "Recommend 3 frequently bought together or highly compatible fashion/electronic accessories that perfectly pair with this premium product: '${product.title}' (Category: ${product.category}). Detail why they go well together in bullet points."
            val response = GeminiService.queryGemini(prompt, "You are a professional luxury fashion consultant and AI shopping matching concierge. Speak elegantly and concisely.")
            _geminiRecommendation.value = response
            _isGeminiLoading.value = false
        }
    }

    fun sendChatMessageToGemini(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            val currentChat = _geminiChat.value.toMutableList()
            currentChat.add(message to true)
            _geminiChat.value = currentChat

            _isGeminiLoading.value = true
            
            // Generate conversation history context for prompt
            val contextPrompt = StringBuilder()
            contextPrompt.append("Conversation history between customer and ShopNest AI Assistant:\n")
            currentChat.takeLast(6).forEach { (msg, isUser) ->
                if (isUser) contextPrompt.append("Customer: $msg\n")
                else contextPrompt.append("AI Concierge: $msg\n")
            }
            contextPrompt.append("\nRespond to the customer's latest request elegantly: '$message'. Suggest top products if relevant (e.g., Cosmic Gold Chronograph, Symphony ANC Headphones, Elite Stratus Sneaker, Velvet Crimson Handbag, Oud Perfume) to keep them engaged.")

            val response = GeminiService.queryGemini(
                contextPrompt.toString(),
                "You are an elegant, customer-obsessed personal stylist and AI shopping assistant for ShopNest, a premium boutique. Address the customer as 'guest' or 'valuable shopper'. Be refined, helpful, and highly sophisticated. Recommend our actual products if they fit the discussion."
            )
            
            val updatedChat = _geminiChat.value.toMutableList()
            updatedChat.add(response to false)
            _geminiChat.value = updatedChat
            _isGeminiLoading.value = false
        }
    }

    fun clearChat() {
        _geminiChat.value = listOf("Hello! I am your ShopNest Concierge. Ask me for recommendations, styling tips, or any product questions!" to false)
    }
}
