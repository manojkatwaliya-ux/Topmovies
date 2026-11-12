package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.LiveTvChannel
import com.example.ui.components.CinemaEmptyState
import com.example.ui.components.CinemaErrorState
import com.example.ui.components.CinemaLoadingState
import com.example.ui.components.LiveChannelCard
import com.example.ui.viewmodel.LiveTvUiState
import com.example.ui.viewmodel.LiveTvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTvScreen(
    viewModel: LiveTvViewModel,
    onPlayChannel: (LiveTvChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Live TV",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            actions = {
                IconButton(
                    onClick = { viewModel.loadChannels() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("livetv_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Live Channels"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        when (val state = uiState) {
            is LiveTvUiState.Loading -> {
                CinemaLoadingState(message = "Loading Live TV channels...")
            }

            is LiveTvUiState.Error -> {
                CinemaErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadChannels() }
                )
            }

            is LiveTvUiState.Success -> {
                val categories = state.categories
                val visibleChannels = remember(categories, selectedCategoryId) {
                    if (selectedCategoryId == null) {
                        categories.flatMap { it.channels }
                    } else {
                        categories.firstOrNull { it.categoryId == selectedCategoryId }?.channels.orEmpty()
                    }
                }

                if (categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { selectedCategoryId = null },
                                label = { Text("All Channels") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                        items(categories, key = { it.categoryId }) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.categoryId,
                                onClick = { selectedCategoryId = cat.categoryId },
                                label = { Text(cat.categoryName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                }

                if (visibleChannels.isEmpty()) {
                    CinemaEmptyState(
                        title = "No Live Channels",
                        subtitle = "No live TV channels are published in this category right now."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 172.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(visibleChannels, key = { it.liveTvId }) { channel ->
                            LiveChannelCard(
                                channel = channel,
                                cardWidth = null,
                                onClick = { onPlayChannel(channel) }
                            )
                        }
                    }
                }
            }
        }
    }
}
