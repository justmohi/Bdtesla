package com.example.data.service

import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class RideService(
    private val locationService: LocationService,
    private val driverService: DriverService,
    private val fareService: FareService
) {
    private val db: FirebaseFirestore? by lazy { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }
    private var passengerRideListener: ListenerRegistration? = null
    private var driverRideListener: ListenerRegistration? = null
    private var availableRideListener: ListenerRegistration? = null
    private val _rideError = MutableStateFlow<String?>(null)
    val rideError: StateFlow<String?> = _rideError.asStateFlow()

    fun reportError(message: String) {
        _rideError.value = message
    }

    private fun persistRide(
        ride: RideRequest,
        onSuccess: (() -> Unit)? = null,
        onFailure: ((Exception) -> Unit)? = null
    ) {
        val data = mapOf(
            "id" to ride.id,
            "passengerId" to ride.passengerId,
            "passengerName" to ride.passengerName,
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
        val firestore = db
        if (firestore == null) {
            onFailure?.invoke(IllegalStateException("Firebase Firestore is unavailable."))
            return
        }
        firestore.collection("rides").document(ride.id).set(data, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess?.invoke()
            }
            .addOnFailureListener { error ->
                if (onFailure != null) onFailure.invoke(error)
                else _rideError.value = error.localizedMessage ?: "Ride update failed. Please try again."
            }
    }

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

    fun observePassengerRides(passengerId: String, passengerPhone: String = "") {
        passengerRideListener?.remove()
        passengerRideListener = db?.collection("rides")
            ?.whereEqualTo("passengerId", passengerId)
            ?.addSnapshotListener { snapshot, _ ->
                val documents = snapshot?.documents.orEmpty()
                if (passengerPhone.isNotBlank()) {
                    documents.filter { document ->
                        document.getString("passengerId") == passengerId &&
                            document.getString("driverId") != null &&
                            document.getString("passengerPhone").isNullOrBlank()
                    }.forEach { document ->
                        // Reveal passenger contact only after a real driver has accepted the booking.
                        document.reference.update("passengerPhone", passengerPhone)
                    }
                }
                val rides = documents.mapNotNull(::mapRide)
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

        // Real incoming requests are loaded from Firestore; no local/mock request is generated.
        availableRideListener?.remove()
        val driver = driverService.currentDriverProfile.value ?: return
        if (!driver.isOnline || driver.verificationStatus != DriverVerificationStatus.APPROVED) return
        availableRideListener = db?.collection("rides")
            ?.whereEqualTo("status", RideStatus.SEARCHING_DRIVER.name)
            ?.whereEqualTo("vehicleType", driver.vehicleType.name)
            ?.addSnapshotListener { snapshot, _ ->
                val request = snapshot?.documents
                    ?.mapNotNull(::mapRide)
                    ?.filter { it.driverId == null && it.status == RideStatus.SEARCHING_DRIVER }
                    ?.minByOrNull { it.createdAt }
                _incomingDriverRequest.value = request
            }
    }

    fun stopRealtimeSync() {
        passengerRideListener?.remove()
        driverRideListener?.remove()
        availableRideListener?.remove()
        passengerRideListener = null
        driverRideListener = null
        availableRideListener = null
    }

    fun requestRide(
        passengerId: String,
        passengerName: String,
        passengerPhone: String,
        pickup: GeoPoint,
        destination: GeoPoint,
        vehicleType: VehicleType,
        routeDistanceKm: Double? = null,
        routeMinutes: Int? = null
    ): RideRequest {
        val distance = routeDistanceKm?.takeIf { it > 0.0 } ?: locationService.calculateDistanceKm(pickup, destination)
        val minutes = routeMinutes?.takeIf { it > 0 } ?: locationService.estimateMinutes(distance)
        val fare = fareService.calculateEstimatedFare(vehicleType, distance)

        val ride = RideRequest(
            id = "BDT-${UUID.randomUUID()}",
            passengerId = passengerId,
            passengerName = passengerName,
            passengerPhone = passengerPhone,
            vehicleType = vehicleType,
            pickup = pickup,
            destination = destination,
            estimatedFare = fare,
            distanceKm = distance,
            estimatedMinutes = minutes,
            status = RideStatus.SEARCHING_DRIVER
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

    // Claim a real Firestore request atomically so two drivers cannot accept the same ride.
    fun driverAcceptRequest(driver: DriverProfile) {
        val ride = _incomingDriverRequest.value ?: return
        val firestore = db ?: return
        if (!driver.isOnline || driver.verificationStatus != DriverVerificationStatus.APPROVED) return
        val ref = firestore.collection("rides").document(ride.id)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val status = snapshot.getString("status")
            val assignedDriver = snapshot.getString("driverId")
            if (!snapshot.exists() || assignedDriver != null || status != RideStatus.SEARCHING_DRIVER.name) {
                throw IllegalStateException("This ride has already been accepted or is no longer available.")
            }
            transaction.update(ref, mapOf(
                "driverId" to driver.driverId,
                "driverName" to driver.fullName,
                "driverPhone" to driver.phone,
                "vehicleNumber" to driver.vehicleNumber,
                "status" to RideStatus.DRIVER_ASSIGNED.name,
                "driverLocation" to mapOf(
                    "latitude" to driver.currentLocation.latitude,
                    "longitude" to driver.currentLocation.longitude,
                    "nameEn" to driver.currentLocation.nameEn,
                    "nameBn" to driver.currentLocation.nameBn
                ),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
        }.addOnSuccessListener {
            val accepted = ride.copy(
                driverId = driver.driverId,
                driverName = driver.fullName,
                driverPhone = driver.phone,
                vehicleNumber = driver.vehicleNumber,
                status = RideStatus.DRIVER_ASSIGNED,
                driverLocation = driver.currentLocation,
                passengerPhone = ""
            )
            _incomingDriverRequest.value = null
            _activeRide.value = accepted
        }.addOnFailureListener {
            // The live Firestore listener refreshes the queue; never fabricate an assignment.
            _incomingDriverRequest.value = null
        }
    }

    // Driver rejects incoming request
    fun driverRejectRequest() {
        _incomingDriverRequest.value = null
    }

    fun updateDriverLocation(driverId: String, latitude: Double, longitude: Double) {
        val current = _activeRide.value ?: return
        if (current.driverId != driverId) return
        if (current.status in listOf(RideStatus.TRIP_COMPLETED, RideStatus.CANCELLED, RideStatus.NONE)) return
        val updated = current.copy(
            driverLocation = GeoPoint(
                latitude = latitude,
                longitude = longitude,
                nameEn = "Live GPS",
                nameBn = "লাইভ GPS"
            )
        )
        _activeRide.value = updated
        persistRide(updated)
    }

    // Driver action: arrived at pickup
    fun driverMarkArrived() {
        _activeRide.value = _activeRide.value?.copy(
            status = RideStatus.DRIVER_ARRIVED
        )
        _activeRide.value?.let(::persistRide)
    }

    // Driver or passenger action: start trip
    fun startTrip() {
        val current = _activeRide.value ?: return
        _activeRide.value = current.copy(
            status = RideStatus.TRIP_STARTED
        )
        persistRide(_activeRide.value ?: current)
        // Driver position is updated from device GPS, never interpolated or simulated.
    }

    // Complete trip
    fun completeTrip() {
        val current = _activeRide.value ?: return
        val completed = current.copy(
            status = RideStatus.TRIP_COMPLETED,
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
