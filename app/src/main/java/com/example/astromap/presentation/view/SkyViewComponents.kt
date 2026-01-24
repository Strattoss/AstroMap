//package com.example.astromap.presentation.view
//
//import android.content.Context
//import android.view.Gravity
//import android.widget.FrameLayout
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.astromap.R
//import com.google.android.material.switchmaterial.SwitchMaterial
//
//class SkyViewComponents(private val context: Context) {
//
//    fun setupLayout(
//        skyView: SkyView,
//        labelView: StarLabelView,
//        sensorController: SensorController
//    ): FrameLayout {
//        val container = FrameLayout(context)
//        container.addView(skyView)
//        container.addView(labelView)
//
//        val explorationSwitch = createExplorationModeSwitch(skyView, labelView, sensorController)
//        container.addView(explorationSwitch)
//
//        ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            val params = explorationSwitch.layoutParams as FrameLayout.LayoutParams
//            params.bottomMargin = systemBars.bottom + 32
//            explorationSwitch.layoutParams = params
//            insets
//        }
//
//        return container
//    }
//
//    private fun createExplorationModeSwitch(
//        skyView: SkyView,
//        labelView: StarLabelView,
//        sensorController: SensorController
//    ): SwitchMaterial {
//        return SwitchMaterial(context).apply {
//            text = context.getString(R.string.exploration_mode)
//            val margin = 32
//            layoutParams = FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//            ).apply {
//                gravity = Gravity.BOTTOM or Gravity.END
//                bottomMargin = margin
//                marginEnd = margin
//            }
//            background = ContextCompat.getDrawable(context, R.drawable.switch_background)
//            setTextColor(ContextCompat.getColor(context, android.R.color.white))
//
//            setOnCheckedChangeListener { _, isChecked ->
//                skyView.renderer.explorationModeEnabled = isChecked
//                if (isChecked) {
//                    sensorController.start()
//                } else {
//                    sensorController.stop()
//                }
//                labelView.invalidate()
//            }
//        }
//    }
//}
package com.example.astromap.presentation.view

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
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

        val controlsPanel = createControlsPanel(
            skyView,
            labelView,
            sensorController
        )

        container.addView(controlsPanel)

        ViewCompat.setOnApplyWindowInsetsListener(container) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = controlsPanel.layoutParams as FrameLayout.LayoutParams
            params.bottomMargin = systemBars.bottom + 32
            params.marginEnd = systemBars.right + 32
            controlsPanel.layoutParams = params
            insets
        }

        return container
    }

    // =========================
    // PANEL ZE SWITCHAMI
    // =========================
    private fun createControlsPanel(
        skyView: SkyView,
        labelView: StarLabelView,
        sensorController: SensorController
    ): LinearLayout {

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            background = ContextCompat.getDrawable(context, R.drawable.switch_background)

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                bottomMargin = 32
                marginEnd = 32
            }

            // 🌍 TRYB EKSPLORACJI
            addView(createExplorationModeSwitch(skyView, labelView, sensorController))

            // ⭐ NAZWY GWIAZD
            addView(createStarLabelsSwitch(labelView))

            // ✨ NAZWY KONSTELACJI
            addView(createConstellationLabelsSwitch(labelView))
        }
    }

    // =========================
    // SWITCH: EXPLORATION MODE
    // =========================
    private fun createExplorationModeSwitch(
        skyView: SkyView,
        labelView: StarLabelView,
        sensorController: SensorController
    ): SwitchMaterial {

        return baseSwitch(context.getString(R.string.exploration_mode)).apply {
            setOnCheckedChangeListener { _, isChecked ->
                skyView.renderer.explorationModeEnabled = isChecked
                if (isChecked) sensorController.start()
                else sensorController.stop()

                labelView.invalidate()
            }
        }
    }

    // =========================
    // SWITCH: STAR LABELS
    // =========================
    private fun createStarLabelsSwitch(
        labelView: StarLabelView
    ): SwitchMaterial {

        return baseSwitch(context.getString(R.string.star_names)).apply {
            isChecked = true

            setOnCheckedChangeListener { _, isChecked ->
                labelView.showStarLabels = isChecked
                labelView.invalidate()
            }
        }
    }

    // =========================
    // SWITCH: CONSTELLATION LABELS
    // =========================
    private fun createConstellationLabelsSwitch(
        labelView: StarLabelView
    ): SwitchMaterial {

        return baseSwitch(context.getString(R.string.constellation_names)).apply {
            isChecked = true

            setOnCheckedChangeListener { _, isChecked ->
                labelView.showConstellationLabels = isChecked
                labelView.invalidate()
            }
        }
    }

    // =========================
    // WSPÓLNY WYGLĄD SWITCHY
    // =========================
    private fun baseSwitch(text: String): SwitchMaterial {
        return SwitchMaterial(context).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }
}

