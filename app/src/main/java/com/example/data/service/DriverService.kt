package com.example.data.service

import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class DriverService(private val locationService: LocationService) {

    private val db: FirebaseFirestore? by lazy { runCatching { FirebaseFirestore.getInstance() }.getOrNull() }
    private var adminDriversListener: ListenerRegistration? = null

    private fun driverMap(driver: DriverProfile): Map<String, Any?> = mapOf(
        "driverId" to driver.driverId,
        "userId" to driver.userId,
        "fullName" to driver.fullName,
        "phone" to driver.phone,
        "vehicleType" to driver.vehicleType.name,
        "vehicleNumber" to driver.vehicleNumber,
        "nidNumber" to driver.nidNumber,
        "licenseNumber" to driver.licenseNumber,
        "address" to driver.address,
        "emergencyContact" to driver.emergencyContact,
        "verificationStatus" to driver.verificationStatus.name,
        "isOnline" to driver.isOnline,
        "currentLocation" to mapOf(
            "latitude" to driver.currentLocation.latitude,
            "longitude" to driver.currentLocation.longitude,
            "nameEn" to driver.currentLocation.nameEn,
            "nameBn" to driver.currentLocation.nameBn
        ),
        "driverRating" to driver.driverRating,
        "totalCompletedTrips" to driver.totalCompletedTrips,
        "todayEarnings" to driver.todayEarnings,
        "totalEarnings" to driver.totalEarnings,
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun persistDriver(driver: DriverProfile) {
        val firestore = db ?: return
        firestore.collection("drivers").document(driver.driverId)
            .set(driverMap(driver))
    }

    // Current driver profile for the logged in user if registered
    private val _currentDriverProfile = MutableStateFlow<DriverProfile?>(null)
    val currentDriverProfile: StateFlow<DriverProfile?> = _currentDriverProfile.asStateFlow()

    // All registered drivers in Kushtia system (used by Admin Dashboard and matching)
    private val _allDrivers = MutableStateFlow<List<DriverProfile>>(emptyList())
    val allDrivers: StateFlow<List<DriverProfile>> = _allDrivers.asStateFlow()

    // No seeded/demo drivers. Only real driver registrations loaded from Firestore are eligible.

    fun loadCurrentDriverProfile(userId: String) {
        db?.collection("drivers")
            ?.whereEqualTo("userId", userId)
            ?.limit(1)
            ?.get()
            ?.addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull() ?: return@addOnSuccessListener
                val loc = doc.get("currentLocation") as? Map<*, *>
                val location = GeoPoint(
                    latitude = (loc?.get("latitude") as? Number)?.toDouble() ?: locationService.getDefaultUserLocation().latitude,
                    longitude = (loc?.get("longitude") as? Number)?.toDouble() ?: locationService.getDefaultUserLocation().longitude,
                    nameEn = loc?.get("nameEn") as? String ?: "Current Location",
                    nameBn = loc?.get("nameBn") as? String ?: "বর্তমান অবস্থান"
                )
                val driver = DriverProfile(
                    driverId = doc.getString("driverId") ?: doc.id,
                    userId = doc.getString("userId") ?: userId,
                    fullName = doc.getString("fullName") ?: "",
                    phone = doc.getString("phone") ?: "",
                    vehicleType = runCatching { VehicleType.valueOf(doc.getString("vehicleType") ?: VehicleType.AUTO.name) }.getOrDefault(VehicleType.AUTO),
                    vehicleNumber = doc.getString("vehicleNumber") ?: "",
                    nidNumber = doc.getString("nidNumber") ?: "",
                    licenseNumber = doc.getString("licenseNumber") ?: "",
                    address = doc.getString("address") ?: "",
                    emergencyContact = doc.getString("emergencyContact") ?: "",
                    verificationStatus = runCatching { DriverVerificationStatus.valueOf(doc.getString("verificationStatus") ?: DriverVerificationStatus.NOT_APPLIED.name) }.getOrDefault(DriverVerificationStatus.NOT_APPLIED),
                    isOnline = doc.getBoolean("isOnline") ?: false,
                    currentLocation = location,
                    driverRating = doc.getDouble("driverRating")?.toFloat() ?: 5.0f,
                    totalCompletedTrips = doc.getLong("totalCompletedTrips")?.toInt() ?: 0,
                    todayEarnings = doc.getDouble("todayEarnings") ?: 0.0,
                    totalEarnings = doc.getDouble("totalEarnings") ?: 0.0
                )
                _currentDriverProfile.value = driver
                _allDrivers.value = _allDrivers.value.filterNot { it.driverId == driver.driverId } + driver
            }
    }

    fun observeAllDriversForAdmin() {
        adminDriversListener?.remove()
        adminDriversListener = db?.collection("drivers")?.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val loaded = snapshot.documents.mapNotNull { doc ->
                val loc = doc.get("currentLocation") as? Map<*, *>
                DriverProfile(
                    driverId = doc.getString("driverId") ?: doc.id,
                    userId = doc.getString("userId") ?: return@mapNotNull null,
                    fullName = doc.getString("fullName") ?: "",
                    phone = doc.getString("phone") ?: "",
                    vehicleType = runCatching { VehicleType.valueOf(doc.getString("vehicleType") ?: VehicleType.AUTO.name) }.getOrDefault(VehicleType.AUTO),
                    vehicleNumber = doc.getString("vehicleNumber") ?: "",
                    nidNumber = doc.getString("nidNumber") ?: "",
                    licenseNumber = doc.getString("licenseNumber") ?: "",
                    address = doc.getString("address") ?: "",
                    emergencyContact = doc.getString("emergencyContact") ?: "",
                    verificationStatus = runCatching { DriverVerificationStatus.valueOf(doc.getString("verificationStatus") ?: DriverVerificationStatus.NOT_APPLIED.name) }.getOrDefault(DriverVerificationStatus.NOT_APPLIED),
                    isOnline = doc.getBoolean("isOnline") ?: false,
                    currentLocation = GeoPoint(
                        latitude = (loc?.get("latitude") as? Number)?.toDouble() ?: locationService.getDefaultUserLocation().latitude,
                        longitude = (loc?.get("longitude") as? Number)?.toDouble() ?: locationService.getDefaultUserLocation().longitude,
                        nameEn = loc?.get("nameEn") as? String ?: "Driver location",
                        nameBn = loc?.get("nameBn") as? String ?: "ড্রাইভারের অবস্থান"
                    ),
                    driverRating = doc.getDouble("driverRating")?.toFloat() ?: 5.0f,
                    totalCompletedTrips = doc.getLong("totalCompletedTrips")?.toInt() ?: 0,
                    todayEarnings = doc.getDouble("todayEarnings") ?: 0.0,
                    totalEarnings = doc.getDouble("totalEarnings") ?: 0.0
                )
            }
            _allDrivers.value = loaded
            _currentDriverProfile.value?.let { current ->
                if (loaded.none { it.driverId == current.driverId }) {
                    _allDrivers.value = loaded + current
                }
            }
        }
    }

    fun stopAdminDriverObserver() {
        adminDriversListener?.remove()
        adminDriversListener = null
    }

    fun submitDriverRegistration(
        userId: String,
        fullName: String,
        phone: String,
        vehicleType: VehicleType,
        vehicleNumber: String,
        nidNumber: String,
        licenseNumber: String,
        address: String,
        emergencyContact: String
    ): DriverProfile {
        val newDriver = DriverProfile(
            driverId = "DRV-${System.currentTimeMillis() % 10000}",
            userId = userId,
            fullName = fullName,
            phone = phone,
            vehicleType = vehicleType,
            vehicleNumber = vehicleNumber,
            nidNumber = nidNumber,
            licenseNumber = licenseNumber,
            address = address,
            emergencyContact = emergencyContact,
            verificationStatus = DriverVerificationStatus.PENDING_APPROVAL,
            isOnline = false,
            currentLocation = locationService.getDefaultUserLocation()
        )
        _currentDriverProfile.value = newDriver
        _allDrivers.value = _allDrivers.value + newDriver
        persistDriver(newDriver)
        return newDriver
    }

    fun updateCurrentLocation(latitude: Double, longitude: Double) {
        if (latitude !in 23.55..24.10 || longitude !in 88.65..89.35) return
        val current = _currentDriverProfile.value ?: return
        if (!current.isOnline || current.verificationStatus != DriverVerificationStatus.APPROVED) return
        val updated = current.copy(
            currentLocation = GeoPoint(
                latitude = latitude,
                longitude = longitude,
                nameEn = "Live GPS",
                nameBn = "লাইভ GPS"
            )
        )
        _currentDriverProfile.value = updated
        _allDrivers.value = _allDrivers.value.map { if (it.driverId == updated.driverId) updated else it }
        persistDriver(updated)
    }

    fun setDriverOnline(isOnline: Boolean) {
        val current = _currentDriverProfile.value ?: return
        if (current.verificationStatus == DriverVerificationStatus.APPROVED) {
            val updated = current.copy(isOnline = isOnline)
            _currentDriverProfile.value = updated
            _allDrivers.value = _allDrivers.value.map { if (it.driverId == current.driverId) updated else it }
            persistDriver(updated)
        }
    }

    // Admin action: approve driver
    fun approveDriver(driverId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.driverId == driverId) it.copy(verificationStatus = DriverVerificationStatus.APPROVED) else it
        }
        if (_currentDriverProfile.value?.driverId == driverId) {
            _currentDriverProfile.value = _currentDriverProfile.value?.copy(verificationStatus = DriverVerificationStatus.APPROVED)
        }
        _allDrivers.value.firstOrNull { it.driverId == driverId }?.let { driver ->
            persistDriver(driver)
            // Keep the user profile's approval state in sync; the security rules use this field for the request queue.
            db?.collection("users")?.document(driver.userId)?.update(
                mapOf("driverStatus" to DriverVerificationStatus.APPROVED.name, "updatedAt" to FieldValue.serverTimestamp())
            )
        }
    }

    // Admin action: reject driver
    fun rejectDriver(driverId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.driverId == driverId) it.copy(verificationStatus = DriverVerificationStatus.REJECTED, isOnline = false) else it
        }
        if (_currentDriverProfile.value?.driverId == driverId) {
            _currentDriverProfile.value = _currentDriverProfile.value?.copy(verificationStatus = DriverVerificationStatus.REJECTED, isOnline = false)
        }
        _allDrivers.value.firstOrNull { it.driverId == driverId }?.let { driver ->
            persistDriver(driver)
            db?.collection("users")?.document(driver.userId)?.update(
                mapOf("driverStatus" to DriverVerificationStatus.REJECTED.name, "updatedAt" to FieldValue.serverTimestamp())
            )
        }
    }

    fun recordCompletedTrip(driverId: String, tripFare: Double) {
        _allDrivers.value = _allDrivers.value.map { driver ->
            if (driver.driverId == driverId) {
                driver.copy(
                    todayEarnings = driver.todayEarnings + tripFare,
                    totalEarnings = driver.totalEarnings + tripFare,
                    totalCompletedTrips = driver.totalCompletedTrips + 1
                )
            } else driver
        }
        if (_currentDriverProfile.value?.driverId == driverId) {
            _currentDriverProfile.value = _currentDriverProfile.value?.let {
                it.copy(
                    todayEarnings = it.todayEarnings + tripFare,
                    totalEarnings = it.totalEarnings + tripFare,
                    totalCompletedTrips = it.totalCompletedTrips + 1
                )
            }
        }
        _allDrivers.value.firstOrNull { it.driverId == driverId }?.let(::persistDriver)
    }
}
