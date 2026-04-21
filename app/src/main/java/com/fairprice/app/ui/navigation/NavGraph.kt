package com.fairprice.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fairprice.app.ui.screens.auth.*
import com.fairprice.app.ui.screens.citizen.*
import com.fairprice.app.ui.screens.admin.*
import com.fairprice.app.ui.theme.ENTER_DURATION
import com.fairprice.app.ui.theme.EXIT_DURATION
import com.fairprice.app.ui.theme.SteadyPulseEasing
import com.fairprice.app.utils.SessionManager

/**
 * PDS Fair Price App — Navigation Graph
 *
 * Full navigation with "Steady Pulse" transitions (300ms cubic-bezier).
 * Flow: Splash → RoleSelection → Login → Verification → ProfileSetup → Main
 *       Splash → (auto-login) → CitizenHome / AdminDashboard
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(ENTER_DURATION, easing = SteadyPulseEasing)) +
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(ENTER_DURATION, easing = SteadyPulseEasing)
                    )
        },
        exitTransition = {
            fadeOut(tween(EXIT_DURATION, easing = SteadyPulseEasing)) +
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(EXIT_DURATION, easing = SteadyPulseEasing)
                    )
        },
        popEnterTransition = {
            fadeIn(tween(ENTER_DURATION, easing = SteadyPulseEasing)) +
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(ENTER_DURATION, easing = SteadyPulseEasing)
                    )
        },
        popExitTransition = {
            fadeOut(tween(EXIT_DURATION, easing = SteadyPulseEasing)) +
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(EXIT_DURATION, easing = SteadyPulseEasing)
                    )
        },
    ) {
        // ─── Auth Flow ─────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToRoleSelection = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToCitizenHome = {
                    navController.navigate(Screen.CitizenHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = { userId ->
                    navController.navigate(Screen.ProfileSetup.createRoute(userId)) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onCitizenClick = { navController.navigate(Screen.CitizenLogin.route) },
                onOfficerClick = { navController.navigate(Screen.OfficerLogin.route) },
            )
        }

        composable(Screen.CitizenLogin.route) {
            CitizenLoginScreen(
                onNavigateToVerification = { phone, rationCardNo ->
                    navController.navigate(Screen.Verification.createRoute(phone, rationCardNo))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.OfficerLogin.route) {
            OfficerLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Verification.route,
            arguments = listOf(
                navArgument("phone") { type = NavType.StringType },
                navArgument("rationCardNo") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val rationCardNo = backStackEntry.arguments?.getString("rationCardNo") ?: ""
            VerificationScreen(
                phone = phone,
                rationCardNo = rationCardNo,
                onVerificationSuccess = { userId, profileRequired ->
                    if (profileRequired) {
                        // New user — complete profile first
                        navController.navigate(Screen.ProfileSetup.createRoute(userId)) {
                            popUpTo(Screen.CitizenLogin.route) { inclusive = true }
                        }
                    } else {
                        // Returning user — profile already complete, go home
                        navController.navigate(Screen.CitizenHome.route) {
                            popUpTo(Screen.CitizenLogin.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ─── Profile Setup (Mandatory) ─────────────────
        composable(
            route = Screen.ProfileSetup.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileSetupScreen(
                userId = userId,
                onProfileComplete = {
                    navController.navigate(Screen.CitizenHome.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
            )
        }

        // ─── Citizen Flow ──────────────────────────────
        composable(Screen.CitizenHome.route) {
            HomeDashboardScreen(
                onPollClick = { pollId ->
                    navController.navigate(Screen.PollVoting.createRoute(pollId))
                },
                onComplaintClick = { navController.navigate(Screen.Complaint.route) },
                onFeedbackClick = { navController.navigate(Screen.Feedback.route) },
            )
        }

        composable(
            route = Screen.PollVoting.route,
            arguments = listOf(navArgument("pollId") { type = NavType.StringType })
        ) {
            val pollId = it.arguments?.getString("pollId") ?: ""
            PollVotingScreen(
                pollId = pollId,
                onSubmitSuccess = {
                    navController.navigate(Screen.Success.route) {
                        popUpTo(Screen.CitizenHome.route)
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Success.route) {
            SuccessScreen(
                onBackToHome = {
                    navController.navigate(Screen.CitizenHome.route) {
                        popUpTo(Screen.CitizenHome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Complaint.route) {
            ComplaintScreen(
                onSubmitSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Feedback.route) {
            FeedbackScreen(
                onSubmitSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onLogout = {
                    // Clear session before navigating to role selection
                    SessionManager.getInstance(context).clearSession()
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ─── Admin Flow ────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onCreatePoll = { navController.navigate(Screen.CreatePoll.route) },
                onManageOfficers = { navController.navigate(Screen.OfficerList.route) },
                onPollAnalytics = { navController.navigate(Screen.PollAnalytics.route) },
                onZoneAnalysis = { navController.navigate(Screen.ZoneAnalysis.route) },
            )
        }

        composable(Screen.CreatePoll.route) {
            CreatePollScreen(
                onPollCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.PollAnalytics.route) {
            PollAnalyticsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.ZoneAnalysis.route) {
            ZoneAnalysisScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        
        composable(Screen.OfficerList.route) {
            OfficerListScreen(
                onCreateOfficerClick = { navController.navigate(Screen.CreateOfficer.route) },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CreateOfficer.route) {
            CreateOfficerScreen(
                onOfficerCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
