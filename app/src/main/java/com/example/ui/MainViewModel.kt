package com.example.ui

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.localization.AppLanguage
import com.example.data.model.*
import com.example.data.service.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AuthStep {
    SPLASH,
    PHONE_INPUT,
    OTP_INPUT,
    PROFILE_SETUP,
    AUTHENTICATED
}

enum class PassengerTab {
    HOME,
    RIDES,
    NOTIFICATIONS,
    PROFILE
}

enum class DriverTab {
    DASHBOARD,
    REQUESTS,
    TRIPS,
    EARNINGS,
    PROFILE
}

data class MainUiState(
    val language: AppLanguage = AppLanguage.BANGLA,
    val authStep: AuthStep = AuthStep.SPLASH,
    val enteredPhone: String = "",
    val enteredOtp: String = "",
    val verificationId: String = "",
    val otpError: String? = null,
    val userProfile: UserProfile? = null,
    val activeRole: UserRole = UserRole.PASSENGER,
    val passengerTab: PassengerTab = PassengerTab.HOME,
    val driverTab: DriverTab = DriverTab.DASHBOARD,
    val showDriverRegistration: Boolean = false,
    val showAdminDashboard: Boolean = false,
    val showChatModal: Boolean = false,
    val showCallDialog: Boolean = false,
    val callTargetName: String = "",
    val callTargetPhone: String = "",
    val selectedPickup: GeoPoint? = null,
    val selectedDestination: GeoPoint? = null,
    val selectedVehicle: VehicleType = VehicleType.AUTO,
    val passengerCount: Int = 1,
    val routeDistanceKm: Double? = null,
    val routeMinutes: Int? = null,
    val showDestinationPicker: Boolean = false,
    val activeRide: RideRequest? = null,
    val incomingDriverRequest: RideRequest? = null,
    val rideError: String? = null,
    val showRatingModal: Boolean = false,
    val ratingComment: String = ""
)

class MainViewModel : ViewModel() {

    val locationService = LocationService()
    val fareService = FareService()
    val driverService = DriverService(locationService)
    val rideService = RideService(locationService, driverService, fareService)
    val authService = AuthService()
    val userService = UserService(driverService)
    val notificationService = NotificationService()
    val chatService = ChatService()
    val paymentService = PaymentService()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        if (authService.currentUser() != null) {
            _uiState.update { it.copy(authStep = AuthStep.AUTHENTICATED) }
            userService.loadCurrentUserFromFirestore()
            authService.currentUser()?.uid?.let { driverService.loadCurrentDriverProfile(it) }
        }

        // Collect reactive state flows from services
        viewModelScope.launch {
            userService.currentUser.collect { user ->
                _uiState.update {
                    it.copy(
                        userProfile = user,
                        activeRole = user.activeRole
                    )
                }
                if (user.id != "LOCAL-USER") {
                    rideService.observePassengerRides(user.id, user.phone)
                }
            }
        }

        viewModelScope.launch {
            driverService.currentDriverProfile.collect { driver ->
                if (driver != null) {
                    rideService.observeDriverRides(driver.driverId)
                }
            }
        }

        viewModelScope.launch {
            rideService.activeRide.collect { ride ->
                _uiState.update { it.copy(activeRide = ride) }
            }
        }

        viewModelScope.launch {
            rideService.incomingDriverRequest.collect { req ->
                _uiState.update { it.copy(incomingDriverRequest = req) }
            }
        }

        viewModelScope.launch {
            rideService.rideError.collect { error ->
                _uiState.update { it.copy(rideError = error) }
            }
        }
    }

    override fun onCleared() {
        rideService.stopRealtimeSync()
        super.onCleared()
    }

    fun toggleLanguage() {
        val next = if (_uiState.value.language == AppLanguage.BANGLA) AppLanguage.ENGLISH else AppLanguage.BANGLA
        _uiState.update { it.copy(language = next) }
    }

    fun setPhoneInput(phone: String) {
        _uiState.update {
            it.copy(
                enteredPhone = phone,
                otpError = null,
                authStep = if (it.authStep == AuthStep.SPLASH) AuthStep.PHONE_INPUT else it.authStep
            )
        }
    }

    fun backToPhoneInput() {
        _uiState.update { it.copy(authStep = AuthStep.PHONE_INPUT, enteredOtp = "", otpError = null) }
    }

    fun setOtpInput(otp: String) {
        _uiState.update { it.copy(enteredOtp = otp, otpError = null) }
    }

    fun sendOtp(activity: android.app.Activity) {
        val phone = _uiState.value.enteredPhone
        authService.sendOtp(
            activity = activity,
            phone = phone,
            onCodeSent = { verificationId ->
                _uiState.update {
                    it.copy(
                        verificationId = verificationId,
                        authStep = AuthStep.OTP_INPUT,
                        otpError = null
                    )
                }
            },
            onError = { message ->
                _uiState.update { it.copy(otpError = message) }
            }
        )
    }

    fun signInWithGoogle(activity: ComponentActivity) {
        authService.signInWithGoogle(
            activity = activity,
            onSuccess = {
                val firebaseUser = authService.currentUser()
                val uid = firebaseUser?.uid
                if (uid != null) {
                    userService.setAuthenticatedIdentity(uid, firebaseUser.phoneNumber.orEmpty())
                    userService.syncCurrentUserToFirestore()
                    userService.loadCurrentUserFromFirestore()
                    driverService.loadCurrentDriverProfile(uid)
                    _uiState.update { it.copy(authStep = AuthStep.AUTHENTICATED, otpError = null) }
                } else {
                    _uiState.update { it.copy(otpError = "Google Sign-In succeeded but user session was not found.") }
                }
            },
            onError = { message -> _uiState.update { it.copy(otpError = message) } }
        )
    }

    fun verifyOtp() {
        val code = _uiState.value.enteredOtp
        authService.verifyOtp(
            verificationId = _uiState.value.verificationId,
            code = code,
            onSuccess = {
                val firebaseUser = authService.currentUser()
                val verifiedPhone = firebaseUser?.phoneNumber ?: _uiState.value.enteredPhone
                firebaseUser?.uid?.let { userService.setAuthenticatedIdentity(it, verifiedPhone) }
                userService.updateProfile(
                    name = userService.currentUser.value.name,
                    phone = verifiedPhone
                )
                userService.syncCurrentUserToFirestore()
                firebaseUser?.uid?.let { driverService.loadCurrentDriverProfile(it) }
                _uiState.update {
                    it.copy(
                        authStep = AuthStep.AUTHENTICATED,
                        otpError = null
                    )
                }
            },
            onError = { message ->
                _uiState.update { it.copy(otpError = message) }
            }
        )
    }

    fun selectPassengerTab(tab: PassengerTab) {
        _uiState.update { it.copy(passengerTab = tab) }
    }

    fun selectDriverTab(tab: DriverTab) {
        _uiState.update { it.copy(driverTab = tab) }
    }

    fun switchRole(role: UserRole) {
        if (role == UserRole.ADMIN) return
        if (role == UserRole.DRIVER &&
            userService.currentUser.value.driverStatus != DriverVerificationStatus.APPROVED
        ) return
        userService.switchRole(role)
        _uiState.update {
            it.copy(
                activeRole = role,
                showAdminDashboard = (role == UserRole.ADMIN),
                showDriverRegistration = false
            )
        }
    }

    fun openDriverRegistration() {
        _uiState.update { it.copy(showDriverRegistration = true) }
    }

    fun closeDriverRegistration() {
        _uiState.update { it.copy(showDriverRegistration = false) }
    }

    fun submitDriverRegistration(
        fullName: String,
        phone: String,
        vehicleType: VehicleType,
        vehicleNumber: String,
        nidNumber: String,
        licenseNumber: String,
        address: String,
        emergencyContact: String
    ) {
        driverService.submitDriverRegistration(
            userId = _uiState.value.userProfile?.id ?: "USR-1001",
            fullName = fullName,
            phone = phone,
            vehicleType = vehicleType,
            vehicleNumber = vehicleNumber,
            nidNumber = nidNumber,
            licenseNumber = licenseNumber,
            address = address,
            emergencyContact = emergencyContact
        )
        userService.onDriverApplicationSubmitted()
        _uiState.update { it.copy(showDriverRegistration = false) }
        notificationService.pushNotification(
            titleEn = "Driver Application Submitted",
            titleBn = "ড্রাইভার আবেদন জমা হয়েছে",
            messageEn = "Your verification documents are being reviewed by Kushtia BD TESLA team.",
            messageBn = "আপনার ড্রাইভার কাগজপত্র পর্যালোচনার জন্য জমা হয়েছে।"
        )
    }

    fun setVehicle(vehicleType: VehicleType) {
        _uiState.update { it.copy(selectedVehicle = vehicleType) }
    }

    fun setPassengerCount(count: Int) {
        _uiState.update { it.copy(passengerCount = count.coerceIn(1, 6)) }
    }

    fun setPickup(geoPoint: GeoPoint) {
        _uiState.update { it.copy(selectedPickup = geoPoint, routeDistanceKm = null, routeMinutes = null) }
    }

    fun setDestination(geoPoint: GeoPoint) {
        _uiState.update { it.copy(selectedDestination = geoPoint, showDestinationPicker = false, routeDistanceKm = null, routeMinutes = null) }
    }

    fun setRouteEstimate(distanceKm: Double, minutes: Int) {
        if (distanceKm > 0.0 && minutes > 0) {
            _uiState.update { it.copy(routeDistanceKm = distanceKm, routeMinutes = minutes) }
        }
    }

    fun toggleDestinationPicker(show: Boolean) {
        _uiState.update { it.copy(showDestinationPicker = show) }
    }

    // Passenger Ride Actions
    fun requestRide() {
        if (_uiState.value.routeDistanceKm == null || _uiState.value.routeMinutes == null) return
        if (_uiState.value.passengerCount > _uiState.value.selectedVehicle.maxPassengers) {
            rideService.reportError("Selected vehicle cannot carry this many passengers. Choose a larger vehicle.")
            return
        }
        val pickup = _uiState.value.selectedPickup ?: return
        val dest = _uiState.value.selectedDestination ?: return
        val user = _uiState.value.userProfile
        if (user == null || user.id == "LOCAL-USER") {
            rideService.reportError("Please sign in with Google before booking a ride.")
            return
        }
        rideService.requestRide(
            passengerId = user?.id ?: "LOCAL-USER",
            passengerName = user?.name ?: "Passenger",
            passengerPhone = user?.phone ?: _uiState.value.enteredPhone,
            pickup = pickup,
            destination = dest,
            vehicleType = _uiState.value.selectedVehicle,
            passengerCount = _uiState.value.passengerCount,
            routeDistanceKm = _uiState.value.routeDistanceKm,
            routeMinutes = _uiState.value.routeMinutes
        )
    }

    fun cancelRide() {
        rideService.cancelRide()
    }

    fun confirmPayment() {
        rideService.confirmPayment()
    }

    fun submitDriverRating(rating: Float) {
        rideService.submitDriverRating(rating, _uiState.value.ratingComment)
        _uiState.update { it.copy(showRatingModal = false, ratingComment = "") }
        rideService.dismissCompletedRide()
    }

    // Driver Ride Actions
    fun toggleDriverOnline(online: Boolean) {
        driverService.setDriverOnline(online)
    }

    fun driverAcceptRequest() {
        val currentDriver = driverService.currentDriverProfile.value ?: return
        rideService.driverAcceptRequest(currentDriver)
    }

    fun driverRejectRequest() {
        rideService.driverRejectRequest()
    }

    fun driverMarkArrived() {
        rideService.driverMarkArrived()
    }

    fun startTrip() {
        rideService.startTrip()
    }

    fun completeTrip() {
        rideService.completeTrip()
    }

    // Communication Actions
    fun openCallDialog(name: String, phone: String) {
        _uiState.update { it.copy(showCallDialog = true, callTargetName = name, callTargetPhone = phone) }
    }

    fun closeCallDialog() {
        _uiState.update { it.copy(showCallDialog = false) }
    }

    fun openChat() {
        _uiState.update { it.copy(showChatModal = true) }
    }

    fun closeChat() {
        _uiState.update { it.copy(showChatModal = false) }
    }

    fun sendChatMessage(text: String, isDriver: Boolean) {
        val rideId = _uiState.value.activeRide?.id ?: "BDT-ACTIVE"
        val sender = if (isDriver) "Driver" else (_uiState.value.userProfile?.name ?: "Passenger")
        chatService.sendMessage(rideId, sender, text, isDriver)
    }

    // Admin Controls
    fun adminApproveDriver(driverId: String) {
        driverService.approveDriver(driverId)
        if (_uiState.value.userProfile?.id == driverId || driverService.currentDriverProfile.value?.driverId == driverId) {
            userService.onDriverApproved()
        }
    }

    fun adminRejectDriver(driverId: String) {
        driverService.rejectDriver(driverId)
    }
}
