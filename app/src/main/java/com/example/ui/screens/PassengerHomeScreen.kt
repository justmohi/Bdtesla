package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
    onRouteCalculated: (Double, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showLocationSheet by remember { mutableStateOf(false) }
    var pickingForDrop by remember { mutableStateOf(true) }
    var locationQuery by remember { mutableStateOf("") }
    var locationResults by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var locationSearchError by remember { mutableStateOf<String?>(null) }
    var isSearchingLocation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationSearchScope = rememberCoroutineScope()
    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }

    fun applyDeviceLocation(latitude: Double, longitude: Double) {
        if (latitude !in 23.55..24.10 || longitude !in 88.65..89.35) {
            locationSearchError = if (language == AppLanguage.BANGLA)
                "আপনার বর্তমান অবস্থান কুষ্টিয়া জেলার বাইরে। কুষ্টিয়ার ভেতরের পিকআপ নির্বাচন করুন।"
            else "Your current location appears to be outside Kushtia District."
            return
        }
        isSearchingLocation = true
        locationSearchScope.launch {
            val point = withContext(Dispatchers.IO) {
                runCatching {
                    val geocoder = Geocoder(context, Locale("en", "BD"))
                    @Suppress("DEPRECATION")
                    val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                    val label = address?.featureName?.takeIf { it.isNotBlank() }
                        ?: address?.locality?.takeIf { it.isNotBlank() }
                        ?: if (language == AppLanguage.BANGLA) "বর্তমান অবস্থান" else "Current location"
                    val addressLine = address?.getAddressLine(0) ?: label
                    GeoPoint(
                        latitude = latitude,
                        longitude = longitude,
                        nameEn = label,
                        nameBn = label,
                        addressEn = addressLine,
                        addressBn = addressLine
                    )
                }.getOrNull()
            }
            isSearchingLocation = false
            if (point == null) {
                locationSearchError = if (language == AppLanguage.BANGLA)
                    "বর্তমান অবস্থানের ঠিকানা পাওয়া যায়নি। আবার চেষ্টা করুন।"
                else "Could not resolve your current location. Try again."
            } else {
                if (pickingForDrop) onSelectDestination(point) else onSelectPickup(point)
                showLocationSheet = false
                locationQuery = ""
                locationResults = emptyList()
                locationSearchError = null
            }
        }
    }

    fun fetchDeviceLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            locationSearchError = if (language == AppLanguage.BANGLA)
                "বর্তমান অবস্থান নিতে Location permission দিন।"
            else "Allow location permission to use your current location."
            return
        }
        isSearchingLocation = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location == null) {
                    isSearchingLocation = false
                    locationSearchError = if (language == AppLanguage.BANGLA)
                        "GPS অবস্থান পাওয়া যায়নি। ফোনের Location চালু করে আবার চেষ্টা করুন।"
                    else "GPS location unavailable. Turn on Location and try again."
                } else {
                    applyDeviceLocation(location.latitude, location.longitude)
                }
            }
            .addOnFailureListener {
                isSearchingLocation = false
                locationSearchError = if (language == AppLanguage.BANGLA)
                    "GPS থেকে অবস্থান নেওয়া যায়নি। আবার চেষ্টা করুন।"
                else "Could not read GPS location. Please try again."
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            fetchDeviceLocation()
        } else {
            locationSearchError = if (language == AppLanguage.BANGLA)
                "লোকেশন পারমিশন দেওয়া হয়নি। ঠিকানা লিখে পিকআপ নির্বাচন করুন।"
            else "Location permission was denied. Search for the pickup address instead."
        }
    }

    fun searchKushtiaLocations() {
        val query = locationQuery.trim()
        if (query.length < 3) {
            locationSearchError = if (language == AppLanguage.BANGLA)
                "কমপক্ষে ৩টি অক্ষর দিয়ে জায়গার নাম লিখুন।"
            else "Enter at least 3 characters to search."
            locationResults = emptyList()
            return
        }
        isSearchingLocation = true
        locationSearchError = null
        locationSearchScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val geocoder = Geocoder(context, Locale("en", "BD"))
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 8).orEmpty()
                        .filter { address ->
                            val lat = address.latitude
                            val lon = address.longitude
                            // Approximate outer bounds of Kushtia District; do not offer results outside the district.
                            lat in 23.55..24.10 && lon in 88.65..89.35
                        }
                        .distinctBy { "${it.latitude},${it.longitude}" }
                        .map { address ->
                            val line = address.getAddressLine(0)
                                ?: listOfNotNull(address.featureName, address.locality, address.adminArea)
                                    .distinct().joinToString(", ")
                            val label = address.featureName?.takeIf { it.isNotBlank() }
                                ?: address.subLocality?.takeIf { it.isNotBlank() }
                                ?: address.locality?.takeIf { it.isNotBlank() }
                                ?: query
                            GeoPoint(
                                latitude = address.latitude,
                                longitude = address.longitude,
                                nameEn = label,
                                nameBn = label,
                                addressEn = line,
                                addressBn = line
                            )
                        }
                }
            }
            isSearchingLocation = false
            result.onSuccess { found ->
                locationResults = found
                locationSearchError = if (found.isEmpty()) {
                    if (language == AppLanguage.BANGLA)
                        "কুষ্টিয়া জেলার মধ্যে জায়গাটি পাওয়া যায়নি। অন্য নামে খুঁজুন।"
                    else "No matching place was found within Kushtia District. Try another name."
                } else null
            }.onFailure {
                locationResults = emptyList()
                locationSearchError = if (language == AppLanguage.BANGLA)
                    "লোকেশন খোঁজা যাচ্ছে না। ইন্টারনেট সংযোগ পরীক্ষা করে আবার চেষ্টা করুন।"
                else "Location search failed. Check your internet connection and try again."
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas layer
        BdTeslaMapCanvas(
            pickup = pickup,
            destination = destination,
            activeRide = activeRide,
            language = language,
            onRouteCalculated = onRouteCalculated,
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
                onDismissRequest = {
                    showLocationSheet = false
                    locationQuery = ""
                    locationResults = emptyList()
                    locationSearchError = null
                },
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = locationQuery,
                            onValueChange = {
                                locationQuery = it
                                locationSearchError = null
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    if (language == AppLanguage.BANGLA)
                                        "এলাকা, বাজার, গ্রাম বা ঠিকানা"
                                    else "Area, market, village or address"
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { searchKushtiaLocations() },
                            enabled = !isSearchingLocation,
                            colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSearchingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = TeslaDarkBg
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TeslaDarkBg)
                            }
                        }
                    }

                    Text(
                        text = if (language == AppLanguage.BANGLA)
                            "কুষ্টিয়া জেলার যেকোনো জায়গা খুঁজুন, অথবা নিচের পরিচিত স্থান বেছে নিন।"
                        else "Search for a place anywhere within Kushtia District, or choose a known landmark below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TeslaDarkTextSecondary
                    )

                    OutlinedButton(
                        onClick = {
                            val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (fineGranted || coarseGranted) {
                                fetchDeviceLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !isSearchingLocation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (language == AppLanguage.BANGLA) "আমার বর্তমান অবস্থান ব্যবহার করুন"
                            else "Use my current location"
                        )
                    }

                    if (locationSearchError != null) {
                        Text(
                            text = locationSearchError.orEmpty(),
                            color = StatusDanger,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(locationResults) { result ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (pickingForDrop) onSelectDestination(result)
                                        else onSelectPickup(result)
                                        showLocationSheet = false
                                        locationQuery = ""
                                        locationResults = emptyList()
                                        locationSearchError = null
                                    },
                                colors = CardDefaults.cardColors(containerColor = TeslaDarkCard)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TeslaCyanAccent)
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.BANGLA) result.nameBn else result.nameEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TeslaDarkTextPrimary
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BANGLA) result.addressBn else result.addressEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TeslaDarkTextSecondary
                                        )
                                    }
                                }
                            }
                        }
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
