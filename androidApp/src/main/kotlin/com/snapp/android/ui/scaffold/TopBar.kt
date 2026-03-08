package com.snapp.android.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.snapp.data.model.layout.NavbarUserMenuItem

// Figma: header and bottom bar use light grayish background
private val HeaderBarColor = androidx.compose.ui.graphics.Color(0xFFE8E8E8)
private val HeaderContentColor = androidx.compose.ui.graphics.Color(0xFF1F2937)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnappTopBar(
    title: String,
    logoUrl: String,
    userName: String,
    userMenuItems: List<NavbarUserMenuItem>,
    onMenuClick: () -> Unit,
    onUserMenuItemClick: (NavbarUserMenuItem) -> Unit,
    onLogout: () -> Unit
) {
    var userMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open menu",
                        tint = HeaderContentColor
                    )
                }
                if (logoUrl.isNotBlank()) {
                    val model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .crossfade(true)
                        .build()
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier.size(width = 80.dp, height = 32.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HeaderContentColor
                )
            }
        },
        navigationIcon = {},
        actions = {
            IconButton(onClick = { /* notifications */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = HeaderContentColor
                )
            }

            Box {
                IconButton(onClick = { userMenuExpanded = true }) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(HeaderContentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userName.firstOrNull()?.uppercaseChar() ?: 'U').toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HeaderContentColor
                        )
                    }
                }

                DropdownMenu(
                    expanded = userMenuExpanded,
                    onDismissRequest = { userMenuExpanded = false }
                ) {
                    userMenuItems.forEach { item ->
                        val isLogout = item.label.equals("Logout", ignoreCase = true)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item.label,
                                    color = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                userMenuExpanded = false
                                if (isLogout) onLogout() else onUserMenuItemClick(item)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = userMenuIconForName(item.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HeaderBarColor,
            titleContentColor = HeaderContentColor,
            navigationIconContentColor = HeaderContentColor,
            actionIconContentColor = HeaderContentColor
        )
    )
}
