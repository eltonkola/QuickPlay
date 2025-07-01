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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide

val GRID_ITEMS = 4

// Games Panel Implementation
@Composable
fun GamesScreen(viewModel: TvAppViewModel) {
    val favorites by viewModel.favoriteGames.collectAsState()
    val nonFavorites by viewModel.nonFavoriteGames.collectAsState()
    val hasGames = favorites.isNotEmpty() || nonFavorites.isNotEmpty()

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
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
                    .padding(8.dp)
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
                                fontWeight = FontWeight.Bold,
                          //      style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(favorites) { game ->
                            GameCard(
                                gameName = game.name,
                                isFavorite = game.isFavorite,
                                onMenu = { viewModel.selectItem(game) },
                                onClick = { context.launchRom(game.filename ?: game.name)  },
                            )
                        }
                    }

                    if (nonFavorites.isNotEmpty()) {
                        item(key = "all", span = { GridItemSpan(GRID_ITEMS) }){
                            Text(

                                "All Games",
                              //  style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(top = 8.dp, bottom = 8.dp)
                            )
                        }

                        items(nonFavorites) { game ->
                            GameCard(
                                gameName = game.name,
                                isFavorite = game.isFavorite,
                                onMenu = { viewModel.selectItem(game) },
                                onClick = { context.launchRom(game.filename ?: game.name)  },
                            )
                        }

                    }

                }


            }
        }
    }
}
