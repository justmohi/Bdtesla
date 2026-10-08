package com.example.data.localization

enum class AppLanguage {
    BANGLA,
    ENGLISH
}

object Strings {
    // Brand
    fun appName(lang: AppLanguage) = "BD TESLA"
    fun tagline(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কুষ্টিয়ার স্মার্ট রাইড" else "Smart Ride for Kushtia"
    fun kushtia(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কুষ্টিয়া সদর" else "Kushtia Sadar"

    // Roles & Modes
    fun passengerMode(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যাত্রী মোড" else "Passenger Mode"
    fun driverMode(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ড্রাইভার মোড" else "Driver Mode"
    fun adminMode(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অ্যাডমিন প্যানেল" else "Admin Panel"
    fun switchToDriver(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ড্রাইভার মোডে যান" else "Switch to Driver Mode"
    fun switchToPassenger(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যাত্রী মোডে ফিরুন" else "Switch to Passenger Mode"

    // Navigation
    fun navHome(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "হোম" else "Home"
    fun navRides(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রাইডসমূহ" else "Rides"
    fun navNotifications(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বিজ্ঞপ্তি" else "Notifications"
    fun navProfile(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রোফাইল" else "Profile"
    fun navDriverDashboard(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ড্যাশবোর্ড" else "Dashboard"
    fun navRequests(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "অনুরোধ" else "Requests"
    fun navTrips(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ট্রিপ ইতিহাস" else "Trips"
    fun navEarnings(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "উপার্জন" else "Earnings"

    // Authentication
    fun enterPhone(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মোবাইল নম্বর লিখুন" else "Enter Phone Number"
    fun phonePlaceholder(lang: AppLanguage) = "017XXXXXXXX"
    fun sendOtp(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ওটিপি পাঠান" else "Send OTP"
    fun enterOtp(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "৬ ডিজিটের ওটিপি লিখুন" else "Enter 6-digit OTP"
    fun verifyOtp(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যাচাই করুন" else "Verify OTP"
    fun createProfile(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রোফাইল তৈরি করুন" else "Create Profile"
    fun fullName(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার সম্পূর্ণ নাম" else "Full Name"
    fun continueBtn(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "এগিয়ে যান" else "Continue"
    fun demoOtpHint(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "টেস্ট ওটিপি: ১২৩৪৫৬" else "Demo OTP: 123456"

    // Passenger Ride Flow
    fun whereTo(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কোথায় যাবেন?" else "Where are you going?"
    fun pickupPoint(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পিকআপ লোকেশন" else "Pickup Location"
    fun destinationPoint(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গন্তব্যস্থল" else "Destination"
    fun selectVehicle(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যানবাহন নির্বাচন করুন" else "Select Vehicle"
    fun estimatedFare(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আনুমানিক ভাড়া" else "Estimated Fare"
    fun requestRide(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রাইড বুক করুন" else "Request BD TESLA Ride"
    fun cancelRide(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রাইড বাতিল করুন" else "Cancel Ride"
    fun searchingDriver(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কাছাকাছি চালক খোঁজা হচ্ছে..." else "Searching for nearby driver..."
    fun driverAssigned(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চালক আপনার অনুরোধ গ্রহণ করেছেন!" else "Driver has accepted your ride!"
    fun driverArriving(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চালক আসছেন আপনার দিকে" else "Driver is arriving at pickup"
    fun driverArrived(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চালক পিকআপে পৌঁছেছেন" else "Driver arrived at pickup"
    fun tripStarted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ট্রিপ চলছে গন্তব্যের দিকে" else "Trip in progress"
    fun tripCompleted(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ট্রিপ সম্পন্ন হয়েছে!" else "Trip Completed!"
    fun callDriver(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "কল করুন" else "Call"
    fun messageDriver(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "মেসেজ" else "Message"
    fun payCash(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ক্যাশ পেমেন্ট করুন" else "Pay Cash"
    fun rateDriver(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "চালককে রেটিং দিন" else "Rate Driver"
    fun submit(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "জমা দিন" else "Submit"

    // Driver Flow
    fun onlineStatus(isOnline: Boolean, lang: AppLanguage) = if (isOnline) {
        if (lang == AppLanguage.BANGLA) "অনলাইন - রাইড গ্রহণে প্রস্তুত" else "ONLINE - Ready for rides"
    } else {
        if (lang == AppLanguage.BANGLA) "অফলাইন - কোনো অনুরোধ আসবে না" else "OFFLINE - Not receiving rides"
    }
    fun todaysEarnings(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আজকের আয়" else "Today's Earnings"
    fun completedTrips(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "সম্পন্ন রাইড" else "Completed Rides"
    fun incomingRequest(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "নতুন রাইডের অনুরোধ!" else "New Ride Request!"
    fun accept(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "গ্রহণ করুন" else "ACCEPT"
    fun reject(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "প্রত্যাখ্যান" else "REJECT"
    fun arrivedAtPickup(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "পিকআপে পৌঁছেছি" else "Arrived at Pickup"
    fun startRide(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রাইড শুরু করুন" else "Start Ride"
    fun completeRide(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "রাইড সম্পন্ন করুন" else "Complete Ride"
    fun collectCash(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ভাড়া গ্রহণ করুন" else "Collect Cash"

    // Driver Registration
    fun registerAsDriver(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ড্রাইভার নিবন্ধন" else "Driver Registration"
    fun driverApplicationPending(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার ড্রাইভার আবেদনটি পর্যালোচনার অধীনে আছে" else "Your driver application is under review"
    fun driverApproved(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আপনার ড্রাইভার অ্যাকাউন্ট অনুমোদিত হয়েছে!" else "Your driver account is approved!"
    fun nidNumber(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "জাতীয় পরিচয়পত্র (NID) নম্বর" else "NID Number"
    fun licenseNumber(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "ড্রাইভিং লাইসেন্স নম্বর" else "Driving License Number"
    fun vehicleNumber(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যানবাহন রেজিস্ট্রেশন নম্বর" else "Vehicle Registration Number"
    fun vehicleType(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "যানবাহনের ধরন" else "Vehicle Type"
    fun address(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "বর্তমান ঠিকানা (কুষ্টিয়া)" else "Present Address (Kushtia)"
    fun emergencyContact(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "জরুরি যোগাযোগ নম্বর" else "Emergency Contact"
    fun submitApplication(lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "আবেদন জমা দিন" else "Submit Application"

    // Currency
    fun bdt(amount: Double) = "৳ ${amount.toInt()}"
    fun distanceKm(km: Double, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "${String.format("%.1f", km)} কিমি" else "${String.format("%.1f", km)} km"
    fun minutes(min: Int, lang: AppLanguage) = if (lang == AppLanguage.BANGLA) "$min মিনিট" else "$min mins"
}
