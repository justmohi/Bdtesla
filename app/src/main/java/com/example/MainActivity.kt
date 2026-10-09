package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.example.data.model.UserRole
import com.example.data.model.VehicleType
import com.example.ui.*
import com.example.ui.components.BdTeslaBottomNavigation
import com.example.ui.components.BdTeslaTopBar
import com.example.ui.components.CallModalDialog
import com.example.ui.components.ChatBottomSheet
import com.example.ui.components.RatingModalDialog
import com.example.ui.screens.*
import com.example.ui.theme.BdTeslaTheme
import com.example.ui.theme.TeslaDarkBg

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BdTeslaTheme(darkTheme = true) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val ridesHistory by viewModel.rideService.rideHistory.collectAsStateWithLifecycle()
                val driverProfile by viewModel.driverService.currentDriverProfile.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatService.messages.collectAsStateWithLifecycle()
                val notifications by viewModel.notificationService.notifications.collectAsStateWithLifecycle()

                val driverLocationClient = remember {
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                }
                var driverLocationPermissionGranted by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    )
                }
                val driverLocationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { grants ->
                    driverLocationPermissionGranted =
                        grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (!driverLocationPermissionGranted && driverProfile?.isOnline == true) {
                        viewModel.toggleDriverOnline(false)
                    }
                }
                val driverLocationRequest = remember {
                    LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
                        .setMinUpdateIntervalMillis(3_000L)
                        .build()
                }
                val driverLocationCallback = remember(viewModel) {
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            result.locations.forEach { location ->
                                viewModel.driverService.updateCurrentLocation(location.latitude, location.longitude)
                                viewModel.driverService.currentDriverProfile.value?.driverId?.let { driverId ->
                                    viewModel.rideService.updateDriverLocation(driverId, location.latitude, location.longitude)
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(driverProfile?.isOnline, driverLocationPermissionGranted) {
                    if (driverProfile?.isOnline == true && !driverLocationPermissionGranted) {
                        driverLocationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                DisposableEffect(driverProfile?.isOnline, driverLocationPermissionGranted, driverLocationCallback) {
                    if (driverProfile?.isOnline == true && driverLocationPermissionGranted) {
                        runCatching {
                            driverLocationClient.requestLocationUpdates(
                                driverLocationRequest,
                                driverLocationCallback,
                                Looper.getMainLooper()
                            )
                        }
                    }
                    onDispose {
                        driverLocationClient.removeLocationUpdates(driverLocationCallback)
                    }
                }

                // Calculate estimated fares
                val pickup = uiState.selectedPickup
                val dest = uiState.selectedDestination
                val hasRealRoute = uiState.routeDistanceKm != null && uiState.routeMinutes != null
                val distanceKm = uiState.routeDistanceKm ?: 0.0
                val estMinutes = uiState.routeMinutes ?: 0
                val autoFare = if (hasRealRoute) viewModel.fareService.calculateEstimatedFare(VehicleType.AUTO, distanceKm) else 0.0
                val rickshawFare = if (hasRealRoute) viewModel.fareService.calculateEstimatedFare(VehicleType.RICKSHAW, distanceKm) else 0.0
                val vanFare = if (hasRealRoute) viewModel.fareService.calculateEstimatedFare(VehicleType.PAKHI_VAN, distanceKm) else 0.0

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TeslaDarkBg
                ) {
                    when (uiState.authStep) {
                        AuthStep.SPLASH -> {
                            SplashScreen(
                                language = uiState.language,
                                onGetStarted = { viewModel.setPhoneInput("") }
                            )
                        }

                        AuthStep.PHONE_INPUT, AuthStep.OTP_INPUT, AuthStep.PROFILE_SETUP -> {
                            AuthScreen(
                                currentStep = uiState.authStep,
                                phone = uiState.enteredPhone,
                                otp = uiState.enteredOtp,
                                otpError = uiState.otpError,
                                language = uiState.language,
                                onPhoneChange = { viewModel.setPhoneInput(it) },
                                onOtpChange = { viewModel.setOtpInput(it) },
                                onSendOtp = { viewModel.sendOtp(this@MainActivity) },
                                onBackToPhone = { viewModel.backToPhoneInput() },
                                onVerifyOtp = { viewModel.verifyOtp() },
                                onGoogleSignIn = { viewModel.signInWithGoogle(this@MainActivity) }
                            )
                        }

                        AuthStep.AUTHENTICATED -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = TeslaDarkBg,
                                topBar = {
                                    if (!uiState.showAdminDashboard && !uiState.showDriverRegistration) {
                                        BdTeslaTopBar(
                                            activeRole = uiState.activeRole,
                                            driverStatus = uiState.userProfile?.driverStatus
                                                ?: com.example.data.model.DriverVerificationStatus.NOT_APPLIED,
                                            language = uiState.language,
                                            onRoleSelected = { viewModel.switchRole(it) },
                                            onToggleLanguage = { viewModel.toggleLanguage() },
                                            onOpenDriverRegistration = { viewModel.openDriverRegistration() }
                                        )
                                    }
                                },
                                bottomBar = {
                                    if (!uiState.showAdminDashboard && !uiState.showDriverRegistration) {
                                        BdTeslaBottomNavigation(
                                            activeRole = uiState.activeRole,
                                            passengerTab = uiState.passengerTab,
                                            driverTab = uiState.driverTab,
                                            language = uiState.language,
                                            onSelectPassengerTab = { viewModel.selectPassengerTab(it) },
                                            onSelectDriverTab = { viewModel.selectDriverTab(it) }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    if (uiState.showAdminDashboard) {
                                        AdminDashboardScreen(
                                            driverService = viewModel.driverService,
                                            fareService = viewModel.fareService,
                                            language = uiState.language,
                                            onApproveDriver = { viewModel.adminApproveDriver(it) },
                                            onRejectDriver = { viewModel.adminRejectDriver(it) },
                                            onCloseAdmin = { viewModel.switchRole(UserRole.PASSENGER) }
                                        )
                                    } else if (uiState.showDriverRegistration) {
                                        DriverRegistrationScreen(
                                            currentStatus = uiState.userProfile?.driverStatus
                                                ?: com.example.data.model.DriverVerificationStatus.NOT_APPLIED,
                                            existingProfile = driverProfile,
                                            language = uiState.language,
                                            onSubmit = { fullName, phone, vehicleType, vehicleNumber, nidNumber, licenseNumber, address, emergencyContact ->
                                                viewModel.submitDriverRegistration(
                                                    fullName,
                                                    phone,
                                                    vehicleType,
                                                    vehicleNumber,
                                                    nidNumber,
                                                    licenseNumber,
                                                    address,
                                                    emergencyContact
                                                )
                                            },
                                            onBack = { viewModel.closeDriverRegistration() },
                                            onSwitchToDriverMode = {
                                                viewModel.switchRole(UserRole.DRIVER)
                                            }
                                        )
                                    } else if (uiState.activeRole == UserRole.PASSENGER) {
                                        // Passenger Screen Tabs
                                        when (uiState.passengerTab) {
                                            PassengerTab.HOME -> {
                                                PassengerHomeScreen(
                                                    pickup = uiState.selectedPickup,
                                                    destination = uiState.selectedDestination,
                                                    selectedVehicle = uiState.selectedVehicle,
                                                    estimatedFareAuto = autoFare,
                                                    estimatedFareRickshaw = rickshawFare,
                                                    estimatedFareVan = vanFare,
                                                    distanceKm = distanceKm,
                                                    estimatedMinutes = estMinutes,
                                                    activeRide = uiState.activeRide,
                                                    rideError = uiState.rideError,
                                                    locationService = viewModel.locationService,
                                                    language = uiState.language,
                                                    onSelectPickup = { viewModel.setPickup(it) },
                                                    onSelectDestination = { viewModel.setDestination(it) },
                                                    onSelectVehicle = { viewModel.setVehicle(it) },
                                                    onRequestRide = { viewModel.requestRide() },
                                                    onCancelRide = { viewModel.cancelRide() },
                                                    onConfirmPayment = { viewModel.confirmPayment() },
                                                    onOpenRating = {
                                                        // Trigger rating modal
                                                    },
                                                    onCallDriver = { name, phone ->
                                                        viewModel.openCallDialog(name, phone)
                                                    },
                                                    onOpenChat = { viewModel.openChat() },
                                                    onRouteCalculated = { km, minutes -> viewModel.setRouteEstimate(km, minutes) },
                                                    hasRealRoute = uiState.routeDistanceKm != null
                                                )
                                            }

                                            PassengerTab.RIDES -> {
                                                RidesHistoryScreen(
                                                    rides = ridesHistory,
                                                    language = uiState.language
                                                )
                                            }

                                            PassengerTab.NOTIFICATIONS -> {
                                                NotificationsScreen(
                                                    notifications = notifications,
                                                    language = uiState.language
                                                )
                                            }

                                            PassengerTab.PROFILE -> {
                                                ProfileScreen(
                                                    user = uiState.userProfile,
                                                    driverProfile = driverProfile,
                                                    activeRole = uiState.activeRole,
                                                    language = uiState.language,
                                                    onSwitchRole = { viewModel.switchRole(it) },
                                                    onOpenDriverRegistration = { viewModel.openDriverRegistration() },
                                                    onToggleLanguage = { viewModel.toggleLanguage() }
                                                )
                                            }
                                        }
                                    } else if (uiState.activeRole == UserRole.DRIVER) {
                                        // Driver Screen Tabs
                                        when (uiState.driverTab) {
                                            DriverTab.DASHBOARD -> {
                                                DriverDashboardScreen(
                                                    driverProfile = driverProfile,
                                                    activeRide = uiState.activeRide,
                                                    incomingRequest = uiState.incomingDriverRequest,
                                                    language = uiState.language,
                                                    onToggleOnline = { viewModel.toggleDriverOnline(it) },
                                                    onAcceptRequest = { viewModel.driverAcceptRequest() },
                                                    onRejectRequest = { viewModel.driverRejectRequest() },
                                                    onMarkArrived = { viewModel.driverMarkArrived() },
                                                    onStartTrip = { viewModel.startTrip() },
                                                    onCompleteTrip = { viewModel.completeTrip() },
                                                    onCallPassenger = { name, phone ->
                                                        viewModel.openCallDialog(name, phone)
                                                    },
                                                    onOpenChat = { viewModel.openChat() }
                                                )
                                            }

                                            DriverTab.REQUESTS -> {
                                                DriverRequestsTab(
                                                    incomingRequest = uiState.incomingDriverRequest,
                                                    isOnline = driverProfile?.isOnline ?: false,
                                                    language = uiState.language,
                                                    onAccept = { viewModel.driverAcceptRequest() },
                                                    onReject = { viewModel.driverRejectRequest() }
                                                )
                                            }

                                            DriverTab.TRIPS -> {
                                                RidesHistoryScreen(
                                                    rides = ridesHistory,
                                                    language = uiState.language
                                                )
                                            }

                                            DriverTab.EARNINGS -> {
                                                DriverEarningsTab(
                                                    driverProfile = driverProfile,
                                                    language = uiState.language
                                                )
                                            }

                                            DriverTab.PROFILE -> {
                                                ProfileScreen(
                                                    user = uiState.userProfile,
                                                    driverProfile = driverProfile,
                                                    activeRole = uiState.activeRole,
                                                    language = uiState.language,
                                                    onSwitchRole = { viewModel.switchRole(it) },
                                                    onOpenDriverRegistration = { viewModel.openDriverRegistration() },
                                                    onToggleLanguage = { viewModel.toggleLanguage() }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Call Dialog
                    if (uiState.showCallDialog) {
                        CallModalDialog(
                            calleeName = uiState.callTargetName,
                            phoneNumber = uiState.callTargetPhone,
                            language = uiState.language,
                            onDismiss = { viewModel.closeCallDialog() }
                        )
                    }

                    // Floating In-App Live Chat Bottom Sheet
                    if (uiState.showChatModal) {
                        ChatBottomSheet(
                            messages = chatMessages,
                            currentUserName = uiState.userProfile?.name ?: "User",
                            language = uiState.language,
                            onSendMessage = { text ->
                                val isDriver = (uiState.activeRole == UserRole.DRIVER)
                                viewModel.sendChatMessage(text, isDriver)
                            },
                            onDismiss = { viewModel.closeChat() }
                        )
                    }

                    // Floating Rating Dialog
                    if (uiState.activeRide != null && uiState.activeRide?.status == com.example.data.model.RideStatus.TRIP_COMPLETED && uiState.activeRide?.isPaid == true && uiState.activeRide?.driverRating == null) {
                        RatingModalDialog(
                            driverName = uiState.activeRide?.driverName ?: "রফিকুল ইসলাম",
                            vehicleType = if (uiState.language == com.example.data.localization.AppLanguage.BANGLA)
                                (uiState.activeRide?.vehicleType?.labelBn ?: "অটো")
                            else
                                (uiState.activeRide?.vehicleType?.labelEn ?: "Auto"),
                            fare = uiState.activeRide?.estimatedFare ?: 80.0,
                            language = uiState.language,
                            onSubmit = { rating ->
                                viewModel.submitDriverRating(rating)
                            },
                            onDismiss = {
                                viewModel.submitDriverRating(5.0f)
                            }
                        )
                    }
                }
            }
        }
    }
}
