package com.example.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.Strings
import com.example.data.model.UserRole
import com.example.ui.DriverTab
import com.example.ui.PassengerTab
import com.example.ui.theme.*

@Composable
fun BdTeslaBottomNavigation(
    activeRole: UserRole,
    passengerTab: PassengerTab,
    driverTab: DriverTab,
    language: AppLanguage,
    onSelectPassengerTab: (PassengerTab) -> Unit,
    onSelectDriverTab: (DriverTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("app_bottom_nav")
            .height(78.dp),
        containerColor = TeslaDarkSurface.copy(alpha = 0.98f),
        tonalElevation = 12.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        if (activeRole == UserRole.PASSENGER) {
            // Passenger Tabs: Home, Rides, Notifications, Profile
            NavigationBarItem(
                selected = passengerTab == PassengerTab.HOME,
                onClick = { onSelectPassengerTab(PassengerTab.HOME) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Home",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navHome(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaGreenNeon,
                    indicatorColor = TeslaGreenNeon,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = passengerTab == PassengerTab.RIDES,
                onClick = { onSelectPassengerTab(PassengerTab.RIDES) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Rides",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navRides(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaGreenNeon,
                    indicatorColor = TeslaGreenNeon,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = passengerTab == PassengerTab.NOTIFICATIONS,
                onClick = { onSelectPassengerTab(PassengerTab.NOTIFICATIONS) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navNotifications(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaGreenNeon,
                    indicatorColor = TeslaGreenNeon,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = passengerTab == PassengerTab.PROFILE,
                onClick = { onSelectPassengerTab(PassengerTab.PROFILE) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navProfile(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaGreenNeon,
                    indicatorColor = TeslaGreenNeon,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )
        } else if (activeRole == UserRole.DRIVER) {
            // Driver Tabs: Dashboard, Requests, Trips, Earnings, Profile
            NavigationBarItem(
                selected = driverTab == DriverTab.DASHBOARD,
                onClick = { onSelectDriverTab(DriverTab.DASHBOARD) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Dashboard",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navDriverDashboard(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaCyanAccent,
                    indicatorColor = TeslaCyanAccent,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = driverTab == DriverTab.REQUESTS,
                onClick = { onSelectDriverTab(DriverTab.REQUESTS) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ElectricRickshaw,
                        contentDescription = "Requests",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navRequests(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaCyanAccent,
                    indicatorColor = TeslaCyanAccent,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = driverTab == DriverTab.TRIPS,
                onClick = { onSelectDriverTab(DriverTab.TRIPS) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Trips",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navTrips(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaCyanAccent,
                    indicatorColor = TeslaCyanAccent,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = driverTab == DriverTab.EARNINGS,
                onClick = { onSelectDriverTab(DriverTab.EARNINGS) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Earnings",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navEarnings(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaCyanAccent,
                    indicatorColor = TeslaCyanAccent,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )

            NavigationBarItem(
                selected = driverTab == DriverTab.PROFILE,
                onClick = { onSelectDriverTab(DriverTab.PROFILE) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(Strings.navProfile(language), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TeslaDarkBg,
                    selectedTextColor = TeslaCyanAccent,
                    indicatorColor = TeslaCyanAccent,
                    unselectedIconColor = TeslaDarkTextMuted,
                    unselectedTextColor = TeslaDarkTextMuted
                )
            )
        }
    }
}

// Extension to avoid typo
private val DriverTab.TRIP_STARTED_OR_HISTORY: Boolean
    get() = this == DriverTab.TRIPS
