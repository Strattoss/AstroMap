package com.example.astromap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.astromap.domain.model.Star
import com.example.astromap.domain.repository.IAstroRepository

class SkyViewModel (private val astroRepo: IAstroRepository) : ViewModel() {
    fun loadStars(): List<Star> {
        return astroRepo.getStars()
    }
}