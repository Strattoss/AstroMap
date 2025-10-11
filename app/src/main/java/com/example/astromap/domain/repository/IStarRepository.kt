package com.example.astromap.domain.repository

import com.example.astromap.domain.model.Star

interface IStarRepository {
    fun getStars(): List<Star>
}