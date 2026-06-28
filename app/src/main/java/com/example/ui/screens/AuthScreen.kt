package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun BVPremiumLogo(modifier: Modifier = Modifier, showBotIcon: Boolean = true, onBotClick: (() -> Unit)? = null) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Gola (Circle Logo)
        Surface(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(3.dp, Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700))))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E1E1E),
                                Color(0xFF0F0F0F)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = "BV SHOP",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "PVT LTD",
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        letterSpacing = 0.5.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "GOLA",
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Help Chat Bot Badge Icon ("ME HELF CHAT BOT")
        if (showBotIcon) {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .clickable { onBotClick?.invoke() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.5.dp, Color(0xFFFFD700))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Help Chat Bot",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AuthScreen(viewModel: ShopViewModel, modifier: Modifier = Modifier) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val chatMessages by viewModel.geminiChat.collectAsState()
    val isGeminiLoading by viewModel.isGeminiLoading.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Email, 1 = Phone OTP, 2 = Signup
    var emailInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var forgotPasswordRequested by remember { mutableStateOf(false) }

    // Signup form state fields
    var regFirstName by remember { mutableStateOf("") }
    var regMiddleName by remember { mutableStateOf("") }
    var regLastName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }

    var regAddressLine1 by remember { mutableStateOf("") }
    var regAddressLine2 by remember { mutableStateOf("") }
    var regLandmark by remember { mutableStateOf("") }
    var regPoliceStation by remember { mutableStateOf("") }
    var regPostOffice by remember { mutableStateOf("") }
    var regPinCode by remember { mutableStateOf("") }

    var regState by remember { mutableStateOf("Jharkhand") }
    var regDistrict by remember { mutableStateOf("Ramgarh (Gola)") }
    var isStateExpanded by remember { mutableStateOf(false) }
    var isDistrictExpanded by remember { mutableStateOf(false) }

    var regSign by remember { mutableStateOf("") }
    var regAgreeTerms by remember { mutableStateOf(false) }

    // Bot Dialog visibility state
    var showAuthBotDialog by remember { mutableStateOf(false) }
    var authBotInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative Premium Background Wave
        Image(
            painter = painterResource(id = R.drawable.img_onboarding_bg),
            contentDescription = "Background decoration",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.5f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = 1000f
                    )
                )
        )

        // Main layout scroll wrapper
        val outerScroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(outerScroll)
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section with Premium Logo and Help Chat Bot Trigger
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                BVPremiumLogo(
                    showBotIcon = true,
                    onBotClick = { showAuthBotDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "BV SHOP PVT LTD",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "GOLA • PREMIUM QUALITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Auth Fields Container Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Luxury 3-Way Tab Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { activeTab = 0; forgotPasswordRequested = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { activeTab = 1; forgotPasswordRequested = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Phone", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { activeTab = 2; forgotPasswordRequested = false },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTab == 2) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (activeTab == 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Sign Up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Login Fields
                    AnimatedVisibility(visible = activeTab == 0 && !forgotPasswordRequested) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("4-Digit Secure PIN") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pin_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            TextButton(
                                onClick = { forgotPasswordRequested = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Forgot PIN?", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }
                    }

                    // Phone OTP Login Fields
                    AnimatedVisibility(visible = activeTab == 1 && !forgotPasswordRequested) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("Enter OTP (e.g. 1234)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    }

                    // SIGN UP TAB (COMPREHENSIVE ADDRESS REGISTER)
                    AnimatedVisibility(visible = activeTab == 2 && !forgotPasswordRequested) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Personal Identity details",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = regFirstName,
                                    onValueChange = { regFirstName = it },
                                    label = { Text("First Name*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = regMiddleName,
                                    onValueChange = { regMiddleName = it },
                                    label = { Text("Middle Name") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            OutlinedTextField(
                                value = regLastName,
                                onValueChange = { regLastName = it },
                                label = { Text("Last Name*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Address*") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Mobile Number*") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = regPin,
                                onValueChange = { regPin = it },
                                label = { Text("Set Wallet PIN (4-Digits)*") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Indian Delivery Destination Address",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = regAddressLine1,
                                onValueChange = { regAddressLine1 = it },
                                label = { Text("Address Line 1*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = regAddressLine2,
                                onValueChange = { regAddressLine2 = it },
                                label = { Text("Address Line 2") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = regLandmark,
                                onValueChange = { regLandmark = it },
                                label = { Text("Landmark*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = regPoliceStation,
                                    onValueChange = { regPoliceStation = it },
                                    label = { Text("Police Station*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = regPostOffice,
                                    onValueChange = { regPostOffice = it },
                                    label = { Text("Post Office*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Dynamic India State Dropdown Choice
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { isStateExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("State: $regState", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                DropdownMenu(
                                    expanded = isStateExpanded,
                                    onDismissRequest = { isStateExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.75f).heightIn(max = 280.dp)
                                ) {
                                    IndianLocationData.statesAndDistricts.keys.sorted().forEach { stateName ->
                                        DropdownMenuItem(
                                            text = { Text(stateName, fontSize = 13.sp) },
                                            onClick = {
                                                regState = stateName
                                                regDistrict = IndianLocationData.statesAndDistricts[stateName]?.firstOrNull() ?: ""
                                                isStateExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Dynamic State District Dropdown Choice
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { isDistrictExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("District: $regDistrict", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                DropdownMenu(
                                    expanded = isDistrictExpanded,
                                    onDismissRequest = { isDistrictExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.75f).heightIn(max = 280.dp)
                                ) {
                                    val districts = IndianLocationData.statesAndDistricts[regState] ?: emptyList()
                                    districts.forEach { distName ->
                                        DropdownMenuItem(
                                            text = { Text(distName, fontSize = 13.sp) },
                                            onClick = {
                                                regDistrict = distName
                                                isDistrictExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = regPinCode,
                                onValueChange = { regPinCode = it },
                                label = { Text("PIN Code*") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Terms, Authorization & Digital Signature",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = regSign,
                                onValueChange = { regSign = it },
                                label = { Text("Sign / Authorized Signature Text*") },
                                placeholder = { Text("E.g. Aria Sterling", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { regAgreeTerms = !regAgreeTerms }
                            ) {
                                Checkbox(checked = regAgreeTerms, onCheckedChange = { regAgreeTerms = it })
                                Text(
                                    text = "I verify the Indian legal billing address & authorized digital signature.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Forgot Password Layout
                    AnimatedVisibility(visible = forgotPasswordRequested) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Reset Secure Wallet PIN",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Enter your registered email below, and we will send you instructions to restore your wallet PIN and BV Shop credentials.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Registered Email") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    forgotPasswordRequested = false
                                    viewModel.addLocalNotification("PIN Reset Request", "Simulated wallet instructions sent to $emailInput")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Send Reset Code")
                            }
                        }
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Execute CTA Button
                    Button(
                        onClick = {
                            if (activeTab == 2) {
                                // SIGN UP VALIDATION & SUBMIT
                                if (regFirstName.isBlank() || regLastName.isBlank() || regEmail.isBlank() || regPhone.isBlank() || regPin.isBlank() || regAddressLine1.isBlank() || regLandmark.isBlank() || regPoliceStation.isBlank() || regPostOffice.isBlank() || regPinCode.isBlank() || regSign.isBlank()) {
                                    viewModel.addLocalNotification("Sign Up Error", "Please fill in all mandatory fields with an asterisk (*).")
                                } else if (!regAgreeTerms) {
                                    viewModel.addLocalNotification("Sign Up Error", "Please agree to the authorization and digital signature checkbox.")
                                } else {
                                    // Save the profile info and the detailed address
                                    val combinedName = "$regFirstName ${if (regMiddleName.isNotEmpty()) "$regMiddleName " else ""}$regLastName"
                                    val fullStreetAddress = "$regAddressLine1, $regAddressLine2"
                                    
                                    // Save Address to Local Database
                                    viewModel.saveAddress(
                                        name = combinedName,
                                        phone = regPhone,
                                        street = fullStreetAddress,
                                        city = regDistrict,
                                        state = regState,
                                        zip = regPinCode,
                                        isDefault = true,
                                        firstName = regFirstName,
                                        middleName = regMiddleName,
                                        lastName = regLastName,
                                        addressLine1 = regAddressLine1,
                                        addressLine2 = regAddressLine2,
                                        landmark = regLandmark,
                                        policeStation = regPoliceStation,
                                        postOffice = regPostOffice,
                                        district = regDistrict,
                                        pinCode = regPinCode,
                                        sign = regSign
                                    )
                                    
                                    // Set mock email login and proceed
                                    viewModel.loginWithEmail(regEmail, regPin)
                                    viewModel.addLocalNotification("Welcome!", "Account successfully created for $combinedName! Shipping address registered in $regDistrict, $regState.")
                                }
                            } else if (activeTab == 0) {
                                viewModel.loginWithEmail(emailInput.ifEmpty { "aria@luxcart.com" }, pinInput.ifEmpty { "7777" })
                            } else {
                                viewModel.loginWithPhone(phoneInput.ifEmpty { "+155501928" }, otpInput.ifEmpty { "1234" })
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (activeTab == 2) "Secure Sign Up & Register" else "Secure Sign In",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Social Authentication Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "— Or Connect With —",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.loginWithSocial("Google") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    OutlinedButton(
                        onClick = { viewModel.loginWithSocial("Apple") },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("apple_login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apple", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // PREMIUM POPUP HELF CHAT BOT DIALOG ("ME HELF CHAT BOT")
    if (showAuthBotDialog) {
        AlertDialog(
            onDismissRequest = { showAuthBotDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("BV Shop Concierge Bot", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Text(
                        text = "Hello! I am your AI Concierge Assistant at BV SHOP PVT LTD GOLA. Ask me anything about registration, shipping addresses, or luxury shopping!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Scrollable Chat Window
                    val chatScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(chatScroll)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.forEach { (msg, isUser) ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Surface(
                                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Text(
                                        text = msg,
                                        fontSize = 11.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(8.dp)
                                        )
                                }
                            }
                        }

                        if (isGeminiLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Typing...", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = authBotInput,
                            onValueChange = { authBotInput = it },
                            placeholder = { Text("Ask bot...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (authBotInput.isNotBlank()) {
                                    viewModel.sendChatMessageToGemini(authBotInput)
                                    authBotInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuthBotDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
