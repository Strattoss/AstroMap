package com.example.astromap.presentation.view

import android.opengl.Matrix
import java.time.Instant

object AstroMath {

    fun julianDate(time: Instant): Double {
        return time.epochSecond / 86400.0 + 2440587.5
    }

    fun gmst(time: Instant): Double {
        val jd = julianDate(time)
        val t = (jd - 2451545.0) / 36525.0
        var gmst = 280.46061837 +
                360.98564736629 * (jd - 2451545.0) +
                0.000387933 * t * t -
                t * t * t / 38710000.0
        gmst %= 360.0
        if (gmst < 0) gmst += 360.0
        return Math.toRadians(gmst)
    }

    fun skyOrientationMatrix(
        latitudeDeg: Double,
        longitudeDeg: Double,
        time: Instant
    ): FloatArray {

        val lat = Math.toRadians(latitudeDeg)
        val lst = gmst(time) + Math.toRadians(longitudeDeg)

        val m = FloatArray(16)
        Matrix.setIdentityM(m, 0)

        // Rotate RA → Hour Angle
        Matrix.rotateM(m, 0, Math.toDegrees(-lst).toFloat(), 0f, 1f, 0f)

        // Tilt equator to horizon
        Matrix.rotateM(m, 0, Math.toDegrees(Math.PI / 2 - lat).toFloat(), 1f, 0f, 0f)

        return m
    }
}
