package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.LiveTvChannel
import com.example.ui.components.CinemaEmptyState
import com.example.ui.components.CinemaErrorState
import com.example.ui.components.CinemaLoadingState
import com.example.ui.components.LiveChannelCard
import com.example.ui.components.PosterMediaCard
import com.example.ui.components.SectionHeader
import com.example.ui.viewmodel.SearchUiState
import com.example.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onOpenDetail: (videoId: String, contentType: String) -> Unit,
    onPlayLiveChannel: (LiveTvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChanged(it) },
                placeholder = { Text("Search movies, series, live TV...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onQueryChanged("") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboard?.hide()
                        viewModel.submitSearch()
                    }
                ),
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input")
            )
        }

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                CinemaEmptyState(
                    title = "Search TopMovies",
                    subtitle = "Type at least 2 characters to search movies, TV series, and live TV channels."
                )
            }

            is SearchUiState.Loading -> {
                CinemaLoadingState(message = "Searching catalog...")
            }

            is SearchUiState.Error -> {
                CinemaErrorState(
                    message = state.message,
                    onRetry = { viewModel.submitSearch() }
                )
            }

            is SearchUiState.Success -> {
                val results = state.results
                val hasAny = results.movies.isNotEmpty() ||
                    results.tvSeries.isNotEmpty() ||
                    results.tvChannels.isNotEmpty()

                if (!hasAny) {
                    CinemaEmptyState(
                        title = "No Matches Found",
                        subtitle = "No movies, series, or live channels matched \"${state.query}\"."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        if (results.movies.isNotEmpty()) {
                            item(key = "search_movies") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SectionHeader(title = "Movies (${results.movies.size})")
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(results.movies, key = { "m_${it.videosId}" }) { item ->
                                            PosterMediaCard(
                                                item = item,
                                                onClick = { onOpenDetail(item.videosId, "movie") }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (results.tvSeries.isNotEmpty()) {
                            item(key = "search_series") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SectionHeader(title = "TV Series (${results.tvSeries.size})")
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(results.tvSeries, key = { "s_${it.videosId}" }) { item ->
                                            PosterMediaCard(
                                                item = item,
                                                onClick = { onOpenDetail(item.videosId, "tvseries") }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (results.tvChannels.isNotEmpty()) {
                            item(key = "search_livetv") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    SectionHeader(title = "Live TV (${results.tvChannels.size})")
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(results.tvChannels, key = { "tv_${it.liveTvId}" }) { ch ->
                                            LiveChannelCard(
                                                channel = ch,
                                                onClick = { onPlayLiveChannel(ch) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}
