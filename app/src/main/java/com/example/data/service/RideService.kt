package com.example.data.service

import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class RideService(
    private val locationService: LocationService,
    private val driverService: DriverService,
    private val fareService: FareService
) {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var trackingJob: Job? = null

    // The currently active ride for either Passenger or Driver
    private val _activeRide = MutableStateFlow<RideRequest?>(null)
    val activeRide: StateFlow<RideRequest?> = _activeRide.asStateFlow()

    // Incoming request specifically offered to the current driver (for Driver Mode modal)
    private val _incomingDriverRequest = MutableStateFlow<RideRequest?>(null)
    val incomingDriverRequest: StateFlow<RideRequest?> = _incomingDriverRequest.asStateFlow()

    // Completed & past rides history
    private val _rideHistory = MutableStateFlow<List<RideRequest>>(emptyList())
    val rideHistory: StateFlow<List<RideRequest>> = _rideHistory.asStateFlow()

    init {
        // Seed sample completed rides for Kushtia history
        val pastRides = listOf(
            RideRequest(
                id = "BDT-7821",
                passengerId = "USER-01",
                passengerName = "তানভীর আহমেদ (Tanvir)",
                passengerPhone = "01700-112233",
                driverId = "DRV-101",
                driverName = "মো. রফিকুল ইসলাম (Rafiq)",
                driverPhone = "01712-345678",
                vehicleType = VehicleType.AUTO,
                vehicleNumber = "কুষ্টিয়া-থ ১১-৩৪০১",
                pickup = locationService.kushtiaHubs[0], // Majompur Gate
                destination = locationService.kushtiaHubs[3], // Medical College
                estimatedFare = 85.0,
                distanceKm = 4.2,
                estimatedMinutes = 12,
                status = RideStatus.TRIP_COMPLETED,
                isPaid = true,
                driverRating = 5.0f,
                passengerRating = 5.0f,
                createdAt = System.currentTimeMillis() - 86400000L,
                completedAt = System.currentTimeMillis() - 86400000L + 900000L
            ),
            RideRequest(
                id = "BDT-7740",
                passengerId = "USER-01",
                passengerName = "তানভীর আহমেদ (Tanvir)",
                passengerPhone = "01700-112233",
                driverId = "DRV-102",
                driverName = "আব্দুল কাদের (Kader)",
                driverPhone = "01815-998877",
                vehicleType = VehicleType.PAKHI_VAN,
                vehicleNumber = "কুষ্টিয়া-ভ ০৭-১২৩৪",
                pickup = locationService.kushtiaHubs[1], // NS Road Bazaar
                destination = locationService.kushtiaHubs[2], // Court Station
                estimatedFare = 45.0,
                distanceKm = 2.1,
                estimatedMinutes = 7,
                status = RideStatus.TRIP_COMPLETED,
                isPaid = true,
                driverRating = 4.8f,
                passengerRating = 5.0f,
                createdAt = System.currentTimeMillis() - 172800000L,
                completedAt = System.currentTimeMillis() - 172800000L + 600000L
            ),
            RideRequest(
                id = "BDT-7655",
                passengerId = "USER-01",
                passengerName = "তানভীর আহমেদ (Tanvir)",
                passengerPhone = "01700-112233",
                driverId = "DRV-104",
                driverName = "মো. কামাল হোসেন (Kamal)",
                driverPhone = "01733-445566",
                vehicleType = VehicleType.RICKSHAW,
                vehicleNumber = "কুষ্টিয়া-র ০৪-৫৬৭৮",
                pickup = locationService.kushtiaHubs[0], // Majompur Gate
                destination = locationService.kushtiaHubs[1], // NS Road Bazaar
                estimatedFare = 30.0,
                distanceKm = 1.2,
                estimatedMinutes = 5,
                status = RideStatus.TRIP_COMPLETED,
                isPaid = true,
                driverRating = 5.0f,
                passengerRating = 5.0f,
                createdAt = System.currentTimeMillis() - 259200000L,
                completedAt = System.currentTimeMillis() - 259200000L + 400000L
            )
        )
        _rideHistory.value = pastRides
    }

    fun requestRide(
        passengerId: String,
        passengerName: String,
        passengerPhone: String,
        pickup: GeoPoint,
        destination: GeoPoint,
        vehicleType: VehicleType
    ): RideRequest {
        val distance = locationService.calculateDistanceKm(pickup, destination)
        val minutes = locationService.estimateMinutes(distance)
        val fare = fareService.calculateEstimatedFare(vehicleType, distance)

        val ride = RideRequest(
            id = "BDT-${(System.currentTimeMillis() % 100000)}",
            passengerId = passengerId,
            passengerName = passengerName,
            passengerPhone = passengerPhone,
            vehicleType = vehicleType,
            pickup = pickup,
            destination = destination,
            estimatedFare = fare,
            distanceKm = distance,
            estimatedMinutes = minutes,
            status = RideStatus.SEARCHING_DRIVER,
            driverLocation = pickup // initial
        )
        _activeRide.value = ride

        // Driver Matching Flow:
        // Find if the current active driver in app can receive it, or assign a simulated nearby driver
        val currentDriver = driverService.currentDriverProfile.value
        if (currentDriver != null && currentDriver.isOnline && currentDriver.vehicleType == vehicleType && currentDriver.verificationStatus == DriverVerificationStatus.APPROVED) {
            // Offer to current driver mode!
            _incomingDriverRequest.value = ride
        }

        // Also initiate automated driver search fallback for passenger simulation
        simulateDriverSearchAndArrival(ride)

        return ride
    }

    private fun simulateDriverSearchAndArrival(ride: RideRequest) {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            delay(3500) // 3.5s searching time
            if (_activeRide.value?.id != ride.id) return@launch
            if (_activeRide.value?.status != RideStatus.SEARCHING_DRIVER) return@launch

            // If not accepted manually, find nearby mock driver
            val candidateDriver = driverService.allDrivers.value.firstOrNull {
                it.vehicleType == ride.vehicleType && it.verificationStatus == DriverVerificationStatus.APPROVED
            } ?: driverService.allDrivers.value.first()

            val startDriverLoc = candidateDriver.currentLocation
            val assignedRide = ride.copy(
                driverId = candidateDriver.driverId,
                driverName = candidateDriver.fullName,
                driverPhone = candidateDriver.phone,
                vehicleNumber = candidateDriver.vehicleNumber,
                status = RideStatus.DRIVER_ASSIGNED,
                driverLocation = startDriverLoc
            )
            _activeRide.value = assignedRide

            delay(2000)
            if (_activeRide.value?.id != ride.id) return@launch
            _activeRide.value = _activeRide.value?.copy(status = RideStatus.DRIVER_ARRIVING)

            // Simulate driver arriving at pickup over 6 steps
            val steps = 6
            for (i in 1..steps) {
                delay(1800)
                if (_activeRide.value?.id != ride.id || _activeRide.value?.status != RideStatus.DRIVER_ARRIVING) return@launch
                val fraction = i / steps.toFloat()
                val currentLoc = locationService.interpolate(startDriverLoc, ride.pickup, fraction)
                _activeRide.value = _activeRide.value?.copy(driverLocation = currentLoc)
            }

            _activeRide.value = _activeRide.value?.copy(
                status = RideStatus.DRIVER_ARRIVED,
                driverLocation = ride.pickup
            )
        }
    }

    // Driver accepts incoming request
    fun driverAcceptRequest(driver: DriverProfile) {
        val ride = _incomingDriverRequest.value ?: _activeRide.value ?: return
        _incomingDriverRequest.value = null
        val accepted = ride.copy(
            driverId = driver.driverId,
            driverName = driver.fullName,
            driverPhone = driver.phone,
            vehicleNumber = driver.vehicleNumber,
            status = RideStatus.DRIVER_ASSIGNED,
            driverLocation = driver.currentLocation
        )
        _activeRide.value = accepted
    }

    // Driver rejects incoming request
    fun driverRejectRequest() {
        _incomingDriverRequest.value = null
    }

    // Driver action: arrived at pickup
    fun driverMarkArrived() {
        _activeRide.value = _activeRide.value?.copy(
            status = RideStatus.DRIVER_ARRIVED,
            driverLocation = _activeRide.value?.pickup
        )
    }

    // Driver or passenger action: start trip
    fun startTrip() {
        val current = _activeRide.value ?: return
        _activeRide.value = current.copy(
            status = RideStatus.TRIP_STARTED,
            driverLocation = current.pickup
        )
        // Simulate trip progression from pickup to destination
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            val steps = 8
            for (i in 1..steps) {
                delay(2200)
                if (_activeRide.value?.id != current.id || _activeRide.value?.status != RideStatus.TRIP_STARTED) return@launch
                val fraction = i / steps.toFloat()
                val intermediate = locationService.interpolate(current.pickup, current.destination, fraction)
                _activeRide.value = _activeRide.value?.copy(driverLocation = intermediate)
            }
        }
    }

    // Complete trip
    fun completeTrip() {
        val current = _activeRide.value ?: return
        trackingJob?.cancel()
        val completed = current.copy(
            status = RideStatus.TRIP_COMPLETED,
            driverLocation = current.destination,
            completedAt = System.currentTimeMillis()
        )
        _activeRide.value = completed
        _rideHistory.value = listOf(completed) + _rideHistory.value

        current.driverId?.let { drvId ->
            driverService.recordCompletedTrip(drvId, current.estimatedFare)
        }
    }

    // Pay cash
    fun confirmPayment() {
        _activeRide.value = _activeRide.value?.copy(isPaid = true)
        _rideHistory.value = _rideHistory.value.map {
            if (it.id == _activeRide.value?.id) it.copy(isPaid = true) else it
        }
    }

    // Rate driver
    fun submitDriverRating(rating: Float, comment: String? = null) {
        val current = _activeRide.value ?: return
        val rated = current.copy(driverRating = rating)
        _activeRide.value = rated
        _rideHistory.value = _rideHistory.value.map {
            if (it.id == current.id) it.copy(driverRating = rating) else it
        }
    }

    // Rate passenger
    fun submitPassengerRating(rating: Float) {
        val current = _activeRide.value ?: return
        val rated = current.copy(passengerRating = rating)
        _activeRide.value = rated
        _rideHistory.value = _rideHistory.value.map {
            if (it.id == current.id) it.copy(passengerRating = rating) else it
        }
    }

    fun dismissCompletedRide() {
        _activeRide.value = null
    }

    fun cancelRide() {
        trackingJob?.cancel()
        val current = _activeRide.value ?: return
        val cancelled = current.copy(status = RideStatus.CANCELLED)
        _rideHistory.value = listOf(cancelled) + _rideHistory.value
        _activeRide.value = null
        _incomingDriverRequest.value = null
    }
}
