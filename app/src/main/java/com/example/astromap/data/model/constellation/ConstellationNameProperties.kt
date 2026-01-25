package com.example.astromap.data.model.constellation

import com.google.gson.annotations.SerializedName

data class ConstellationNameProperties(
    @SerializedName("name") val name: String?,
    @SerializedName("en") val en: String?,
    @SerializedName("desig") val desig: String?,
    @SerializedName("display") val display: List<Double>?
)