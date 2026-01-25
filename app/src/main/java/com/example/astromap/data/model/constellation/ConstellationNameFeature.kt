package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationNameFeature(
    @SerializedName("id") val id: String, // 3-letter designator
    @SerializedName("properties") val properties: ConstellationNameProperties,
    @SerializedName("geometry") val geometry: ConstellationNameGeometry
)