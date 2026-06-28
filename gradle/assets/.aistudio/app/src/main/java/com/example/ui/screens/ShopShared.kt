package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.ui.viewmodel.ShopViewModel

object Localizer {
    fun translate(key: String, lang: String): String {
        val en = mapOf(
            "app_title" to "ShopNest",
            "tagline" to "Elevate Your Lifestyle",
            "search_placeholder" to "Search luxury catalog...",
            "categories" to "Categories",
            "flash_sale" to "FLASH SALE",
            "best_sellers" to "Best Sellers",
            "new_arrivals" to "New Arrivals",
            "wallet_balance" to "Wallet Balance",
            "add_to_cart" to "Add to Cart",
            "wishlist" to "Wishlist",
            "cart" to "Shopping Cart",
            "profile" to "My Profile",
            "checkout" to "Checkout",
            "place_order" to "Place Order",
            "seller_panel" to "Seller Portal",
            "admin_panel" to "Admin Control",
            "ai_assistant" to "Concierge AI",
            "orders" to "My Orders",
            "address_mgmt" to "Address Manager",
            "language" to "Language",
            "dark_mode" to "Dark Mode",
            "recharge" to "Add Funds",
            "order_tracking" to "Track Order",
            "out_of_stock" to "Out of Stock",
            "price" to "Price"
        )

        val hi = mapOf(
            "app_title" to "शॉपनेस्ट",
            "tagline" to "अपनी जीवन शैली को ऊपर उठाएं",
            "search_placeholder" to "लक्जरी कैटलॉग खोजें...",
            "categories" to "श्रेणियां",
            "flash_sale" to "फ्लैश सेल",
            "best_sellers" to "बेस्ट सेलर्स",
            "new_arrivals" to "नए आगमन",
            "wallet_balance" to "वॉलेट बैलेंस",
            "add_to_cart" to "कार्ट में जोड़ें",
            "wishlist" to "इच्छा-सूची",
            "cart" to "शॉपिंग कार्ट",
            "profile" to "मेरी प्रोफ़ाइल",
            "checkout" to "चेकआउट",
            "place_order" to "ऑर्डर करें",
            "seller_panel" to "विक्रेता पोर्टल",
            "admin_panel" to "एडमिन कंट्रोल",
            "ai_assistant" to "एआई सहायक",
            "orders" to "मेरे ऑर्डर",
            "address_mgmt" to "पता प्रबंधक",
            "language" to "भाषा",
            "dark_mode" to "डार्क मोड",
            "recharge" to "पैसे जोड़ें",
            "order_tracking" to "ऑर्डर ट्रैकिंग",
            "out_of_stock" to "स्टॉक समाप्त",
            "price" to "कीमत"
        )

        val es = mapOf(
            "app_title" to "ShopNest",
            "tagline" to "Eleve su estilo de vida",
            "search_placeholder" to "Buscar catálogo de lujo...",
            "categories" to "Categorías",
            "flash_sale" to "VENTA FLASH",
            "best_sellers" to "Los Más Vendidos",
            "new_arrivals" to "Novedades",
            "wallet_balance" to "Saldo de Cartera",
            "add_to_cart" to "Añadir al Carrito",
            "wishlist" to "Favoritos",
            "cart" to "Carrito",
            "profile" to "Mi Perfil",
            "checkout" to "Pagar",
            "place_order" to "Realizar Pedido",
            "seller_panel" to "Portal del Vendedor",
            "admin_panel" to "Control de Admin",
            "ai_assistant" to "Conserje IA",
            "orders" to "Mis Pedidos",
            "address_mgmt" to "Mis Direcciones",
            "language" to "Idioma",
            "dark_mode" to "Modo Oscuro",
            "recharge" to "Añadir Fondos",
            "order_tracking" to "Rastrear Pedido",
            "out_of_stock" to "Agotado",
            "price" to "Precio"
        )

        val selectedMap = when (lang) {
            "hi" -> hi
            "es" -> es
            else -> en
        }

        return selectedMap[key] ?: en[key] ?: key
    }
}
