package com.example.astromap.presentation.view

import android.Manifest
import android.os.Bundle
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.astromap.data.repository.FileAstroRepository
import com.example.astromap.presentation.sensors.ObservationController
import com.example.astromap.presentation.viewmodel.SkyViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var skyView: SkyView
    private lateinit var viewModel: SkyViewModel
    private lateinit var observationController: ObservationController
    private lateinit var labelView: AstroLabelView

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionHelper = LocationPermissionHelper(this)

        if (!locationPermissionHelper.hasPermission()) {
            locationPermissionHelper.request()
        }

        val astroRepo = FileAstroRepository(this)
        viewModel = SkyViewModel(astroRepo)

        observationController = ObservationController(this) { snapshot ->
            if (::skyView.isInitialized) {
                skyView.updateObservation(snapshot)
            }
        }

        setupViews()
    }

    private fun setupViews() {
        val stars = viewModel.loadStars()
        val constellations = viewModel.loadConstellations()

        skyView = SkyView(this, stars, constellations)
        labelView = AstroLabelView(this, stars, constellations, skyView.renderer)

        skyView.setOnRotationChangeListener {
            labelView.invalidate()
        }

        skyView.setOnStarClickListener { star ->
            AlertDialog.Builder(this)
                .setTitle(star.name ?: "Unknown")
                .setMessage("""
            Mag: ${star.mag}
            B-V: ${star.bval ?: "N/A"}
            RA: ${star.ra}
            Dec: ${star.dec}
        """.trimIndent())
                .setPositiveButton("OK", null)
                .show()
        }

        val skyViewComponents = SkyViewComponents(this)
        val container = skyViewComponents.setupLayout(skyView, labelView, observationController)
        setContentView(container)
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION])
    override fun onResume() {
        super.onResume()
        if (::skyView.isInitialized) {
            skyView.onResume()
            if (skyView.renderer.explorationModeEnabled) {
                observationController.start()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::skyView.isInitialized) {
            skyView.onPause()
        }
        observationController.stop()
    }
}
