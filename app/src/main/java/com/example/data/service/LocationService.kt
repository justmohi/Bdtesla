package com.example.data.service

import com.example.data.model.GeoPoint
import kotlin.math.*

class LocationService {

    // Predefined key hubs and landmarks in Kushtia District
    val kushtiaHubs = listOf(
        GeoPoint(23.9026, 89.1221, "Majompur Gate", "মজমপুর গেট", "Central Hub, NS Road, Kushtia", "সেন্ট্রাল মোড়, এনএস রোড, কুষ্টিয়া"),
        GeoPoint(23.9085, 89.1198, "NS Road Bazaar", "এন এস রোড বাজার", "Shopping District, Kushtia", "শপিং এলাকা, কুষ্টিয়া সদর"),
        GeoPoint(23.9002, 89.1290, "Kushtia Court Station", "কুষ্টিয়া কোর্ট স্টেশন", "Railway Station Road", "রেলওয়ে স্টেশন রোড, কুষ্টিয়া"),
        GeoPoint(23.8931, 89.1085, "Kushtia Medical College", "কুষ্টিয়া মেডিকেল কলেজ", "Hospital Road, Kushtia", "হাসপাতাল রোড, কুষ্টিয়া"),
        GeoPoint(23.9142, 89.1360, "Gorai Bridge", "গড়াই সেতু", "Gorai River Crossing, Kushtia", "গড়াই নদীর সংযোগস্থল, কুষ্টিয়া"),
        GeoPoint(23.7225, 89.1502, "Islamic University (IU)", "ইসলামী বিশ্ববিদ্যালয় (IU)", "IU Campus, Shantidanga", "আইইউ ক্যাম্পাস, শান্তিডাঙ্গা"),
        GeoPoint(23.8967, 89.1345, "Mohini Mills", "মোহিনী মিলস", "Industrial Area, Kushtia", "শিল্প এলাকা, কুষ্টিয়া সদর"),
        GeoPoint(23.9215, 89.1764, "Shilaidaha Kuthibari", "শিলাইদহ কুঠিবাড়ি", "Rabindranath Tagore Memorial", "রবীন্দ্রনাথ স্মৃতিবিজড়িত কুঠিবাড়ি"),
        GeoPoint(23.8640, 88.9812, "Poradaha Junction", "পোড়াদহ রেলওয়ে জংশন", "Railway Junction, Kushtia", "রেলওয়ে জংশন, মিরপুর কুষ্টিয়া"),
        GeoPoint(23.9051, 89.1152, "Thana Morh", "থানা মোড়", "Sadar Police Station Intersection", "সদর থানা সংলগ্ন মোড়, কুষ্টিয়া")
    )

    fun getDefaultUserLocation(): GeoPoint = kushtiaHubs[0] // Majompur Gate

    // Haversine formula to compute great-circle distance in kilometers
    fun calculateDistanceKm(start: GeoPoint, end: GeoPoint): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        // Kushtia road winding factor (~1.25x direct geodesic distance)
        return max(0.8, (distance * 1.25 * 10.0).roundToInt() / 10.0)
    }

    fun estimateMinutes(distanceKm: Double, speedKmH: Double = 22.0): Int {
        val hours = distanceKm / speedKmH
        val mins = (hours * 60).roundToInt()
        return max(3, mins)
    }

    // Interpolates an intermediate coordinate along the trajectory fraction (0.0 to 1.0)
    fun interpolate(start: GeoPoint, end: GeoPoint, fraction: Float): GeoPoint {
        val clamped = fraction.coerceIn(0f, 1f)
        val lat = start.latitude + (end.latitude - start.latitude) * clamped
        val lon = start.longitude + (end.longitude - start.longitude) * clamped
        return GeoPoint(
            latitude = lat,
            longitude = lon,
            nameEn = "In Transit",
            nameBn = "চলমান রুট"
        )
    }
}
