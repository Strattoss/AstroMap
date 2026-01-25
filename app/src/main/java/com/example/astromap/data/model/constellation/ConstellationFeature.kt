package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationFeature(
    @SerializedName("id") val id: String,
    @SerializedName("properties") val properties: ConstellationProperties,
    @SerializedName("geometry") val geometry: ConstellationGeometry
)