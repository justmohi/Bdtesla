package com.example.data.model

enum class UserRole {
    PASSENGER,
    DRIVER,
    ADMIN
}

enum class DriverVerificationStatus {
    NOT_APPLIED,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED
}

enum class VehicleType(
    val labelEn: String,
    val labelBn: String,
    val capacity: String,
    val maxPassengers: Int,
    val iconEmoji: String = "🛺"
) {
    AUTO("Auto", "অটো", "৪-৫ যাত্রী", 5, "🚗"),
    RICKSHAW("Rickshaw", "রিকশা", "২ যাত্রী", 2, "🛺"),
    PAKHI_VAN("Pakhi Van", "পাখি ভ্যান", "৪-৬ যাত্রী / মালামাল", 6, "🛺")
}

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val nameEn: String,
    val nameBn: String,
    val addressEn: String = "Kushtia, Bangladesh",
    val addressBn: String = "কুষ্টিয়া, বাংলাদেশ"
)

data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val photoUrl: String = "",
    val activeRole: UserRole = UserRole.PASSENGER,
    val driverStatus: DriverVerificationStatus = DriverVerificationStatus.NOT_APPLIED,
    val passengerRating: Float = 4.9f,
    val totalRidesAsPassenger: Int = 14
)

data class DriverProfile(
    val driverId: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val vehicleType: VehicleType,
    val vehicleNumber: String,
    val nidNumber: String,
    val licenseNumber: String,
    val address: String,
    val emergencyContact: String,
    val verificationStatus: DriverVerificationStatus,
    val isOnline: Boolean = false,
    val currentLocation: GeoPoint,
    val driverRating: Float = 4.85f,
    val totalCompletedTrips: Int = 142,
    val todayEarnings: Double = 840.0,
    val totalEarnings: Double = 28450.0
)

enum class RideStatus {
    NONE,
    REQUESTED,
    SEARCHING_DRIVER,
    DRIVER_ASSIGNED,
    DRIVER_ARRIVING,
    DRIVER_ARRIVED,
    TRIP_STARTED,
    TRIP_COMPLETED,
    CANCELLED
}

data class RideRequest(
    val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val vehicleType: VehicleType,
    val vehicleNumber: String = "কুষ্টিয়া-থ ১১-২০২৪",
    val pickup: GeoPoint,
    val destination: GeoPoint,
    val estimatedFare: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val status: RideStatus = RideStatus.NONE,
    val driverLocation: GeoPoint? = null,
    val paymentMethod: String = "CASH",
    val isPaid: Boolean = false,
    val driverRating: Float? = null,
    val passengerRating: Float? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val passengerCount: Int = 1
)

data class FareConfig(
    val vehicleType: VehicleType,
    val baseFare: Double,
    val perKmRate: Double,
    val minimumFare: Double,
    val waitingChargePerMin: Double,
    val cancellationCharge: Double,
    val commissionPercent: Double = 10.0 // BD TESLA platform commission
)

data class NotificationItem(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val messageEn: String,
    val messageBn: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "INFO"
)

data class ChatMessage(
    val id: String,
    val rideId: String,
    val senderName: String,
    val message: String,
    val isFromDriver: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class AdminPlatformMetrics(
    val totalUsers: Int = 1240,
    val totalDrivers: Int = 185,
    val activeOnlineDrivers: Int = 42,
    val activeRidesNow: Int = 8,
    val completedRidesCount: Int = 3920,
    val cancelledRidesCount: Int = 114,
    val totalPlatformRevenueBdt: Double = 78420.0
)
