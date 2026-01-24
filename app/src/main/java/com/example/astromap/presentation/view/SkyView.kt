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
    private var onRotationChangeListener: (() -> Unit)? = null

    private var onStarClicked: ((Star) -> Unit)? = null

    private var previousX = 0f
    private var previousY = 0f

    init {
        setEGLContextClientVersion(2)
        renderer = SkyRenderer(stars, constellations)
        setRenderer(renderer)
//        renderMode = RENDERMODE_CONTINUOUSLY;

        setOnTouchListener { _, event ->
            if (!renderer.explorationModeEnabled && event.action == MotionEvent.ACTION_DOWN) {
                handleTouch(event.x, event.y)
            }
            true
        }
    }

    fun setOnRotationChangeListener(listener: () -> Unit) {
        onRotationChangeListener = listener
    }

    fun setOnStarClickListener(listener: (Star) -> Unit) {
        onStarClicked = listener
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
                onRotationChangeListener?.invoke()
                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }

    fun updateRotation(rotationMatrix: FloatArray) {
        renderer.updateRotation(rotationMatrix)
        onRotationChangeListener?.invoke()
        requestRender()
    }

    companion object {
        private const val TOUCH_SCALE_FACTOR = 0.1f
    }

    private fun handleTouch(x: Float, y: Float) {
        // szukamy gwiazdy najbliższej kliknięciu
        val thresholdPx = 50  // tolerancja w pikselach
        var closest: Star? = null
        var minDist = Float.MAX_VALUE

        for (star in stars) {
            val screenPos = renderer.projectStarToScreen(star.ra, star.dec, width, height) ?: continue
            val dx = screenPos.first - x
            val dy = screenPos.second - y
            val dist = dx*dx + dy*dy
            if (dist < minDist && dist < thresholdPx*thresholdPx) {
                minDist = dist
                closest = star
            }
        }

        closest?.let { onStarClicked?.invoke(it) }
    }
}
