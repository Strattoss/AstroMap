package com.example.astromap.presentation.view

import android.opengl.Matrix
import android.util.Log
import java.time.Instant

// ---- Time & angle constants ----
private const val SECONDS_PER_DAY = 86400.0
private const val DEGREES_PER_CIRCLE = 360.0

// ---- Julian date constants ----
// Julian Date of Unix epoch (1970-01-01T00:00:00Z)
private const val JD_UNIX_EPOCH = 2440587.5

// Julian Date of J2000.0 epoch (2000-01-01T12:00:00 TT)
private const val JD_J2000 = 2451545.0

// ---- GMST polynomial constants (IAU 2006 / Meeus) ----
private const val GMST_BASE_DEG = 280.46061837
private const val GMST_RATE_DEG_PER_DAY = 360.98564736629
private const val GMST_T2_COEFF = 0.000387933
private const val GMST_T3_DENOM = 38710000.0

// ---- Angle constants ----
private const val HALF_TURN_RAD = Math.PI / 2.0

object AstroMath {

    fun julianDate(time: Instant): Double {
        return time.epochSecond / SECONDS_PER_DAY + JD_UNIX_EPOCH
    }

    fun gmst(time: Instant): Double { // Greenwich Mean Sidereal Time
        val jd = julianDate(time)
        val centuriesSinceJ2000 = (jd - JD_J2000) / 36525.0

        var gmstDeg =
            GMST_BASE_DEG +
                    GMST_RATE_DEG_PER_DAY * (jd - JD_J2000) +
                    GMST_T2_COEFF * centuriesSinceJ2000 * centuriesSinceJ2000 -
                    centuriesSinceJ2000 * centuriesSinceJ2000 * centuriesSinceJ2000 / GMST_T3_DENOM

        gmstDeg %= DEGREES_PER_CIRCLE
        if (gmstDeg < 0) gmstDeg += DEGREES_PER_CIRCLE

        return Math.toRadians(gmstDeg)
    }


    fun skyOrientationMatrix(
        latitudeDeg: Double,
        longitudeDeg: Double,
        time: Instant
    ): FloatArray {

        val latitudeRad = Math.toRadians(latitudeDeg)
        val localSiderealTimeRad = gmst(time) + Math.toRadians(longitudeDeg)
        Log.d("myapp", localSiderealTimeRad.toString());

        val m = FloatArray(16)
        Matrix.setIdentityM(m, 0)

        // Rotate Right Ascension → Hour Angle
        Matrix.rotateM(
            m,
            0,
            Math.toDegrees(-localSiderealTimeRad).toFloat(),
            0f, 1f, 0f
        )

        // Tilt celestial equator to horizon
        // Altitude of NCP = latitude
        Matrix.rotateM(
            m,
            0,
            Math.toDegrees(HALF_TURN_RAD - latitudeRad).toFloat(),
            1f, 0f, 0f
        )

        return m
    }
}
