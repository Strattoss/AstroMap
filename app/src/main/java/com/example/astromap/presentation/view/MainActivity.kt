package com.example.astromap.presentation.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.astromap.data.repository.FileAstroRepository
import com.example.astromap.data.repository.RandomAstroRepository
import com.example.astromap.presentation.viewmodel.SkyViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var skyView: SkyView
    private lateinit var viewModel: SkyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val astroRepo = RandomAstroRepository(500, 200)    // uncomment if want to use fake data
        val astroRepo = FileAstroRepository(this)

        viewModel = SkyViewModel(astroRepo)
        val stars = viewModel.loadStars()

        skyView = SkyView(this, stars)
        setContentView(skyView)
    }

    override fun onResume() {
        super.onResume()
        skyView.onResume()
    }

    override fun onPause() {
        super.onPause()
        skyView.onPause()
    }
}