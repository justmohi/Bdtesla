package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.VehicleType
import com.example.ui.theme.*

@Composable
fun VehicleSelectorCard(
    vehicleType: VehicleType,
    isSelected: Boolean,
    estimatedFare: Double,
    estimatedMinutes: Int,
    language: AppLanguage,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) TeslaGreenNeon else TeslaDarkCardBorder
    val bgColor = if (isSelected) TeslaGreenDark.copy(alpha = 0.25f) else TeslaDarkCard

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("vehicle_card_${vehicleType.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Vehicle Image & details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Vehicle Drawable
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TeslaDarkSurface)
                        .border(1.dp, TeslaDarkCardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val drawableRes = when (vehicleType) {
                        VehicleType.AUTO -> R.drawable.bd_tesla_auto_1791386877871
                        VehicleType.RICKSHAW -> R.drawable.bd_tesla_rickshaw_1791388208106
                        VehicleType.PAKHI_VAN -> R.drawable.bd_tesla_van_1791386895212
                    }
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = vehicleType.labelEn,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${vehicleType.iconEmoji} ${if (language == AppLanguage.BANGLA) vehicleType.labelBn else vehicleType.labelEn}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TeslaDarkTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Electric",
                            tint = TeslaGreenNeon,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Capacity",
                            tint = TeslaDarkTextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = vehicleType.capacity,
                            style = MaterialTheme.typography.bodySmall,
                            color = TeslaDarkTextSecondary
                        )
                    }

                    Text(
                        text = when (vehicleType) {
                            VehicleType.AUTO -> if (language == AppLanguage.BANGLA) "কুষ্টিয়ার দ্রুত ইলেকট্রিক অটো" else "Fast electric auto ride"
                            VehicleType.RICKSHAW -> if (language == AppLanguage.BANGLA) "স্বল্প দূরত্বের সাশ্রয়ী ঐতিহ্যবাহী রিকশা" else "Affordable local rickshaw"
                            VehicleType.PAKHI_VAN -> if (language == AppLanguage.BANGLA) "যাত্রী ও মালামাল পরিবহনে সেরা" else "Passenger & cargo electric van"
                        },
                        fontSize = 11.sp,
                        color = TeslaDarkTextMuted
                    )
                }
            }

            // Fare & ETA
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = Strings.bdt(estimatedFare),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TeslaGreenNeon
                )
                Text(
                    text = Strings.minutes(estimatedMinutes, language),
                    style = MaterialTheme.typography.labelSmall,
                    color = TeslaCyanAccent
                )
            }
        }
    }
}
