package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.ChatMessage
import com.example.data.model.RideRequest
import com.example.ui.theme.*

@Composable
fun CallModalDialog(
    calleeName: String,
    phoneNumber: String,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isSpeaker by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeslaDarkSurface,
        titleContentColor = TeslaDarkTextPrimary,
        title = {
            Text(
                text = if (language == AppLanguage.BANGLA) "BD TESLA ইন-অ্যাপ কল" else "BD TESLA Secure Call",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TeslaGreenNeon.copy(alpha = 0.2f))
                        .border(2.dp, TeslaGreenNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = TeslaGreenNeon,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = calleeName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TeslaCyanAccent
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) "কল চলছে... (০:১৫)" else "Connected... (0:15)",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusOnline
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isMuted) StatusDanger else TeslaDarkCard, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { isSpeaker = !isSpeaker },
                        modifier = Modifier
                            .size(48.dp)
                            .background(if (isSpeaker) TeslaCyanAccent.copy(alpha = 0.4f) else TeslaDarkCard, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSpeaker) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Speaker",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            // Launch native dialer as real integration fallback
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phoneNumber")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(TeslaGreenDark, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneForwarded,
                            contentDescription = "Dialer",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = StatusDanger),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("end_call_button")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.BANGLA) "কল সমাপ্ত করুন" else "End Call",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    messages: List<ChatMessage>,
    currentUserName: String,
    language: AppLanguage,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val quickReplies = if (language == AppLanguage.BANGLA) listOf(
        "আমি মজমপুর মোড়ে আছি",
        "২ মিনিট লাগবে",
        "লোকেশন কনফার্ম করুন",
        "পৌঁছে গেছি ভাই"
    ) else listOf(
        "I am at Majompur Gate",
        "2 mins away",
        "Please confirm pickup",
        "I have arrived"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TeslaDarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TeslaDarkCardBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "লাইভ চ্যাট" else "Live Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeslaDarkTextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TeslaDarkTextSecondary)
                }
            }

            HorizontalDivider(color = TeslaDarkCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderName.contains(currentUserName.split(" ").firstOrNull() ?: "Tanvir", ignoreCase = true)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.senderName,
                                fontSize = 11.sp,
                                color = TeslaDarkTextMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isMe) 14.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 14.dp
                                        )
                                    )
                                    .background(if (isMe) TeslaGreenDark else TeslaDarkCard)
                                    .border(1.dp, if (isMe) TeslaGreenNeon.copy(alpha = 0.5f) else TeslaDarkCardBorder, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick reply chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickReplies.take(2).forEach { reply ->
                    SuggestionChip(
                        onClick = { onSendMessage(reply) },
                        label = { Text(reply, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = TeslaDarkCard,
                            labelColor = TeslaCyanAccent
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = TeslaDarkCardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Input Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.BANGLA) "মেসেজ লিখুন..." else "Type message...",
                            color = TeslaDarkTextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TeslaGreenNeon,
                        unfocusedBorderColor = TeslaDarkCardBorder,
                        focusedTextColor = TeslaDarkTextPrimary,
                        unfocusedTextColor = TeslaDarkTextPrimary
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(TeslaGreenNeon, CircleShape)
                        .testTag("send_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = TeslaDarkBg
                    )
                }
            }
        }
    }
}

@Composable
fun RatingModalDialog(
    driverName: String,
    vehicleType: String,
    fare: Double,
    language: AppLanguage,
    onSubmit: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRating by remember { mutableStateOf(5) }
    var feedback by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeslaDarkSurface,
        title = {
            Text(
                text = if (language == AppLanguage.BANGLA) "রাইড সফলভাবে সম্পন্ন!" else "Trip Completed!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TeslaGreenNeon
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "${Strings.bdt(fare)} • $driverName ($vehicleType)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeslaDarkTextSecondary
                )

                Text(
                    text = if (language == AppLanguage.BANGLA) "আপনার অভিজ্ঞতা কেমন ছিল?" else "How was your ride experience?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TeslaDarkTextPrimary
                )

                // 5 Stars selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedRating = i },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (i <= selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i Stars",
                                tint = TeslaGoldAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.BANGLA) "মন্তব্য লিখুন (ঐচ্ছিক)..." else "Optional comment...",
                            color = TeslaDarkTextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedRating.toFloat()) },
                colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_rating_button")
            ) {
                Text(
                    text = Strings.submit(language),
                    color = TeslaDarkBg,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun DriverIncomingRequestDialog(
    request: RideRequest,
    language: AppLanguage,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, TeslaCyanAccent, RoundedCornerShape(20.dp))
            .testTag("incoming_driver_request_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TeslaDarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StatusOnline)
                    )
                    Text(
                        text = Strings.incomingRequest(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaCyanAccent
                    )
                }

                Text(
                    text = Strings.bdt(request.estimatedFare),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TeslaGreenNeon
                )
            }

            HorizontalDivider(color = TeslaDarkCardBorder)

            // Passenger Name & Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = request.passengerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TeslaDarkTextPrimary
                    )
                    Text(
                        text = if (language == AppLanguage.BANGLA) request.vehicleType.labelBn else request.vehicleType.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = TeslaDarkTextMuted
                    )
                }
                Text(
                    text = Strings.distanceKm(request.distanceKm, language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TeslaDarkTextPrimary
                )
            }

            // Route points
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(TeslaGreenNeon, CircleShape))
                    Text(
                        text = if (language == AppLanguage.BANGLA) request.pickup.nameBn else request.pickup.nameEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TeslaDarkTextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(TeslaCyanAccent, CircleShape))
                    Text(
                        text = if (language == AppLanguage.BANGLA) request.destination.nameBn else request.destination.nameEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TeslaCyanAccent
                    )
                }
            }

            // Accept & Reject Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("driver_reject_button")
                ) {
                    Text(Strings.reject(language), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = TeslaGreenNeon),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("driver_accept_button")
                ) {
                    Text(Strings.accept(language), color = TeslaDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
