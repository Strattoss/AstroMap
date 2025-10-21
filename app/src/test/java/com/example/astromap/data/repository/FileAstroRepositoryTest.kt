package com.example.astromap.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.example.astromap.domain.model.Star
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.ByteArrayInputStream

class FileAstroRepositoryTest {

    private lateinit var mockContext: Context
    private lateinit var mockAssetManager: AssetManager
    private lateinit var repository: FileAstroRepository

    @Before
    fun setUp() {
        mockContext = mock()
        mockAssetManager = mock()
        doReturn(mockAssetManager).`when`(mockContext).assets
    }

    @Test
    fun `getStars should return a list of stars`() {
        val json = """ 
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "id": 1,
              "properties": {
                "mag": 1.0
              },
              "geometry": {
                "type": "Point",
                "coordinates": [
                  1.0,
                  1.0
                ]
              }
            },
            {
              "type": "Feature",
              "id": 2,
              "properties": {
                "mag": 2.0
              },
              "geometry": {
                "type": "Point",
                "coordinates": [
                  2.0,
                  2.0
                ]
              }
            }
          ]
        }
        """.trimIndent()
        val inputStream = ByteArrayInputStream(json.toByteArray())
        doReturn(inputStream).`when`(mockAssetManager).open("stars.6.json")

        repository = FileAstroRepository(mockContext)

        val stars = repository.getStars()

        assertEquals(2, stars.size)
        assertEquals(Star(1, 1.0, 1.0, 1.0), stars[0])
        assertEquals(Star(2, 2.0, 2.0, 2.0), stars[1])
    }

    @Test
    fun `getConstellations should return a list of constellations`() {
        val starsJson = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "id": 1,
              "properties": {
                "mag": 1.0
              },
              "geometry": {
                "type": "Point",
                "coordinates": [
                  1.0,
                  1.0
                ]
              }
            },
            {
              "type": "Feature",
              "id": 2,
              "properties": {
                "mag": 2.0
              },
              "geometry": {
                "type": "Point",
                "coordinates": [
                  2.0,
                  2.0
                ]
              }
            }
          ]
        }
        """.trimIndent()
        val constellationsJson = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "id": "And",
              "properties": {
                "rank": "1"
              },
              "geometry": {
                "type": "MultiLineString",
                "coordinates": [
                  [
                    [
                      1.0,
                      1.0
                    ],
                    [
                      2.0,
                      2.0
                    ]
                  ]
                ]
              }
            }
          ]
        }
        """.trimIndent()
        val starsInputStream = ByteArrayInputStream(starsJson.toByteArray())
        val constellationsInputStream = ByteArrayInputStream(constellationsJson.toByteArray())

        doReturn(starsInputStream).`when`(mockAssetManager).open("stars.6.json")
        doReturn(constellationsInputStream).`when`(mockAssetManager).open("constellations.lines.json")

        repository = FileAstroRepository(mockContext)

        val constellations = repository.getConstellations()

        val star1 = Star(1, 1.0, 1.0, 1.0)
        val star2 = Star(2, 2.0, 2.0, 2.0)

        assertEquals(1, constellations.size)
        assertEquals(setOf(star1, star2), constellations[0].stars)
        assertEquals(setOf(Pair(star1, star2)), constellations[0].lines)
    }
}
