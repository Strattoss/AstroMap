package com.example.astromap.presentation.sensors

import java.time.Instant

data class ObservationSnapshot(
    val rotationMatrix: FloatArray,
    val latitude: Double,
    val longitude: Double,
    val timeUtc: Instant
)
