package com.example.astromap.data.model.star

import com.google.gson.annotations.SerializedName

data class StarNames(
    @SerializedName("name") val name: String?,
    @SerializedName("desig") val desig: String?,
    @SerializedName("bayer") val bayer: String?,
    @SerializedName("flam") val flam: String?,
    @SerializedName("var") val variable: String?,
    @SerializedName("gliese") val gliese: String?,
    @SerializedName("hd") val hd: String?,
    @SerializedName("hip") val hip: String?
    // other languages can be added here
)