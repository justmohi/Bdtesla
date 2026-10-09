package com.example.data.service

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthService {
    private val auth: FirebaseAuth? by lazy {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }

    fun currentUser() = auth?.currentUser

    fun normalizePhone(phone: String): String {
        val clean = phone.trim().replace(" ", "").replace("-", "")
        return when {
            clean.startsWith("+880") -> clean
            clean.startsWith("880") -> "+$clean"
            clean.startsWith("0") && clean.length == 11 -> "+88$clean"
            clean.matches(Regex("^1[3-9]\\d{8}$")) -> "+880$clean"
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

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not configured. Add google-services.json to the Android app.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                firebaseAuth.signInWithCredential(credential)
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

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(normalized)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not configured.")
            return
        }

        val clientId = runCatching {
            val resourceId = activity.resources.getIdentifier(
                "default_web_client_id",
                "string",
                activity.packageName
            )
            if (resourceId == 0) "" else activity.getString(resourceId)
        }.getOrNull().orEmpty()
        if (clientId.isBlank()) {
            onError("Google Sign-In is not configured yet. Enable Google provider in Firebase Authentication and add the Web client ID.")
            return
        }

        activity.lifecycleScope.launch {
            try {
                val googleOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(clientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val result = CredentialManager.create(activity).getCredential(activity, request)
                val credential = result.credential
                if (credential !is CustomCredential ||
                    credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    onError("Google Sign-In was cancelled or returned an unsupported credential.")
                    return@launch
                }
                val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Google Sign-In failed.") }
            } catch (e: GoogleIdTokenParsingException) {
                onError("Could not read Google account credential.")
            } catch (e: Exception) {
                onError(e.message ?: "Google Sign-In failed.")
            }
        }
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
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            onError("Firebase is not configured. Add google-services.json to the Android app.")
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        firebaseAuth.signInWithCredential(credential)
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

    fun setAuthenticatedIdentity(uid: String, phone: String) {
        _currentUser.value = _currentUser.value.copy(
            id = uid,
            phone = phone,
            name = if (_currentUser.value.name == "Passenger") "Passenger" else _currentUser.value.name
        )
    }

    fun loadCurrentUserFromFirestore() {
        val firebaseUid = runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() ?: return
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(firebaseUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) return@addOnSuccessListener
                val role = snapshot.getString("activeRole")
                    ?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
                    ?: UserRole.PASSENGER
                val driverStatus = snapshot.getString("driverStatus")
                    ?.let { runCatching { DriverVerificationStatus.valueOf(it) }.getOrNull() }
                    ?: DriverVerificationStatus.NOT_APPLIED
                _currentUser.value = _currentUser.value.copy(
                    id = firebaseUid,
                    name = snapshot.getString("name") ?: "Passenger",
                    phone = snapshot.getString("phone")
                        ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.phoneNumber.orEmpty(),
                    photoUrl = snapshot.getString("photoUrl").orEmpty(),
                    activeRole = if (role == UserRole.ADMIN) UserRole.PASSENGER else role,
                    driverStatus = driverStatus,
                    passengerRating = snapshot.getDouble("passengerRating")?.toFloat() ?: _currentUser.value.passengerRating,
                    totalRidesAsPassenger = snapshot.getLong("totalRidesAsPassenger")?.toInt() ?: 0
                )
            }
    }

    fun syncCurrentUserToFirestore() {
        val user = _currentUser.value
        val firebaseUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(firebaseUid)
            .set(
                mapOf(
                    "id" to firebaseUid,
                    "name" to user.name,
                    "phone" to user.phone,
                    "photoUrl" to user.photoUrl,
                    "activeRole" to user.activeRole.name,
                    "driverStatus" to user.driverStatus.name,
                    "passengerRating" to user.passengerRating,
                    "totalRidesAsPassenger" to user.totalRidesAsPassenger,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
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
