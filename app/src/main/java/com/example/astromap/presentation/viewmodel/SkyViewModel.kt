package com.example.astromap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.astromap.domain.model.Star
import com.example.astromap.domain.repository.IStarRepository

class SkyViewModel (private val starRepository: IStarRepository) : ViewModel() {
    fun loadStars(): List<Star> {
        return starRepository.getStars()
    }
}