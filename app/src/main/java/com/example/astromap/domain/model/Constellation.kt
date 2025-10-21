package com.example.astromap.domain.model

data class Constellation(
    val stars: Set<Star>,
    val lines: Set<Pair<Star, Star>>
)
