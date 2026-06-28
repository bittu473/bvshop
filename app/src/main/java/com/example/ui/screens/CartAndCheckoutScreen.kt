package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.data.CartProduct
import com.example.data.UserAddress
import com.example.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    viewModel: ShopViewModel,
    onNavigateToTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val cartProductsList by viewModel.cartProducts.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val addressesList by viewModel.addresses.collectAsState()

    // Coupon states
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponDiscount by viewModel.couponDiscount.collectAsState()
    var couponInput by remember { mutableStateOf("") }
    var couponFeedback by remember { mutableStateOf("") }

    // Checkout navigation steps: 0 = Cart, 1 = Address Select, 2 = Payment Select, 3 = Confirmation
    var checkoutStep by remember { mutableStateOf(0) }

    // Selected values
    var selectedShippingAddress by remember { mutableStateOf<UserAddress?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf("Wallet") } // Default to local wallet

    // Address form state
    var isAddingAddress by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }
    var addPhone by remember { mutableStateOf("") }
    var addStreet by remember { mutableStateOf("") }
    var addCity by remember { mutableStateOf("Ramgarh (Gola)") }
    var addState by remember { mutableStateOf("Jharkhand") }
    var addZip by remember { mutableStateOf("") }

    // Detailed Indian Address Fields
    var addFirstName by remember { mutableStateOf("") }
    var addMiddleName by remember { mutableStateOf("") }
    var addLastName by remember { mutableStateOf("") }
    var addAddressLine1 by remember { mutableStateOf("") }
    var addAddressLine2 by remember { mutableStateOf("") }
    var addLandmark by remember { mutableStateOf("") }
    var addPoliceStation by remember { mutableStateOf("") }
    var addPostOffice by remember { mutableStateOf("") }
    var addDistrict by remember { mutableStateOf("Ramgarh (Gola)") }
    var addPinCode by remember { mutableStateOf("") }
    var addSign by remember { mutableStateOf("") }

    var isCheckoutStateExpanded by remember { mutableStateOf(false) }
    var isCheckoutDistrictExpanded by remember { mutableStateOf(false) }

    // On setup, choose default shipping address if available
    LaunchedEffect(addressesList) {
        if (selectedShippingAddress == null && addressesList.isNotEmpty()) {
            selectedShippingAddress = addressesList.firstOrNull { it.isDefault } ?: addressesList.first()
        }
    }

    // Calculations
    val subtotal = cartProductsList.fold(0.0) { sum, cp -> sum + (cp.product.price * cp.cartItem.quantity) }
    val discount = if (appliedCoupon == "LUX20") {
        subtotal * 0.20
    } else if (appliedCoupon == "WELCOME50") {
        50.0
    } else {
        0.0
    }
    val finalTotal = (subtotal - discount).coerceAtLeast(0.0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (checkoutStep) {
                            0 -> Localizer.translate("cart", selectedLang)
                            1 -> "Select Shipping Address"
                            2 -> "Select Payment Mode"
                            else -> "Confirm Premium Order"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (checkoutStep > 0) {
                        IconButton(onClick = { checkoutStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
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
            // checkout flow indicator progress bar
            if (checkoutStep > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 10.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckoutStepPill(step = 1, title = "Address", active = checkoutStep >= 1)
                    Divider(modifier = Modifier.width(30.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    CheckoutStepPill(step = 2, title = "Payment", active = checkoutStep >= 2)
                    Divider(modifier = Modifier.width(30.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    CheckoutStepPill(step = 3, title = "Review", active = checkoutStep >= 3)
                }
            }

            // Step Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (checkoutStep) {
                    0 -> {
                        // Cart View List
                        if (cartProductsList.isEmpty()) {
                            EmptyCartView(selectedLang)
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(10.dp)) }

                                // Items list mapper
                                items(cartProductsList) { cartProd ->
                                    CartItemRow(
                                        cartProd = cartProd,
                                        onIncrement = { viewModel.incrementCartQty(cartProd.cartItem) },
                                        onDecrement = { viewModel.decrementCartQty(cartProd.cartItem) },
                                        onRemove = { viewModel.removeCartItem(cartProd.cartItem) }
                                    )
                                }

                                // Coupon Application Panel
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Apply Premium Coupons", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = couponInput,
                                            onValueChange = { couponInput = it },
                                            placeholder = { Text("Code e.g. LUX20, WELCOME50", fontSize = 12.sp) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("coupon_input"),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true
                                        )

                                        Button(
                                            onClick = {
                                                couponFeedback = viewModel.applyCoupon(couponInput)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("apply_coupon_button")
                                        ) {
                                            Text("Apply", fontSize = 12.sp)
                                        }
                                    }

                                    if (couponFeedback.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = couponFeedback,
                                            color = if (couponFeedback.contains("Success")) Color.Green else MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (appliedCoupon != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        ChipCouponApplied(code = appliedCoupon!!, onRemove = {
                                            viewModel.removeCoupon()
                                            couponFeedback = ""
                                        })
                                    }
                                }

                                // Order pricing breakdown
                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Subtotal", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text("$${String.format("%.2f", subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            if (discount > 0) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                    Text("Deduction (Coupon)", fontSize = 13.sp, color = Color.Green)
                                                    Text("-$${String.format("%.2f", discount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Estimated Shipping", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                Text("FREE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text("Total Price", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                                Text("$${String.format("%.2f", finalTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }

                                // Next checkout CTA button
                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { checkoutStep = 1 },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("proceed_checkout_button")
                                    ) {
                                        Text("Proceed to Checkout", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Address Select Step
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Select Shipping Destination", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (!isAddingAddress) {
                                    TextButton(onClick = { isAddingAddress = true }, modifier = Modifier.testTag("add_address_button")) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add New", fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isAddingAddress) {
                                // Add address form input block (Scrollable Container inside checkout column)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 420.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Contact Identity Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = addFirstName,
                                            onValueChange = { addFirstName = it },
                                            label = { Text("First Name*") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = addMiddleName,
                                            onValueChange = { addMiddleName = it },
                                            label = { Text("Middle Name") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                    OutlinedTextField(
                                        value = addLastName,
                                        onValueChange = { addLastName = it },
                                        label = { Text("Last Name*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = addPhone,
                                        onValueChange = { addPhone = it },
                                        label = { Text("Contact Phone Mobile*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Delivery Address Coordinates", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)

                                    OutlinedTextField(
                                        value = addAddressLine1,
                                        onValueChange = { addAddressLine1 = it },
                                        label = { Text("Address Line 1*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = addAddressLine2,
                                        onValueChange = { addAddressLine2 = it },
                                        label = { Text("Address Line 2") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    OutlinedTextField(
                                        value = addLandmark,
                                        onValueChange = { addLandmark = it },
                                        label = { Text("Landmark*") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = addPoliceStation,
                                            onValueChange = { addPoliceStation = it },
                                            label = { Text("Police Station*") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = addPostOffice,
                                            onValueChange = { addPostOffice = it },
                                            label = { Text("Post Office*") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    // Dynamic State Dropdown
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { isCheckoutStateExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("State: $addState", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = isCheckoutStateExpanded,
                                            onDismissRequest = { isCheckoutStateExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.75f).heightIn(max = 280.dp)
                                        ) {
                                            IndianLocationData.statesAndDistricts.keys.sorted().forEach { stateName ->
                                                DropdownMenuItem(
                                                    text = { Text(stateName, fontSize = 13.sp) },
                                                    onClick = {
                                                        addState = stateName
                                                        addDistrict = IndianLocationData.statesAndDistricts[stateName]?.firstOrNull() ?: ""
                                                        isCheckoutStateExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Dynamic District Dropdown
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { isCheckoutDistrictExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("District: $addDistrict", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = isCheckoutDistrictExpanded,
                                            onDismissRequest = { isCheckoutDistrictExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.75f).heightIn(max = 280.dp)
                                        ) {
                                            val districts = IndianLocationData.statesAndDistricts[addState] ?: emptyList()
                                            districts.forEach { distName ->
                                                DropdownMenuItem(
                                                    text = { Text(distName, fontSize = 13.sp) },
                                                    onClick = {
                                                        addDistrict = distName
                                                        isCheckoutDistrictExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = addPinCode,
                                        onValueChange = { addPinCode = it },
                                        label = { Text("PIN Code*") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = addSign,
                                        onValueChange = { addSign = it },
                                        label = { Text("Authorized digital Signature*") },
                                        placeholder = { Text("E.g. Aria Sterling", fontSize = 12.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { isAddingAddress = false },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel")
                                        }

                                        Button(
                                            onClick = {
                                                if (addFirstName.isNotBlank() && addLastName.isNotBlank() && addAddressLine1.isNotBlank() && addLandmark.isNotBlank() && addPoliceStation.isNotBlank() && addPostOffice.isNotBlank() && addPinCode.isNotBlank() && addSign.isNotBlank()) {
                                                    val fullName = "$addFirstName ${if (addMiddleName.isNotEmpty()) "$addMiddleName " else ""}$addLastName"
                                                    val combinedStreet = "$addAddressLine1, $addAddressLine2"
                                                    viewModel.saveAddress(
                                                        name = fullName,
                                                        phone = addPhone,
                                                        street = combinedStreet,
                                                        city = addDistrict,
                                                        state = addState,
                                                        zip = addPinCode,
                                                        isDefault = addressesList.isEmpty(),
                                                        firstName = addFirstName,
                                                        middleName = addMiddleName,
                                                        lastName = addLastName,
                                                        addressLine1 = addAddressLine1,
                                                        addressLine2 = addAddressLine2,
                                                        landmark = addLandmark,
                                                        policeStation = addPoliceStation,
                                                        postOffice = addPostOffice,
                                                        district = addDistrict,
                                                        pinCode = addPinCode,
                                                        sign = addSign
                                                    )
                                                    isAddingAddress = false
                                                    // Reset fields
                                                    addFirstName = ""; addMiddleName = ""; addLastName = ""; addPhone = ""
                                                    addAddressLine1 = ""; addAddressLine2 = ""; addLandmark = ""
                                                    addPoliceStation = ""; addPostOffice = ""; addPinCode = ""; addSign = ""
                                                } else {
                                                    viewModel.addLocalNotification("Form Error", "Please fill in all required Indian address fields (*)")
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f).testTag("save_address_btn")
                                        ) {
                                            Text("Save Address")
                                        }
                                    }
                                }
                            } else {
                                // Address List Mapper
                                if (addressesList.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(40.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("No saved destinations yet. Please add a contact address to proceed.", fontSize = 12.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(addressesList) { addr ->
                                            val isSelected = selectedShippingAddress?.id == addr.id
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedShippingAddress = addr },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                ),
                                                border = BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            ) {
                                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    RadioButton(selected = isSelected, onClick = { selectedShippingAddress = addr })
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(addr.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        if (addr.firstName.isNotEmpty()) {
                                                            Text(
                                                                text = "${addr.addressLine1}${if (addr.addressLine2.isNotEmpty()) ", ${addr.addressLine2}" else ""}\n" +
                                                                        "Landmark: ${addr.landmark}\n" +
                                                                        "P.S.: ${addr.policeStation}, P.O.: ${addr.postOffice}\n" +
                                                                        "District: ${addr.district}, State: ${addr.state} - ${addr.pinCode}\n" +
                                                                        "Signature: ${addr.sign}",
                                                                fontSize = 11.sp,
                                                                lineHeight = 15.sp,
                                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                            )
                                                        } else {
                                                            Text("${addr.street}, ${addr.city}, ${addr.state} - ${addr.zipCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                                        }
                                                        Text("Phone: ${addr.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                                    }
                                                }
                                            }
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = { checkoutStep = 2 },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("proceed_to_payment_btn"),
                                                enabled = selectedShippingAddress != null
                                            ) {
                                                Text("Proceed to Payment Mode")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Payment select step
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Select Payment Mechanism", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            val paymentModes = listOf(
                                "Wallet" to "ShopNest Secure Local Wallet",
                                "UPI" to "UPI Gateway (Instant GPay, PhonePe)",
                                "Card" to "Credit or Debit Card Gateway",
                                "COD" to "Cash on Delivery"
                            )

                            paymentModes.forEach { (code, label) ->
                                val isSelected = selectedPaymentMethod == code
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPaymentMethod = code },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    ),
                                    border = BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isSelected, onClick = { selectedPaymentMethod = code })
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(code, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }

                            // Show Local Wallet Info
                            if (selectedPaymentMethod == "Wallet") {
                                val currentBal = profile?.walletBalance ?: 0.0
                                val balanceShortage = finalTotal > currentBal

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (balanceShortage) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("Wallet Balance: $${String.format("%.2f", currentBal)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        if (balanceShortage) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Insufficient balance! Please add funds in your Profile page first (recharges trigger cashback bonus!). Or choose Cash on Delivery.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        } else {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Sufficient funds available. Checkout will credit cashback rewards dynamically if coupons are active.", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { checkoutStep = 3 },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("proceed_to_review_btn"),
                                enabled = selectedPaymentMethod != "Wallet" || (profile?.walletBalance ?: 0.0) >= finalTotal
                            ) {
                                Text("Review and Order")
                            }
                        }
                    }

                    3 -> {
                        // Summary Review Page
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Confirm Premium Checkout Details", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Order Recap", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Total Items", fontSize = 12.sp)
                                        Text("${cartProductsList.sumOf { it.cartItem.quantity }} Items", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Final Billing", fontSize = 12.sp)
                                        Text("$${String.format("%.2f", finalTotal)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(selectedShippingAddress?.name ?: "", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${selectedShippingAddress?.street}, ${selectedShippingAddress?.city} - ${selectedShippingAddress?.zipCode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Payment Preference", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Selected: $selectedPaymentMethod", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    viewModel.placeOrder(
                                        address = selectedShippingAddress!!,
                                        paymentMethod = selectedPaymentMethod,
                                        onComplete = { success ->
                                            if (success) {
                                                checkoutStep = 0
                                                onNavigateToTracking()
                                            }
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_order_button")
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pay & Confirmed Order", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutStepPill(step: Int, title: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(step.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(title, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun EmptyCartView(selectedLang: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Cart is Empty", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Discover premium collections in the main dashboard and add luxury accessories to get started.", fontSize = 12.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun CartItemRow(
    cartProd: CartProduct,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Determine image res
            val imageResId = when (cartProd.product.imageResName) {
                "img_hero_banner" -> R.drawable.img_hero_banner
                "img_onboarding_bg" -> R.drawable.img_onboarding_bg
                else -> R.drawable.img_shop_logo
            }

            Image(
                painter = painterResource(id = imageResId),
                contentDescription = cartProd.product.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(cartProd.product.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                Text("Style: ${cartProd.cartItem.selectedColor} (${cartProd.cartItem.selectedSize})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))
                Text("$${cartProd.product.price.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            // Increment/Decrement state controllers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(12.dp))
                }
                Text(cartProd.cartItem.quantity.toString(), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onIncrement, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp).testTag("delete_cart_item_${cartProd.product.id}")) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun ChipCouponApplied(code: String, onRemove: () -> Unit) {
    Surface(
        color = Color.Green.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, Color.Green.copy(alpha = 0.4f)),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color.Green, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Coupon applied: $code", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Green)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.Green,
                modifier = Modifier
                    .size(12.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}
