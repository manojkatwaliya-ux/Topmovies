package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.HomeContentResponse
import com.example.data.repository.TopMoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeContentResponse) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(private val repository: TopMoviesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHome(initial = true)
    }

    fun loadHome(initial: Boolean = false) {
        viewModelScope.launch {
            if (initial || _uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            } else {
                _isRefreshing.value = true
            }
            repository.getHomeContent()
                .onSuccess { data ->
                    _uiState.value = HomeUiState.Success(data)
                }
                .onFailure { error ->
                    if (_uiState.value !is HomeUiState.Success) {
                        _uiState.value = HomeUiState.Error(
                            error.localizedMessage ?: "Failed to load TopMovies catalog"
                        )
                    }
                }
            _isRefreshing.value = false
        }
    }

    companion object {
        fun factory(repository: TopMoviesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(repository) as T
            }
    }
}
