package wombat.joshattic.us.wear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import coil.compose.SubcomposeAsyncImage
import wombat.joshattic.us.wear.WearUiState
import wombat.joshattic.us.wear.ui.theme.BrandIndigo
import wombat.joshattic.us.wear.ui.theme.BrandIndigoVariant
import wombat.joshattic.us.wear.ui.theme.DarkCardSurface
import wombat.joshattic.us.wear.ui.theme.DarkCardVariant

/**
 * Launcher home screen.
 *
 * Uses [BoxWithConstraints] to measure the actual screen dimensions and compute
 * the chord width of the circle at every row's y-position. Each row is given
 * exactly as much horizontal padding as needed so its tiles never extend
 * outside the circular bezel — and no more, so they fill the available arc.
 *
 * Layout:
 *   Row 1 (top)    [Profile] [Notifications]
 *   Row 2 (middle) [Home]    [Explore]
 *   Row 3 (bottom) [+ New Post ──────────── ]
 */
@Composable
fun HomeScreen(
    uiState: WearUiState,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onExploreClick: () -> Unit,
    onComposeClick: () -> Unit
) {
    val unreadCount = remember(uiState.notifications) {
        uiState.notifications.count { !it.read }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // ── Geometry ────────────────────────────────────────────────────────
        // We remove padding between buttons and clip the entire column
        // to a CircleShape so the buttons perfectly segment the watch face.
        val gap = 0.dp
        val corner = 0.dp

        TimeText()

        Column(
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Row 1: Profile | Notifications ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                // Profile tile
                HomeTile(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    shape = RoundedCornerShape(corner),
                    onClick = onProfileClick,
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        // Nudge content inward and down away from the bezel
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp)
                    ) {
                        val pfpUrl = uiState.session?.username?.let {
                            "https://wasteof-image-proxy.tnix.dev/$it?t=SKV8xWyDpBwzIg6Hz42EapKh5RKvb7N3"
                        }
                        if (pfpUrl != null) {
                            SubcomposeAsyncImage(
                                model = pfpUrl,
                                contentDescription = "Profile",
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp).padding(2.dp),
                                        strokeWidth = 1.5.dp
                                    )
                                },
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.caption3,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Notifications tile
                HomeTile(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    shape = RoundedCornerShape(corner),
                    onClick = onNotificationsClick,
                    gradient = if (unreadCount > 0) {
                        Brush.linearGradient(colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1)))
                    } else {
                        Brush.linearGradient(colors = listOf(DarkCardSurface, DarkCardVariant))
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        // Nudge content inward and down away from the bezel
                        modifier = Modifier.padding(top = 16.dp, end = 16.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = if (unreadCount > 0) Color.White else Color(0xFF94A3B8)
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (unreadCount > 0) "$unreadCount new" else "Inbox",
                            style = MaterialTheme.typography.caption3,
                            fontWeight = FontWeight.SemiBold,
                            color = if (unreadCount > 0) Color.White else Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Row 2: Home | Explore ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                HomeTile(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    shape = RoundedCornerShape(corner),
                    onClick = onHomeClick,
                    gradient = Brush.linearGradient(
                        colors = listOf(DarkCardVariant, DarkCardSurface)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        // Nudge slightly inward
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF94A3B8)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Home",
                            style = MaterialTheme.typography.caption3,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                HomeTile(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    shape = RoundedCornerShape(corner),
                    onClick = onExploreClick,
                    gradient = Brush.linearGradient(
                        colors = listOf(DarkCardVariant, DarkCardSurface)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        // Nudge slightly inward
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Tag,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF94A3B8)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Explore",
                            style = MaterialTheme.typography.caption3,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Row 3: New Post ──────────────────────────────
            HomeTile(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(corner),
                onClick = onComposeClick,
                gradient = Brush.horizontalGradient(
                    colors = listOf(BrandIndigoVariant, BrandIndigo)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    // Nudge content upward away from the bottom bezel
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "New Post",
                        style = MaterialTheme.typography.caption1,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * A single tappable home-screen tile. The [shape] controls which corners
 * are large (circle-edge facing) vs small (inner gap-facing).
 */
@Composable
private fun HomeTile(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    gradient: Brush,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient, shape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
