package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.DriverProfile
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserProfile?,
    driverProfile: DriverProfile?,
    activeRole: UserRole,
    language: AppLanguage,
    onSwitchRole: (UserRole) -> Unit,
    onOpenDriverRegistration: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSupportDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.navProfile(language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TeslaDarkTextPrimary
        )

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(TeslaGreenDark)
                        .border(2.dp, TeslaGreenNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = TeslaGreenNeon,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "Passenger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                    Text(
                        text = user?.phone ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TeslaDarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = TeslaGoldAccent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${user?.passengerRating ?: 4.9} ★",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TeslaGoldAccent
                        )
                        Text(
                            text = "• ${user?.totalRidesAsPassenger ?: 18} ${if (language == AppLanguage.BANGLA) "রাইড" else "rides"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TeslaDarkTextMuted
                        )
                    }
                }
            }
        }

        // Mode Switching CTA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (activeRole == UserRole.PASSENGER) TeslaCyanAccent.copy(alpha = 0.12f) else TeslaGreenNeon.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(18.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (activeRole == UserRole.PASSENGER) TeslaCyanAccent else TeslaGreenNeon
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (activeRole == UserRole.PASSENGER) Icons.Default.TwoWheeler else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (activeRole == UserRole.PASSENGER) TeslaCyanAccent else TeslaGreenNeon
                    )
                    Text(
                        text = if (activeRole == UserRole.PASSENGER)
                            (if (language == AppLanguage.BANGLA) "ড্রাইভার হিসেবে আয় করুন" else "Earn as a BD TESLA Driver")
                        else
                            (if (language == AppLanguage.BANGLA) "যাত্রী হিসেবে রাইড নিন" else "Book rides as Passenger"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                }

                if (activeRole == UserRole.PASSENGER) {
                    val driverStatus = user?.driverStatus ?: DriverVerificationStatus.NOT_APPLIED
                    if (driverStatus == DriverVerificationStatus.APPROVED) {
                        Button(
                            onClick = { onSwitchRole(UserRole.DRIVER) },
                            colors = ButtonDefaults.buttonColors(containerColor = TeslaCyanAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_switch_to_driver_button")
                        ) {
                            Text(Strings.switchToDriver(language), color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onOpenDriverRegistration,
                            colors = ButtonDefaults.buttonColors(containerColor = TeslaCyanAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("profile_register_as_driver_button")
                        ) {
                            Text(Strings.registerAsDriver(language), color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { onSwitchRole(UserRole.PASSENGER) },
                        colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_switch_to_passenger_button")
                    ) {
                        Text(Strings.switchToPassenger(language), color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Account & App Settings List
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                ProfileOptionItem(
                    icon = Icons.Default.Language,
                    title = if (language == AppLanguage.BANGLA) "ভাষা (Language)" else "Language (ভাষা)",
                    subtitle = if (language == AppLanguage.BANGLA) "বাংলা" else "English",
                    onClick = onToggleLanguage
                )
                HorizontalDivider(color = TeslaDarkCardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileOptionItem(
                    icon = Icons.Default.Payments,
                    title = if (language == AppLanguage.BANGLA) "পেমেন্ট সেটিংস" else "Payment Settings",
                    subtitle = if (language == AppLanguage.BANGLA) "নগদ টাকা (ক্যাশ) সক্রিয়" else "Cash On Ride Active",
                    onClick = {}
                )
                HorizontalDivider(color = TeslaDarkCardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileOptionItem(
                    icon = Icons.Default.HelpOutline,
                    title = if (language == AppLanguage.BANGLA) "সহায়তা ও সাপোর্ট" else "Help & Support",
                    subtitle = if (language == AppLanguage.BANGLA) "অফিসিয়াল সাপোর্ট চ্যানেল" else "Kushtia Helpline",
                    onClick = { showSupportDialog = true }
                )
                HorizontalDivider(color = TeslaDarkCardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileOptionItem(
                    icon = Icons.Default.Policy,
                    title = if (language == AppLanguage.BANGLA) "শর্তাবলী ও গোপনীয়তা নীতি" else "Terms & Privacy Policy",
                    subtitle = "BD TESLA Kushtia Platform",
                    onClick = { showTermsDialog = true }
                )
                HorizontalDivider(color = TeslaDarkCardBorder, modifier = Modifier.padding(horizontal = 16.dp))

                ProfileOptionItem(
                    icon = Icons.Default.AdminPanelSettings,
                    title = if (language == AppLanguage.BANGLA) "অ্যাডমিন প্যানেল" else "Admin Dashboard",
                    subtitle = "Manage drivers & fares",
                    onClick = { onSwitchRole(UserRole.ADMIN) }
                )
            }
        }
    }

    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            containerColor = TeslaDarkSurface,
            title = { Text("BD TESLA কুষ্টিয়া সহায়তা কেন্দ্র", color = TeslaGreenNeon, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "যে কোনো সহায়তার জন্য আমাদের কুষ্টিয়া সদর অফিসে যোগাযোগ করুন:\n\n" +
                    "📍 অফিসিয়াল সাপোর্ট চ্যানেল\n" +
                    "📞 হটলাইন: অ্যাপে প্রকাশিত অফিসিয়াল নম্বর\n" +
                    "✉️ ইমেইল: অ্যাপে প্রকাশিত অফিসিয়াল ইমেইল\n\n" +
                    "২৪/৭ গ্রাহক ও চালক সেবা চালু রয়েছে।",
                    color = TeslaDarkTextPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text("ঠিক আছে", color = TeslaGreenNeon)
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = TeslaDarkSurface,
            title = { Text("শর্তাবলী ও নীতিমালা", color = TeslaGreenNeon, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "১. BD TESLA কুষ্টিয়া জেলায় পরিবেশবান্ধব ইলেকট্রিক অটো এবং পাখি ভ্যান রাইড প্রদান করে।\n" +
                    "২. চালক এবং যাত্রী উভয়কেই নিরাপদ আচরণ বজায় রাখতে হবে।\n" +
                    "৩. নির্ধারিত মিটার অনুযায়ী ন্যায্য ভাড়া প্রদান আবশ্যক।\n" +
                    "৪. কোনো অসদুপায় অবলম্বন করলে অ্যাকাউন্ট সাময়িক বরখাস্ত হতে পারে।",
                    color = TeslaDarkTextPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("সম্মত আছি", color = TeslaGreenNeon)
                }
            }
        )
    }
}

@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TeslaGreenNeon, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TeslaDarkTextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = TeslaDarkTextMuted)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TeslaDarkTextMuted, modifier = Modifier.size(20.dp))
    }
}
