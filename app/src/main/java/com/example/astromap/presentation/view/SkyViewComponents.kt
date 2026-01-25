package com.example.astromap.presentation.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.astromap.R
import com.example.astromap.presentation.sensors.ObservationController
import com.google.android.material.switchmaterial.SwitchMaterial

import android.widget.SeekBar
import android.widget.TextView


class SkyViewComponents(private val context: Context) {

    fun setupLayout(
        skyView: SkyView,
        labelView: AstroLabelView,
        observationController: ObservationController
    ): FrameLayout {
        val container = FrameLayout(context)
        container.addView(skyView)
        container.addView(labelView)

        // ---- LinearLayout w prawym dolnym rogu na switche ----
        val switchContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(16, 16, 16, 16)

            setBackgroundColor(0xCC000000.toInt())

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.BOTTOM or Gravity.END }
        }

        // tworzymy switche
        val starLabelSwitch = createStarLabelSwitch(labelView)
        val constellationLabelSwitch = createConstellationLabelSwitch(labelView)
        val explorationSwitch = createExplorationModeSwitch(skyView, labelView, observationController)
        val starClickSwitch = createStarClickSwitch(skyView)

        // dodajemy switche do linear layout
        switchContainer.addView(starLabelSwitch)
        switchContainer.addView(constellationLabelSwitch)
        switchContainer.addView(explorationSwitch)
        switchContainer.addView(starClickSwitch)

        switchContainer.addView(createMagnitudeSlider(labelView))

        container.addView(switchContainer)

        return container
    }

    private fun createStarLabelSwitch(labelView: AstroLabelView): SwitchMaterial {
        return SwitchMaterial(context).apply {
            text = context.getString(R.string.show_star_labels)
            setTextColor(Color.WHITE)
            isChecked = true
            setOnCheckedChangeListener { _, isChecked ->
                labelView.showStarLabels = isChecked
                labelView.invalidate()
            }
        }
    }


    private fun createConstellationLabelSwitch(labelView: AstroLabelView) = SwitchMaterial(context).apply {
        text = context.getString(R.string.show_constellation_labels)
        setTextColor(Color.WHITE)
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.START }
        setOnCheckedChangeListener { _, isChecked ->
            labelView.showConstellationLabels = isChecked
            labelView.invalidate()
        }
    }

    private fun createExplorationModeSwitch(
        skyView: SkyView,
        labelView: AstroLabelView,
        observationController: ObservationController
    ) = SwitchMaterial(context).apply {
        text = context.getString(R.string.exploration_mode)
        setTextColor(Color.WHITE)
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.END }
        @SuppressLint("MissingPermission")
        setOnCheckedChangeListener { _, isChecked ->
            skyView.renderer.explorationModeEnabled = isChecked
            if (isChecked) observationController.start() else observationController.stop()
            labelView.invalidate()
        }
    }

    private fun createStarClickSwitch(skyView: SkyView) = SwitchMaterial(context).apply {
        text = context.getString(R.string.enable_star_clicks)
        setTextColor(Color.WHITE)
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM or Gravity.END }

        isChecked = false

        setOnCheckedChangeListener { _, isChecked ->
            skyView.starClickEnabled = isChecked
        }
    }

    private fun createMagnitudeSlider(labelView: AstroLabelView): LinearLayout {
        val title = TextView(context).apply {
            text = "Magnitude ≤ 2.5"
            setTextColor(Color.WHITE)
            textSize = 12f
        }

        val minMag = -3.0
        val maxMag = 7.0
        val step = 0.1
        val startMag = 2.5

        val seekBarMax = ((maxMag - minMag) / step).toInt()
        val initialProgress = ((startMag / step).toInt())

        val seekBar = SeekBar(context).apply {
            max = seekBarMax
            progress = initialProgress
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = minMag + progress * step
                labelView.magnitudeThreshold = value
                title.text = "Magnitude ≤ %.1f".format(value)
                labelView.invalidate()
            }

            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 0)
            addView(title)
            addView(seekBar)
        }
    }

}

