package com.example.astromap.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star

//class StarLabelView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyle: Int = 0
//) : View(context, attrs, defStyle) {
//
//    var stars: List<Star> = emptyList()
//    var renderer: SkyRenderer? = null
//
//    private val paint = Paint().apply {
//        color = Color.WHITE
//        textSize = 32f
//        isAntiAlias = true
//        setShadowLayer(4f, 2f, 2f, Color.BLACK)
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        val r = renderer ?: return
//        if (!r.isInitialized()) return
//
//        for (star in stars) {
//            val name = star.name ?: continue
//            val pos = r.projectStarToScreen(star.ra, star.dec, width, height) ?: continue
//
//            // Draw text slightly offset from the star center
//            canvas.drawText(name, pos.first + 10f, pos.second - 10f, paint)
//        }
//    }
//}

//class StarLabelView @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null
//) : View(context, attrs) {
//
//    private var constellations: List<Constellation> = emptyList()
//    private var screenWidth = 0
//    private var screenHeight = 0
//    private var skyRenderer: SkyRenderer? = null
//
//    private val paint = Paint().apply {
//        color = Color.YELLOW
//        textSize = 36f
//        isAntiAlias = true
//    }
//
//    fun setData(constellations: List<Constellation>, renderer: SkyRenderer) {
//        this.constellations = constellations
//        this.skyRenderer = renderer
//        invalidate()
//    }
//
//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        screenWidth = w
//        screenHeight = h
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        val renderer = skyRenderer ?: return
//        if (!renderer.isInitialized()) return
//
//        for (c in constellations) {
//            val name = c.name ?: continue
//            val displayCoords = c.displayCoords ?: continue
//            val (ra, dec, _) = displayCoords
//
//            val screenPos = renderer.projectStarToScreen(ra, dec, screenWidth, screenHeight)
//            screenPos?.let { (x, y) ->
//                canvas.drawText(name, x, y, paint)
//            }
//        }
//    }
//}

class StarLabelView(context: Context) : View(context) {

    var stars: List<Star> = emptyList()                     // białe nazwy gwiazd
    var constellations: List<Constellation> = emptyList()   // żółte nazwy konstelacji
    var renderer: SkyRenderer? = null

    private val starPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // biały
        textSize = 28f
        isAntiAlias = true
    }

    private val constellationPaint = Paint().apply {
        color = 0xFFFFFF00.toInt() // żółty
        textSize = 40f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val r = renderer ?: return
        val screenWidth = width
        val screenHeight = height

        // 1️⃣ Nazwy gwiazd (białe)
        for (star in stars) {
            val name = star.name ?: continue
            val pos = r.projectStarToScreen(star.ra, star.dec, screenWidth, screenHeight) ?: continue
            canvas.drawText(name, pos.first, pos.second, starPaint)
        }

        // 2️⃣ Nazwy konstelacji (żółte)
        for (constellation in constellations) {
            val name = constellation.name ?: continue
            val displayCoords = constellation.displayCoords ?: continue
            val (ra, dec, _) = displayCoords
            val pos = r.projectStarToScreen(ra, dec, screenWidth, screenHeight) ?: continue
            canvas.drawText(name, pos.first, pos.second, constellationPaint)
        }
    }
}

