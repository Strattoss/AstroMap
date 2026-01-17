package com.example.astromap.data.repository

import android.content.Context
import android.util.Log
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
        val currentYear = 2026.0
        val yearsSince2000 = currentYear - 2000.0

        // --- 1. Wczytanie głównego pliku z gwiazdami ---
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val starFeatureCollection = Gson().fromJson(jsonString, StarFeatureCollection::class.java)

        // --- 2. Wczytanie pliku z nazwami (BSC) ---
        val jsonStringNames = context.assets.open(getStarNamesNameByLocation()).bufferedReader().use { it.readText() }
        val bscStars = Gson().fromJson(jsonStringNames, Array<BSCStar>::class.java)

        // --- 3. Stworzenie mapy gwiazd z nazwami (po RA/DEC w stopniach) ---
        val nameMap = mutableMapOf<Pair<Double, Double>, String>()
        for (bsc in bscStars) {
            val raDeg = hmsToDegrees(bsc.RA) + (bsc.RA_PM.toDoubleOrNull() ?: 0.0) / 3600.0 * yearsSince2000
            val decDeg = dmsToDegrees(bsc.DEC) + (bsc.DEC_PM.toDoubleOrNull() ?: 0.0) / 3600.0 * yearsSince2000
            nameMap[Pair(raDeg, decDeg)] = bsc.Title_HD
        }

        // --- 4. Tworzymy listę gwiazd z nazwami dopasowanymi ---
        return starFeatureCollection.features.map { f ->
            val ra = f.geometry.coordinates[0]
            val dec = f.geometry.coordinates[1]

            val name = if (f.properties.mag <= 2.5) {
                // szukamy nazwy tylko dla jasnych gwiazd
                nameMap.entries.firstOrNull { (key, _) ->
                    val dRA = key.first - ra
                    val dDec = key.second - dec
                    (dRA*dRA + dDec*dDec) < 0.01*0.01
                }?.value
            } else null  // słabe gwiazdy – nazwa = null

            Star(
                id = f.id,
                ra = ra,
                dec = dec,
                mag = f.properties.mag,
                name = name
            )
        }
    }

    private fun parseConstellations(fileName: String): List<Constellation> {
        val starsMap = getStars().associateBy { Pair(it.ra, it.dec) }
        val jsonString =
            context.assets.open(fileName).bufferedReader()
                .use { it.readText() }
        val constellationFeatureCollection = Gson().fromJson(jsonString, ConstellationFeatureCollection::class.java)

        return constellationFeatureCollection.features.map { feature ->
            val stars = mutableSetOf<Star>()
            val lines = mutableSetOf<Pair<Star, Star>>()
            feature.geometry.coordinates.forEach { line ->
                for (i in 0 until line.size - 1) {
                    val fromRa = line[i][0]
                    val fromDec = line[i][1]
                    val toRa = line[i + 1][0]
                    val toDec = line[i + 1][1]

                    val fromStar = starsMap[Pair(fromRa, fromDec)]
                    val toStar = starsMap[Pair(toRa, toDec)]

                    if (fromStar != null && toStar != null) {
                        stars.add(fromStar)
                        stars.add(toStar)
                        lines.add(Pair(fromStar, toStar))
                    } else {
                        Log.w("astroMap", "Star not found: $fromRa, $fromDec or $toRa, $toDec")
                    }
                }
            }
            Constellation(stars, lines)
        }
    }

    private fun getStarsFileNameByLocation(): String {
        return "stars.6.json"
    }

    private fun getConstellationsFileNameByLocation(): String {
        return "constellations.lines.json"
    }

    private fun getStarNamesNameByLocation(): String {
        return "BSC.json"
    }
}

private data class StarFeatureCollection(@SerializedName("features") val features: List<StarFeature>)

//private data class StarNameEntry(
//    @SerializedName("ra") val ra: Double,
//    @SerializedName("dec") val dec: Double,
//    @SerializedName("titleHD") val name: String
//)

private data class StarFeature(
    @SerializedName("id") val id: Int,
    @SerializedName("properties") val properties: StarProperties,
    @SerializedName("geometry") val geometry: StarGeometry,
)

// --- Klasa dla BSC.json ---
private data class BSCStar(
    @SerializedName("RA") val RA: String,
    @SerializedName("DEC") val DEC: String,
    @SerializedName("RA PM") val RA_PM: String,
    @SerializedName("DEC PM") val DEC_PM: String,
    @SerializedName("Title HD") val Title_HD: String
)

private fun hmsToDegrees(hms: String): Double {
    val parts = hms.split(":")
    val h = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val m = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    val s = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
    return (h + m/60.0 + s/3600.0) * 15.0
}

private fun dmsToDegrees(dms: String): Double {
    val sign = if (dms.startsWith("-")) -1 else 1
    val clean = dms.replace("+","").replace("-","")
    val parts = clean.split(":")
    val d = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val m = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    val s = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
    return sign * (d + m/60.0 + s/3600.0)
}

//fun mapStarNames(starsGeoJson: String, bscJson: String) {
//    val geoStars = Gson().fromJson(starsGeoJson, StarFeatureCollection::class.java).features
//    val bscStars = Gson().fromJson(bscJson, Array<BscStar>::class.java)
//
//    // Mapowanie po przybliżonych współrzędnych
//    val bscMap = bscStars.associateBy {
//        Pair("%.2f".format(hmsToDegrees(it.RA)), "%.2f".format(dmsToDegrees(it.DEC)))
//    }
//
//    for (star in geoStars) {
//        val ra = star.geometry.coordinates[0]
//        val dec = star.geometry.coordinates[1]
//        val key = Pair("%.2f".format(ra), "%.2f".format(dec))
//        val name = bscMap[key]?.titleHD
//        Log.d("AstroMap", "Star ${star.id} -> $name")
//    }
//}
private data class StarProperties(@SerializedName("mag") val mag: Double)
private data class StarGeometry(@SerializedName("coordinates") val coordinates: List<Double>)

private data class ConstellationFeatureCollection(@SerializedName("features") val features: List<ConstellationFeature>)
private data class ConstellationFeature(@SerializedName("geometry") val geometry: ConstellationGeometry)
private data class ConstellationGeometry(@SerializedName("coordinates") val coordinates: List<List<List<Double>>>)
