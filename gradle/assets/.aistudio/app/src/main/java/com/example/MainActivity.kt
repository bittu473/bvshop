package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val selectedLang by viewModel.selectedLanguage.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                if (!isLoggedIn) {
                    AuthScreen(viewModel = viewModel)
                } else {
                    MainAppScaffold(viewModel = viewModel, selectedLang = selectedLang)
                }
            }
        }
    }
}

enum class ActiveTab {
    HOME, CART, PROFILE
}

enum class ActiveScreen {
    MAIN, DETAILS, TRACKING, ASSISTANT
}

@Composable
fun MainAppScaffold(viewModel: ShopViewModel, selectedLang: String) {
    var activeTab by remember { mutableStateOf(ActiveTab.HOME) }
    var activeScreen by remember { mutableStateOf(ActiveScreen.MAIN) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (activeScreen == ActiveScreen.MAIN) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == ActiveTab.HOME,
                        onClick = { activeTab = ActiveTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text(Localizer.translate("app_title", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = activeTab == ActiveTab.CART,
                        onClick = { activeTab = ActiveTab.CART },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text(Localizer.translate("cart", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_cart")
                    )
                    NavigationBarItem(
                        selected = activeTab == ActiveTab.PROFILE,
                        onClick = { activeTab = ActiveTab.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text(Localizer.translate("profile", selectedLang), fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeScreen) {
                ActiveScreen.MAIN -> {
                    when (activeTab) {
                        ActiveTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToDetails = { activeScreen = ActiveScreen.DETAILS },
                                onNavigateToAssistant = { activeScreen = ActiveScreen.ASSISTANT }
                            )
                        }
                        ActiveTab.CART -> {
                            CartAndCheckoutScreen(
                                viewModel = viewModel,
                                onNavigateToTracking = { activeScreen = ActiveScreen.TRACKING }
                            )
                        }
                        ActiveTab.PROFILE -> {
                            ProfileAndDashboardsScreen(
                                viewModel = viewModel,
                                activeSubView = "PROFILE",
                                onNavigateBackToProfile = { /* noop */ }
                            )
                        }
                    }
                }

                ActiveScreen.DETAILS -> {
                    ProductDetailsScreen(
                        viewModel = viewModel,
                        onBack = { activeScreen = ActiveScreen.MAIN }
                    )
                }

                ActiveScreen.TRACKING -> {
                    ProfileAndDashboardsScreen(
                        viewModel = viewModel,
                        activeSubView = "TRACKING",
                        onNavigateBackToProfile = { activeScreen = ActiveScreen.MAIN }
                    )
                }

                ActiveScreen.ASSISTANT -> {
                    ProfileAndDashboardsScreen(
                        viewModel = viewModel,
                        activeSubView = "ASSISTANT",
                        onNavigateBackToProfile = { activeScreen = ActiveScreen.MAIN }
                    )
                }
            }
        }
    }
}
