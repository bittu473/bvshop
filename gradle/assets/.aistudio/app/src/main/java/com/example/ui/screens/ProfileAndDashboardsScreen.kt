package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Order
import com.example.data.Product
import com.example.ui.viewmodel.ShopViewModel
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndDashboardsScreen(
    viewModel: ShopViewModel,
    activeSubView: String, // "PROFILE", "TRACKING", "ASSISTANT"
    onNavigateBackToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val ordersList by viewModel.orders.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()

    // Assistant States
    val chatMessages by viewModel.geminiChat.collectAsState()
    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()
    var chatInput by remember { mutableStateOf("") }

    // Scroll state
    val scrollState = rememberScrollState()

    // Recharging wallet state
    var rechargeAmount by remember { mutableStateOf("") }
    var isRecharging by remember { mutableStateOf(false) }

    // Seller fields state
    var isRegisteringSeller by remember { mutableStateOf(false) }
    var sellTitle by remember { mutableStateOf("") }
    var sellDesc by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var sellCategory by remember { mutableStateOf("Luxury") }
    var sellStock by remember { mutableStateOf("") }

    // Admin state
    val allProducts by viewModel.products.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (activeSubView) {
            "TRACKING" -> {
                // LIVE ORDER TRACKING TIMELINE VIEW
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Live Shipment Progression", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBackToProfile) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    if (selectedOrder == null) {
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                            Text("Select an order from your profile history to track delivery.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    } else {
                        val activeOrder = selectedOrder!!
                        val trackingLogs = remember(activeOrder.trackingLogsJson) {
                            val list = mutableListOf<Triple<String, Long, String>>()
                            try {
                                val arr = JSONArray(activeOrder.trackingLogsJson)
                                for (i in 0 until arr.length()) {
                                    val obj = arr.getJSONObject(i)
                                    list.add(Triple(obj.getString("status"), obj.getLong("timestamp"), obj.optString("desc", "")))
                                }
                            } catch (e: Exception) {
                                // fallback
                            }
                            list
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Order ID: ${activeOrder.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Transit Status: ${activeOrder.status}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("Total Billing: $${String.format("%.2f", activeOrder.totalPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            Text("Delivery Progress Timeline", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            // Progression status list
                            val stages = listOf("Processing", "Confirmed", "Shipped", "Out for Delivery", "Delivered")
                            val currentStageIndex = stages.indexOf(activeOrder.status)

                            stages.forEachIndexed { idx, stage ->
                                val isDone = idx <= currentStageIndex
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        } else {
                                            Text((idx + 1).toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDone) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                                        val matchLog = trackingLogs.find { it.first == stage || (stage == "Processing" && it.first == "Order Placed") }
                                        if (matchLog != null) {
                                            Text(matchLog.third, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        } else {
                                            Text("Awaiting dispatch node validation.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ASSISTANT" -> {
                // PREMIUM FLOATING AI CONCIERGE CHAT WINDOW
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("ShopNest Concierge AI", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBackToProfile) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                TextButton(onClick = { viewModel.clearChat() }) {
                                    Text("Clear", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Quick Voice Search & Barcode Scan Shortcuts Panel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.addLocalNotification("Voice Search Simulation", "Aria searched 'headphones' via voice assistance.")
                                    viewModel.sendChatMessageToGemini("Show me headphones in the catalog")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.addLocalNotification("Barcode Scanned", "ShopNest simulated barcode match: Oud Perfume.")
                                    viewModel.sendChatMessageToGemini("Scan barcode: Oud Eclipse Perfume")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Barcode Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Chat Thread List
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(chatMessages) { (text, isUser) ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.82f)
                                    ) {
                                        Text(
                                            text = text,
                                            fontSize = 12.sp,
                                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            if (isGeminiLoading) {
                                item {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Concierge AI is thinking...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Input bottom action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Ask Concierge AI...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("assistant_chat_input"),
                                shape = RoundedCornerShape(50.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendChatMessageToGemini(chatInput)
                                        chatInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("assistant_send_button")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            else -> {
                // DEFAULT PROFILE SETTINGS & DASHBOARDS EXPANSION SCREEN
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Profile Header card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(profile?.name ?: "Aria Sterling", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(profile?.email ?: "aria.sterling@luxcart.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(profile?.phone ?: "+1 (555) 019-2831", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }

                    // Settings: Language & Theme
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Application Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Language Choice", fontSize = 12.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("en" to "EN", "hi" to "HI", "es" to "ES").forEach { (code, label) ->
                                        val isSel = selectedLang == code
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { viewModel.changeLanguage(code) }
                                        ) {
                                            Text(label, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("App Dark Mode", fontSize = 12.sp)
                                Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleTheme() }, modifier = Modifier.testTag("dark_mode_switch"))
                            }
                        }
                    }

                    // WALLET RECHARGE AND LEDGER TRANSACTIONS SECTION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("ShopNest Private Wallet", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("$${String.format("%.2f", profile?.walletBalance ?: 0.0)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }

                                Button(
                                    onClick = { isRecharging = !isRecharging },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Funds", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            AnimatedVisibility(visible = isRecharging) {
                                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = rechargeAmount,
                                        onValueChange = { rechargeAmount = it },
                                        label = { Text("Recharge Amount ($)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().testTag("wallet_recharge_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Button(
                                        onClick = {
                                            val amt = rechargeAmount.toDoubleOrNull()
                                            if (amt != null && amt > 0) {
                                                viewModel.rechargeWallet(amt, "Simulated Premium Visa/UPI")
                                                isRecharging = false
                                                rechargeAmount = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("confirm_recharge_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Authorize Simulated Recharge")
                                    }
                                }
                            }
                        }
                    }

                    // Order history logs
                    Text("Order History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (ordersList.isEmpty()) {
                        Text("No order records found in historical logs.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    } else {
                        ordersList.forEach { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectOrder(order)
                                        // Set active navigation target in parent main thread
                                        viewModel.addLocalNotification("Order Selected", "Viewing status timeline for order ${order.id}")
                                        // To proceed to timeline log view, the main activity handles switching the state.
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Order ID: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Status: ${order.status}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Billing: $${String.format("%.2f", order.totalPrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // SELLER PANEL PORTAL
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Register as Seller", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(checked = profile?.isSeller == true, onCheckedChange = { viewModel.setSellerRole(it) }, modifier = Modifier.testTag("seller_mode_switch"))
                    }

                    AnimatedVisibility(visible = profile?.isSeller == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Seller Dashboard", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Upload Luxury Inventory Item", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    OutlinedTextField(value = sellTitle, onValueChange = { sellTitle = it }, label = { Text("Product Title") }, modifier = Modifier.fillMaxWidth().testTag("seller_title_input"))
                                    OutlinedTextField(value = sellDesc, onValueChange = { sellDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = sellPrice, onValueChange = { sellPrice = it }, label = { Text("Price ($)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = sellStock, onValueChange = { sellStock = it }, label = { Text("Stock Qty") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    }

                                    Button(
                                        onClick = {
                                            val pr = sellPrice.toDoubleOrNull() ?: 0.0
                                            val st = sellStock.toIntOrNull() ?: 0
                                            if (sellTitle.isNotBlank() && pr > 0 && st > 0) {
                                                viewModel.insertProductBySeller(sellTitle, sellDesc, pr, sellCategory, "One Size", "Classic Black", st)
                                                sellTitle = ""; sellDesc = ""; sellPrice = ""; sellStock = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("seller_upload_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Submit to Admin approval")
                                    }
                                }
                            }
                        }
                    }

                    // ADMIN PANEL PORTAL
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Admin Controls", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(checked = profile?.isAdmin == true, onCheckedChange = { viewModel.setAdminRole(it) }, modifier = Modifier.testTag("admin_mode_switch"))
                    }

                    AnimatedVisibility(visible = profile?.isAdmin == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Admin Verification Console", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)

                            // List unapproved products
                            val pendingProducts = allProducts.filter { !it.isApproved }
                            if (pendingProducts.isEmpty()) {
                                Text("No pending seller inventory uploads.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            } else {
                                Text("Awaiting Approval:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                pendingProducts.forEach { prod ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Seller: ${prod.sellerName} | Price: $${prod.price}", fontSize = 11.sp)
                                            }
                                            Button(
                                                onClick = { viewModel.approveProductByAdmin(prod.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.White),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Approve", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Order overrides (allow admin to change order statuses dynamically)
                            if (ordersList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Transit Overrides (Change active orders status):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                ordersList.forEach { o ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("OrderID: ${o.id} | Current Node: ${o.status}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                listOf("Confirmed", "Shipped", "Out for Delivery", "Delivered").forEach { node ->
                                                    TextButton(
                                                        onClick = { viewModel.updateOrderStatusByAdmin(o.id, node) },
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(node, fontSize = 9.sp, fontWeight = if (o.status == node) FontWeight.Bold else FontWeight.Normal)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
