package com.example.astromap.data.model.star

import com.google.gson.annotations.SerializedName

data class StarFeature(
    @SerializedName("id") val id: String, // Hipparcos number
    @SerializedName("properties") val properties: StarProperties,
    @SerializedName("geometry") val geometry: StarGeometry
)