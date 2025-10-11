package com.example.astromap.data.model

data class StarDto(
    val id: Int,
    val ra: Double,   // Right Ascension in degrees (https://en.wikipedia.org/wiki/Right_ascension)
    val dec: Double,  // Declination in degrees (https://en.wikipedia.org/wiki/Declination)
    val mag: Double   // Apparent magnitude (brightness) (https://en.wikipedia.org/wiki/Magnitude_(astronomy))

    // TODO: add B-V color index (because it's in the json data)
//    val bvColor: Double
)