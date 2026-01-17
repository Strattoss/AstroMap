package com.example.astromap.presentation.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.astromap.domain.model.Star

class StarLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var stars: List<Star> = emptyList()
    var renderer: SkyRenderer? = null

    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        isAntiAlias = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = renderer ?: return
        if (!r.isInitialized()) return

        for (star in stars) {
            val name = star.name ?: continue
            val pos = r.projectStarToScreen(star.ra, star.dec, width, height) ?: continue
            
            // Draw text slightly offset from the star center
            canvas.drawText(name, pos.first + 10f, pos.second - 10f, paint)
        }
    }
}
