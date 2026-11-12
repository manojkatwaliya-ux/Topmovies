package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.remote.EpisodeItem
import com.example.data.remote.SingleDetailsResponse
import com.example.data.remote.isTruthy
import com.example.ui.components.CinemaErrorState
import com.example.ui.components.CinemaLoadingState
import com.example.ui.components.PosterMediaCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.viewmodel.DetailUiState
import com.example.ui.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    contentType: String,
    onBack: () -> Unit,
    onPlayStream: (title: String, streamUrl: String, subtitle: String, contentId: String, type: String) -> Unit,
    onOpenRelated: (videoId: String, contentType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is DetailUiState.Loading -> {
                CinemaLoadingState(message = "Loading details...")
            }

            is DetailUiState.Error -> {
                CinemaErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadDetails() }
                )
            }

            is DetailUiState.Success -> {
                DetailContent(
                    details = state.details,
                    contentType = contentType,
                    isBookmarked = isBookmarked,
                    onToggleWatchlist = { viewModel.toggleWatchlist(state.details) },
                    onPlayStream = { streamUrl, subtitle ->
                        viewModel.recordPlay(state.details, subtitle.ifBlank { state.details.title })
                        onPlayStream(
                            state.details.title,
                            streamUrl,
                            subtitle,
                            state.details.videosId,
                            contentType
                        )
                    },
                    onOpenRelated = onOpenRelated
                )
            }
        }

        // Top floating Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(CinemaObsidian.copy(alpha = 0.65f))
                .testTag("detail_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    details: SingleDetailsResponse,
    contentType: String,
    isBookmarked: Boolean,
    onToggleWatchlist: () -> Unit,
    onPlayStream: (streamUrl: String, subtitle: String) -> Unit,
    onOpenRelated: (videoId: String, contentType: String) -> Unit
) {
    var selectedStreamIdx by remember(details.videosId) { mutableIntStateOf(0) }
    var selectedSeasonIdx by remember(details.videosId) { mutableIntStateOf(0) }

    val availableStreams = details.videos.filter { it.fileUrl.isNotBlank() }
    val activeStream = availableStreams.getOrNull(selectedStreamIdx)
    val seasons = details.seasons
    val activeSeason = seasons.getOrNull(selectedSeasonIdx)
    val firstPlayableEpisode = seasons.firstOrNull()?.episodes?.firstOrNull { it.fileUrl.isNotBlank() }

    val primaryPlayUrl = activeStream?.fileUrl
        ?: firstPlayableEpisode?.fileUrl
        ?: details.trailerUrl.orEmpty()

    val primarySubtitle = when {
        activeStream != null -> activeStream.label
        firstPlayableEpisode != null -> firstPlayableEpisode.episodesName
        !details.trailerUrl.isNullOrBlank() -> "Trailer"
        else -> ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Backdrop & Title Header
        item(key = "header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                SubcomposeAsyncImage(
                    model = details.bestBackdropUrl,
                    contentDescription = details.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Image(
                            painter = painterResource(id = R.drawable.img_cinema_hero),
                            contentDescription = details.title,
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
                                    CinemaObsidian.copy(alpha = 0.25f),
                                    CinemaObsidian.copy(alpha = 0.55f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val rating = details.imdbRating?.takeIf { it.isNotBlank() && it != "0" }
                        if (rating != null) {
                            Surface(
                                color = CinemaGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = "IMDb Rating",
                                        tint = CinemaGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = rating,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CinemaGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        details.videoQuality?.takeIf { it.isNotBlank() }?.let { quality ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = quality.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        details.release?.take(4)?.takeIf { it.isNotBlank() }?.let { year ->
                            Text(
                                text = year,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        details.runtime?.takeIf { it.isNotBlank() }?.let { runtime ->
                            Text(
                                text = "• $runtime",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (details.isPaid.isTruthy()) {
                            Surface(
                                color = CinemaGold,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "VIP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaObsidian,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = details.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Action Buttons
        item(key = "actions") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (primaryPlayUrl.isNotBlank()) {
                            onPlayStream(primaryPlayUrl, primarySubtitle)
                        }
                    },
                    enabled = primaryPlayUrl.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (primaryPlayUrl.isNotBlank()) "Watch Now" else "No Stream Linked",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier
                        .height(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("detail_watchlist_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove from Watchlist" else "Add to Watchlist",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBookmarked) "Saved" else "Watchlist",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Stream Sources Selector (if multiple servers/qualities exist)
        if (availableStreams.size > 1) {
            item(key = "servers") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Streaming Servers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableStreams.size) { index ->
                            val source = availableStreams[index]
                            FilterChip(
                                selected = selectedStreamIdx == index,
                                onClick = { selectedStreamIdx = index },
                                label = { Text(source.label.ifBlank { "Server ${index + 1}" }) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }
            }
        }

        // Genres & Synopsis
        item(key = "synopsis") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (details.genre.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        details.genre.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag.name) }
                            )
                        }
                    }
                }

                details.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (details.director.isNotEmpty()) {
                    Text(
                        text = "Director: ${details.director.joinToString { it.name }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (details.cast.isNotEmpty()) {
                    Text(
                        text = "Cast: ${details.cast.take(8).joinToString { it.name }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // TV Series Seasons & Episodes
        if (seasons.isNotEmpty()) {
            item(key = "seasons_tabs") {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    SectionHeader(title = "Seasons & Episodes")
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(seasons.size) { idx ->
                            FilterChip(
                                selected = selectedSeasonIdx == idx,
                                onClick = { selectedSeasonIdx = idx },
                                label = { Text(seasons[idx].seasonsName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }
            }

            val episodes = activeSeason?.episodes.orEmpty()
            items(episodes, key = { "ep_${it.episodesId}_${it.episodesName}" }) { ep ->
                EpisodeRowItem(
                    episode = ep,
                    onClick = {
                        if (ep.fileUrl.isNotBlank()) {
                            onPlayStream(ep.fileUrl, "${activeSeason?.seasonsName.orEmpty()} • ${ep.episodesName}")
                        }
                    }
                )
            }
        }

        // Related Titles
        val related = if (contentType == "tvseries") {
            details.relatedTvSeries.ifEmpty { details.relatedMovies }
        } else {
            details.relatedMovies.ifEmpty { details.relatedTvSeries }
        }
        if (related.isNotEmpty()) {
            item(key = "related") {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionHeader(title = "More Like This")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(related, key = { "rel_${it.contentType}_${it.videosId}" }) { item ->
                            PosterMediaCard(
                                item = item,
                                onClick = { onOpenRelated(item.videosId, item.contentType) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeRowItem(
    episode: EpisodeItem,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = episode.fileUrl.isNotBlank(), onClick = onClick)
            .testTag("episode_item_${episode.episodesId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = episode.imageUrl,
                    contentDescription = episode.episodesName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.episodesName.ifBlank { "Episode" },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                episode.fileType?.takeIf { it.isNotBlank() }?.let { type ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play ${episode.episodesName}",
                tint = if (episode.fileUrl.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
