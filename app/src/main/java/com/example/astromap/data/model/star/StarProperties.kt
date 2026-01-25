package com.example.astromap.data.model.star

import com.google.gson.annotations.SerializedName

data class StarProperties(
    @SerializedName("mag") val mag: Double,
    @SerializedName("bv") val bv: String?
)