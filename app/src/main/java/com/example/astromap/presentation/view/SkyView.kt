package com.example.astromap.presentation.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.astromap.domain.model.Star

class SkyView(context: Context, stars: List<Star>) : GLSurfaceView(context) {
    private val renderer: SkyRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = SkyRenderer(stars)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    private var previousX = 0f
    private var previousY = 0f
    private val touchScale = 0.25f  // sensitivity factor


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY

                // Invert Y so dragging up rotates upward
                renderer.angleY += dx * touchScale
                renderer.angleX += dy * touchScale

                // Keep angles in range
                renderer.angleX = renderer.angleX.coerceIn(-90f, 90f)
            }
        }

        previousX = x
        previousY = y
        return true
    }
}