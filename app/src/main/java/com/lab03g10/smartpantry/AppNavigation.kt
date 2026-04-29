package com.lab03g10.smartpantry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.lab03g10.smartpantry.ui.screens.AddEditFoodScreen
import com.lab03g10.smartpantry.ui.screens.AddSensorScreen
import com.lab03g10.smartpantry.ui.screens.AnalyticsScreen
import com.lab03g10.smartpantry.ui.screens.BarcodeScanResultScreen
import com.lab03g10.smartpantry.ui.screens.DashboardScreen
import com.lab03g10.smartpantry.ui.screens.EditSensorScreen
import com.lab03g10.smartpantry.ui.screens.ExpiryCalendarScreen
import com.lab03g10.smartpantry.ui.screens.FoodDetailScreen
import com.lab03g10.smartpantry.ui.screens.ForgotPasswordScreen
import com.lab03g10.smartpantry.ui.screens.StorageMonitorScreen
import com.lab03g10.smartpantry.ui.screens.InventoryListScreen
import com.lab03g10.smartpantry.ui.screens.LoginScreen
import com.lab03g10.smartpantry.ui.screens.NotificationsScreen
import com.lab03g10.smartpantry.ui.screens.OnboardingScreen
import com.lab03g10.smartpantry.ui.screens.ProfileScreen
import com.lab03g10.smartpantry.ui.screens.RegistrationScreen
import com.lab03g10.smartpantry.ui.screens.ScannerScreen
import com.lab03g10.smartpantry.ui.screens.SensorDetailScreen
import com.lab03g10.smartpantry.ui.screens.ShoppingListScreen
import com.lab03g10.smartpantry.ui.screens.WasteLogScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


private object Route {
    const val LOGIN          = "login"
    const val REGISTRATION   = "registration"
    const val DASHBOARD      = "dashboard"
    const val INVENTORY_LIST = "inventory_list"
    const val ADD_EDIT_FOOD  = "add_edit_food"
    const val SCANNER        = "scanner"
    const val ANALYTICS      = "analytics"
    const val STORAGE_MONITOR = "storage_monitor"
    const val SHOPPING_LIST  = "shopping_list"
    const val PROFILE        = "profile"
    const val ADD_SENSOR           = "add_sensor"
    const val FOOD_DETAIL          = "food_detail"
    const val NOTIFICATIONS        = "notifications"
    const val WASTE_LOG            = "waste_log"
    const val BARCODE_SCAN_RESULT  = "barcode_scan_result"
    const val EXPIRY_CALENDAR      = "expiry_calendar"
    const val SENSOR_DETAIL        = "sensor_detail"
    const val EDIT_SENSOR          = "edit_sensor"
    const val FORGOT_PASSWORD      = "forgot_password"
    const val ONBOARDING           = "onboarding"
}

//Bottom navigation tabs

private enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Dashboard(Route.DASHBOARD,      "Dashboard", Icons.Filled.Home),
    Inventory(Route.INVENTORY_LIST, "Inventory", Icons.Filled.Inventory),
    Scanner  (Route.SCANNER,        "Scanner",   Icons.Filled.QrCodeScanner),
    Analytics(Route.ANALYTICS,      "Analytics", Icons.Filled.BarChart),
    Profile  (Route.PROFILE,        "Profile",   Icons.Filled.Person),
}

private val bottomBarRoutes: Set<String> = BottomTab.entries.map { it.route }.toSet()


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                SmartPantryBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Route.LOGIN,
            modifier         = Modifier.padding(innerPadding),
        ) {
            composable(Route.LOGIN) {
                LoginScreen(
                    onLoginSuccess             = {
                        navController.navigate(Route.DASHBOARD) {
                            popUpTo(Route.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister       = { navController.navigate(Route.REGISTRATION) },
                    onNavigateToForgotPassword = { navController.navigate(Route.FORGOT_PASSWORD) },
                )
            }
            composable(Route.REGISTRATION) {
                RegistrationScreen(
                    onRegistrationSuccess = {
                        navController.navigate(Route.ONBOARDING) {
                            popUpTo(Route.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin     = { navController.popBackStack() },
                )
            }

            composable(Route.DASHBOARD) {
                DashboardScreen(
                    onNavigateToStorageMonitor = { navController.navigate(Route.STORAGE_MONITOR) },
                    onNavigateToShoppingList   = { navController.navigate(Route.SHOPPING_LIST) },
                    onNavigateToAddSensor      = { navController.navigate(Route.ADD_SENSOR) },
                    onNavigateToNotifications  = { navController.navigate(Route.NOTIFICATIONS) },
                    onNavigateToExpiryCalendar = { navController.navigate(Route.EXPIRY_CALENDAR) },
                    onNavigateToProfile =  { navController.navigate(Route.PROFILE) },
                    onNavigateToInventory = { navController.navigate(Route.INVENTORY_LIST) }
                )
            }
            composable(Route.INVENTORY_LIST) {
                InventoryListScreen(
                    onNavigateToAddFood    = { navController.navigate(Route.ADD_EDIT_FOOD) },
                    onNavigateToFoodDetail = { navController.navigate(Route.FOOD_DETAIL) },
                )
            }
            composable(Route.SCANNER) {
                ScannerScreen(
                    onNavigateToAddFood       = { navController.navigate(Route.ADD_EDIT_FOOD) },
                    onNavigateToBarcodeResult = { navController.navigate(Route.BARCODE_SCAN_RESULT) },
                    onNavigateBack            = { navController.popBackStack() },
                )
            }
            composable(Route.ANALYTICS) {
                AnalyticsScreen(
                    onNavigateToWasteLog = { navController.navigate(Route.WASTE_LOG) },
                )
            }
            composable(Route.PROFILE) {
                ProfileScreen()
            }

            composable(Route.ADD_EDIT_FOOD) {
                AddEditFoodScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.STORAGE_MONITOR) {
                StorageMonitorScreen(
                    onNavigateBack           = { navController.popBackStack() },
                    onNavigateToSensorDetail = { navController.navigate(Route.SENSOR_DETAIL) },
                )
            }
            composable(Route.SHOPPING_LIST) {
                ShoppingListScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.ADD_SENSOR) {
                AddSensorScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.FOOD_DETAIL) {
                FoodDetailScreen(
                    onNavigateBack    = { navController.popBackStack() },
                    onNavigateToEdit  = { navController.navigate(Route.ADD_EDIT_FOOD) },
                )
            }
            composable(Route.NOTIFICATIONS) {
                NotificationsScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.WASTE_LOG) {
                WasteLogScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.BARCODE_SCAN_RESULT) {
                BarcodeScanResultScreen(
                    onNavigateToAddFood = { navController.navigate(Route.ADD_EDIT_FOOD) },
                    onNavigateBack      = { navController.popBackStack() },
                )
            }
            composable(Route.EXPIRY_CALENDAR) {
                ExpiryCalendarScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.SENSOR_DETAIL) {
                SensorDetailScreen(
                    onNavigateBack   = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate(Route.EDIT_SENSOR) },
                )
            }
            composable(Route.EDIT_SENSOR) {
                EditSensorScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.FORGOT_PASSWORD) {
                ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Route.DASHBOARD) {
                            popUpTo(Route.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}


@Composable
private fun SmartPantryBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected  = selected,
                onClick   = { onTabSelected(tab.route) },
                icon      = {
                    Icon(imageVector = tab.icon, contentDescription = tab.label)
                },
                label     = { Text(tab.label) },
                colors    = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    indicatorColor      = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}




@Composable
private fun PlaceholderScreen(
    title: String,
    actions: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text  = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            actions?.invoke()
        }
    }
}
