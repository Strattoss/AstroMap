package com.example.astromap.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star

class AstroLabelView(
    context: Context,
    var stars: List<Star> = emptyList(),
    var constellations: List<Constellation> = emptyList(),
    var renderer: SkyRenderer? = null
) : View(context) {

    var showStarLabels = true
    var showConstellationLabels = true
    var MAG_THRESH = 2.5

    private val starPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        isAntiAlias = true
    }

    private val constellationPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 40f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val r = renderer ?: return
        val screenWidth = width
        val screenHeight = height

        if (showStarLabels) {
            // Nazwy gwiazd (białe)
            for (star in stars) {
                val name = star.name ?: continue
                if (star.mag > MAG_THRESH) continue
                val pos =
                    r.projectStarToScreen(star.ra, star.dec, screenWidth, screenHeight) ?: continue
                canvas.drawText(name, pos.first, pos.second, starPaint)
            }
        }

        if (showConstellationLabels) {
            // Nazwy konstelacji (żółte)
            for (constellation in constellations) {
                val name = constellation.name ?: continue
                val displayCoords = constellation.displayCoords ?: continue
                val (ra, dec, _) = displayCoords
                val pos = r.projectStarToScreen(ra, dec, screenWidth, screenHeight) ?: continue
                canvas.drawText(name, pos.first, pos.second, constellationPaint)
            }
        }
    }
}

