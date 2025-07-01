package com.eltonkola.quickplay.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.Cloud
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.eltonkola.quickplay.data.DownloadState
import com.eltonkola.quickplay.data.RemoteItem


// Download Panel Implementation
@Composable
fun DownloadPanel(viewModel: TvAppViewModel) {
    val remoteItems by viewModel.remoteItems.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (remoteItems.isEmpty() && !viewModel.isLoading) {
            EmptyState(
                icon = Lucide.CloudOff,
                title = "No Content Available",
                description = "Unable to fetch download items. Check your connection and try again",
                actionText = "Retry",
                onAction = { viewModel.loadNextPage() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {


                // 6-column grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_ITEMS),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    item(key = "title", span = { GridItemSpan(GRID_ITEMS) }){
                        Text(
                            "Available Downloads",
                           // style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    itemsIndexed(remoteItems) { index, item ->
                        LandscapeDownloadCard(
                            item = item,
                            downloadState = viewModel.downloadStates.value[item.downloadUrl] ?: DownloadState(),
                            onClick = { viewModel.selectItem(item) }
                        )


                        if (index >= remoteItems.size - 4) {
                            LaunchedEffect(Unit) {
                                viewModel.loadNextPage()
                            }
                        }


                    }

                    if (viewModel.isLoading) {
                        item(span = { GridItemSpan(GRID_ITEMS) }) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(all = 16.dp)
                            ){
                                CircularProgressIndicator()
                            }
                        }
                    }


                }
            }
        }
    }
}

// Landscape Download Card Implementation
@Composable
fun LandscapeDownloadCard(
    item: RemoteItem,
    downloadState: DownloadState,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderWidth = if (isFocused) 4.dp else 1.dp
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column {
            // Landscape image (16:9 aspect ratio)
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

            // Item title and status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                when {
                    downloadState.isDownloading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Lucide.Download,
                                contentDescription = "Downloading",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadState.progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${(downloadState.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    item.isDownloaded -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Lucide.CheckCheck,
                            contentDescription = "Downloaded",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Downloaded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Lucide.Cloud,
                            contentDescription = "Available",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Available",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
