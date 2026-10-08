package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.RideRequest
import com.example.data.model.RideStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RidesHistoryScreen(
    rides: List<RideRequest>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var selectedRideForReceipt by remember { mutableStateOf<RideRequest?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = Strings.navRides(language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TeslaDarkTextPrimary
        )

        if (rides.isEmpty()) {
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
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "No rides",
                        tint = TeslaDarkTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কোনো রাইড ইতিহাস নেই" else "No ride history yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = TeslaDarkTextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("rides_history_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rides) { ride ->
                    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(ride.createdAt))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, TeslaDarkCardBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedRideForReceipt = ride },
                        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header Row: Vehicle, Date, Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricRickshaw,
                                        contentDescription = null,
                                        tint = TeslaGreenNeon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) ride.vehicleType.labelBn else ride.vehicleType.labelEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TeslaDarkTextPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (ride.status) {
                                                RideStatus.TRIP_COMPLETED -> StatusSuccess.copy(alpha = 0.2f)
                                                RideStatus.CANCELLED -> StatusDanger.copy(alpha = 0.2f)
                                                else -> StatusOnline.copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = when (ride.status) {
                                            RideStatus.TRIP_COMPLETED -> if (language == AppLanguage.BANGLA) "সম্পন্ন" else "Completed"
                                            RideStatus.CANCELLED -> if (language == AppLanguage.BANGLA) "বাতিল" else "Cancelled"
                                            else -> if (language == AppLanguage.BANGLA) "চলমান" else "In Progress"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (ride.status) {
                                            RideStatus.TRIP_COMPLETED -> StatusSuccess
                                            RideStatus.CANCELLED -> StatusDanger
                                            else -> StatusOnline
                                        }
                                    )
                                }
                            }

                            // Route Points
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(8.dp).background(TeslaGreenNeon, CircleShape))
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) ride.pickup.nameBn else ride.pickup.nameEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TeslaDarkTextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(8.dp).background(TeslaCyanAccent, CircleShape))
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) ride.destination.nameBn else ride.destination.nameEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TeslaDarkTextSecondary
                                    )
                                }
                            }

                            HorizontalDivider(color = TeslaDarkCardBorder)

                            // Fare & Driver info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TeslaDarkTextMuted
                                )
                                Text(
                                    text = Strings.bdt(ride.estimatedFare),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TeslaGreenNeon
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Detail Dialog
    selectedRideForReceipt?.let { r ->
        AlertDialog(
            onDismissRequest = { selectedRideForReceipt = null },
            containerColor = TeslaDarkSurface,
            title = {
                Text(
                    text = if (language == AppLanguage.BANGLA) "রাইড রসিদ (${r.id})" else "Trip Receipt (${r.id})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeslaGreenNeon
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("মোট ভাড়া (Fare):", color = TeslaDarkTextSecondary)
                        Text(Strings.bdt(r.estimatedFare), fontWeight = FontWeight.Bold, color = TeslaGreenNeon)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("দূরত্ব (Distance):", color = TeslaDarkTextSecondary)
                        Text(Strings.distanceKm(r.distanceKm, language), color = TeslaDarkTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("যানবাহন (Vehicle):", color = TeslaDarkTextSecondary)
                        Text(r.vehicleType.labelBn, color = TeslaDarkTextPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("পেমেন্ট পদ্ধতি:", color = TeslaDarkTextSecondary)
                        Text("নগদ টাকা (Cash)", color = TeslaCyanAccent, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("চালক:", color = TeslaDarkTextSecondary)
                        Text(r.driverName ?: "রফিকুল ইসলাম", color = TeslaDarkTextPrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRideForReceipt = null }) {
                    Text(if (language == AppLanguage.BANGLA) "ঠিক আছে" else "OK", color = TeslaGreenNeon)
                }
            }
        )
    }
}
