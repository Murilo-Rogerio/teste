package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.ui.theme.ApexError
import com.example.ui.theme.ApexOnSurface
import com.example.ui.theme.ApexOnSurfaceVariant
import com.example.ui.theme.ApexOutlineVariant
import com.example.ui.theme.ApexPrimary
import com.example.ui.theme.ApexPrimaryContainer
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexSurfaceContainerHigh
import com.example.ui.theme.ApexSurfaceContainerHighest
import com.example.ui.theme.ApexSurfaceContainerLowest
import com.example.ui.theme.ApexTextStyles
import com.example.viewmodel.ApexTab

private const val PROFILE_AVATAR_URL =
    "https://lh3.googleusercontent.com/aida-public/AB6AXuBIvAGvCeb0z055152F4yt5Iv2qW0xa8FYwAIixFfwUfunD9HlgoOVFIgW80x2fhEd20Lb84QzJrEaxZPPLtnkK2p3v3BZGI4c8KYrTv62iDOCrRwh4xoC8fYCQ9d_iqs6dr4H6DKR3xLvaCoMDmP0N5_rb6oGP2jfJAVxjWPN8_DZuaOPVzYliYuXzf8kBF8HNOkjqb-O-Ml18RwacnlJiFGFJ5jb1NlB6PNvdtSQ"

@Composable
fun ApexTopBar(
    subtitle: String,
    level: Int,
    streakDays: Int,
    isOffline: Boolean,
    onInstallClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ApexSurface.copy(alpha = 0.92f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left brand block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.apex_launcher_icon_1795117497573),
                    contentDescription = stringResource(R.string.cd_apex_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "APEX",
                            style = ApexTextStyles.HeadlineSm.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = ApexOnSurface
                        )
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ApexSurfaceContainerHigh)
                                .border(1.dp, ApexPrimary.copy(alpha = 0.25f), CircleShape)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = ApexPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "LVL $level",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                color = ApexPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(ApexOutlineVariant)
                            )
                            Text(
                                text = "${streakDays}D 🔥",
                                style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                                color = ApexOnSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = subtitle.uppercase(),
                        style = ApexTextStyles.LabelCaps.copy(fontSize = 10.sp),
                        color = ApexOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right actions block
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isOffline) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ApexSurfaceContainerHigh)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = "Offline",
                            tint = ApexError,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "OFFLINE",
                            style = ApexTextStyles.LabelCaps.copy(fontSize = 9.sp),
                            color = ApexError
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ApexSurfaceContainerHigh)
                        .clickable(onClick = onInstallClick)
                        .testTag("header_install_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.InstallMobile,
                        contentDescription = stringResource(R.string.cd_install_app),
                        tint = ApexPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onProfileClick)
                        .testTag("header_profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = PROFILE_AVATAR_URL,
                        contentDescription = stringResource(R.string.cd_profile_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, ApexPrimary.copy(alpha = 0.45f), CircleShape),
                        error = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ApexSurfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = ApexPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-1).dp, y = (-1).dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ApexPrimaryContainer)
                            .border(2.dp, ApexSurface, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ApexBottomNavigationBar(
    selectedTab: ApexTab,
    onSelectTab: (ApexTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "active_dot_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp),
        color = ApexSurfaceContainerLowest.copy(alpha = 0.94f)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ApexTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val icon: ImageVector = when (tab) {
                        ApexTab.TREINOS -> Icons.Rounded.FitnessCenter
                        ApexTab.ATIVO -> Icons.Rounded.Timer
                        ApexTab.IA_COACH -> Icons.Rounded.SmartToy
                        ApexTab.NUTRICAO -> Icons.Rounded.Restaurant
                        ApexTab.PROGRESSO -> Icons.Rounded.MilitaryTech
                    }
                    val tint = if (isSelected) ApexPrimary else ApexOnSurfaceVariant

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = ApexPrimary),
                                onClick = { onSelectTab(tab) }
                            )
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ApexPrimaryContainer.copy(alpha = 0.16f))
                                )
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(23.dp)
                            )
                            if (tab == ApexTab.ATIVO) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-2).dp)
                                        .size(8.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(ApexPrimary.copy(alpha = 0.45f))
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-2).dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ApexPrimary)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label.uppercase(),
                            style = ApexTextStyles.LabelCaps.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                letterSpacing = 0.6.sp
                            ),
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ApexToastBanner(
    message: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(tween(200)) + slideInVertically { -it },
        exit = fadeOut(tween(200)) + slideOutVertically { -it },
        modifier = modifier
    ) {
        if (message != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .shadow(14.dp, CircleShape)
                    .clip(CircleShape)
                    .background(ApexSurfaceContainerHighest)
                    .border(1.dp, ApexPrimary.copy(alpha = 0.25f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = ApexPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    style = ApexTextStyles.LabelInteractive.copy(fontSize = 13.sp),
                    color = ApexOnSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PulsingCyanDot(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cyan_dot")
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale"
    )
    Box(modifier = modifier.size(12.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(ApexPrimary.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(ApexPrimary)
        )
    }
}
