package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.*
import com.example.ui.components.BdTeslaMapCanvas
import com.example.ui.components.DriverIncomingRequestDialog
import com.example.ui.theme.*

@Composable
fun DriverDashboardScreen(
    driverProfile: DriverProfile?,
    activeRide: RideRequest?,
    incomingRequest: RideRequest?,
    language: AppLanguage,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptRequest: () -> Unit,
    onRejectRequest: () -> Unit,
    onMarkArrived: () -> Unit,
    onStartTrip: () -> Unit,
    onCompleteTrip: () -> Unit,
    onCallPassenger: (String, String) -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = driverProfile?.isOnline ?: false

    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas layer
        BdTeslaMapCanvas(
            pickup = activeRide?.pickup ?: driverProfile?.currentLocation,
            destination = activeRide?.destination,
            activeRide = activeRide,
            language = language,
            modifier = Modifier.fillMaxSize()
        )

        // Top Status Bar: Online / Offline Switcher & Today's Earnings pill
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) StatusOnline else StatusOffline)
                        )
                        Column {
                            Text(
                                text = Strings.onlineStatus(isOnline, language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) StatusOnline else TeslaDarkTextSecondary
                            )
                            Text(
                                text = "${driverProfile?.fullName ?: "ড্রাইভার"} • ${driverProfile?.vehicleNumber ?: "কুষ্টিয়া"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TeslaDarkTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = onToggleOnline,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TeslaDarkBg,
                            checkedTrackColor = TeslaGreenNeon,
                            uncheckedTrackColor = TeslaDarkCard
                        ),
                        modifier = Modifier.testTag("driver_online_toggle")
                    )
                }
            }

            // Quick Stats Row: Today's Earnings & Completed Rides
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TeslaDarkCard.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = Strings.todaysEarnings(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = TeslaDarkTextMuted
                        )
                        Text(
                            text = Strings.bdt(driverProfile?.todayEarnings ?: 840.0),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TeslaGreenNeon
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TeslaDarkCard.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = Strings.completedTrips(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = TeslaDarkTextMuted
                        )
                        Text(
                            text = "${driverProfile?.totalCompletedTrips ?: 4} টি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TeslaCyanAccent
                        )
                    }
                }
            }
        }

        // Bottom Controls: Active Trip Control or Incoming Request
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(14.dp)
        ) {
            // If there's an incoming ride request modal
            if (incomingRequest != null && isOnline) {
                DriverIncomingRequestDialog(
                    request = incomingRequest,
                    language = language,
                    onAccept = onAcceptRequest,
                    onReject = onRejectRequest
                )
            } else if (activeRide != null && activeRide.status != RideStatus.NONE) {
                // Driver In-Trip Management Panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_active_trip_panel"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface.copy(alpha = 0.98f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Trip status and Fare
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = when (activeRide.status) {
                                        RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING -> "যাত্রীর কাছে পৌঁছানোর নির্দেশ"
                                        RideStatus.DRIVER_ARRIVED -> "আপনি পিকআপে পৌঁছেছেন"
                                        RideStatus.TRIP_STARTED -> "গন্তব্যের পথে চলমান"
                                        RideStatus.TRIP_COMPLETED -> "ট্রিপ সম্পন্ন! ভাড়া গ্রহণ করুন"
                                        else -> "চলমান রাইড"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TeslaCyanAccent
                                )
                                Text(
                                    text = "যাত্রী: ${activeRide.passengerName} • ${activeRide.passengerCount} জন",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TeslaDarkTextPrimary
                                )
                            }

                            Text(
                                text = Strings.bdt(activeRide.estimatedFare),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TeslaGreenNeon
                            )
                        }

                        // Call & Chat with Passenger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onCallPassenger(activeRide.passengerName, activeRide.passengerPhone) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = TeslaGreenNeon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.callDriver(language), color = TeslaGreenNeon)
                            }

                            OutlinedButton(
                                onClick = onOpenChat,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = TeslaCyanAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.messageDriver(language), color = TeslaCyanAccent)
                            }
                        }

                        // Dynamic Action Button based on Trip progression
                        when (activeRide.status) {
                            RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING -> {
                                Button(
                                    onClick = onMarkArrived,
                                    colors = ButtonDefaults.buttonColors(containerColor = TeslaCyanAccent),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_arrived_button")
                                ) {
                                    Text(
                                        text = Strings.arrivedAtPickup(language),
                                        fontWeight = FontWeight.Bold,
                                        color = TeslaDarkBg
                                    )
                                }
                            }
                            RideStatus.DRIVER_ARRIVED -> {
                                Button(
                                    onClick = onStartTrip,
                                    colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_start_trip_button")
                                ) {
                                    Text(
                                        text = Strings.startRide(language),
                                        fontWeight = FontWeight.Bold,
                                        color = TeslaDarkBg
                                    )
                                }
                            }
                            RideStatus.TRIP_STARTED -> {
                                Button(
                                    onClick = onCompleteTrip,
                                    colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("driver_complete_trip_button")
                                ) {
                                    Text(
                                        text = Strings.completeRide(language),
                                        fontWeight = FontWeight.Bold,
                                        color = TeslaDarkBg
                                    )
                                }
                            }
                            RideStatus.TRIP_COMPLETED -> {
                                Button(
                                    onClick = onCompleteTrip,
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text(
                                        text = Strings.collectCash(language),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            } else if (!isOnline) {
                // Offline guidance message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface.copy(alpha = 0.92f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TeslaCyanAccent)
                        Text(
                            text = if (language == AppLanguage.BANGLA)
                                "রাইড অনুরোধ গ্রহণ করতে উপরের সুইচটি অন করুন।"
                            else
                                "Toggle online above to start receiving ride requests in Kushtia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TeslaDarkTextSecondary
                        )
                    }
                }
            }
        }
    }
}
