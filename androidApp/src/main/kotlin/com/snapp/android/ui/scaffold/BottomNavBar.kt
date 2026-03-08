package com.snapp.android.ui.scaffold

import com.snapp.android.ui.navigation.AppRoute
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.snapp.data.model.layout.NavItem

private const val MAX_BOTTOM_TABS = 4

// Figma: bottom bar same light gray as header
private val BottomBarColor = Color(0xFFE8E8E8)
// Unselected items: darker gray so labels are readable and bolder-looking
private val UnselectedContentColor = Color(0xFF5C5C5C)

@Composable
fun SnappBottomNavBar(
    navItems: List<NavItem>,
    currentRoute: String,
    onItemClick: (NavItem) -> Unit,
    onMoreClick: () -> Unit
) {
    val showMore = navItems.size > MAX_BOTTOM_TABS
    val visibleItems = if (showMore) navItems.take(MAX_BOTTOM_TABS) else navItems

    NavigationBar(
        containerColor = BottomBarColor,
        tonalElevation = androidx.compose.ui.unit.Dp(2f)
    ) {
        visibleItems.forEach { item ->
            val selected = currentRoute == AppRoute.normalizeEntitySlug(item.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = iconForName(item.icon),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unselectedIconColor = UnselectedContentColor,
                    unselectedTextColor = UnselectedContentColor
                )
            )
        }

        if (showMore) {
            NavigationBarItem(
                selected = false,
                onClick = onMoreClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More"
                    )
                },
                label = {
                    Text(
                        text = "More",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = UnselectedContentColor,
                    unselectedTextColor = UnselectedContentColor
                )
            )
        }
    }
}
