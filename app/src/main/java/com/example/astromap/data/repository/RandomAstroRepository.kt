package com.example.astromap.data.repository

import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star
import com.example.astromap.domain.repository.IAstroRepository
import kotlin.math.asin
import kotlin.random.Random

class RandomAstroRepository(numStars: Int, numConstellations: Int) : IAstroRepository {
    private val stars = List(numStars) { id ->
        val ra = Random.nextDouble(0.0, 360.0)
        val dec = Math.toDegrees(asin(Random.nextDouble(-1.0, 1.0)))
        val mag = Random.nextDouble(-1.0, 6.0)
        Star(id, ra, dec, mag)
    }

    private val constellations = List(numConstellations) { randomConstellation() }

    override fun getStars(): List<Star> = stars
    override fun getConstellations(): List<Constellation> = constellations

    private fun randomConstellation(): Constellation {
        var fromId: Int
        var toId: Int
        do {
            fromId = Random.nextInt(0, stars.size)
            toId = Random.nextInt(0, stars.size)
        } while (fromId == toId)

        return Constellation(fromId, toId)
    }
}