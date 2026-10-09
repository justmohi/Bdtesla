package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TeslaCyanAccent
import com.example.ui.theme.TeslaDarkBg
import com.example.ui.theme.TeslaGreenNeon

/**
 * Shared BD TESLA brand mark.
 * Built from vector UI primitives so it remains crisp at every screen density.
 */
@Composable
fun BdTeslaBrandMark(
    size: Dp,
    modifier: Modifier = Modifier,
    iconScale: Float = 0.56f
) {
    val shape = RoundedCornerShape(size * 0.28f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = size * 0.12f,
                shape = shape,
                ambientColor = TeslaGreenNeon.copy(alpha = 0.28f),
                spotColor = TeslaCyanAccent.copy(alpha = 0.28f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00F5A0),
                        TeslaGreenNeon,
                        TeslaCyanAccent
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.35f),
                shape = shape
            )
            .padding(size * 0.13f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "BD TESLA",
            tint = TeslaDarkBg,
            modifier = Modifier.size(size * iconScale)
        )
    }
}
