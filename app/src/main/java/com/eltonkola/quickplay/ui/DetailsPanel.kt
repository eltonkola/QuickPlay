package com.eltonkola.quickplay.ui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.HeartOff
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.eltonkola.quickplay.data.DownloadState
import com.eltonkola.quickplay.data.RemoteItem
import com.eltonkola.quickplay.data.local.GameEntity


// Details Panel Implementation
@Composable
fun DetailsPanel(
    item: Any,
    viewModel: TvAppViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TV elevation and surface
    val elevation = 8.dp
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh

    BackHandler(true) {
        Log.d("TAG", "OnBackPressed")
        onClose()
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
        color = surfaceColor,
        shadowElevation = elevation,
        shape = MaterialTheme.shapes.extraLarge.copy(
            topEnd = MaterialTheme.shapes.small.topStart,
            bottomEnd = MaterialTheme.shapes.small.bottomStart
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Details",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )

            // Content with smaller image
            when (item) {
                is GameEntity -> GameDetails(game = item, viewModel = viewModel)
                is RemoteItem -> DownloadDetails(item = item, viewModel = viewModel)
            }
        }
    }
}

// Game Details Implementation
@Composable
fun GameDetails(
    game: GameEntity,
    viewModel: TvAppViewModel,
) {

    val playFocusRequester = remember { FocusRequester() }

    // Defer focus request until layout is done
    LaunchedEffect(Unit) {
        playFocusRequester.requestFocus()
    }


    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                game.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Name: ${game.filename}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Action Buttons as vertical list
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Play action
            ActionListItem(
                icon = Lucide.Play,
                title = "Play Game",
                description = "Launch this game now",
                onClick = { viewModel.launchGame(game.filename?: game.name) },
                focusRequester = playFocusRequester
            )

            // Favorite action
            ActionListItem(
                icon = if (game.isFavorite) Lucide.Heart else Lucide.HeartOff,
                title = if (game.isFavorite) "Remove from Favorites" else "Add to Favorites",
                description = if (game.isFavorite) "Remove from favorites" else "Add to favorites",
                onClick = {
                    viewModel.toggleFavorite(game)
                },
            )

            // Delete action
            ActionListItem(
                icon = Lucide.Trash2,
                title = "Delete Game",
                description = "Remove from device",
                onClick = {
                    viewModel.deleteGame(game)
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
            )
        }
    }
}

// Download Details Implementation
@Composable
fun DownloadDetails(
    item: RemoteItem,
    viewModel: TvAppViewModel
) {
    val downloadState = viewModel.downloadStates.value[item.downloadUrl] ?: DownloadState()
    val playFocusRequester = remember { FocusRequester() }

    // Defer focus request until layout is done
    LaunchedEffect(Unit) {
        playFocusRequester.requestFocus()
    }



    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Name: ${item.filename}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Status information
        when {
            downloadState.isDownloading -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Download in progress",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${(downloadState.progress * 100).toInt()}% completed",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            item.isDownloaded -> {
                Text(
                    "✓ Downloaded",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                Text(
                    "Available",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons as vertical list
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!item.isDownloaded && !downloadState.isDownloading) {
                ActionListItem(
                    icon = Lucide.Download,
                    title = "Download Now",
                    description = "Download to your device",
                    onClick = { viewModel.downloadRom(item) },
                    focusRequester = playFocusRequester,
                )
            }

            if (item.isDownloaded) {
                ActionListItem(
                    icon = Lucide.Play,
                    title = "Launch Game",
                    description = "Play this game now",
                    onClick = {
                        viewModel.launchGame(item.filename)
                    }
                )
            }

            if (item.isDownloaded || downloadState.isDownloading) {
                ActionListItem(
                    icon = Lucide.Trash2,
                    title = if (downloadState.isDownloading) "Cancel Download" else "Delete Download",
                    description = "Remove this item from your device",
                    onClick = {
                        viewModel.deleteDownload(item)
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
            }
        }
    }
}

// Action List Item Implementation
@Composable
fun ActionListItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor = if (isFocused) {
        containerColor.copy(alpha = 0.9f)
    } else {
        containerColor
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .focusRequester(focusRequester)
        ,
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = "Action",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }


    }
}
