package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.DriverProfile
import com.example.data.model.DriverVerificationStatus
import com.example.data.model.VehicleType
import com.example.ui.theme.*

@Composable
fun DriverRegistrationScreen(
    currentStatus: DriverVerificationStatus,
    existingProfile: DriverProfile?,
    language: AppLanguage,
    onSubmit: (
        fullName: String,
        phone: String,
        vehicleType: VehicleType,
        vehicleNumber: String,
        nidNumber: String,
        licenseNumber: String,
        address: String,
        emergencyContact: String
    ) -> Unit,
    onBack: () -> Unit,
    onSwitchToDriverMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf(existingProfile?.fullName ?: "মো. রফিকুল ইসলাম") }
    var phone by remember { mutableStateOf(existingProfile?.phone ?: "01712-345678") }
    var selectedVehicle by remember { mutableStateOf(existingProfile?.vehicleType ?: VehicleType.AUTO) }
    var vehicleNumber by remember { mutableStateOf(existingProfile?.vehicleNumber ?: "কুষ্টিয়া-থ ১১-৩৪০১") }
    var nidNumber by remember { mutableStateOf(existingProfile?.nidNumber ?: "1988501234567890") }
    var licenseNumber by remember { mutableStateOf(existingProfile?.licenseNumber ?: "KUS-DL-9941") }
    var address by remember { mutableStateOf(existingProfile?.address ?: "মজমপুর, কুষ্টিয়া সদর") }
    var emergencyContact by remember { mutableStateOf(existingProfile?.emergencyContact ?: "01911-223344") }
    var uploadedDocuments by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TeslaDarkTextPrimary)
            }
            Text(
                text = Strings.registerAsDriver(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TeslaDarkTextPrimary
            )
        }

        // Current Status Card if submitted
        if (currentStatus != DriverVerificationStatus.NOT_APPLIED) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (currentStatus) {
                        DriverVerificationStatus.APPROVED -> TeslaGreenDark.copy(alpha = 0.35f)
                        DriverVerificationStatus.PENDING_APPROVAL -> TeslaDarkCard
                        else -> StatusDanger.copy(alpha = 0.2f)
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        when (currentStatus) {
                            DriverVerificationStatus.APPROVED -> TeslaGreenNeon
                            DriverVerificationStatus.PENDING_APPROVAL -> StatusPending
                            else -> StatusDanger
                        }
                    )
                ),
                modifier = Modifier.fillMaxWidth().testTag("driver_status_banner")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (currentStatus) {
                            DriverVerificationStatus.APPROVED -> Icons.Default.CheckCircle
                            DriverVerificationStatus.PENDING_APPROVAL -> Icons.Default.HourglassTop
                            else -> Icons.Default.Cancel
                        },
                        contentDescription = "Status",
                        tint = when (currentStatus) {
                            DriverVerificationStatus.APPROVED -> TeslaGreenNeon
                            DriverVerificationStatus.PENDING_APPROVAL -> StatusPending
                            else -> StatusDanger
                        },
                        modifier = Modifier.size(32.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (currentStatus) {
                                DriverVerificationStatus.APPROVED -> Strings.driverApproved(language)
                                DriverVerificationStatus.PENDING_APPROVAL -> Strings.driverApplicationPending(language)
                                else -> "আবেদন প্রত্যাখ্যাত হয়েছে"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TeslaDarkTextPrimary
                        )
                        Text(
                            text = if (currentStatus == DriverVerificationStatus.APPROVED)
                                "আপনি এখন ড্রাইভার মোড সক্রিয় করে রাইড গ্রহণ করতে পারবেন।"
                            else
                                "আমাদের কুষ্টিয়া অ্যাডমিন টিম আপনার তথ্য পরীক্ষা করছেন। শীঘ্রই অনুমোদন পাবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TeslaDarkTextSecondary
                        )
                    }
                }
            }

            if (currentStatus == DriverVerificationStatus.APPROVED) {
                Button(
                    onClick = onSwitchToDriverMode,
                    colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("switch_to_driver_mode_from_reg_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = TeslaDarkBg)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.switchToDriver(language),
                        color = TeslaDarkBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Registration Form Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "চালকের ব্যক্তিগত ও যানের তথ্য",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeslaGreenNeon
                )

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(Strings.fullName(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Strings.enterPhone(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Vehicle Type selector
                Text(
                    text = Strings.vehicleType(language),
                    style = MaterialTheme.typography.labelMedium,
                    color = TeslaDarkTextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VehicleType.values().forEach { vType ->
                        val isSel = (selectedVehicle == vType)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) TeslaGreenDark.copy(alpha = 0.4f) else TeslaDarkCard)
                                .border(1.dp, if (isSel) TeslaGreenNeon else TeslaDarkCardBorder, RoundedCornerShape(12.dp))
                                .clickable { selectedVehicle = vType }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${vType.iconEmoji} ${if (language == AppLanguage.BANGLA) vType.labelBn else vType.labelEn}",
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) TeslaGreenNeon else TeslaDarkTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text(Strings.vehicleNumber(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = nidNumber,
                    onValueChange = { nidNumber = it },
                    label = { Text(Strings.nidNumber(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = licenseNumber,
                    onValueChange = { licenseNumber = it },
                    label = { Text(Strings.licenseNumber(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(Strings.address(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text(Strings.emergencyContact(language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Document upload simulation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TeslaDarkCard)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = TeslaCyanAccent)
                        Text(
                            text = "NID ও লাইসেন্স ফটো (সংযুক্ত)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TeslaDarkTextPrimary
                        )
                    }
                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusOnline)
                }

                Button(
                    onClick = {
                        onSubmit(
                            fullName,
                            phone,
                            selectedVehicle,
                            vehicleNumber,
                            nidNumber,
                            licenseNumber,
                            address,
                            emergencyContact
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_driver_application_button")
                ) {
                    Text(
                        text = Strings.submitApplication(language),
                        color = TeslaDarkBg,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
