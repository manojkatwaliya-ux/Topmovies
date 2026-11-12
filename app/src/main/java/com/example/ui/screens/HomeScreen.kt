package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.remote.HomeContentResponse
import com.example.data.remote.LiveTvChannel
import com.example.data.remote.SlideItem
import com.example.data.remote.VideoSummary
import com.example.ui.components.CinemaEmptyState
import com.example.ui.components.CinemaErrorState
import com.example.ui.components.CinemaLoadingState
import com.example.ui.components.LiveChannelCard
import com.example.ui.components.PosterMediaCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CinemaObsidian
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSearch: () -> Unit,
    onOpenMovies: () -> Unit,
    onOpenSeries: () -> Unit,
    onOpenLiveTv: () -> Unit,
    onOpenGenre: (genreId: String, genreName: String) -> Unit,
    onOpenCountry: (countryId: String, countryName: String) -> Unit,
    onOpenDetail: (videoId: String, contentType: String) -> Unit,
    onPlayLiveChannel: (LiveTvChannel) -> Unit,
    onOpenApiSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeTopHeader(
            onSearchClick = onOpenSearch,
            onRefreshClick = { viewModel.loadHome(initial = false) }
        )

        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CinemaLoadingState(message = "Connecting to TopMovies catalog...")
            }

            is HomeUiState.Error -> {
                CinemaErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadHome(initial = true) },
                    onOpenApiSettings = onOpenApiSettings
                )
            }

            is HomeUiState.Success -> {
                HomeFeedContent(
                    data = state.data,
                    onOpenMovies = onOpenMovies,
                    onOpenSeries = onOpenSeries,
                    onOpenLiveTv = onOpenLiveTv,
                    onOpenGenre = onOpenGenre,
                    onOpenCountry = onOpenCountry,
                    onOpenDetail = onOpenDetail,
                    onPlayLiveChannel = onPlayLiveChannel
                )
            }
        }
    }
}

@Composable
private fun HomeTopHeader(
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "TopMovies",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "TopMovies",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("home_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh Catalog",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("home_search_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Movies and Series",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun HomeFeedContent(
    data: HomeContentResponse,
    onOpenMovies: () -> Unit,
    onOpenSeries: () -> Unit,
    onOpenLiveTv: () -> Unit,
    onOpenGenre: (String, String) -> Unit,
    onOpenCountry: (String, String) -> Unit,
    onOpenDetail: (String, String) -> Unit,
    onPlayLiveChannel: (LiveTvChannel) -> Unit
) {
    val heroSlides = data.slider?.slides.orEmpty()
    val fallbackSlides = if (heroSlides.isEmpty()) {
        data.latestMovies.take(5).map { movie ->
            SlideItem(
                id = movie.videosId,
                title = movie.title,
                description = movie.description.orEmpty(),
                imageLink = movie.bestImageUrl,
                actionType = movie.contentType,
                actionId = movie.videosId
            )
        }
    } else {
        heroSlides
    }

    val isEverythingEmpty = fallbackSlides.isEmpty() &&
        data.latestMovies.isEmpty() &&
        data.latestTvSeries.isEmpty() &&
        data.featuredTvChannel.isEmpty() &&
        data.featuredGenreRows.isEmpty()

    if (isEverythingEmpty) {
        CinemaEmptyState(
            title = "Catalog Is Empty",
            subtitle = "Connected to TopMovies REST API, but no published movies or channels were returned yet."
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (fallbackSlides.isNotEmpty()) {
            item(key = "hero_carousel") {
                HeroCarousel(
                    slides = fallbackSlides,
                    onSlideClick = { slide ->
                        val targetId = slide.actionId?.takeIf { it.isNotBlank() } ?: slide.id
                        val targetType = when (slide.actionType?.lowercase()) {
                            "tvseries", "tv_series", "series" -> "tvseries"
                            else -> "movie"
                        }
                        if (targetId.isNotBlank()) {
                            onOpenDetail(targetId, targetType)
                        }
                    }
                )
            }
        }

        if (data.allGenre.isNotEmpty()) {
            item(key = "genres_strip") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = "Genres")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(data.allGenre, key = { it.genreId }) { genre ->
                            FilterChip(
                                selected = false,
                                onClick = { onOpenGenre(genre.genreId, genre.name) },
                                label = {
                                    Text(
                                        text = genre.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("genre_chip_${genre.genreId}")
                            )
                        }
                    }
                }
            }
        }

        if (data.featuredTvChannel.isNotEmpty()) {
            item(key = "live_tv_row") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Live TV Channels",
                        actionLabel = "All",
                        onActionClick = onOpenLiveTv
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(data.featuredTvChannel, key = { it.liveTvId }) { channel ->
                            LiveChannelCard(
                                channel = channel,
                                onClick = { onPlayLiveChannel(channel) }
                            )
                        }
                    }
                }
            }
        }

        if (data.latestMovies.isNotEmpty()) {
            item(key = "latest_movies_row") {
                VideoHorizontalSection(
                    title = "Latest Movies",
                    items = data.latestMovies,
                    actionLabel = "See All",
                    onActionClick = onOpenMovies,
                    onItemClick = { video ->
                        onOpenDetail(video.videosId, video.contentType)
                    }
                )
            }
        }

        if (data.latestTvSeries.isNotEmpty()) {
            item(key = "latest_series_row") {
                VideoHorizontalSection(
                    title = "Latest TV Series",
                    items = data.latestTvSeries,
                    actionLabel = "See All",
                    onActionClick = onOpenSeries,
                    onItemClick = { video ->
                        onOpenDetail(video.videosId, "tvseries")
                    }
                )
            }
        }

        items(data.featuredGenreRows, key = { "genre_row_${it.genreId}" }) { group ->
            if (group.videos.isNotEmpty()) {
                VideoHorizontalSection(
                    title = group.name,
                    items = group.videos,
                    actionLabel = "Explore",
                    onActionClick = { onOpenGenre(group.genreId, group.name) },
                    onItemClick = { video ->
                        onOpenDetail(video.videosId, video.contentType)
                    }
                )
            }
        }

        if (data.allCountry.isNotEmpty()) {
            item(key = "countries_strip") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = "Browse by Region")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(data.allCountry, key = { it.countryId }) { country ->
                            FilterChip(
                                selected = false,
                                onClick = { onOpenCountry(country.countryId, country.name) },
                                label = {
                                    Text(
                                        text = country.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCarousel(
    slides: List<SlideItem>,
    onSlideClick: (SlideItem) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(232.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onSlideClick(slide) }
                    .testTag("hero_slide_$page")
            ) {
                SubcomposeAsyncImage(
                    model = slide.imageLink,
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Image(
                            painter = painterResource(id = R.drawable.img_cinema_hero),
                            contentDescription = slide.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CinemaObsidian.copy(alpha = 0.4f),
                                    CinemaObsidian.copy(alpha = 0.94f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FEATURED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (slide.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onSlideClick(slide) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Watch Now",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = slide.actionBtnText?.takeIf { it.isNotBlank() } ?: "Watch Now",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        if (slides.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (selected) 20.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoHorizontalSection(
    title: String,
    items: List<VideoSummary>,
    actionLabel: String,
    onActionClick: () -> Unit,
    onItemClick: (VideoSummary) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = title,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { "${it.contentType}_${it.videosId}" }) { video ->
                PosterMediaCard(
                    item = video,
                    onClick = { onItemClick(video) }
                )
            }
        }
    }
}
