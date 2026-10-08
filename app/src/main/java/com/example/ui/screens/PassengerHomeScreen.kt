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
import com.example.data.model.*
import com.example.data.service.LocationService
import com.example.ui.components.BdTeslaMapCanvas
import com.example.ui.components.VehicleSelectorCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    pickup: GeoPoint?,
    destination: GeoPoint?,
    selectedVehicle: VehicleType,
    estimatedFareAuto: Double,
    estimatedFareRickshaw: Double,
    estimatedFareVan: Double,
    distanceKm: Double,
    estimatedMinutes: Int,
    activeRide: RideRequest?,
    locationService: LocationService,
    language: AppLanguage,
    onSelectPickup: (GeoPoint) -> Unit,
    onSelectDestination: (GeoPoint) -> Unit,
    onSelectVehicle: (VehicleType) -> Unit,
    onRequestRide: () -> Unit,
    onCancelRide: () -> Unit,
    onConfirmPayment: () -> Unit,
    onOpenRating: () -> Unit,
    onCallDriver: (String, String) -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLocationSheet by remember { mutableStateOf(false) }
    var pickingForDrop by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas layer
        BdTeslaMapCanvas(
            pickup = pickup,
            destination = destination,
            activeRide = activeRide,
            language = language,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Sheet / Card Overlay for Ride Booking or Active Ride
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (activeRide == null || activeRide.status == RideStatus.NONE) {
                // Booking Interface
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ride_booking_sheet"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Location Selector Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(TeslaDarkCard)
                                .border(1.dp, TeslaDarkCardBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Pickup row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        pickingForDrop = false
                                        showLocationSheet = true
                                    }
                                    .testTag("pickup_selector_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(TeslaGreenNeon)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Strings.pickupPoint(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TeslaDarkTextMuted
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) (pickup?.nameBn ?: "পিকআপ নির্বাচন করুন") else (pickup?.nameEn ?: "Select Pickup"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TeslaDarkTextPrimary
                                    )
                                }
                                Icon(Icons.Default.EditLocation, contentDescription = "Edit", tint = TeslaGreenNeon, modifier = Modifier.size(18.dp))
                            }

                            HorizontalDivider(color = TeslaDarkCardBorder)

                            // Destination row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        pickingForDrop = true
                                        showLocationSheet = true
                                    }
                                    .testTag("destination_selector_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(TeslaCyanAccent)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Strings.whereTo(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TeslaDarkTextMuted
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BANGLA) (destination?.nameBn ?: "গন্তব্য খুঁজুন") else (destination?.nameEn ?: "Select Destination"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TeslaCyanAccent
                                    )
                                }
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = TeslaCyanAccent, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Trip Estimation Info (Distance & ETA)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${Strings.distanceKm(distanceKm, language)} • ${Strings.minutes(estimatedMinutes, language)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeslaDarkTextSecondary
                            )
                            Text(
                                text = if (language == AppLanguage.BANGLA) "কুষ্টিয়া সদরে ন্যায্য মিটার রেট" else "Fair Kushtia Sadar rates",
                                style = MaterialTheme.typography.labelSmall,
                                color = TeslaGreenNeon
                            )
                        }

                        // Vehicle Selection Cards: Auto, Rickshaw, and Pakhi Van
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            VehicleSelectorCard(
                                vehicleType = VehicleType.AUTO,
                                isSelected = (selectedVehicle == VehicleType.AUTO),
                                estimatedFare = estimatedFareAuto,
                                estimatedMinutes = estimatedMinutes,
                                language = language,
                                onSelect = { onSelectVehicle(VehicleType.AUTO) }
                            )

                            VehicleSelectorCard(
                                vehicleType = VehicleType.RICKSHAW,
                                isSelected = (selectedVehicle == VehicleType.RICKSHAW),
                                estimatedFare = estimatedFareRickshaw,
                                estimatedMinutes = estimatedMinutes + 1,
                                language = language,
                                onSelect = { onSelectVehicle(VehicleType.RICKSHAW) }
                            )

                            VehicleSelectorCard(
                                vehicleType = VehicleType.PAKHI_VAN,
                                isSelected = (selectedVehicle == VehicleType.PAKHI_VAN),
                                estimatedFare = estimatedFareVan,
                                estimatedMinutes = estimatedMinutes + 2,
                                language = language,
                                onSelect = { onSelectVehicle(VehicleType.PAKHI_VAN) }
                            )
                        }

                        // Request Ride Action Button
                        Button(
                            onClick = onRequestRide,
                            colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("request_ride_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = TeslaDarkBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Strings.requestRide(language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeslaDarkBg
                            )
                        }
                    }
                }
            } else {
                // Active Ride State Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_ride_status_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface.copy(alpha = 0.98f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Status Header with animation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (activeRide.status == RideStatus.SEARCHING_DRIVER) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = TeslaGreenNeon,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (activeRide.status) {
                                                    RideStatus.TRIP_COMPLETED -> StatusSuccess
                                                    RideStatus.TRIP_STARTED -> StatusOnline
                                                    else -> StatusArriving
                                                }
                                            )
                                    )
                                }

                                Text(
                                    text = when (activeRide.status) {
                                        RideStatus.SEARCHING_DRIVER -> Strings.searchingDriver(language)
                                        RideStatus.DRIVER_ASSIGNED -> Strings.driverAssigned(language)
                                        RideStatus.DRIVER_ARRIVING -> Strings.driverArriving(language)
                                        RideStatus.DRIVER_ARRIVED -> Strings.driverArrived(language)
                                        RideStatus.TRIP_STARTED -> Strings.tripStarted(language)
                                        RideStatus.TRIP_COMPLETED -> Strings.tripCompleted(language)
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
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

                        // Driver Details if assigned
                        if (activeRide.driverName != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(TeslaDarkCard)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(TeslaGreenDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Driver",
                                            tint = TeslaGreenNeon,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = activeRide.driverName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TeslaDarkTextPrimary
                                        )
                                        Text(
                                            text = "${activeRide.vehicleNumber} • ${if (language == AppLanguage.BANGLA) activeRide.vehicleType.labelBn else activeRide.vehicleType.labelEn}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TeslaCyanAccent
                                        )
                                    }
                                }

                                // Call & Message Action Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            activeRide.driverPhone?.let { phone ->
                                                onCallDriver(activeRide.driverName, phone)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(TeslaGreenDark, CircleShape)
                                            .testTag("call_driver_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = onOpenChat,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(TeslaDarkCardBorder, CircleShape)
                                            .testTag("chat_driver_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "Chat",
                                            tint = TeslaCyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Trip Completion / Actions
                        if (activeRide.status == RideStatus.TRIP_COMPLETED) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!activeRide.isPaid) {
                                    Button(
                                        onClick = onConfirmPayment,
                                        colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("pay_cash_button")
                                    ) {
                                        Text(
                                            text = Strings.payCash(language),
                                            color = TeslaDarkBg,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = onOpenRating,
                                        colors = ButtonDefaults.buttonColors(containerColor = TeslaGoldAccent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("rate_driver_button")
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = TeslaDarkBg)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Strings.rateDriver(language),
                                            color = TeslaDarkBg,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = onCancelRide,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cancel_ride_button")
                            ) {
                                Text(Strings.cancelRide(language), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Kushtia Landmark Picker Modal Bottom Sheet
        if (showLocationSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLocationSheet = false },
                containerColor = TeslaDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = if (pickingForDrop) Strings.destinationPoint(language) else Strings.pickupPoint(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locationService.kushtiaHubs) { hub ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (pickingForDrop) {
                                            onSelectDestination(hub)
                                        } else {
                                            onSelectPickup(hub)
                                        }
                                        showLocationSheet = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = TeslaDarkCard)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (pickingForDrop) TeslaCyanAccent else TeslaGreenNeon
                                    )
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.BANGLA) hub.nameBn else hub.nameEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TeslaDarkTextPrimary
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BANGLA) hub.addressBn else hub.addressEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TeslaDarkTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
