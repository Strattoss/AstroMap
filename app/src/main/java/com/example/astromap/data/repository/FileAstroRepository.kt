package com.example.astromap.data.repository

import android.content.Context
import com.example.astromap.data.model.constellation.ConstellationFeatureCollection
import com.example.astromap.data.model.constellation.ConstellationNameCollection
import com.example.astromap.data.model.star.StarFeatureCollection
import com.example.astromap.data.model.star.StarNames
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star
import com.example.astromap.domain.repository.IAstroRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

const val STARS_FILE = "stars.6.json"
const val STAR_NAMES_FILE = "starnames.json"
const val CONSTELLATIONS_FILE = "constellations.lines.json"
const val CONSTELLATION_NAMES_FILE = "constellations.json"

class FileAstroRepository(
    private val context: Context
) : IAstroRepository {

    private val gson = Gson()

    private val starsCache: List<Star> by lazy {
        parseStars(STARS_FILE, STAR_NAMES_FILE)
    }

    private val constellationsCache: List<Constellation> by lazy {
        parseConstellations(
            CONSTELLATIONS_FILE,
            CONSTELLATION_NAMES_FILE
        )
    }

    override fun getStars(): List<Star> = starsCache
    override fun getConstellations(): List<Constellation> = constellationsCache

    // ------------------------------------------------------------
    // Stars
    // ------------------------------------------------------------

    private fun parseStars(
        starsFile: String,
        namesFile: String
    ): List<Star> {

        // 1. Load main stars GeoJSON
        val starsJson = readAsset(starsFile)
        val starCollection =
            gson.fromJson(starsJson, StarFeatureCollection::class.java)

        // 2. Load secondary names file (key = Hipparcos ID)
        val namesJson = readAsset(namesFile)
        val starNamesByHip: Map<String, StarNames> =
            gson.fromJson(namesJson, object : TypeToken<Map<String, StarNames>>() {}.type)

        // 3. Merge by Hipparcos ID
        return starCollection.features.map { feature ->

            val hipId = feature.id
            val names = starNamesByHip[hipId]

            Star(
                id = hipId.toInt(),
                ra = feature.geometry.coordinates[0],
                dec = feature.geometry.coordinates[1],
                mag = feature.properties.mag,
                bval = feature.properties.bv?.toDoubleOrNull(),

                // naming priority (easy to change later)
                name = names?.name
            )
        }
    }

    // ------------------------------------------------------------
    // Constellations
    // ------------------------------------------------------------

    private fun parseConstellations(
        linesFile: String,
        namesFile: String
    ): List<Constellation> {

        // ---- load stars for line matching (unchanged) ----
        val starsByPosition =
            starsCache.associateBy { Pair(it.ra, it.dec) }

        // ---- load constellation lines ----
        val linesJson = readAsset(linesFile)
        val lineCollection =
            gson.fromJson(linesJson, ConstellationFeatureCollection::class.java)

        // ---- load constellation names ----
        val namesJson = readAsset(namesFile)
        val nameCollection =
            gson.fromJson(namesJson, ConstellationNameCollection::class.java)

        // Map: "ORI" -> name feature
        val namesById = nameCollection.features.associateBy { it.id }

        // ---- merge geometry + names ----
        return lineCollection.features.map { lineFeature ->

            val constellationStars = mutableSetOf<Star>()
            val lines = mutableSetOf<Pair<Star, Star>>()

            lineFeature.geometry.coordinates.forEach { polyline ->
                for (i in 0 until polyline.size - 1) {

                    val from = findStar(starsByPosition, polyline[i])
                    val to = findStar(starsByPosition, polyline[i + 1])

                    if (from != null && to != null) {
                        constellationStars += from
                        constellationStars += to
                        lines += from to to
                    }
                }
            }

            // ---- attach name + display data ----
            val nameFeature = namesById[lineFeature.id]

            Constellation(
                name = nameFeature?.properties?.en
                    ?: nameFeature?.properties?.name, // fallback
                stars = constellationStars,
                lines = lines,
                displayCoords = nameFeature?.properties?.display?.let {
                    Triple(it[0], it[1], it[2])
                }
            )
        }
    }

    private fun findStar(
        stars: Map<Pair<Double, Double>, Star>,
        coord: List<Double>
    ): Star? {
        val ra = coord[0]
        val dec = coord[1]

        return stars.entries.firstOrNull { (key, _) ->
            val dRa = key.first - ra
            val dDec = key.second - dec
            (dRa * dRa + dDec * dDec) < 0.0001
        }?.value
    }

    private fun readAsset(name: String): String =
        context.assets.open(name).bufferedReader().use { it.readText() }
}
