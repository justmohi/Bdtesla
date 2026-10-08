package com.example.data.service

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthService {
    // Current authenticated phone number and OTP verification state
    val demoOtp = "123456"

    fun verifyOtp(phone: String, enteredOtp: String): Boolean {
        // Any 6 digit OTP or matching demo OTP accepts for prototype testing
        return enteredOtp.length == 6
    }
}

class UserService(private val driverService: DriverService) {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "USR-1001",
            name = "তানভীর আহমেদ (Tanvir)",
            phone = "01711-234567",
            activeRole = UserRole.PASSENGER,
            driverStatus = DriverVerificationStatus.NOT_APPLIED,
            passengerRating = 4.92f,
            totalRidesAsPassenger = 18
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    fun updateProfile(name: String, phone: String) {
        _currentUser.value = _currentUser.value.copy(name = name, phone = phone)
    }

    fun switchRole(role: UserRole) {
        _currentUser.value = _currentUser.value.copy(activeRole = role)
    }

    fun onDriverApplicationSubmitted() {
        _currentUser.value = _currentUser.value.copy(driverStatus = DriverVerificationStatus.PENDING_APPROVAL)
    }

    fun onDriverApproved() {
        _currentUser.value = _currentUser.value.copy(driverStatus = DriverVerificationStatus.APPROVED)
    }
}
