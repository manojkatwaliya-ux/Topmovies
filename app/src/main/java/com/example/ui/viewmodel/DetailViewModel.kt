package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.SingleDetailsResponse
import com.example.data.repository.TopMoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val details: SingleDetailsResponse) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    private val repository: TopMoviesRepository,
    private val videoId: String,
    private val contentType: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isBookmarked: StateFlow<Boolean> = repository.observeIsBookmarked(videoId, contentType)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            repository.getSingleDetails(type = contentType, id = videoId)
                .onSuccess { details ->
                    _uiState.value = DetailUiState.Success(details)
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(
                        error.localizedMessage ?: "Could not load details"
                    )
                }
        }
    }

    fun toggleWatchlist(details: SingleDetailsResponse) {
        viewModelScope.launch {
            repository.toggleWatchlist(
                videosId = details.videosId.ifBlank { videoId },
                contentType = contentType,
                title = details.title,
                posterUrl = details.bestPosterUrl,
                releaseYear = details.release,
                imdbRating = details.imdbRating,
                videoQuality = details.videoQuality
            )
        }
    }

    fun recordPlay(details: SingleDetailsResponse, watchedLabel: String) {
        viewModelScope.launch {
            repository.recordWatchHistory(
                videosId = details.videosId.ifBlank { videoId },
                contentType = contentType,
                title = details.title,
                posterUrl = details.bestPosterUrl,
                releaseYear = details.release,
                imdbRating = details.imdbRating,
                videoQuality = details.videoQuality,
                watchedLabel = watchedLabel
            )
        }
    }

    companion object {
        fun factory(
            repository: TopMoviesRepository,
            videoId: String,
            contentType: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetailViewModel(repository, videoId, contentType) as T
        }
    }
}
