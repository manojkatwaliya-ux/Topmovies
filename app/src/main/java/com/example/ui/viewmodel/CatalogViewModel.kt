package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.LiveTvCategory
import com.example.data.remote.VideoSummary
import com.example.data.repository.TopMoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class CatalogMode {
    MOVIES,
    SERIES,
    GENRE,
    COUNTRY
}

data class CatalogUiState(
    val items: List<VideoSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1,
    val errorMessage: String? = null
)

class CatalogViewModel(
    private val repository: TopMoviesRepository,
    private val mode: CatalogMode,
    private val filterId: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState(isLoading = true)
            fetchPage(page = 1, append = false)
        }
    }

    fun loadNextPage() {
        val current = _uiState.value
        if (current.isLoading || current.isLoadingMore || !current.canLoadMore) return
        viewModelScope.launch {
            _uiState.value = current.copy(isLoadingMore = true)
            fetchPage(page = current.currentPage + 1, append = true)
        }
    }

    private suspend fun fetchPage(page: Int, append: Boolean) {
        val result = when (mode) {
            CatalogMode.MOVIES -> repository.getMovies(page)
            CatalogMode.SERIES -> repository.getTvSeries(page)
            CatalogMode.GENRE -> repository.getContentByGenre(filterId, page)
            CatalogMode.COUNTRY -> repository.getContentByCountry(filterId, page)
        }
        result.onSuccess { list ->
            val currentItems = if (append) _uiState.value.items else emptyList()
            val merged = (currentItems + list).distinctBy { "${it.contentType}_${it.videosId}" }
            _uiState.value = CatalogUiState(
                items = merged,
                isLoading = false,
                isLoadingMore = false,
                canLoadMore = list.isNotEmpty(),
                currentPage = page,
                errorMessage = null
            )
        }.onFailure { err ->
            val current = _uiState.value
            _uiState.value = current.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = if (current.items.isEmpty()) {
                    err.localizedMessage ?: "Failed to load catalog"
                } else null
            )
        }
    }

    companion object {
        fun factory(
            repository: TopMoviesRepository,
            mode: CatalogMode,
            filterId: String = ""
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CatalogViewModel(repository, mode, filterId) as T
        }
    }
}

sealed interface LiveTvUiState {
    data object Loading : LiveTvUiState
    data class Success(val categories: List<LiveTvCategory>) : LiveTvUiState
    data class Error(val message: String) : LiveTvUiState
}

class LiveTvViewModel(private val repository: TopMoviesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<LiveTvUiState>(LiveTvUiState.Loading)
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = LiveTvUiState.Loading
            repository.getLiveTvCategories(1)
                .onSuccess { categories ->
                    _uiState.value = LiveTvUiState.Success(categories)
                }
                .onFailure { err ->
                    _uiState.value = LiveTvUiState.Error(
                        err.localizedMessage ?: "Failed to load Live TV channels"
                    )
                }
        }
    }

    companion object {
        fun factory(repository: TopMoviesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    LiveTvViewModel(repository) as T
            }
    }
}
