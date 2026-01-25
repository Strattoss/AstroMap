package com.example.astromap.domain.model

data class Constellation(
    val name: String?, // English name
    val stars: Set<Star>,
    val lines: Set<Pair<Star, Star>>,
    val displayCoords: Triple<Double, Double, Double>? = null
)
