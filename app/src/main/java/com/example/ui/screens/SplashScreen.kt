package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.ui.theme.*
import com.example.ui.components.BdTeslaBrandMark

@Composable
fun SplashScreen(
    language: AppLanguage,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BdTeslaBrandMark(size = 52.dp, iconScale = 0.60f)
                    Text(
                        text = "BD TESLA",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TeslaGreenNeon,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.tagline(language),
                    style = MaterialTheme.typography.titleMedium,
                    color = TeslaCyanAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Hero Graphic
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.bd_tesla_hero_1791386860772),
                        contentDescription = "BD TESLA Kushtia Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, TeslaDarkBg.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA)
                            "কুষ্টিয়ায় পরিবেশবান্ধব অটো ও পাখি ভ্যান যাত্রা"
                        else
                            "Eco-Friendly Smart Auto & Pakhi Van in Kushtia",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }

            // Bottom CTA Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BANGLA)
                        "এক অ্যাপেই যাত্রী ও ড্রাইভার সেবা • নিরাপদ • দ্রুত • ন্যায্য ভাড়া"
                    else
                        "Passenger & Driver Mode in One Single App • Safe • Fast • Fair Fare",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = TeslaDarkTextSecondary
                )

                Button(
                    onClick = onGetStarted,
                    colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("get_started_button")
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "শুরু করুন" else "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkBg
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Start",
                        tint = TeslaDarkBg
                    )
                }
            }
        }
    }
}
