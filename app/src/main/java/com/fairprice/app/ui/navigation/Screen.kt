package com.fairprice.app.ui.navigation

/**
 * PDS Fair Price App — Navigation Routes
 *
 * Sealed class defining all screen destinations.
 * Organized by flow: Auth → Citizen → Admin
 */
sealed class Screen(val route: String) {

    // ─── Auth Flow ─────────────────────────────────
    data object Splash : Screen("splash")
    data object RoleSelection : Screen("role_selection")
    data object CitizenLogin : Screen("citizen_login")
    data object OfficerLogin : Screen("officer_login")
    data object Verification : Screen("verification/{phone}") {
        fun createRoute(phone: String) = "verification/$phone"
    }
    data object ProfileSetup : Screen("profile_setup/{userId}") {
        fun createRoute(userId: String) = "profile_setup/$userId"
    }

    // ─── Citizen Flow ──────────────────────────────
    data object CitizenHome : Screen("citizen_home")
    data object PollVoting : Screen("poll_voting/{pollId}") {
        fun createRoute(pollId: String) = "poll_voting/$pollId"
    }
    data object Success : Screen("success")
    data object History : Screen("history")
    data object Complaint : Screen("complaint")
    data object Feedback : Screen("feedback")
    data object Profile : Screen("profile")

    // ─── Admin Flow ────────────────────────────────
    data object AdminDashboard : Screen("admin_dashboard")
    data object CreatePoll : Screen("create_poll")
    data object ZoneAnalysis : Screen("zone_analysis")
    data object Notifications : Screen("notifications")
}
