package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.GateRepository
import com.example.network.AiTutorService

class ViewModelFactory(
    private val repository: GateRepository,
    private val aiTutorService: AiTutorService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GateViewModel::class.java)) {
            return GateViewModel(repository, aiTutorService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
