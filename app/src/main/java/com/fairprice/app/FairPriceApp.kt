package com.fairprice.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fairprice.app.ui.components.GlassBottomBar
import com.fairprice.app.ui.components.citizenNavItems
import com.fairprice.app.ui.components.adminNavItems
import com.fairprice.app.ui.navigation.NavGraph
import com.fairprice.app.ui.navigation.Screen
import com.fairprice.app.utils.SessionManager

/**
 * Root composable for the Fair Price app.
 * Manages the NavHost and conditionally shows the bottom navigation bar.
 *
 * Uses SessionManager to determine the correct bottom bar (admin vs citizen)
 * instead of route-based detection, which was incorrectly redirecting
 * officers to the citizen profile view.
 */

// Routes where the bottom nav should be visible
private val citizenMainRoutes = setOf(
    Screen.CitizenHome.route,
    Screen.History.route,
    Screen.Notifications.route,
    Screen.Profile.route,
)

private val adminMainRoutes = setOf(
    Screen.AdminDashboard.route,
    Screen.Notifications.route,
    Screen.Profile.route,
)

private val authRoutes = setOf(
    Screen.Splash.route,
    Screen.RoleSelection.route,
    Screen.CitizenLogin.route,
    Screen.OfficerLogin.route,
    Screen.Verification.route,
    Screen.ProfileSetup.route,
)

@Composable
fun FairPriceApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val session = remember { SessionManager.getInstance(context) }

    val showBottomBar = currentRoute in citizenMainRoutes || currentRoute in adminMainRoutes
    // Use SessionManager role instead of route-based detection to fix
    // the bug where officer profile was redirecting to citizen view
    val isAdmin = session.isOfficer()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                GlassBottomBar(
                    items = if (isAdmin) adminNavItems else citizenNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavGraph(
                navController = navController,
            )
        }
    }
}
