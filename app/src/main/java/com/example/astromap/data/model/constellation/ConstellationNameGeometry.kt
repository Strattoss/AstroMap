package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationNameGeometry(
    @SerializedName("coordinates") val coordinates: List<Double>
)