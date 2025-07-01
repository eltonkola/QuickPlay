package com.eltonkola.quickplay.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.eltonkola.quickplay.data.local.GameEntity
import com.eltonkola.quickplay.ui.elements.GameImage

val GRID_ITEMS = 4

// Games Panel Implementation
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


                    // 6-column grid for favorites
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GRID_ITEMS),
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                    if (favorites.isNotEmpty()) {
                        item(key = "fav", span = { GridItemSpan(GRID_ITEMS) }){
                            Text(
                                "Favorites",
                          //      style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        items(favorites) { game ->
                            LandscapeGameCard(
                                game = game,
                                onClick = { viewModel.selectItem(game) }
                            )
                        }
                    }

                    if (nonFavorites.isNotEmpty()) {
                        item(key = "all", span = { GridItemSpan(GRID_ITEMS) }){
                            Text(
                                "All Games",
                              //  style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .padding(top = 32.dp, bottom = 16.dp)
                            )
                        }

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



// Landscape Game Card Implementation
@Composable
fun LandscapeGameCard(
    game: GameEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderWidth = if (isFocused) 4.dp else 1.dp
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column {
            // Landscape image (16:9 aspect ratio)

            GameImage(
                gameName = game.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )

//            AsyncImage(
//                model = game.imageUrl,
//                contentDescription = game.name,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(16f / 9f)
//                    .clip(MaterialTheme.shapes.large),
//                contentScale = ContentScale.Crop
//            )

            // Game title and favorite indicator
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
