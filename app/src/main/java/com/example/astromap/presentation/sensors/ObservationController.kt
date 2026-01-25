package com.example.astromap.presentation.sensors

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission

class ObservationController(
    context: Context,
    private val onSnapshotReady: (ObservationSnapshot) -> Unit
) {

    private val timeProvider = TimeProvider()

    private var rotationMatrix: FloatArray? = null
    private var latitude: Double? = 50.0 // default
    private var longitude: Double? = 19.94 // default

    private val orientationSensor =
        OrientationSensor(context) {
            rotationMatrix = it
            emitIfReady()
        }

    private val locationProvider =
        LocationProvider(context) { lat, lon ->
            latitude = lat
            longitude = lon
            emitIfReady()
        }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    fun start() {
        orientationSensor.start()
        locationProvider.start()
    }

    fun stop() {
        orientationSensor.stop()
        locationProvider.stop()
    }

    private fun emitIfReady() {
        val rot = rotationMatrix ?: return
        val lat = latitude ?: return
        val lon = longitude ?: return

        val observation = ObservationSnapshot(
            rotationMatrix = rot,
            latitude = lat,
            longitude = lon,
            timeUtc = timeProvider.nowUtc()
        )
        onSnapshotReady(observation)
    }
}
