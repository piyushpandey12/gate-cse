package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.GateRepository
import com.example.network.AiTutorService
import com.example.network.auth.TokenManager

class ViewModelFactory(
    private val repository: GateRepository,
    private val aiTutorService: AiTutorService,
    private val tokenManager: TokenManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GateViewModel::class.java)) {
            return GateViewModel(repository, aiTutorService, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
