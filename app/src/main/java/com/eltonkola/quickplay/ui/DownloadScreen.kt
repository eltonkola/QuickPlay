package com.eltonkola.quickplay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.Lucide
import com.eltonkola.quickplay.data.DownloadState


// Download Panel Implementation
@Composable
fun DownloadScreen(viewModel: TvAppViewModel) {
    val remoteItems by viewModel.remoteItems.collectAsState()

    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
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
                    .padding(8.dp)
            ) {


                // 6-column grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(GRID_ITEMS),
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    item(key = "title", span = { GridItemSpan(GRID_ITEMS) }){
                        Text(
                            "Available Downloads",
                            fontWeight = FontWeight.Bold,
                           // style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    itemsIndexed(remoteItems) { index, item ->
                        DownloadGameCard(
                            item = item,
                            downloadState = viewModel.downloadStates.value[item.downloadUrl] ?: DownloadState(),
                            onMenu = { viewModel.selectItem(item) },
                            onClick = {
                                if(item.isDownloaded) {
                                    viewModel.selectItem(item)
                                }else{
                                    viewModel.downloadRom(item)
                                }
                            }
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
                                modifier = Modifier.padding(all = 8.dp)
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

