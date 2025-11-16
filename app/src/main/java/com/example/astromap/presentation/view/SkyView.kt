package com.example.astromap.presentation.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.astromap.domain.model.Constellation
import com.example.astromap.domain.model.Star

class SkyView(
    context: Context,
    private val stars: List<Star>,
    private val constellations: List<Constellation>
) : GLSurfaceView(context) {

    val renderer: SkyRenderer

    private var previousX = 0f
    private var previousY = 0f

    init {
        setEGLContextClientVersion(2)
        renderer = SkyRenderer(stars, constellations)
        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (renderer.explorationModeEnabled) return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY

                renderer.rotateWithTouch(dx * TOUCH_SCALE_FACTOR, dy * TOUCH_SCALE_FACTOR)
                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }

    fun updateRotation(rotationMatrix: FloatArray) {
        renderer.updateRotation(rotationMatrix)
        requestRender()
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.1f
    }
}
