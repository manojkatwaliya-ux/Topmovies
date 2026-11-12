package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.example.data.local.ApiEndpointConfig
import com.example.data.local.WatchlistEntity
import com.example.data.repository.ConnectionCheckResult
import com.example.ui.components.CinemaEmptyState
import com.example.ui.theme.CinemaEmerald
import com.example.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenDetail: (videoId: String, contentType: String) -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val endpointConfig by viewModel.endpointConfig.collectAsStateWithLifecycle()
    val connectionCheck by viewModel.connectionCheck.collectAsStateWithLifecycle()
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    var showEditEndpointDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Library & API",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Watchlist (${watchlist.size})") },
                modifier = Modifier.testTag("tab_watchlist")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("History (${history.size})") },
                modifier = Modifier.testTag("tab_history")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("REST API") },
                modifier = Modifier.testTag("tab_rest_api")
            )
        }

        when (selectedTab) {
            0 -> {
                if (watchlist.isEmpty()) {
                    CinemaEmptyState(
                        title = "Watchlist Empty",
                        subtitle = "Save movies or TV series from any detail page to find them here."
                    )
                } else {
                    SavedMediaList(
                        items = watchlist,
                        showDelete = true,
                        onItemClick = { onOpenDetail(it.videosId, it.contentType) },
                        onDeleteClick = { viewModel.removeWatchlistItem(it.contentKey) }
                    )
                }
            }

            1 -> {
                if (history.isEmpty()) {
                    CinemaEmptyState(
                        title = "No Watch History",
                        subtitle = "Titles you play will appear here for quick resuming."
                    )
                } else {
                    SavedMediaList(
                        items = history,
                        showDelete = false,
                        onItemClick = { onOpenDetail(it.videosId, it.contentType) },
                        onDeleteClick = {}
                    )
                }
            }

            2 -> {
                ApiConfigPanel(
                    config = endpointConfig,
                    checkResult = connectionCheck,
                    isTesting = isTesting,
                    onTestConnection = { viewModel.runConnectionTest() },
                    onEditClick = { showEditEndpointDialog = true },
                    onResetDefaults = { viewModel.resetEndpoint() }
                )
            }
        }
    }

    if (showEditEndpointDialog) {
        EditEndpointDialog(
            currentConfig = endpointConfig,
            onDismiss = { showEditEndpointDialog = false },
            onSave = { baseUrl, apiKey ->
                viewModel.updateEndpoint(baseUrl, apiKey)
                showEditEndpointDialog = false
            }
        )
    }
}

@Composable
private fun SavedMediaList(
    items: List<WatchlistEntity>,
    showDelete: Boolean,
    onItemClick: (WatchlistEntity) -> Unit,
    onDeleteClick: (WatchlistEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.contentKey }) { item ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onItemClick(item) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubcomposeAsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(64.dp)
                            .height(92.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val meta = listOfNotNull(
                            item.contentType.uppercase(),
                            item.releaseYear?.take(4)?.takeIf { it.isNotBlank() },
                            item.videoQuality?.takeIf { it.isNotBlank() }
                        ).joinToString(" • ")
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        item.lastWatchedTitle?.takeIf { it.isNotBlank() }?.let { watched ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Last: $watched",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (showDelete) {
                        IconButton(
                            onClick = { onDeleteClick(item) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Remove from Watchlist",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiConfigPanel(
    config: ApiEndpointConfig,
    checkResult: ConnectionCheckResult?,
    isTesting: Boolean,
    onTestConnection: () -> Unit,
    onEditClick: () -> Unit,
    onResetDefaults: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OXOO REST API Endpoint",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Base URL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = config.baseUrl,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "API Key",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = config.apiKey,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Configured Endpoint",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = config.combinedUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onTestConnection,
                            enabled = !isTesting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("test_api_button")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTesting) "Testing..." else "Test API")
                        }

                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_api_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Endpoint",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Default TopMovies Endpoint")
                    }
                }
            }
        }

        if (checkResult != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (checkResult.success) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = if (checkResult.success) CinemaEmerald else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (checkResult.success) "API Connection Healthy" else "Connection Check Failed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = checkResult.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = checkResult.resolvedUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditEndpointDialog(
    currentConfig: ApiEndpointConfig,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var baseUrl by remember { mutableStateOf(currentConfig.baseUrl) }
    var apiKey by remember { mutableStateOf(currentConfig.apiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure OXOO REST API") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("REST API URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(baseUrl, apiKey) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
