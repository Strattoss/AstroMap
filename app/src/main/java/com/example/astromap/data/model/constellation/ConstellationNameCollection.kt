package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationNameCollection(
    @SerializedName("features") val features: List<ConstellationNameFeature>
)