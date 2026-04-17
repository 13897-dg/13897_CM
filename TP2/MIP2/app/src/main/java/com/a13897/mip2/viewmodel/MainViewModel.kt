package com.a13897.mip2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.mip2.model.ImageItem
import com.a13897.mip2.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<ImageItem>) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    private val repository = ImageRepository()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var currentPage = 1

    init {
        fetchImages()
    }

    fun fetchImages(isRefresh: Boolean = false) {
        if (isRefresh) {
            // Picsum API is highly deterministic. Page 1 is always the same identical list.
            // To simulate a "random feed" refresh, we jump to a random page.
            currentPage = (1..50).random()
        }
        
        viewModelScope.launch {
            if (!isRefresh) {
                _uiState.value = UiState.Loading
            }
            try {
                // To fetch random lists on refresh reliably from picsum, we can increment page
                val response = repository.getImages(page = currentPage, limit = 20)
                _uiState.value = UiState.Success(response)
                currentPage++
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
