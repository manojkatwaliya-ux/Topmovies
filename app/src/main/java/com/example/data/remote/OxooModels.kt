package com.example.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Resilient Moshi adapter that converts numbers, booleans, or strings into a String
 * without crashing on CodeIgniter/PHP mixed JSON types.
 */
class LenientStringAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }
            JsonReader.Token.BOOLEAN -> reader.nextBoolean().toString()
            JsonReader.Token.NUMBER -> reader.nextString()
            JsonReader.Token.STRING -> reader.nextString()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: String?) {
        writer.value(value)
    }
}

fun String?.isTruthy(): Boolean {
    val clean = this?.trim()?.lowercase() ?: return false
    return clean == "1" || clean == "true" || clean == "yes"
}

data class HomeContentResponse(
    @Json(name = "slider") val slider: SliderSection? = null,
    @Json(name = "all_genre") val allGenre: List<GenreItem> = emptyList(),
    @Json(name = "all_country") val allCountry: List<CountryItem> = emptyList(),
    @Json(name = "featured_tv_channel") val featuredTvChannel: List<LiveTvChannel> = emptyList(),
    @Json(name = "latest_movies") val latestMovies: List<VideoSummary> = emptyList(),
    @Json(name = "latest_tvseries") val latestTvSeries: List<VideoSummary> = emptyList(),
    @Json(name = "features_genre_and_movie") val featuredGenreRows: List<GenreWithVideos> = emptyList()
)

data class SliderSection(
    @Json(name = "slider_type") val sliderType: String? = null,
    @Json(name = "slide") val slides: List<SlideItem> = emptyList()
)

data class SlideItem(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String = "",
    @Json(name = "image_link") val imageLink: String? = null,
    @Json(name = "action_type") val actionType: String? = null,
    @Json(name = "action_id") val actionId: String? = null,
    @Json(name = "action_url") val actionUrl: String? = null,
    @Json(name = "action_btn_text") val actionBtnText: String? = null
)

data class GenreItem(
    @Json(name = "genre_id") val genreId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)

data class CountryItem(
    @Json(name = "country_id") val countryId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "url") val url: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)

data class GenreWithVideos(
    @Json(name = "genre_id") val genreId: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "videos") val videos: List<VideoSummary> = emptyList()
)

data class VideoSummary(
    @Json(name = "videos_id") val videosId: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "release") val release: String? = null,
    @Json(name = "runtime") val runtime: String? = null,
    @Json(name = "video_quality") val videoQuality: String? = null,
    @Json(name = "imdb_rating") val imdbRating: String? = null,
    @Json(name = "is_tvseries") val isTvSeries: String? = null,
    @Json(name = "is_paid") val isPaid: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "poster_url") val posterUrl: String? = null
) {
    val contentType: String get() = if (isTvSeries.isTruthy()) "tvseries" else "movie"
    val bestImageUrl: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbnailUrl?.takeIf { it.isNotBlank() }
    val bestPosterUrl: String? get() = thumbnailUrl?.takeIf { it.isNotBlank() } ?: posterUrl?.takeIf { it.isNotBlank() }
}

data class SingleDetailsResponse(
    @Json(name = "videos_id") val videosId: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "release") val release: String? = null,
    @Json(name = "runtime") val runtime: String? = null,
    @Json(name = "video_quality") val videoQuality: String? = null,
    @Json(name = "imdb_rating") val imdbRating: String? = null,
    @Json(name = "is_tvseries") val isTvSeries: String? = null,
    @Json(name = "is_paid") val isPaid: String? = null,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "poster_url") val posterUrl: String? = null,
    @Json(name = "trailer_url") val trailerUrl: String? = null,
    @Json(name = "genre") val genre: List<NamedTag> = emptyList(),
    @Json(name = "country") val country: List<NamedTag> = emptyList(),
    @Json(name = "director") val director: List<NamedTag> = emptyList(),
    @Json(name = "writer") val writer: List<NamedTag> = emptyList(),
    @Json(name = "cast") val cast: List<CastMember> = emptyList(),
    @Json(name = "videos") val videos: List<VideoStreamFile> = emptyList(),
    @Json(name = "season") val seasons: List<SeasonItem> = emptyList(),
    @Json(name = "related_movie") val relatedMovies: List<VideoSummary> = emptyList(),
    @Json(name = "related_tvseries") val relatedTvSeries: List<VideoSummary> = emptyList(),
    @Json(name = "current_tv_channel") val currentTvChannel: LiveTvChannel? = null,
    @Json(name = "all_tv_channel") val allTvChannels: List<LiveTvChannel> = emptyList()
) {
    val bestBackdropUrl: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbnailUrl?.takeIf { it.isNotBlank() }
    val bestPosterUrl: String? get() = thumbnailUrl?.takeIf { it.isNotBlank() } ?: posterUrl?.takeIf { it.isNotBlank() }
}

data class NamedTag(
    @Json(name = "genre_id") val genreId: String? = null,
    @Json(name = "country_id") val countryId: String? = null,
    @Json(name = "star_id") val starId: String? = null,
    @Json(name = "name") val name: String = ""
)

data class CastMember(
    @Json(name = "star_id") val starId: String? = null,
    @Json(name = "name") val name: String = "",
    @Json(name = "image_url") val imageUrl: String? = null
)

data class VideoStreamFile(
    @Json(name = "video_file_id") val videoFileId: String = "",
    @Json(name = "label") val label: String = "HD Stream",
    @Json(name = "stream_key") val streamKey: String? = null,
    @Json(name = "file_type") val fileType: String? = null,
    @Json(name = "file_url") val fileUrl: String = "",
    @Json(name = "source_type") val sourceType: String? = null
)

data class SeasonItem(
    @Json(name = "seasons_id") val seasonsId: String = "",
    @Json(name = "seasons_name") val seasonsName: String = "Season 1",
    @Json(name = "episodes") val episodes: List<EpisodeItem> = emptyList()
)

data class EpisodeItem(
    @Json(name = "episodes_id") val episodesId: String = "",
    @Json(name = "episodes_name") val episodesName: String = "",
    @Json(name = "stream_key") val streamKey: String? = null,
    @Json(name = "file_type") val fileType: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "file_url") val fileUrl: String = "",
    @Json(name = "source_type") val sourceType: String? = null
)

data class LiveTvCategory(
    @Json(name = "live_tv_category_id") val categoryId: String = "",
    @Json(name = "live_tv_category") val categoryName: String = "",
    @Json(name = "channels") val channels: List<LiveTvChannel> = emptyList()
)

data class LiveTvChannel(
    @Json(name = "live_tv_id") val liveTvId: String = "",
    @Json(name = "tv_name") val tvName: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "is_paid") val isPaid: String? = null,
    @Json(name = "stream_from") val streamFrom: String? = null,
    @Json(name = "stream_label") val streamLabel: String? = null,
    @Json(name = "stream_url") val streamUrl: String = "",
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "poster_url") val posterUrl: String? = null
) {
    val bestImageUrl: String? get() = posterUrl?.takeIf { it.isNotBlank() } ?: thumbnailUrl?.takeIf { it.isNotBlank() }
}

data class SearchResponse(
    @Json(name = "movie") val movies: List<VideoSummary> = emptyList(),
    @Json(name = "tvseries") val tvSeries: List<VideoSummary> = emptyList(),
    @Json(name = "tv_channels") val tvChannels: List<LiveTvChannel> = emptyList()
)

data class AppConfigResponse(
    @Json(name = "app_config") val appConfig: AppInfoConfig? = null,
    @Json(name = "apk_version_info") val apkVersionInfo: ApkVersionInfo? = null
)

data class AppInfoConfig(
    @Json(name = "app_name") val appName: String? = null,
    @Json(name = "mandatory_login") val mandatoryLogin: String? = null,
    @Json(name = "genre_visible") val genreVisible: String? = null,
    @Json(name = "country_visible") val countryVisible: String? = null
)

data class ApkVersionInfo(
    @Json(name = "version_code") val versionCode: String? = null,
    @Json(name = "version_name") val versionName: String? = null,
    @Json(name = "whats_new") val whatsNew: String? = null
)
