package com.example.astromap.domain.repository

import com.example.astromap.domain.model.Constellation

interface IConstellationRepository {
    fun getConstellations(): List<Constellation>
}