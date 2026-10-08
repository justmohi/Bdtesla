package com.example.data.service

import com.example.data.model.ChatMessage
import com.example.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class NotificationService {

    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                id = "NOTIF-1",
                titleEn = "Welcome to BD TESLA!",
                titleBn = "BD TESLA-তে স্বাগতম!",
                messageEn = "Smart electric ride-booking for Kushtia Sadar is now live.",
                messageBn = "কুষ্টিয়ায় স্মার্ট পরিবেশবান্ধব অটো ও পাখি ভ্যান রাইড এখন হাতের মুঠোয়।",
                timestamp = System.currentTimeMillis() - 3600000L,
                type = "PROMO"
            ),
            NotificationItem(
                id = "NOTIF-2",
                titleEn = "Driver Mode Enabled",
                titleBn = "ড্রাইভার সুবিধা চালু",
                messageEn = "You can register your Auto or Pakhi Van from your profile menu.",
                messageBn = "আপনার অটো বা পাখি ভ্যান নিবন্ধন করে কুষ্টিয়ায় আয় বৃদ্ধি করুন।",
                timestamp = System.currentTimeMillis() - 7200000L,
                type = "UPDATE"
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun pushNotification(titleEn: String, titleBn: String, messageEn: String, messageBn: String) {
        val item = NotificationItem(
            id = "NOTIF-${System.currentTimeMillis() % 10000}",
            titleEn = titleEn,
            titleBn = titleBn,
            messageEn = messageEn,
            messageBn = messageBn
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
}

class ChatService {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("MSG-1", "BDT-1", "তানভীর (Passenger)", "আসসালামু আলাইকুম, আপনি কোন দিকে আছেন?", false, System.currentTimeMillis() - 120000L),
            ChatMessage("MSG-2", "BDT-1", "রফিকুল (Driver)", "ওয়ালাইকুম আসসালাম ভাই, আমি মজমপুর মোড়ে জ্যামে আছি, ২ মিনিটে আসছি।", true, System.currentTimeMillis() - 60000L)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(rideId: String, senderName: String, text: String, isFromDriver: Boolean) {
        val msg = ChatMessage(
            id = "MSG-${System.currentTimeMillis() % 10000}",
            rideId = rideId,
            senderName = senderName,
            message = text,
            isFromDriver = isFromDriver
        )
        _messages.value = _messages.value + msg
    }
}

class PaymentService {
    // Architecture prepared for future bKash, Nagad, Rocket, Upay API integration
    enum class PaymentGateway {
        CASH,
        BKASH,
        NAGAD
    }

    fun processCashSettlement(amount: Double): Boolean {
        // Records cash collection successfully
        return true
    }
}
