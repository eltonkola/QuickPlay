package com.eltonkola.quickplay.ui

import android.R.attr.label
import android.R.attr.onClick
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import com.composables.icons.lucide.CheckCheck
import com.composables.icons.lucide.CircleOff
import com.composables.icons.lucide.CloudDownload
import com.composables.icons.lucide.Gamepad
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.QrCode
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Tv
import com.eltonkola.quickplay.data.DownloadState
import com.eltonkola.quickplay.data.RemoteItem
import com.eltonkola.quickplay.data.local.GameDao
import com.eltonkola.quickplay.data.local.GameEntity
import com.eltonkola.quickplay.data.remote.RomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.composables.icons.lucide.*


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvApp(
    viewModel: TvAppViewModel = viewModel()
) {
        val selectedTab by viewModel.selectedTab.collectAsState()
        val selectedItem by viewModel.selectedItem.collectAsState()

        Row(modifier = Modifier.fillMaxSize()) {
            // Persistent Navigation Drawer (Left Panel)
            NavigationDrawer(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                modifier = Modifier.fillMaxHeight()
            )

            // Center Content Panel
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> GamesPanel(viewModel)
                    1 -> DownloadPanel(viewModel)
                    2 -> ServerPanel(viewModel)
                }
            }
        }

        // Details Dialog - Shows when an item is selected
        if (selectedItem != null) {
            DetailsDialog(
                item = selectedItem!!,
                viewModel = viewModel,
                onDismiss = { viewModel.closeDetails() }
            )
        }
    }

    // Navigation Drawer with Lucide icons
    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
    @Composable
    fun NavigationDrawer(
        selectedTab: Int,
        onTabSelected: (Int) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val tabTitles = listOf("Games Library", "Download Center", "Web Server")
        val icons = listOf(
            Lucide.Gamepad2,
            Lucide.Download,
            Lucide.Server
        )

        Surface(
            modifier = modifier.width(280.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with Lucide icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.Star,
                        contentDescription = "App Icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Retro Console",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Navigation Items with Lucide icons
                tabTitles.forEachIndexed { index, title ->
                    NavigationDrawerItem(
                        label = { Text(title) },
                        icon = { Icon(
                            icons[index],
                            contentDescription = null,
                        )} ,
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) }
                    )
                }


                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { /* Open settings */ }
                            .padding(vertical = 12.dp)
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        "v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }

    // Details Dialog with Lucide icons
    @OptIn(ExperimentalTvMaterial3Api::class)
    @Composable
    fun DetailsDialog(
        item: Any,
        viewModel: TvAppViewModel,
        onDismiss: () -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .width(700.dp)
                    .heightIn(min = 400.dp, max = 800.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header with Lucide close icon
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
                            onClick = onDismiss,
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

                    // Content
                    when (item) {
                        is GameEntity -> GameDetails(game = item, viewModel = viewModel, onDismiss = onDismiss)
                        is RemoteItem -> DownloadDetails(item = item, viewModel = viewModel, onDismiss = onDismiss)
                    }
                }
            }
        }
    }

    // Game Details with Lucide icons
    @Composable
    fun GameDetails(
        game: GameEntity,
        viewModel: TvAppViewModel,
        onDismiss: () -> Unit
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Landscape image
            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    game.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ID: ${game.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Action Buttons as vertical list with Lucide icons
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Play action
                ActionListItem(
                    icon = Lucide.Play,
                    title = "Play Game",
                    description = "Launch this game now",
                    onClick = {
                        viewModel.launchGame(game)
                        onDismiss()
                    }
                )

                // Favorite action
                ActionListItem(
                    icon = if (game.isFavorite) Lucide.Heart else Lucide.HeartOff,
                    title = if (game.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    description = if (game.isFavorite) "Remove from your favorites list" else "Add to your favorites list",
                    onClick = {
                        viewModel.toggleFavorite(game)
                    }
                )

                // Delete action
                ActionListItem(
                    icon = Lucide.Trash2,
                    title = "Delete Game",
                    description = "Remove this game from your device",
                    onClick = {
                        viewModel.deleteGame(game)
                        onDismiss()
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            }
        }
    }

    // Download Details with Lucide icons
    @Composable
    fun DownloadDetails(
        item: RemoteItem,
        viewModel: TvAppViewModel,
        onDismiss: () -> Unit
    ) {
        val downloadState = viewModel.downloadStates.value[item.downloadUrl] ?: DownloadState()

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Landscape image
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Id: ${item.mediaId ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

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

            // Action Buttons as vertical list with Lucide icons
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!item.isDownloaded && !downloadState.isDownloading) {
                    ActionListItem(
                        icon = Lucide.Download,
                        title = "Download Now",
                        description = "Download to your device",
                        onClick = { viewModel.downloadRom(item) }
                    )
                }

                if (item.isDownloaded) {
                    ActionListItem(
                        icon = Lucide.Play,
                        title = "Launch Game",
                        description = "Play this game now",
                        onClick = {
                            viewModel.launchGame(item)
                            onDismiss()
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
                            onDismiss()
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
        }
    }

    // Action List Item with Lucide icons
    @OptIn(ExperimentalTvMaterial3Api::class)
    @Composable
    fun ActionListItem(
        icon: ImageVector,
        title: String,
        description: String,
        onClick: () -> Unit,
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = containerColor
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

    // Games Panel with Lucide icons
    @Composable
    fun GamesPanel(viewModel: TvAppViewModel) {
        val favorites by viewModel.favoriteGames.collectAsState()
        val nonFavorites by viewModel.nonFavoriteGames.collectAsState()
        val hasGames = favorites.isNotEmpty() || nonFavorites.isNotEmpty()

        Box(modifier = Modifier.fillMaxSize()) {
            if (!hasGames) {
                EmptyState(
                    icon = Lucide.Gamepad2,
                    title = "Your Game Library is Empty",
                    description = "Visit the Download Center to add games to your collection",
                    actionText = "Go to Download Center",
                    onAction = { viewModel.selectTab(1) }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (favorites.isNotEmpty()) {
                        Text(
                            "Favorites",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 6-column grid for favorites
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(favorites) { game ->
                                LandscapeGameCard(
                                    game = game,
                                    onClick = { viewModel.selectItem(game) }
                                )
                            }
                        }
                    }

                    if (nonFavorites.isNotEmpty()) {
                        Text(
                            "All Games",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .padding(top = 32.dp, bottom = 16.dp)
                        )

                        // 6-column grid for all games
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(nonFavorites) { game ->
                                LandscapeGameCard(
                                    game = game,
                                    onClick = { viewModel.selectItem(game) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Landscape Game Card with Lucide icons
    @OptIn(ExperimentalTvMaterial3Api::class)
    @Composable
    fun LandscapeGameCard(
        game: GameEntity,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .width(220.dp)
                .clickable { onClick() },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
//            border = CardDefaults.border(
//                focusedBorder = BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
//                unfocusedBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
//            )
        ) {
            Column {
                // Landscape image (16:9 aspect ratio)
                AsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )

                // Game title and favorite indicator with Lucide icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = game.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (game.isFavorite) {
                        Icon(
                            imageVector = Lucide.Star,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Download Panel with Lucide icons
    @Composable
    fun DownloadPanel(viewModel: TvAppViewModel) {
        val remoteItems by viewModel.remoteItems.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            if (remoteItems.isEmpty()) {
                EmptyState(
                    icon = Lucide.CloudOff,
                    title = "No Content Available",
                    description = "Unable to fetch download items. Check your connection and try again",
                    actionText = "Retry",
                    onAction = { viewModel.refreshRemoteItems() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "Available Downloads",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // 6-column grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(remoteItems) { item ->
                            LandscapeDownloadCard(
                                item = item,
                                downloadState = viewModel.downloadStates.value[item.downloadUrl] ?: DownloadState(),
                                onClick = { viewModel.selectItem(item) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Landscape Download Card with Lucide icons
    @OptIn(ExperimentalTvMaterial3Api::class)
    @Composable
    fun LandscapeDownloadCard(
        item: RemoteItem,
        downloadState: DownloadState,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .width(220.dp)
                .clickable { onClick() },
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
//            border = CardDefaults.border(
//                focusedBorder = BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
//                unfocusedBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
//            )
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

                // Item title and status with Lucide-inspired indicators
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

    // Server Panel with Lucide icons
    @Composable
    fun ServerPanel(viewModel: TvAppViewModel) {
        val isServerOn by viewModel.isServerOn.collectAsState()
        val serverUrl by viewModel.serverUrl.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Server Status Card with Lucide icon
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(24.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Lucide.Server,
                            contentDescription = "Server",
                            modifier = Modifier.size(64.dp),
                            tint = if (isServerOn) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (isServerOn) "Web Server: ACTIVE" else "Web Server: INACTIVE",
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (isServerOn) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Switch(
                            checked = isServerOn,
                            onCheckedChange = { viewModel.toggleServer(it) },
                            modifier = Modifier.padding(16.dp)
                        )

                        if (isServerOn && !serverUrl.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(32.dp))

                            // QR Code Placeholder
                            Box(
                                modifier = Modifier
                                    .size(250.dp)
                                    .background(Color.White, MaterialTheme.shapes.large)
                                    .border(2.dp, Color.Black, MaterialTheme.shapes.large)
                            ) {
                                Text(
                                    "QR CODE",
                                    modifier = Modifier.align(Alignment.Center),
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Scan to connect:",
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = serverUrl ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Empty State Component with Lucide icons
    @Composable
    fun EmptyState(
        icon: ImageVector,
        title: String,
        description: String,
        actionText: String? = null,
        onAction: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 48.dp)
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onAction,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.width(300.dp).height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(actionText, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
