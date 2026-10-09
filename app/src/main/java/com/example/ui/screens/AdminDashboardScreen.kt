package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.*
import com.example.data.service.DriverService
import com.example.data.service.FareService
import com.example.ui.theme.*

enum class AdminTab {
    OVERVIEW,
    DRIVERS,
    FARES,
    SETTINGS
}

@Composable
fun AdminDashboardScreen(
    driverService: DriverService,
    fareService: FareService,
    language: AppLanguage,
    onApproveDriver: (String) -> Unit,
    onRejectDriver: (String) -> Unit,
    onCloseAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    DisposableEffect(driverService) {
        driverService.observeAllDriversForAdmin()
        onDispose { driverService.stopAdminDriverObserver() }
    }

    var selectedTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    val allDrivers by driverService.allDrivers.collectAsState()
    val autoConfig by fareService.autoConfig.collectAsState()
    val rickshawConfig by fareService.rickshawConfig.collectAsState()
    val pakhiVanConfig by fareService.pakhiVanConfig.collectAsState()

    var autoBaseFare by remember(autoConfig) { mutableStateOf(autoConfig.baseFare.toString()) }
    var autoPerKm by remember(autoConfig) { mutableStateOf(autoConfig.perKmRate.toString()) }
    var rickshawBaseFare by remember(rickshawConfig) { mutableStateOf(rickshawConfig.baseFare.toString()) }
    var rickshawPerKm by remember(rickshawConfig) { mutableStateOf(rickshawConfig.perKmRate.toString()) }
    var vanBaseFare by remember(pakhiVanConfig) { mutableStateOf(pakhiVanConfig.baseFare.toString()) }
    var vanPerKm by remember(pakhiVanConfig) { mutableStateOf(pakhiVanConfig.perKmRate.toString()) }
    var showFareSavedAlert by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TeslaDarkBg)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = onCloseAdmin) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TeslaDarkTextPrimary)
                }
                Column {
                    Text(
                        text = "BD TESLA কুষ্টিয়া অ্যাডমিন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TeslaGoldAccent
                    )
                    Text(
                        text = "Kushtia Operations Control Center",
                        style = MaterialTheme.typography.labelSmall,
                        color = TeslaDarkTextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(TeslaGoldAccent.copy(alpha = 0.2f))
                    .border(1.dp, TeslaGoldAccent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("ADMIN", fontWeight = FontWeight.Bold, color = TeslaGoldAccent, fontSize = 11.sp)
            }
        }

        // Sub-tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = TeslaDarkSurface,
            contentColor = TeslaGoldAccent
        ) {
            Tab(
                selected = selectedTab == AdminTab.OVERVIEW,
                onClick = { selectedTab = AdminTab.OVERVIEW },
                text = { Text("ওভারভিউ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == AdminTab.DRIVERS,
                onClick = { selectedTab = AdminTab.DRIVERS },
                text = { Text("চালকগণ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == AdminTab.FARES,
                onClick = { selectedTab = AdminTab.FARES },
                text = { Text("ভাড়া নির্ধারণ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            AdminTab.OVERVIEW -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Metric Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard("মোট ব্যবহারকারী", "১,২৪০ জন", Icons.Default.People, TeslaCyanAccent, Modifier.weight(1f))
                        AdminStatCard("মোট নিবন্ধিত চালক", "${allDrivers.size + 42} জন", Icons.Default.TwoWheeler, TeslaGreenNeon, Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard("অনলাইন চালক", "৪২ জন", Icons.Default.Sensors, StatusOnline, Modifier.weight(1f))
                        AdminStatCard("আজকের সম্পন্ন ট্রিপ", "৩১৮ টি", Icons.Default.CheckCircle, TeslaGreenNeon, Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard("মোট প্ল্যাটফর্ম আয়", "৳ ৭৮,৪২০", Icons.Default.AccountBalanceWallet, TeslaGoldAccent, Modifier.weight(1f))
                        AdminStatCard("বাতিল ট্রিপ", "১৪ টি", Icons.Default.Cancel, StatusDanger, Modifier.weight(1f))
                    }

                    // Kushtia Zone Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "কুষ্টিয়া জেলা জোন মনিটরিং",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TeslaGreenNeon
                            )
                            Text("• মজমপুর গেট জোন: উচ্চ চাহিদা (অটো: ২৪, ভ্যান: ১৮)", color = TeslaDarkTextPrimary, fontSize = 13.sp)
                            Text("• এনএস রোড মার্কেট জোন: সক্রিয় রাইড চলমান", color = TeslaDarkTextPrimary, fontSize = 13.sp)
                            Text("• মেডিকেল কলেজ ও ইসলামী বিশ্ববিদ্যালয়: পর্যাপ্ত পরিবহন সংযুক্ত", color = TeslaDarkTextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            AdminTab.DRIVERS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("admin_drivers_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allDrivers) { driver ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = driver.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TeslaDarkTextPrimary
                                        )
                                        Text(
                                            text = "${driver.phone} • ${driver.vehicleType.labelBn}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TeslaCyanAccent
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (driver.verificationStatus) {
                                                    DriverVerificationStatus.APPROVED -> StatusOnline.copy(alpha = 0.2f)
                                                    DriverVerificationStatus.PENDING_APPROVAL -> StatusPending.copy(alpha = 0.2f)
                                                    else -> StatusDanger.copy(alpha = 0.2f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = when (driver.verificationStatus) {
                                                DriverVerificationStatus.APPROVED -> "অনুমোদিত"
                                                DriverVerificationStatus.PENDING_APPROVAL -> "অনুমোদন বাকি"
                                                else -> "স্থগিত"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (driver.verificationStatus) {
                                                DriverVerificationStatus.APPROVED -> StatusOnline
                                                DriverVerificationStatus.PENDING_APPROVAL -> StatusPending
                                                else -> StatusDanger
                                            }
                                        )
                                    }
                                }

                                Text(
                                    text = "যানবাহন নম্বর: ${driver.vehicleNumber} | লাইসেন্স: ${driver.licenseNumber}",
                                    fontSize = 12.sp,
                                    color = TeslaDarkTextSecondary
                                )

                                Text(
                                    text = "ঠিকানা: ${driver.address} (জরুরি: ${driver.emergencyContact})",
                                    fontSize = 12.sp,
                                    color = TeslaDarkTextMuted
                                )

                                // Action Buttons (Approve / Reject)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (driver.verificationStatus != DriverVerificationStatus.APPROVED) {
                                        Button(
                                            onClick = { onApproveDriver(driver.driverId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("অনুমোদন করুন", color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (driver.verificationStatus != DriverVerificationStatus.REJECTED) {
                                        OutlinedButton(
                                            onClick = { onRejectDriver(driver.driverId) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("স্থগিত / বাতিল", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AdminTab.FARES -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "কুষ্টিয়া জেলা ভাড়া কনফিগারেশন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaGreenNeon
                    )

                    // Auto Fare Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🚗 অটো ভাড়া (Auto)", fontWeight = FontWeight.Bold, color = TeslaGreenNeon)
                            OutlinedTextField(
                                value = autoBaseFare,
                                onValueChange = { autoBaseFare = it },
                                label = { Text("বেস ভাড়া (টাকা)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = autoPerKm,
                                onValueChange = { autoPerKm = it },
                                label = { Text("প্রতি কিমি ভাড়া (টাকা)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Rickshaw Fare Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🛺 রিকশা ভাড়া (Rickshaw)", fontWeight = FontWeight.Bold, color = TeslaGoldAccent)
                            OutlinedTextField(
                                value = rickshawBaseFare,
                                onValueChange = { rickshawBaseFare = it },
                                label = { Text("বেস ভাড়া (টাকা)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = rickshawPerKm,
                                onValueChange = { rickshawPerKm = it },
                                label = { Text("প্রতি কিমি ভাড়া (টাকা)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Pakhi Van Fare Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🛺 পাখি ভ্যান ভাড়া (Pakhi Van)", fontWeight = FontWeight.Bold, color = TeslaCyanAccent)
                            OutlinedTextField(
                                value = vanBaseFare,
                                onValueChange = { vanBaseFare = it },
                                label = { Text("বেস ভাড়া (টাকা)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = vanPerKm,
                                onValueChange = { vanPerKm = it },
                                label = { Text("প্রতি কিমি ভাড়া (টাকা)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val aBase = autoBaseFare.toDoubleOrNull() ?: 25.0
                            val aKm = autoPerKm.toDoubleOrNull() ?: 16.0
                            val rBase = rickshawBaseFare.toDoubleOrNull() ?: 15.0
                            val rKm = rickshawPerKm.toDoubleOrNull() ?: 10.0
                            val vBase = vanBaseFare.toDoubleOrNull() ?: 20.0
                            val vKm = vanPerKm.toDoubleOrNull() ?: 12.0
                            fareService.updateAutoConfig(aBase, aKm, 30.0, 2.0)
                            fareService.updateRickshawConfig(rBase, rKm, 20.0, 1.0)
                            fareService.updatePakhiVanConfig(vBase, vKm, 25.0, 1.5)
                            showFareSavedAlert = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TeslaGoldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_fare_config_button")
                    ) {
                        Text("নতুন ভাড়া সংরক্ষণ করুন", color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                    }

                    if (showFareSavedAlert) {
                        Text("✓ ভাড়া সফলভাবে আপডেট হয়েছে!", color = TeslaGreenNeon, fontWeight = FontWeight.Bold)
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(text = title, fontSize = 11.sp, color = TeslaDarkTextMuted)
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TeslaDarkTextPrimary)
        }
    }
}
