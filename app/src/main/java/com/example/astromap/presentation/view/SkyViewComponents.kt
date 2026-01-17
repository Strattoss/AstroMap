package com.example.astromap.presentation.view

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.astromap.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SkyViewComponents(private val context: Context) {

    fun setupLayout(
        skyView: SkyView,
        labelView: StarLabelView,
        sensorController: SensorController
    ): FrameLayout {
        val container = FrameLayout(context)
        container.addView(skyView)
        container.addView(labelView)

        val explorationSwitch = createExplorationModeSwitch(skyView, labelView, sensorController)
        container.addView(explorationSwitch)

        ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = explorationSwitch.layoutParams as FrameLayout.LayoutParams
            params.bottomMargin = systemBars.bottom + 32
            explorationSwitch.layoutParams = params
            insets
        }

        return container
    }

    private fun createExplorationModeSwitch(
        skyView: SkyView,
        labelView: StarLabelView,
        sensorController: SensorController
    ): SwitchMaterial {
        return SwitchMaterial(context).apply {
            text = context.getString(R.string.exploration_mode)
            val margin = 32
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = margin
                marginEnd = margin
            }
            background = ContextCompat.getDrawable(context, R.drawable.switch_background)
            setTextColor(ContextCompat.getColor(context, android.R.color.white))

            setOnCheckedChangeListener { _, isChecked ->
                skyView.renderer.explorationModeEnabled = isChecked
                if (isChecked) {
                    sensorController.start()
                } else {
                    sensorController.stop()
                }
                labelView.invalidate()
            }
        }
    }
}
