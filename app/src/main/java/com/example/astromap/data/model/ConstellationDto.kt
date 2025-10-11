package com.example.astromap.data.model

data class ConstellationDto(
    // TODO: for now this DTO is really poor, but in the future it should contain info
    //  available in out GeoJSONs
    val from: Int,  // star id
    val to: Int // star id
)