package com.example.astromap.presentation.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.astromap.data.repository.FileAstroRepository
import com.example.astromap.data.repository.RandomAstroRepository
import com.example.astromap.presentation.viewmodel.SkyViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var skyView: SkyView
    private lateinit var viewModel: SkyViewModel
    private lateinit var sensorController: SensorController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val astroRepo = RandomAstroRepository(500, 200)    // uncomment if want to use fake data
        val astroRepo = FileAstroRepository(this)

        viewModel = SkyViewModel(astroRepo)

        sensorController = SensorController(this) { rotationMatrix ->
            skyView.updateRotation(rotationMatrix)
        }
        setupViews()
    }

    private fun setupViews() {
        val stars = viewModel.loadStars()
        val constellations = viewModel.loadConstellations()

        skyView = SkyView(this, stars, constellations)
        val skyViewComponents = SkyViewComponents(this)
        val container = skyViewComponents.setupLayout(skyView, sensorController)
        setContentView(container)
    }

    override fun onResume() {
        super.onResume()
        skyView.onResume()
        if (skyView.renderer.explorationModeEnabled) {
            sensorController.start()
        }
    }

    override fun onPause() {
        super.onPause()
        skyView.onPause()
        sensorController.stop()
    }
}
