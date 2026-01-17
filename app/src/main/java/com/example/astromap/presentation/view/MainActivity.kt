package com.example.astromap.presentation.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.astromap.data.repository.FileAstroRepository
import com.example.astromap.presentation.viewmodel.SkyViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var skyView: SkyView
    private lateinit var viewModel: SkyViewModel
    private lateinit var sensorController: SensorController
    private lateinit var labelView: StarLabelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val astroRepo = FileAstroRepository(this)
        viewModel = SkyViewModel(astroRepo)
        labelView = StarLabelView(this)

        sensorController = SensorController(this) { rotationMatrix ->
            if (::skyView.isInitialized) {
                skyView.updateRotation(rotationMatrix)
            }
        }

        setupViews()
    }

    private fun setupViews() {
        val stars = viewModel.loadStars()
        val constellations = viewModel.loadConstellations()

        skyView = SkyView(this, stars, constellations)
        skyView.setOnRotationChangeListener {
            labelView.invalidate()
        }

        labelView.stars = stars
        labelView.renderer = skyView.renderer

        val skyViewComponents = SkyViewComponents(this)
        val container = skyViewComponents.setupLayout(skyView, labelView, sensorController)
        setContentView(container)
    }

    override fun onResume() {
        super.onResume()
        if (::skyView.isInitialized) {
            skyView.onResume()
            if (skyView.renderer.explorationModeEnabled) {
                sensorController.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::skyView.isInitialized) {
            skyView.onPause()
        }
        sensorController.stop()
    }
}
