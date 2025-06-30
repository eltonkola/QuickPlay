package com.eltonkola.quickplay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltonkola.quickplay.data.DownloadState
import com.eltonkola.quickplay.data.LocalItem
import com.eltonkola.quickplay.data.RemoteItem
import com.eltonkola.quickplay.data.local.GameDao
import com.eltonkola.quickplay.data.local.GameEntity
import com.eltonkola.quickplay.data.remote.RomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus



@HiltViewModel
class TvAppViewModel  @Inject constructor(
    private val repository: RomRepository,
    private val dao: GameDao

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

    private val _serverUrl = MutableStateFlow<String?>(null)
    val serverUrl: StateFlow<String?> = _serverUrl

    init {
        refreshRemoteItems()
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
        viewModelScope.launch {
            dao.updateGame(game.copy(isFavorite = !game.isFavorite))
        }
    }

    fun deleteGame(game: GameEntity) {
        viewModelScope.launch {
            game.filename?.let { repository.deleteRom(it) }
            dao.deleteGame(game)
        }
    }

    fun downloadRom(remoteItem: RemoteItem) {
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
        viewModelScope.launch {
            item.downloadFileName?.let { repository.deleteRom(it) }
            _remoteItems.update { items ->
                items.map { it.copy(isDownloaded = it.downloadUrl == item.downloadUrl) }
            }
            //TODO
           // dao.deleteGame(item.downloadFileName)
        }
    }

    fun launchGame(item: Any) {
        // Launch game based on type
    }

    fun toggleServer(on: Boolean) {
        _isServerOn.value = on
        if (on) {
            // Start server and get URL
            _serverUrl.value = "http://yourserver.com:8080"
        } else {
            // Stop server
            _serverUrl.value = null
        }
    }

}

