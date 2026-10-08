package com.example

import com.example.data.model.GeoPoint
import com.example.data.model.RideStatus
import com.example.data.model.VehicleType
import com.example.data.service.DriverService
import com.example.data.service.FareService
import com.example.data.service.LocationService
import com.example.data.service.RideService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFareCalculationForAutoAndPakhiVan() {
        val fareService = FareService()
        // Auto: 25 base + (5 km * 16) = 105 BDT
        val autoFare = fareService.calculateEstimatedFare(VehicleType.AUTO, 5.0)
        assertTrue("Auto fare should be around 105 BDT", autoFare >= 100.0)

        // Pakhi Van: 20 base + (5 km * 12) = 80 BDT
        val vanFare = fareService.calculateEstimatedFare(VehicleType.PAKHI_VAN, 5.0)
        assertTrue("Pakhi Van should be cheaper than Auto", vanFare < autoFare)
        assertEquals(80.0, vanFare, 5.0)
    }

    @Test
    fun testFareCalculationForRickshaw() {
        val fareService = FareService()
        // Rickshaw: 15 base + (3 km * 10) = 45 BDT
        val rickshawFare = fareService.calculateEstimatedFare(VehicleType.RICKSHAW, 3.0)
        assertEquals(45.0, rickshawFare, 1.0)

        // Auto for 3 km: 25 + (3 * 16) = 73 -> rounded 75 BDT
        val autoFare = fareService.calculateEstimatedFare(VehicleType.AUTO, 3.0)
        assertTrue("Rickshaw should be significantly more affordable than Auto", rickshawFare < autoFare)
    }

    @Test
    fun testLocationDistanceAndEstimation() {
        val locationService = LocationService()
        val majompur = locationService.kushtiaHubs[0] // Majompur Gate
        val medical = locationService.kushtiaHubs[3]  // Medical College

        val distanceKm = locationService.calculateDistanceKm(majompur, medical)
        assertTrue("Distance between Majompur and Medical should be positive", distanceKm > 0.5)

        val estimatedMinutes = locationService.estimateMinutes(distanceKm)
        assertTrue("Estimated travel time should be at least 3 minutes", estimatedMinutes >= 3)
    }

    @Test
    fun testRideServiceLifecycleWithRickshaw() {
        val locationService = LocationService()
        val fareService = FareService()
        val driverService = DriverService(locationService)
        val rideService = RideService(locationService, driverService, fareService)

        val pickup = locationService.kushtiaHubs[0]
        val dest = locationService.kushtiaHubs[1]

        val ride = rideService.requestRide(
            passengerId = "USER-1",
            passengerName = "তানভীর",
            passengerPhone = "01700112233",
            pickup = pickup,
            destination = dest,
            vehicleType = VehicleType.RICKSHAW
        )

        assertEquals(RideStatus.SEARCHING_DRIVER, ride.status)
        assertEquals(VehicleType.RICKSHAW, ride.vehicleType)
        assertEquals("USER-1", ride.passengerId)
        assertTrue("Estimated fare should be greater than 0", ride.estimatedFare > 0)
    }
}
