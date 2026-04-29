package com.gagan.lokdiksha.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gagan.lokdiksha.ui.theme.FairPriceColors
import com.gagan.lokdiksha.ui.theme.ShapeTokens
import com.gagan.lokdiksha.ui.theme.steadyPulseSpec

/**
 * Glassmorphic Bottom Navigation Bar.
 *
 * Design spec:
 * - Surface at 80% opacity with 20px backdrop blur
 * - Content flows behind it (spatial depth)
 * - Rounded top corners
 * - Active: primary icon + label, inactive: onSurfaceVariant
 */

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val citizenNavItems = listOf(
    BottomNavItem("Home", Icons.Rounded.Home, "citizen_home"),
    BottomNavItem("History", Icons.Rounded.History, "history"),
    BottomNavItem("Alerts", Icons.Rounded.Notifications, "notifications"),
    BottomNavItem("Profile", Icons.Rounded.Person, "profile"),
)

val adminNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Rounded.Home, "admin_dashboard"),
    BottomNavItem("Alerts", Icons.Rounded.Notifications, "notifications"),
    BottomNavItem("Profile", Icons.Rounded.Person, "profile"),
)

@Composable
fun GlassBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeTokens.BottomSheet)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.80f))
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    animationSpec = steadyPulseSpec(),
                    label = "navIconColor"
                )

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    } else {
                        Color.Transparent
                    },
                    animationSpec = steadyPulseSpec(),
                    label = "navBgColor"
                )

                IconButton(
                    onClick = { onItemClick(item) },
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bgColor)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
