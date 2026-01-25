package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationGeometry(
    @SerializedName("coordinates") val coordinates: List<List<List<Double>>>
)