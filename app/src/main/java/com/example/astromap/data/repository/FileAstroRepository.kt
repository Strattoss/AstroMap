package com.example.astromap.data.repository

import android.content.Context
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star
import com.example.astromap.domain.repository.IAstroRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class FileAstroRepository(private val context: Context) : IAstroRepository {
    private val _stars: List<Star> by lazy {
        parseStars(getStarsFileNameByLocation())
    }

    private val _constellations: List<Constellation> by lazy {
        parseConstellations(getConstellationsFileNameByLocation())
    }

    override fun getStars(): List<Star> = _stars

    override fun getConstellations(): List<Constellation> = _constellations

    private fun parseStars(fileName: String): List<Star> {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val starFeatureCollection = Gson().fromJson(jsonString, StarFeatureCollection::class.java)

        return starFeatureCollection.features.map {
            Star(
                id = it.id,
                ra = it.geometry.coordinates[0],
                dec = it.geometry.coordinates[1],
                mag = it.properties.mag
            )
        }
    }

    private fun parseConstellations(fileName: String): List<Constellation> {
        val constellations = mutableListOf<Constellation>()
        val starsMap = getStars().associateBy { Pair(it.ra, it.dec) }
        val jsonString =
            context.assets.open(fileName).bufferedReader()
                .use { it.readText() }
        val constellationFeatureCollection = Gson().fromJson(jsonString, ConstellationFeatureCollection::class.java)

        constellationFeatureCollection.features.forEach { feature ->
            feature.geometry.coordinates.forEach { line ->
                for (i in 0 until line.size - 1) {
                    val fromRa = line[i][0]
                    val fromDec = line[i][1]
                    val toRa = line[i + 1][0]
                    val toDec = line[i + 1][1]

                    val fromStar = starsMap[Pair(fromRa, fromDec)]
                    val toStar = starsMap[Pair(toRa, toDec)]

                    if (fromStar != null && toStar != null) {
                        constellations.add(Constellation(fromStar.id, toStar.id))
                    }
                }
            }
        }
        return constellations
    }

    private fun getStarsFileNameByLocation(): String {
        // TODO: Implementation for location
        return "stars.6.json"
    }

    private fun getConstellationsFileNameByLocation(): String {
        // TODO: Implementation for location
        return "constellations.lines.json"
    }
}

private data class StarFeatureCollection(@SerializedName("features") val features: List<StarFeature>)
private data class StarFeature(
    @SerializedName("id") val id: Int,
    @SerializedName("properties") val properties: StarProperties,
    @SerializedName("geometry") val geometry: StarGeometry
)
private data class StarProperties(@SerializedName("mag") val mag: Double)
private data class StarGeometry(@SerializedName("coordinates") val coordinates: List<Double>)

private data class ConstellationFeatureCollection(@SerializedName("features") val features: List<ConstellationFeature>)
private data class ConstellationFeature(@SerializedName("geometry") val geometry: ConstellationGeometry)
private data class ConstellationGeometry(@SerializedName("coordinates") val coordinates: List<List<List<Double>>>)
