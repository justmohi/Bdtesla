package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.ui.AuthStep
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    currentStep: AuthStep,
    phone: String,
    otp: String,
    otpError: String?,
    language: AppLanguage,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onBackToPhone: () -> Unit,
    onVerifyOtp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showGoogleFallback = currentStep == AuthStep.PHONE_INPUT && otpError?.lowercase()?.let { error ->
        error.contains("sms unable") ||
            error.contains("region") ||
            error.contains("sign-in provider is disabled") ||
            error.contains("operation is not allowed") ||
            error.contains("quota")
    } == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                if (currentStep == AuthStep.OTP_INPUT) {
                    IconButton(onClick = onBackToPhone) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TeslaDarkTextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "BD TESLA",
                    style = MaterialTheme.typography.titleLarge,
                    color = TeslaGreenNeon,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (currentStep) {
                        AuthStep.PHONE_INPUT -> if (language == AppLanguage.BANGLA) "মোবাইল নম্বর দিয়ে লগইন করুন" else "Login with Mobile Number"
                        AuthStep.OTP_INPUT -> if (language == AppLanguage.BANGLA) "ওটিপি কোড যাচাই করুন" else "Verify OTP Code"
                        else -> if (language == AppLanguage.BANGLA) "প্রোফাইল সেটআপ" else "Profile Setup"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeslaDarkTextPrimary
                )
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "কুষ্টিয়ায় দ্রুত নিরাপদ ভ্রমণের সূচনা"
                    else
                        "Start your safe and fast smart ride in Kushtia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeslaDarkTextSecondary
                )
            }

            // Body Fields based on step
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (currentStep) {
                        AuthStep.PHONE_INPUT, AuthStep.SPLASH -> {
                            Text(
                                text = Strings.enterPhone(language),
                                style = MaterialTheme.typography.titleSmall,
                                color = TeslaDarkTextPrimary
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = onPhoneChange,
                                placeholder = { Text(Strings.phonePlaceholder(language)) },
                                leadingIcon = {
                                    Text(
                                        text = "+880 ",
                                        fontWeight = FontWeight.Bold,
                                        color = TeslaCyanAccent,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (otpError != null) {
                                Text(text = otpError, color = StatusDanger, fontSize = 12.sp)
                            }

                            if (showGoogleFallback) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = TeslaGreenNeon.copy(alpha = 0.10f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BANGLA)
                                            "এই মুহূর্তে SMS OTP পাঠানো যাচ্ছে না। সরাসরি Google অ্যাকাউন্ট দিয়ে লগইন করুন।"
                                        else
                                            "SMS OTP is currently unavailable. Sign in directly with your Google account.",
                                        modifier = Modifier.padding(12.dp),
                                        color = TeslaDarkTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = onSendOtp,
                                colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("send_otp_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = Strings.sendOtp(language),
                                    fontWeight = FontWeight.Bold,
                                    color = TeslaDarkBg
                                )
                            }

                            OutlinedButton(
                                onClick = onGoogleSignIn,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TeslaDarkTextPrimary)
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        language == AppLanguage.BANGLA && showGoogleFallback -> "Google দিয়ে সরাসরি লগইন করুন"
                                        language == AppLanguage.BANGLA -> "Google দিয়ে চালিয়ে যান"
                                        showGoogleFallback -> "Sign in directly with Google"
                                        else -> "Continue with Google"
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        AuthStep.OTP_INPUT -> {
                            Text(
                                text = "${Strings.enterOtp(language)} (+880 $phone)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TeslaDarkTextPrimary
                            )

                            OutlinedTextField(
                                value = otp,
                                onValueChange = onOtpChange,
                                placeholder = { Text("6-digit OTP") },
                                leadingIcon = {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = TeslaGreenNeon)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_input_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (otpError != null) {
                                Text(
                                    text = otpError,
                                    color = StatusDanger,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    onVerifyOtp()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = Strings.verifyOtp(language),
                                    fontWeight = FontWeight.Bold,
                                    color = TeslaDarkBg
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }

        }
    }
}
