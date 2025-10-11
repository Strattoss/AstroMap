package com.example.astromap.domain.model

data class Constellation(
    // TODO: for now every constellation consists of one segment (one line),
    //  real constellations contain several segments;
    //  change this in order to be able to represent constellations
    val fromId: Int,
    val toId: Int
)

