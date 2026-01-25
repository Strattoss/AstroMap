package com.example.astromap.presentation.sensors

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.annotation.RequiresPermission

class LocationProvider(
    context: Context,
    private val onLocationChanged: (latitude: Double, longitude: Double) -> Unit
) : LocationListener {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    fun start() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5_000L,   // minTime (ms)
            1f,       // minDistance (meters)
            this
        )
    }

    fun stop() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        onLocationChanged(location.latitude, location.longitude)
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}
}
