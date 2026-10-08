package com.example.data.service

import com.example.data.model.FareConfig
import com.example.data.model.VehicleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class FareService {

    private val _autoConfig = MutableStateFlow(
        FareConfig(
            vehicleType = VehicleType.AUTO,
            baseFare = 25.0,
            perKmRate = 16.0,
            minimumFare = 30.0,
            waitingChargePerMin = 2.0,
            cancellationCharge = 20.0,
            commissionPercent = 10.0
        )
    )
    val autoConfig: StateFlow<FareConfig> = _autoConfig.asStateFlow()

    private val _rickshawConfig = MutableStateFlow(
        FareConfig(
            vehicleType = VehicleType.RICKSHAW,
            baseFare = 15.0,
            perKmRate = 10.0,
            minimumFare = 20.0,
            waitingChargePerMin = 1.0,
            cancellationCharge = 10.0,
            commissionPercent = 6.0
        )
    )
    val rickshawConfig: StateFlow<FareConfig> = _rickshawConfig.asStateFlow()

    private val _pakhiVanConfig = MutableStateFlow(
        FareConfig(
            vehicleType = VehicleType.PAKHI_VAN,
            baseFare = 20.0,
            perKmRate = 12.0,
            minimumFare = 25.0,
            waitingChargePerMin = 1.5,
            cancellationCharge = 15.0,
            commissionPercent = 8.0
        )
    )
    val pakhiVanConfig: StateFlow<FareConfig> = _pakhiVanConfig.asStateFlow()

    fun calculateEstimatedFare(vehicleType: VehicleType, distanceKm: Double, estimatedWaitingMinutes: Int = 0): Double {
        val config = when (vehicleType) {
            VehicleType.AUTO -> _autoConfig.value
            VehicleType.RICKSHAW -> _rickshawConfig.value
            VehicleType.PAKHI_VAN -> _pakhiVanConfig.value
        }
        val rawFare = config.baseFare + (distanceKm * config.perKmRate) + (estimatedWaitingMinutes * config.waitingChargePerMin)
        val finalFare = if (rawFare < config.minimumFare) config.minimumFare else rawFare
        return (finalFare / 5.0).roundToInt() * 5.0 // rounded to nearest 5 BDT for clean local change
    }

    fun updateAutoConfig(baseFare: Double, perKm: Double, minFare: Double, waitingPerMin: Double) {
        _autoConfig.value = _autoConfig.value.copy(
            baseFare = baseFare,
            perKmRate = perKm,
            minimumFare = minFare,
            waitingChargePerMin = waitingPerMin
        )
    }

    fun updateRickshawConfig(baseFare: Double, perKm: Double, minFare: Double, waitingPerMin: Double) {
        _rickshawConfig.value = _rickshawConfig.value.copy(
            baseFare = baseFare,
            perKmRate = perKm,
            minimumFare = minFare,
            waitingChargePerMin = waitingPerMin
        )
    }

    fun updatePakhiVanConfig(baseFare: Double, perKm: Double, minFare: Double, waitingPerMin: Double) {
        _pakhiVanConfig.value = _pakhiVanConfig.value.copy(
            baseFare = baseFare,
            perKmRate = perKm,
            minimumFare = minFare,
            waitingChargePerMin = waitingPerMin
        )
    }
}
