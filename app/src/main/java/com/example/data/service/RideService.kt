package com.example.data.service

import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
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
    private val db: FirebaseFirestore? by lazy { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }
    private var passengerRideListener: ListenerRegistration? = null
    private var driverRideListener: ListenerRegistration? = null

    private fun persistRide(ride: RideRequest) {
        val data = mapOf(
            "id" to ride.id,
            "passengerId" to ride.passengerId,
            "passengerName" to ride.passengerName,
            "passengerPhone" to ride.passengerPhone,
            "driverId" to ride.driverId,
            "driverName" to ride.driverName,
            "driverPhone" to ride.driverPhone,
            "vehicleType" to ride.vehicleType.name,
            "vehicleNumber" to ride.vehicleNumber,
            "pickup" to mapOf("latitude" to ride.pickup.latitude, "longitude" to ride.pickup.longitude, "nameEn" to ride.pickup.nameEn, "nameBn" to ride.pickup.nameBn),
            "destination" to mapOf("latitude" to ride.destination.latitude, "longitude" to ride.destination.longitude, "nameEn" to ride.destination.nameEn, "nameBn" to ride.destination.nameBn),
            "estimatedFare" to ride.estimatedFare,
            "distanceKm" to ride.distanceKm,
            "estimatedMinutes" to ride.estimatedMinutes,
            "status" to ride.status.name,
            "driverLocation" to ride.driverLocation?.let { mapOf("latitude" to it.latitude, "longitude" to it.longitude) },
            "paymentMethod" to ride.paymentMethod,
            "isPaid" to ride.isPaid,
            "driverRating" to ride.driverRating,
            "passengerRating" to ride.passengerRating,
            "createdAt" to ride.createdAt,
            "completedAt" to ride.completedAt,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db?.collection("rides")?.document(ride.id)?.set(data)
    }
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

    // Ride history is populated only from authenticated Firestore data.

    private fun mapRide(document: com.google.firebase.firestore.DocumentSnapshot): RideRequest? {
        val d = document.data ?: return null
        fun point(key: String): GeoPoint? {
            val p = d[key] as? Map<*, *> ?: return null
            return GeoPoint(
                latitude = (p["latitude"] as? Number)?.toDouble() ?: return null,
                longitude = (p["longitude"] as? Number)?.toDouble() ?: return null,
                nameEn = p["nameEn"] as? String ?: "Location",
                nameBn = p["nameBn"] as? String ?: "লোকেশন"
            )
        }
        val pickup = point("pickup") ?: return null
        val destination = point("destination") ?: return null
        val driverLocation = point("driverLocation")
        return RideRequest(
            id = d["id"] as? String ?: document.id,
            passengerId = d["passengerId"] as? String ?: return null,
            passengerName = d["passengerName"] as? String ?: "",
            passengerPhone = d["passengerPhone"] as? String ?: "",
            driverId = d["driverId"] as? String,
            driverName = d["driverName"] as? String,
            driverPhone = d["driverPhone"] as? String,
            vehicleType = runCatching { VehicleType.valueOf(d["vehicleType"] as? String ?: VehicleType.AUTO.name) }.getOrDefault(VehicleType.AUTO),
            vehicleNumber = d["vehicleNumber"] as? String ?: "",
            pickup = pickup,
            destination = destination,
            estimatedFare = (d["estimatedFare"] as? Number)?.toDouble() ?: 0.0,
            distanceKm = (d["distanceKm"] as? Number)?.toDouble() ?: 0.0,
            estimatedMinutes = (d["estimatedMinutes"] as? Number)?.toInt() ?: 0,
            status = runCatching { RideStatus.valueOf(d["status"] as? String ?: RideStatus.NONE.name) }.getOrDefault(RideStatus.NONE),
            driverLocation = driverLocation,
            paymentMethod = d["paymentMethod"] as? String ?: "CASH",
            isPaid = d["isPaid"] as? Boolean ?: false,
            driverRating = (d["driverRating"] as? Number)?.toFloat(),
            passengerRating = (d["passengerRating"] as? Number)?.toFloat(),
            createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            completedAt = (d["completedAt"] as? Number)?.toLong()
        )
    }

    fun observePassengerRides(passengerId: String) {
        passengerRideListener?.remove()
        passengerRideListener = db?.collection("rides")
            ?.whereEqualTo("passengerId", passengerId)
            ?.addSnapshotListener { snapshot, _ ->
                val rides = snapshot?.documents?.mapNotNull(::mapRide).orEmpty()
                val active = rides
                    .filter { it.status != RideStatus.TRIP_COMPLETED && it.status != RideStatus.CANCELLED }
                    .maxByOrNull { it.createdAt }
                if (active != null) _activeRide.value = active
                _rideHistory.value = rides.filter {
                    it.status == RideStatus.TRIP_COMPLETED || it.status == RideStatus.CANCELLED
                }.sortedByDescending { it.createdAt }
            }
    }

    fun observeDriverRides(driverId: String) {
        driverRideListener?.remove()
        driverRideListener = db?.collection("rides")
            ?.whereEqualTo("driverId", driverId)
            ?.addSnapshotListener { snapshot, _ ->
                val rides = snapshot?.documents?.mapNotNull(::mapRide).orEmpty()
                val active = rides
                    .filter { it.status != RideStatus.TRIP_COMPLETED && it.status != RideStatus.CANCELLED }
                    .maxByOrNull { it.createdAt }
                if (active != null) _activeRide.value = active
            }
    }

    fun stopRealtimeSync() {
        passengerRideListener?.remove()
        driverRideListener?.remove()
        passengerRideListener = null
        driverRideListener = null
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
        persistRide(ride)

        // Driver Matching Flow:
        // Find if the current active driver in app can receive it, or assign a simulated nearby driver
        val currentDriver = driverService.currentDriverProfile.value
        if (currentDriver != null && currentDriver.isOnline && currentDriver.vehicleType == vehicleType && currentDriver.verificationStatus == DriverVerificationStatus.APPROVED) {
            // Offer to current driver mode!
            _incomingDriverRequest.value = ride
        }

        // Real driver matching is handled through registered, approved drivers only.
        // Never auto-assign a mock driver when no real driver accepts the request.

        return ride
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
        persistRide(accepted)
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
        _activeRide.value?.let(::persistRide)
    }

    // Driver or passenger action: start trip
    fun startTrip() {
        val current = _activeRide.value ?: return
        _activeRide.value = current.copy(
            status = RideStatus.TRIP_STARTED,
            driverLocation = current.pickup
        )
        persistRide(_activeRide.value ?: current)
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
                _activeRide.value?.let(::persistRide)
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
        persistRide(completed)
        _rideHistory.value = listOf(completed) + _rideHistory.value

        current.driverId?.let { drvId ->
            driverService.recordCompletedTrip(drvId, current.estimatedFare)
        }
    }

    // Pay cash
    fun confirmPayment() {
        _activeRide.value = _activeRide.value?.copy(isPaid = true)
        _activeRide.value?.let(::persistRide)
        _rideHistory.value = _rideHistory.value.map {
            if (it.id == _activeRide.value?.id) it.copy(isPaid = true) else it
        }
    }

    // Rate driver
    fun submitDriverRating(rating: Float, comment: String? = null) {
        val current = _activeRide.value ?: return
        val rated = current.copy(driverRating = rating)
        _activeRide.value = rated
        persistRide(rated)
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
        persistRide(cancelled)
        _rideHistory.value = listOf(cancelled) + _rideHistory.value
        _activeRide.value = null
        _incomingDriverRequest.value = null
    }
}
