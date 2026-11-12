package com.example.data.repository

import com.example.data.local.ApiEndpointConfig
import com.example.data.local.SettingsPreferences
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.remote.AppConfigResponse
import com.example.data.remote.HomeContentResponse
import com.example.data.remote.LiveTvCategory
import com.example.data.remote.SearchResponse
import com.example.data.remote.SingleDetailsResponse
import com.example.data.remote.TopMoviesApiService
import com.example.data.remote.VideoSummary
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

data class ConnectionCheckResult(
    val success: Boolean,
    val resolvedUrl: String,
    val httpCode: Int?,
    val latencyMs: Long,
    val message: String
)

class TopMoviesRepository(
    private val api: TopMoviesApiService,
    private val watchlistDao: WatchlistDao,
    private val settingsPreferences: SettingsPreferences
) {
    val endpointConfigFlow: Flow<ApiEndpointConfig> = settingsPreferences.endpointConfigFlow
    val watchlistFlow: Flow<List<WatchlistEntity>> = watchlistDao.observeWatchlist()
    val historyFlow: Flow<List<WatchlistEntity>> = watchlistDao.observeRecentHistory()

    fun observeIsBookmarked(videosId: String, contentType: String): Flow<Boolean> =
        watchlistDao.observeIsBookmarked(buildContentKey(videosId, contentType))

    suspend fun getHomeContent(): Result<HomeContentResponse> = executeWithFallback(
        endpoint = "home_content_for_android"
    ) { url, key ->
        api.getHomeContent(url = url, apiKey = key)
    }

    suspend fun getMovies(page: Int = 1): Result<List<VideoSummary>> = executeWithFallback(
        endpoint = "movies"
    ) { url, key ->
        api.getVideoList(url = url, apiKey = key, params = mapOf("page" to page.toString()))
    }

    suspend fun getTvSeries(page: Int = 1): Result<List<VideoSummary>> = executeWithFallback(
        endpoint = "tvseries"
    ) { url, key ->
        api.getVideoList(url = url, apiKey = key, params = mapOf("page" to page.toString()))
    }

    suspend fun getContentByGenre(genreId: String, page: Int = 1): Result<List<VideoSummary>> = executeWithFallback(
        endpoint = "content_by_genre_id"
    ) { url, key ->
        api.getVideoList(
            url = url,
            apiKey = key,
            params = mapOf("id" to genreId, "page" to page.toString())
        )
    }

    suspend fun getContentByCountry(countryId: String, page: Int = 1): Result<List<VideoSummary>> = executeWithFallback(
        endpoint = "content_by_country_id"
    ) { url, key ->
        api.getVideoList(
            url = url,
            apiKey = key,
            params = mapOf("id" to countryId, "page" to page.toString())
        )
    }

    suspend fun getLiveTvCategories(page: Int = 1): Result<List<LiveTvCategory>> = executeWithFallback(
        endpoint = "all_tv_channel_by_category"
    ) { url, key ->
        api.getLiveTvByCategory(url = url, apiKey = key, page = page)
    }

    suspend fun getSingleDetails(type: String, id: String): Result<SingleDetailsResponse> = executeWithFallback(
        endpoint = "single_details"
    ) { url, key ->
        api.getSingleDetails(url = url, apiKey = key, type = type, id = id)
    }

    suspend fun search(query: String, page: Int = 1): Result<SearchResponse> = executeWithFallback(
        endpoint = "search"
    ) { url, key ->
        api.search(url = url, apiKey = key, query = query, page = page)
    }

    suspend fun getAppConfig(): Result<AppConfigResponse> = executeWithFallback(
        endpoint = "config"
    ) { url, key ->
        api.getConfig(url = url, apiKey = key)
    }

    suspend fun testConnection(): ConnectionCheckResult {
        val config = settingsPreferences.getEndpointConfig()
        val start = System.currentTimeMillis()
        var lastCode: Int? = null
        var lastError = "Unknown connection error"
        for (candidate in candidateUrls(config, "home_content_for_android")) {
            try {
                val response = api.getHomeContent(url = candidate, apiKey = config.apiKey)
                val latency = System.currentTimeMillis() - start
                lastCode = response.code()
                if (response.isSuccessful && response.body() != null) {
                    return ConnectionCheckResult(
                        success = true,
                        resolvedUrl = candidate,
                        httpCode = response.code(),
                        latencyMs = latency,
                        message = "Connected (${response.code()}) in ${latency}ms"
                    )
                }
                lastError = "HTTP ${response.code()} ${response.message()}"
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: e.javaClass.simpleName
            }
        }
        val latency = System.currentTimeMillis() - start
        return ConnectionCheckResult(
            success = false,
            resolvedUrl = "${config.baseUrl.trimEnd('/')}/v130/home_content_for_android",
            httpCode = lastCode,
            latencyMs = latency,
            message = lastError
        )
    }

    suspend fun updateEndpointConfig(rawUrl: String, overrideKey: String? = null) {
        settingsPreferences.updateEndpointConfig(rawUrl, overrideKey)
    }

    suspend fun resetEndpointConfig() {
        settingsPreferences.resetDefaults()
    }

    suspend fun toggleWatchlist(
        videosId: String,
        contentType: String,
        title: String,
        posterUrl: String?,
        releaseYear: String?,
        imdbRating: String?,
        videoQuality: String?
    ): Boolean {
        val key = buildContentKey(videosId, contentType)
        val existing = watchlistDao.getByKey(key)
        return if (existing != null && !existing.isHistoryOnly) {
            if (existing.lastWatchedTitle != null) {
                watchlistDao.upsert(existing.copy(isHistoryOnly = true))
            } else {
                watchlistDao.deleteByKey(key)
            }
            false
        } else {
            watchlistDao.upsert(
                WatchlistEntity(
                    contentKey = key,
                    videosId = videosId,
                    contentType = contentType,
                    title = title,
                    posterUrl = posterUrl,
                    releaseYear = releaseYear,
                    imdbRating = imdbRating,
                    videoQuality = videoQuality,
                    isHistoryOnly = false,
                    lastWatchedTitle = existing?.lastWatchedTitle,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            true
        }
    }

    suspend fun recordWatchHistory(
        videosId: String,
        contentType: String,
        title: String,
        posterUrl: String?,
        releaseYear: String?,
        imdbRating: String?,
        videoQuality: String?,
        watchedLabel: String
    ) {
        val key = buildContentKey(videosId, contentType)
        val existing = watchlistDao.getByKey(key)
        watchlistDao.upsert(
            WatchlistEntity(
                contentKey = key,
                videosId = videosId,
                contentType = contentType,
                title = title,
                posterUrl = posterUrl,
                releaseYear = releaseYear,
                imdbRating = imdbRating,
                videoQuality = videoQuality,
                isHistoryOnly = existing?.isHistoryOnly ?: true,
                lastWatchedTitle = watchedLabel,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeWatchlistItem(contentKey: String) {
        watchlistDao.deleteByKey(contentKey)
    }

    private suspend fun <T> executeWithFallback(
        endpoint: String,
        call: suspend (url: String, apiKey: String) -> Response<T>
    ): Result<T> {
        val config = settingsPreferences.getEndpointConfig()
        var lastException: Throwable? = null
        val urls = candidateUrls(config, endpoint)

        for (url in urls) {
            try {
                val response = call(url, config.apiKey)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        return Result.success(body)
                    }
                    lastException = IllegalStateException("Empty response from $url")
                } else if (response.code() != 404) {
                    val errText = response.errorBody()?.string()?.take(200)
                    lastException = IllegalStateException(
                        "Server returned HTTP ${response.code()}${if (!errText.isNullOrBlank()) ": $errText" else ""}"
                    )
                    break
                } else {
                    lastException = IllegalStateException("Endpoint not found (HTTP 404) at $url")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        return Result.failure(
            lastException ?: IllegalStateException("Unable to connect to TopMovies REST API")
        )
    }

    private fun candidateUrls(config: ApiEndpointConfig, endpoint: String): List<String> {
        val cleanBase = config.baseUrl.trimEnd('/')
        val cleanKey = config.apiKey.trim('/')
        return listOf(
            "$cleanBase/v130/$endpoint",
            "$cleanBase/$cleanKey/v130/$endpoint",
            "$cleanBase/$endpoint"
        ).distinct()
    }

    private fun buildContentKey(videosId: String, contentType: String): String =
        "${contentType.lowercase()}_$videosId"
}
