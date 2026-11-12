package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ApiEndpointConfig
import com.example.data.local.WatchlistEntity
import com.example.data.repository.ConnectionCheckResult
import com.example.data.repository.TopMoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(private val repository: TopMoviesRepository) : ViewModel() {

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<WatchlistEntity>> = repository.historyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val endpointConfig: StateFlow<ApiEndpointConfig> = repository.endpointConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiEndpointConfig())

    private val _connectionCheck = MutableStateFlow<ConnectionCheckResult?>(null)
    val connectionCheck: StateFlow<ConnectionCheckResult?> = _connectionCheck.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    fun runConnectionTest() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionCheck.value = repository.testConnection()
            _isTestingConnection.value = false
        }
    }

    fun updateEndpoint(rawUrl: String, overrideKey: String? = null) {
        viewModelScope.launch {
            repository.updateEndpointConfig(rawUrl, overrideKey)
            runConnectionTest()
        }
    }

    fun resetEndpoint() {
        viewModelScope.launch {
            repository.resetEndpointConfig()
            runConnectionTest()
        }
    }

    fun removeWatchlistItem(contentKey: String) {
        viewModelScope.launch {
            repository.removeWatchlistItem(contentKey)
        }
    }

    companion object {
        fun factory(repository: TopMoviesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LibraryViewModel(repository) as T
            }
    }
}
