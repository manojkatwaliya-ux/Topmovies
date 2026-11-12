package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.SearchResponse
import com.example.data.repository.TopMoviesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val query: String, val results: SearchResponse) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(private val repository: TopMoviesRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        searchJob?.cancel()
        val trimmed = newQuery.trim()
        if (trimmed.length < 2) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(380)
            executeSearch(trimmed)
        }
    }

    fun submitSearch() {
        val trimmed = _query.value.trim()
        if (trimmed.isEmpty()) return
        searchJob?.cancel()
        viewModelScope.launch {
            executeSearch(trimmed)
        }
    }

    private suspend fun executeSearch(trimmed: String) {
        _uiState.value = SearchUiState.Loading
        repository.search(trimmed)
            .onSuccess { response ->
                _uiState.value = SearchUiState.Success(trimmed, response)
            }
            .onFailure { err ->
                _uiState.value = SearchUiState.Error(
                    err.localizedMessage ?: "Search request failed"
                )
            }
    }

    companion object {
        fun factory(repository: TopMoviesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SearchViewModel(repository) as T
            }
    }
}
