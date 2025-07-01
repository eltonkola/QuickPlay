package com.eltonkola.quickplay.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltonkola.quickplay.data.DownloadState
import com.eltonkola.quickplay.data.RemoteItem
import com.eltonkola.quickplay.data.WebServerManager
import com.eltonkola.quickplay.data.local.GameDao
import com.eltonkola.quickplay.data.local.GameEntity
import com.eltonkola.quickplay.data.remote.RomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class TvAppViewModel  @Inject constructor(
    private val repository: RomRepository,
    private val dao: GameDao,
    private val application: Application,
    private val webServerManager: WebServerManager
) : ViewModel() {
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    // Selected item for details dialog
    private val _selectedItem = MutableStateFlow<Any?>(null)
    val selectedItem: StateFlow<Any?> = _selectedItem

    // Games data
    val favoriteGames: StateFlow<List<GameEntity>> =
        dao.getAllGamesFlow()
            .map { list -> list.filter { it.isFavorite } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    val nonFavoriteGames: StateFlow<List<GameEntity>> =
        dao.getAllGamesFlow()
            .map { list -> list.filter {
                println("filter item: $it")
                !it.isFavorite
            } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    // Download data
    private val _remoteItems = MutableStateFlow<List<RemoteItem>>(emptyList())
    val remoteItems: StateFlow<List<RemoteItem>> = _remoteItems

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates

    // Server state
    private val _isServerOn = MutableStateFlow(false)
    val isServerOn: StateFlow<Boolean> = _isServerOn


    private val _serverState = MutableStateFlow(WebServerManager.ServerState(running = false))
    val serverState: StateFlow<WebServerManager.ServerState> = _serverState


    init {
        refreshRemoteItems()

        viewModelScope.launch {
            webServerManager.serverState.collect { state ->
                _serverState.value = state
            }
        }
    }

    fun refreshRemoteItems() {
        viewModelScope.launch {
            _remoteItems.value = repository.fetchRomsFromWebsite()
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _selectedItem.value = null
    }

    fun selectItem(item: Any) {
        _selectedItem.value = item
    }

    fun closeDetails(): Boolean {
        return if (_selectedItem.value != null) {
            _selectedItem.value = null
            true
        } else {
            false
        }
    }

    fun toggleFavorite(game: GameEntity) {
        closeDetails()
        viewModelScope.launch {
            dao.updateGame(game.copy(isFavorite = !game.isFavorite))
        }
    }

    fun deleteGame(game: GameEntity) {
        closeDetails()
        viewModelScope.launch {
            game.filename?.let { repository.deleteRom(it) }
            dao.deleteGame(game)
        }
    }

    fun downloadRom(remoteItem: RemoteItem) {
        closeDetails()
        if (remoteItem.isDownloaded) return

        _downloadStates.update { it + (remoteItem.downloadUrl to DownloadState(isDownloading = true)) }

        viewModelScope.launch {
            try {
                val downloadedItem = repository.downloadRom(remoteItem)
                _remoteItems.update { items ->
                    items.map { item ->
                        if (item.downloadUrl == remoteItem.downloadUrl) downloadedItem else item
                    }
                }
                // Add to games list
                dao.insertGame(
                    GameEntity(
                        name = downloadedItem.name,
                        imageUrl = downloadedItem.imageUrl,
                        filename = downloadedItem.downloadFileName,
                        isFavorite = false
                    )
                )
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _downloadStates.update { it - remoteItem.downloadUrl }
            }
        }
    }

    fun deleteDownload(item: RemoteItem) {
        closeDetails()
        viewModelScope.launch {
            item.downloadFileName?.let { repository.deleteRom(it) }
            _remoteItems.update { items ->
                items.map { it.copy(isDownloaded = it.downloadUrl == item.downloadUrl) }
            }
            //TODO
           // dao.deleteGame(item.downloadFileName)
        }
    }

    fun launchGame(romFileName: String) {
        // Launch game based on type

        closeDetails()

        application.launchRom(romFileName)
    }

    fun toggleServer(on: Boolean) {
        _isServerOn.value = on
        viewModelScope.launch(Dispatchers.IO) {
            if (on) {
                webServerManager.startServer()
            } else {
                webServerManager.stopServer()
            }
        }
    }

}

fun Context.launchRom(romFileName: String) {
    try {
        // romFileName is the extracted .smc or .sfc filename (e.g. "Super Mario World (U) [!].smc")
        val romFile = File(getExternalFilesDir("roms"), romFileName)

        if (!romFile.exists()) {
            Toast.makeText(this, "ROM file not found: $romFileName", Toast.LENGTH_LONG).show()
            return
        }

        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            romFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/octet-stream")
            setPackage("com.explusalpha.Snes9xPlus")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Snes9x EX+ not installed", Toast.LENGTH_LONG).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(this, "Failed to launch ROM: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
