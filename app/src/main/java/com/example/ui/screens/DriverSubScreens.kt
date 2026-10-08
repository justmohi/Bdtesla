package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.DriverProfile
import com.example.data.model.RideRequest
import com.example.ui.theme.*

@Composable
fun DriverRequestsTab(
    incomingRequest: RideRequest?,
    isOnline: Boolean,
    language: AppLanguage,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.navRequests(language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TeslaDarkTextPrimary
        )

        if (!isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = StatusOffline, modifier = Modifier.size(32.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA)
                            "আপনি বর্তমানে অফলাইনে আছেন। ড্যাশবোর্ড থেকে অনলাইন করুন।"
                        else
                            "You are currently offline. Toggle online from Dashboard to receive requests.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TeslaDarkTextSecondary
                    )
                }
            }
        } else if (incomingRequest != null) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("pending_request_card"),
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(TeslaCyanAccent)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "নতুন অনুরোধ (কুষ্টিয়া)",
                            fontWeight = FontWeight.Bold,
                            color = TeslaCyanAccent
                        )
                        Text(
                            text = Strings.bdt(incomingRequest.estimatedFare),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = TeslaGreenNeon
                        )
                    }

                    Text(
                        text = "যাত্রী: ${incomingRequest.passengerName} (${incomingRequest.passengerPhone})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TeslaDarkTextPrimary
                    )

                    Text(
                        text = "পিকআপ: ${incomingRequest.pickup.nameBn} ➔ গন্তব্য: ${incomingRequest.destination.nameBn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TeslaDarkTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Strings.reject(language), color = StatusDanger)
                        }

                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Strings.accept(language), color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = TeslaCyanAccent, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA)
                            "কুষ্টিয়ার নিকটবর্তী রাইড খোঁজা হচ্ছে..."
                        else
                            "Searching for Kushtia ride requests...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TeslaDarkTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun DriverEarningsTab(
    driverProfile: DriverProfile?,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.navEarnings(language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TeslaDarkTextPrimary
        )

        // Today's Earnings Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(TeslaGreenNeon)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Strings.todaysEarnings(language),
                    style = MaterialTheme.typography.labelMedium,
                    color = TeslaDarkTextMuted
                )
                Text(
                    text = Strings.bdt(driverProfile?.todayEarnings ?: 840.0),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TeslaGreenNeon
                )
                Text(
                    text = if (language == AppLanguage.BANGLA) "৪টি ট্রিপ থেকে অর্জিত" else "Earned from 4 trips today",
                    style = MaterialTheme.typography.bodySmall,
                    color = TeslaCyanAccent
                )
            }
        }

        // Lifetime summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "সর্বমোট আয়", style = MaterialTheme.typography.labelSmall, color = TeslaDarkTextMuted)
                    Text(
                        text = Strings.bdt(driverProfile?.totalEarnings ?: 28450.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "সম্পন্ন ট্রিপ", style = MaterialTheme.typography.labelSmall, color = TeslaDarkTextMuted)
                    Text(
                        text = "${driverProfile?.totalCompletedTrips ?: 142} টি",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                }
            }
        }

        // Commission policy info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TeslaDarkCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TeslaGoldAccent)
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "BD TESLA সার্ভিস চার্জ মাত্র ১০%। নগদ ভাড়ার ৯০% সরাসরি আপনার কাছে থাকে।"
                    else
                        "BD TESLA commission is only 10%. 90% of cash fare remains with you directly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TeslaDarkTextSecondary
                )
            }
        }
    }
}
