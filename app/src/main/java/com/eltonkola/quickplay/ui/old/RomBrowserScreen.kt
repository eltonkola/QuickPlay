package com.eltonkola.quickplay.ui.old


import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomBrowserScreen() {
//
//    val context = LocalContext.current
//
//    val viewModel: RomBrowserViewModel = viewModel(
//        factory = RomBrowserViewModel.provideFactory(context)
//    )
//
//    val uiState by viewModel.uiState.collectAsState()
//    val scope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) {
//        viewModel.loadRoms()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Header
//        Text(
//            text = "SNES ROM Browser",
//            fontSize = 28.sp,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        // Filter buttons
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            FilterChip(
//                onClick = { viewModel.setFilter(RomFilter.ALL) },
//                label = { Text("All ROMs") },
//                selected = uiState.currentFilter == RomFilter.ALL
//            )
//            FilterChip(
//                onClick = { viewModel.setFilter(RomFilter.DOWNLOADED) },
//                label = { Text("Downloaded") },
//                selected = uiState.currentFilter == RomFilter.DOWNLOADED
//            )
//            FilterChip(
//                onClick = { viewModel.setFilter(RomFilter.FAVORITES) },
//                label = { Text("Favorites") },
//                selected = uiState.currentFilter == RomFilter.FAVORITES
//            )
//        }
//
//        // Loading indicator
//        if (uiState.isLoading) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    CircularProgressIndicator()
//                    Text(
//                        text = "Loading ROMs...",
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
//                }
//            }
//        } else {
//            // ROM Grid
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(4),
//                contentPadding = PaddingValues(8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                items(uiState.filteredRoms) { rom ->
//                    RomCard(
//                        rom = rom,
//                        onDownload = {
//                            scope.launch {
//                                viewModel.downloadRom(rom)
//                            }
//                        },
//                        onDelete = { viewModel.deleteRom(rom) },
//                        onToggleFavorite = { viewModel.toggleFavorite(rom) },
//                        onLaunch = {
//                            context.launchRom(rom.downloadFileName)
//
//
//                        }
//                    )
//                }
//            }
//        }
//
//        // Error message
//        uiState.errorMessage?.let { error ->
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
//            ) {
//                Text(
//                    text = error,
//                    modifier = Modifier.padding(16.dp),
//                    color = Color.Red
//                )
//            }
//        }
//    }
//}
//fun Context.launchRom(romFileName: String) {
//    try {
//        // romFileName is the extracted .smc or .sfc filename (e.g. "Super Mario World (U) [!].smc")
//        val romFile = File(getExternalFilesDir("roms"), romFileName)
//
//        if (!romFile.exists()) {
//            Toast.makeText(this, "ROM file not found: $romFileName", Toast.LENGTH_LONG).show()
//            return
//        }
//
//        val uri = FileProvider.getUriForFile(
//            this,
//            "$packageName.provider",
//            romFile
//        )
//
//        val intent = Intent(Intent.ACTION_VIEW).apply {
//            setDataAndType(uri, "application/octet-stream")
//            setPackage("com.explusalpha.Snes9xPlus")
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        if (intent.resolveActivity(packageManager) != null) {
//            startActivity(intent)
//        } else {
//            Toast.makeText(this, "Snes9x EX+ not installed", Toast.LENGTH_LONG).show()
//        }
//
//    } catch (e: Exception) {
//        e.printStackTrace()
//        Toast.makeText(this, "Failed to launch ROM: ${e.message}", Toast.LENGTH_LONG).show()
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RomCard(
//    rom: RomItem,
//    onDownload: () -> Unit,
//    onDelete: () -> Unit,
//    onToggleFavorite: () -> Unit,
//    onLaunch: () -> Unit
//) {
//    val focusRequester = remember { FocusRequester() }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//            .focusRequester(focusRequester),
//        onClick = { if (rom.isDownloaded) onLaunch() else onDownload() }
//    ) {
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ){
//            AsyncImage(
//                model = rom.imageUrl,
//                contentDescription = rom.name,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(1.4f)
//                    .padding(bottom = 8.dp)
//            )
//
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(12.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//
//
//
//            // Title
//            Text(
//                text = rom.name,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            // Status indicators
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Download status
//                Text(
//                    text = if (rom.isDownloaded) "✓" else "↓",
//                    fontSize = 18.sp,
//                    color = if (rom.isDownloaded) Color.Green else MaterialTheme.colorScheme.primary
//                )
//
//                // Favorite status
//                Text(
//                    text = if (rom.isFavorite) "★" else "☆",
//                    fontSize = 18.sp,
//                    color = if (rom.isFavorite) Color.Yellow else Color.Gray
//                )
//            }
//
//            // Action buttons
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                if (rom.isDownloaded) {
//                    // Launch button
//                    FilledTonalButton(
//                        onClick = onLaunch,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("Play", fontSize = 10.sp)
//                    }
//
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    // Delete button
//                    OutlinedButton(
//                        onClick = onDelete,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("Delete", fontSize = 10.sp)
//                    }
//                } else {
//                    // Download button
//                    FilledTonalButton(
//                        onClick = onDownload,
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text("Download", fontSize = 10.sp)
//                    }
//                }
//            }
//
//            // Favorite toggle
//            OutlinedButton(
//                onClick = onToggleFavorite,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    text = if (rom.isFavorite) "Remove Favorite" else "Add Favorite",
//                    fontSize = 10.sp
//                )
//            }
//        }
//        }
//    }
}

