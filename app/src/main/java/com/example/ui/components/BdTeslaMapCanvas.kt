package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.model.GeoPoint
import com.example.data.model.RideRequest
import com.example.data.model.RideStatus
import com.example.ui.theme.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun BdTeslaMapCanvas(
    pickup: GeoPoint?,
    destination: GeoPoint?,
    activeRide: RideRequest?,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1A1C)) // Deep obsidian teal map background
    ) {
        Canvas(modifier = Modifier.fillMaxSize().testTag("map_canvas")) {
            val width = size.width
            val height = size.height

            // 1. Draw stylized city grid & road network of Kushtia
            drawKushtiaMapGrid(width, height)

            // 2. Draw Gorai River diagonal curve across Kushtia
            drawGoraiRiver(width, height)

            // 3. Normalized coordinate mapping for Kushtia landmarks
            // Majompur Gate is roughly near center-top (0.48, 0.38)
            // NS Road: (0.42, 0.32)
            // Medical College: (0.35, 0.58)
            // Court Station: (0.58, 0.44)
            // Gorai Bridge: (0.75, 0.28)
            // IU Kushtia: (0.70, 0.82)
            val pickupOffset = getOffsetForPoint(pickup, width, height, defaultX = 0.45f, defaultY = 0.40f)
            val destOffset = getOffsetForPoint(destination, width, height, defaultX = 0.35f, defaultY = 0.62f)

            // 4. Draw route path between pickup and destination
            val routePath = Path().apply {
                moveTo(pickupOffset.x, pickupOffset.y)
                // Curve slightly to mimic real Kushtia city streets
                val midX = (pickupOffset.x + destOffset.x) / 2 + 30f
                val midY = (pickupOffset.y + destOffset.y) / 2 - 20f
                quadraticBezierTo(midX, midY, destOffset.x, destOffset.y)
            }

            // Route shadow glow
            drawPath(
                path = routePath,
                color = TeslaGreenNeon.copy(alpha = 0.25f),
                style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Main Route Line
            drawPath(
                path = routePath,
                color = TeslaGreenNeon,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
                )
            )

            // 5. Draw Pickup Pin (Green) with pulse
            drawCircle(
                color = TeslaGreenNeon.copy(alpha = 0.2f),
                radius = 28f * pulseScale,
                center = pickupOffset
            )
            drawCircle(
                color = TeslaGreenNeon,
                radius = 14f,
                center = pickupOffset
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = pickupOffset
            )

            // Label for Pickup
            drawText(
                textMeasurer = textMeasurer,
                text = if (language == AppLanguage.BANGLA) (pickup?.nameBn ?: "পিকআপ") else (pickup?.nameEn ?: "Pickup"),
                topLeft = Offset(pickupOffset.x - 60f, pickupOffset.y - 42f),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    background = TeslaDarkSurface.copy(alpha = 0.85f)
                )
            )

            // 6. Draw Destination Pin (Cyan)
            drawCircle(
                color = TeslaCyanAccent.copy(alpha = 0.25f),
                radius = 26f,
                center = destOffset
            )
            drawCircle(
                color = TeslaCyanAccent,
                radius = 14f,
                center = destOffset
            )
            drawCircle(
                color = Color.Black,
                radius = 5f,
                center = destOffset
            )

            // Label for Destination
            drawText(
                textMeasurer = textMeasurer,
                text = if (language == AppLanguage.BANGLA) (destination?.nameBn ?: "গন্তব্য") else (destination?.nameEn ?: "Drop"),
                topLeft = Offset(destOffset.x - 60f, destOffset.y + 18f),
                style = TextStyle(
                    color = TeslaCyanAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    background = TeslaDarkSurface.copy(alpha = 0.85f)
                )
            )

            // Show a driver marker only when a real Firestore ride has an actual driver location.
            val driverLoc = activeRide?.driverLocation
            if (driverLoc != null && activeRide.driverId != null) {
                val driverOffset = getOffsetForPoint(driverLoc, width, height, defaultX = 0.5f, defaultY = 0.5f)
                drawCircle(
                    color = TeslaGoldAccent.copy(alpha = 0.35f),
                    radius = 22f,
                    center = driverOffset
                )
                drawCircle(
                    color = TeslaGoldAccent,
                    radius = 12f,
                    center = driverOffset
                )
                drawCircle(
                    color = Color.Black,
                    radius = 5f,
                    center = driverOffset
                )
            }
        }

        // Map overlays
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TeslaDarkCard.copy(alpha = 0.9f))
                    .border(1.dp, TeslaDarkCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TeslaGreenNeon)
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কুষ্টিয়া স্যাটেলাইট লাইভ" else "Kushtia Live GPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TeslaDarkTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawKushtiaMapGrid(width: Float, height: Float) {
    val roadColor = Color(0xFF1E3136)
    val majorRoadColor = Color(0xFF28444B)

    // Minor streets
    for (x in 0..width.toInt() step 90) {
        drawLine(
            color = roadColor,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), height),
            strokeWidth = 2.5f
        )
    }
    for (y in 0..height.toInt() step 90) {
        drawLine(
            color = roadColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(width, y.toFloat()),
            strokeWidth = 2.5f
        )
    }

    // Major arteries: NS Road and Station Road
    drawLine(
        color = majorRoadColor,
        start = Offset(0f, height * 0.42f),
        end = Offset(width, height * 0.42f),
        strokeWidth = 8f
    )
    drawLine(
        color = majorRoadColor,
        start = Offset(width * 0.45f, 0f),
        end = Offset(width * 0.45f, height),
        strokeWidth = 8f
    )
    drawLine(
        color = majorRoadColor,
        start = Offset(width * 0.15f, 0f),
        end = Offset(width * 0.85f, height),
        strokeWidth = 6f
    )
}

private fun DrawScope.drawGoraiRiver(width: Float, height: Float) {
    val riverPath = Path().apply {
        moveTo(width * 0.65f, 0f)
        cubicTo(
            width * 0.75f, height * 0.25f,
            width * 0.85f, height * 0.45f,
            width, height * 0.70f
        )
    }
    drawPath(
        path = riverPath,
        color = Color(0xFF0F3B4A).copy(alpha = 0.7f),
        style = Stroke(width = 38f, cap = StrokeCap.Round)
    )
}

private fun getOffsetForPoint(
    point: GeoPoint?,
    width: Float,
    height: Float,
    defaultX: Float,
    defaultY: Float
): Offset {
    if (point == null) return Offset(width * defaultX, height * defaultY)
    // Map lat/lng range of Kushtia [23.70 - 23.95, 89.00 - 89.20]
    val minLat = 23.72
    val maxLat = 23.95
    val minLon = 88.98
    val maxLon = 89.20

    val normY = 1f - ((point.latitude - minLat) / (maxLat - minLat)).coerceIn(0.0, 1.0).toFloat()
    val normX = ((point.longitude - minLon) / (maxLon - minLon)).coerceIn(0.0, 1.0).toFloat()

    // Map to canvas with inset padding
    val px = width * (0.12f + normX * 0.76f)
    val py = height * (0.12f + normY * 0.76f)
    return Offset(px, py)
}
