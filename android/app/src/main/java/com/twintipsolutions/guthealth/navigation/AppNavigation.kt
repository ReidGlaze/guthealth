package com.twintipsolutions.guthealth.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.twintipsolutions.guthealth.ui.dashboard.DashboardScreen
import com.twintipsolutions.guthealth.ui.fodmap.FODMAPGuideScreen
import com.twintipsolutions.guthealth.ui.insights.InsightsScreen
import com.twintipsolutions.guthealth.ui.log.LogScreen
import com.twintipsolutions.guthealth.ui.onboarding.OnboardingScreen
import com.twintipsolutions.guthealth.ui.onboarding.hasCompletedOnboarding
import com.twintipsolutions.guthealth.ui.history.HistoryScreen
import com.twintipsolutions.guthealth.ui.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.AutoMirrored.Filled.Assignment)
    data object Log : Screen("log", "Log", Icons.Filled.AddCircle)
    data object FODMAPGuide : Screen("fodmap", "FODMAP", Icons.Filled.Eco)
    data object Insights : Screen("insights", "Insights", Icons.AutoMirrored.Filled.ShowChart)
}

private val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Log,
    Screen.FODMAPGuide,
    Screen.Insights
)

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    var showOnboarding by remember { mutableStateOf(!hasCompletedOnboarding(context)) }

    if (showOnboarding) {
        OnboardingScreen(onComplete = { showOnboarding = false })
        return
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onSettingsClick = { navController.navigate("settings") },
                    onSeeAllHistory = { navController.navigate("history") }
                )
            }
            composable(Screen.Log.route) { LogScreen() }
            composable(Screen.FODMAPGuide.route) { FODMAPGuideScreen() }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable("settings") {
                SettingsScreen(onDismiss = { navController.popBackStack() })
            }
            composable("history") {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
