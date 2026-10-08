package com.example.data.service

import android.app.Activity
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun currentUser() = auth.currentUser

    fun normalizePhone(phone: String): String {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return when {
            clean.startsWith("+880") -> clean
            clean.startsWith("880") -> "+$clean"
            clean.startsWith("0") && clean.length == 11 -> "+88$clean"
            else -> clean
        }
    }

    fun sendOtp(
        activity: Activity,
        phone: String,
        onCodeSent: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val normalized = normalizePhone(phone)
        if (!Regex("^\\+8801[3-9]\\d{8}$").matches(normalized)) {
            onError("Enter a valid Bangladesh mobile number.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { }
                    .addOnFailureListener { onError(it.message ?: "Phone verification failed.") }
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                onError(e.message ?: "Could not send verification code.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(normalized)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        verificationId: String,
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (verificationId.isBlank() || code.length != 6) {
            onError("Invalid OTP. Enter the 6-digit verification code.")
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Invalid verification code.") }
    }
}

class UserService(private val driverService: DriverService) {

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "LOCAL-USER",
            name = "Passenger",
            phone = "",
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
