package com.example.astromap.presentation.sensors

import java.time.Instant

class TimeProvider {
    fun nowUtc(): Instant = Instant.now()
}
