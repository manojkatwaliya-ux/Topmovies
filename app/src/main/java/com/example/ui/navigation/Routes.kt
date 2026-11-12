package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object MoviesRoute

@Serializable
data object SeriesRoute

@Serializable
data object LiveTvRoute

@Serializable
data object LibraryRoute

@Serializable
data object SearchRoute

@Serializable
data class DetailRoute(
    val videoId: String,
    val contentType: String = "movie"
)

@Serializable
data class GenreBrowseRoute(
    val filterId: String,
    val filterName: String,
    val isCountry: Boolean = false
)

@Serializable
data class PlayerRoute(
    val title: String,
    val streamUrl: String,
    val subtitle: String = "",
    val contentId: String = "",
    val contentType: String = "movie"
)
