package com.example.astromap.data.repository

import android.content.Context
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star
import com.example.astromap.domain.repository.IConstellationRepository
import com.example.astromap.domain.repository.IStarRepository

class FileStellarRepository(private val context: Context) : IStarRepository, IConstellationRepository {
    override fun getStars(): List<Star> {
        TODO("Implement reading from file assets/stars.6.json (use Gson?)")
    }

    override fun getConstellations(): List<Constellation> {
        TODO("Implement readinf from file assets/constellations.lines.json (with Gson?)")
    }
}