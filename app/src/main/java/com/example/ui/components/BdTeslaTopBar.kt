package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun BdTeslaTopBar(
    activeRole: UserRole,
    driverStatus: DriverVerificationStatus,
    language: AppLanguage,
    onRoleSelected: (UserRole) -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenDriverRegistration: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 10.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Tagline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(TeslaGreenNeon, TeslaCyanAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "BD TESLA Bolt",
                        tint = TeslaDarkBg,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BD TESLA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TeslaGreenNeon,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TeslaGreenDark.copy(alpha = 0.4f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "KUSHTIA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TeslaCyanAccent
                            )
                        }
                    }
                    Text(
                        text = Strings.tagline(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right side: Mode Switcher Badge & Language Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role Selector Pill
                Box {
                    val roleBadgeColor = when (activeRole) {
                        UserRole.PASSENGER -> TeslaGreenNeon.copy(alpha = 0.15f)
                        UserRole.DRIVER -> TeslaCyanAccent.copy(alpha = 0.18f)
                        UserRole.ADMIN -> TeslaGoldAccent.copy(alpha = 0.2f)
                    }
                    val roleTextColor = when (activeRole) {
                        UserRole.PASSENGER -> TeslaGreenNeon
                        UserRole.DRIVER -> TeslaCyanAccent
                        UserRole.ADMIN -> TeslaGoldAccent
                    }
                    val roleLabel = when (activeRole) {
                        UserRole.PASSENGER -> Strings.passengerMode(language)
                        UserRole.DRIVER -> Strings.driverMode(language)
                        UserRole.ADMIN -> Strings.adminMode(language)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(roleBadgeColor)
                            .border(1.dp, roleTextColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { showRoleMenu = true }
                            .padding(horizontal = 11.dp, vertical = 7.dp)
                            .testTag("mode_switcher_pill"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (activeRole) {
                                UserRole.PASSENGER -> Icons.Default.DirectionsCar
                                UserRole.DRIVER -> Icons.Default.TwoWheeler
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = "Role Icon",
                            tint = roleTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = roleLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = roleTextColor
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = roleTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(Strings.passengerMode(language)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TeslaGreenNeon)
                            },
                            onClick = {
                                onRoleSelected(UserRole.PASSENGER)
                                showRoleMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(Strings.driverMode(language))
                                    if (driverStatus != DriverVerificationStatus.APPROVED) {
                                        Text(
                                            text = if (driverStatus == DriverVerificationStatus.PENDING_APPROVAL)
                                                "অনুমোদন প্রক্রিয়াধীন" else "নিবন্ধন প্রয়োজন",
                                            fontSize = 11.sp,
                                            color = StatusPending
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ElectricRickshaw, contentDescription = null, tint = TeslaCyanAccent)
                            },
                            onClick = {
                                if (driverStatus == DriverVerificationStatus.APPROVED) {
                                    onRoleSelected(UserRole.DRIVER)
                                } else {
                                    onOpenDriverRegistration()
                                }
                                showRoleMenu = false
                            }
                        )
                    }
                }

                // Language Toggle Button (বাংলা / EN)
                OutlinedButton(
                    onClick = onToggleLanguage,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("language_toggle_button")
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "EN" else "বাংলা",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
