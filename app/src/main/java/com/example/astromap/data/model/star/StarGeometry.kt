package com.example.astromap.data.model.star

import com.google.gson.annotations.SerializedName

data class StarGeometry(
    @SerializedName("coordinates") val coordinates: List<Double>
)