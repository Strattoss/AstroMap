package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationFeatureCollection(
    @SerializedName("features") val features: List<ConstellationFeature>
)