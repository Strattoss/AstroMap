package com.example.astromap.data.model.star

import com.google.gson.annotations.SerializedName

data class StarFeatureCollection(
    @SerializedName("features") val features: List<StarFeature>
)