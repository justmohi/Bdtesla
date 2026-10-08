package com.example.data.service

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class DriverService(private val locationService: LocationService) {

    // Current driver profile for the logged in user if registered
    private val _currentDriverProfile = MutableStateFlow<DriverProfile?>(null)
    val currentDriverProfile: StateFlow<DriverProfile?> = _currentDriverProfile.asStateFlow()

    // All registered drivers in Kushtia system (used by Admin Dashboard and matching)
    private val _allDrivers = MutableStateFlow<List<DriverProfile>>(emptyList())
    val allDrivers: StateFlow<List<DriverProfile>> = _allDrivers.asStateFlow()

    init {
        // Seed default drivers for realistic Kushtia simulation
        val seeded = listOf(
            DriverProfile(
                driverId = "DRV-101",
                userId = "USR-201",
                fullName = "মো. রফিকুল ইসলাম (Rafiq)",
                phone = "01712-345678",
                vehicleType = VehicleType.AUTO,
                vehicleNumber = "কুষ্টিয়া-থ ১১-৩৪০১",
                nidNumber = "1988501234567890",
                licenseNumber = "KUS-DL-9941",
                address = "মজমপুর, কুষ্টিয়া সদর",
                emergencyContact = "01911-223344",
                verificationStatus = DriverVerificationStatus.APPROVED,
                isOnline = true,
                currentLocation = locationService.kushtiaHubs[1], // NS Road
                driverRating = 4.9f,
                totalCompletedTrips = 320,
                todayEarnings = 1250.0,
                totalEarnings = 48200.0
            ),
            DriverProfile(
                driverId = "DRV-102",
                userId = "USR-202",
                fullName = "আব্দুল কাদের (Kader)",
                phone = "01815-998877",
                vehicleType = VehicleType.PAKHI_VAN,
                vehicleNumber = "কুষ্টিয়া-ভ ০৭-১২৩৪",
                nidNumber = "1975509876543210",
                licenseNumber = "KUS-DL-7721",
                address = "কোর্টপাড়া, কুষ্টিয়া সদর",
                emergencyContact = "01722-113355",
                verificationStatus = DriverVerificationStatus.APPROVED,
                isOnline = true,
                currentLocation = locationService.kushtiaHubs[3], // Medical College
                driverRating = 4.8f,
                totalCompletedTrips = 215,
                todayEarnings = 890.0,
                totalEarnings = 32150.0
            ),
            DriverProfile(
                driverId = "DRV-104",
                userId = "USR-204",
                fullName = "মো. কামাল হোসেন (Kamal)",
                phone = "01733-445566",
                vehicleType = VehicleType.RICKSHAW,
                vehicleNumber = "কুষ্টিয়া-র ০৪-৫৬৭৮",
                nidNumber = "1982503344556677",
                licenseNumber = "KUS-DL-4412",
                address = "থানা মোড়, কুষ্টিয়া সদর",
                emergencyContact = "01922-334455",
                verificationStatus = DriverVerificationStatus.APPROVED,
                isOnline = true,
                currentLocation = locationService.kushtiaHubs[0], // Majompur Gate
                driverRating = 4.95f,
                totalCompletedTrips = 410,
                todayEarnings = 620.0,
                totalEarnings = 34500.0
            ),
            DriverProfile(
                driverId = "DRV-103",
                userId = "USR-203",
                fullName = "সোহেল রানা (Sohel)",
                phone = "01912-776655",
                vehicleType = VehicleType.AUTO,
                vehicleNumber = "কুষ্টিয়া-থ ১৩-৫৮৯২",
                nidNumber = "1994508899221100",
                licenseNumber = "KUS-DL-3382",
                address = "চরখাশিমপুর, কুষ্টিয়া",
                emergencyContact = "01611-998877",
                verificationStatus = DriverVerificationStatus.PENDING_APPROVAL,
                isOnline = false,
                currentLocation = locationService.kushtiaHubs[4], // Gorai Bridge
                driverRating = 5.0f,
                totalCompletedTrips = 0,
                todayEarnings = 0.0,
                totalEarnings = 0.0
            )
        )
        _allDrivers.value = seeded
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
        return newDriver
    }

    fun setDriverOnline(isOnline: Boolean) {
        val current = _currentDriverProfile.value ?: return
        if (current.verificationStatus == DriverVerificationStatus.APPROVED) {
            val updated = current.copy(isOnline = isOnline)
            _currentDriverProfile.value = updated
            _allDrivers.value = _allDrivers.value.map { if (it.driverId == current.driverId) updated else it }
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
    }

    // Admin action: reject driver
    fun rejectDriver(driverId: String) {
        _allDrivers.value = _allDrivers.value.map {
            if (it.driverId == driverId) it.copy(verificationStatus = DriverVerificationStatus.REJECTED, isOnline = false) else it
        }
        if (_currentDriverProfile.value?.driverId == driverId) {
            _currentDriverProfile.value = _currentDriverProfile.value?.copy(verificationStatus = DriverVerificationStatus.REJECTED, isOnline = false)
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
    }
}
