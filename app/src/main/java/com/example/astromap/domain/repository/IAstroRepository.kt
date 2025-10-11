package com.example.astromap.domain.repository

import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star

interface IAstroRepository {
    fun getStars(): List<Star>

    fun getConstellations(): List<Constellation>
}