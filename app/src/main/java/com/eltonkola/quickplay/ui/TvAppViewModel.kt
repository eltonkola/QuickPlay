package com.eltonkola.quickplay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eltonkola.quickplay.data.DownloadState
import com.eltonkola.quickplay.data.LocalItem
import com.eltonkola.quickplay.data.RemoteItem
import com.eltonkola.quickplay.data.local.GameDao
import com.eltonkola.quickplay.data.local.GameEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.collections.map


data class TvAppUiState(
    val localItems: List<LocalItem> = emptyList(),
    val remoteItems: List<RemoteItem> = emptyList(),
    val downloadStates: Map<String, DownloadState> = emptyMap(),
    val isWebsiteActive: Boolean = false,
    val websiteUrl: String = "https://example.com",
    val barcodeData: String = "1234567890",
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val selectedLocalItem: LocalItem? = null,
    val selectedRemoteItem: RemoteItem? = null,
    val showOptionsPanel: Boolean = false
)


@HiltViewModel
class TvAppViewModel  @Inject constructor(
    private val dao: GameDao
) : ViewModel() {

    private val allGamesFlow: Flow<List<GameEntity>> = dao.getAllGamesFlow()

    val favoriteGames: StateFlow<List<GameEntity>> =
        allGamesFlow
            .map { list -> list.filter { it.isFavorite } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nonFavoriteGames: StateFlow<List<GameEntity>> =
        allGamesFlow
            .map { list -> list.filter { !it.isFavorite } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    private val _uiState = MutableStateFlow(TvAppUiState())
    val uiState: StateFlow<TvAppUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Mock data for demonstration
            val mockLocalItems = listOf(
                LocalItem("The Great Adventure", "adventure.mp4", "https://via.placeholder.com/400x225", true),
                LocalItem("Comedy Night Special", "comedy.mp4", "https://via.placeholder.com/400x225", false),
                LocalItem("Documentary: Nature", "nature.mp4", "https://via.placeholder.com/400x225", true),
                LocalItem("Action Thriller", "action.mp4", "https://via.placeholder.com/400x225", false),
                LocalItem("Family Movie", "family.mp4", "https://via.placeholder.com/400x225", true),
                LocalItem("Sci-Fi Epic", "scifi.mp4", "https://via.placeholder.com/400x225", false),
                LocalItem("Horror Classic", "horror.mp4", "https://via.placeholder.com/400x225", false),
                LocalItem("Romance Story", "romance.mp4", "https://via.placeholder.com/400x225", true),
            )

            val mockRemoteItems = listOf(
                RemoteItem("New Release 2024", "new2024.mp4", "https://example.com/download1",
                    imageUrl = "https://via.placeholder.com/400x225", isFavorite = true),
                RemoteItem("Classic Collection", "classic.mp4", "https://example.com/download2",
                    imageUrl = "https://via.placeholder.com/400x225", isDownloaded = true),
                RemoteItem("Award Winner", "award.mp4", "https://example.com/download3",
                    imageUrl = "https://via.placeholder.com/400x225"),
                RemoteItem("Director's Cut", "directors.mp4", "https://example.com/download4",
                    imageUrl = "https://via.placeholder.com/400x225"),
                RemoteItem("Behind the Scenes", "bts.mp4", "https://example.com/download5",
                    imageUrl = "https://via.placeholder.com/400x225"),
                RemoteItem("International Film", "intl.mp4", "https://example.com/download6",
                    imageUrl = "https://via.placeholder.com/400x225"),
            )

            _uiState.value = _uiState.value.copy(
                localItems = mockLocalItems,
                remoteItems = mockRemoteItems,
                isLoading = false
            )
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTab = index,
            showOptionsPanel = false,
            selectedLocalItem = null,
            selectedRemoteItem = null
        )
    }

    fun selectLocalItem(item: LocalItem) {
        _uiState.value = _uiState.value.copy(
            selectedLocalItem = item,
            selectedRemoteItem = null,
            showOptionsPanel = true
        )
    }

    fun selectRemoteItem(item: RemoteItem) {
        _uiState.value = _uiState.value.copy(
            selectedRemoteItem = item,
            selectedLocalItem = null,
            showOptionsPanel = true
        )
    }

    fun hideOptionsPanel() {
        _uiState.value = _uiState.value.copy(
            showOptionsPanel = false,
            selectedLocalItem = null,
            selectedRemoteItem = null
        )
    }

    fun toggleLocalFavorite(item: LocalItem) {
        val updatedItems = _uiState.value.localItems.map {
            if (it.filename == item.filename) {
                it.copy(isFavorite = !it.isFavorite)
            } else it
        }
        val updatedItem = updatedItems.find { it.filename == item.filename }
        _uiState.value = _uiState.value.copy(
            localItems = updatedItems,
            selectedLocalItem = updatedItem
        )
    }

    fun deleteLocalItem(item: LocalItem) {
        val updatedItems = _uiState.value.localItems.filter { it.filename != item.filename }
        _uiState.value = _uiState.value.copy(
            localItems = updatedItems,
            showOptionsPanel = false,
            selectedLocalItem = null
        )
    }

    fun downloadRemoteItem(item: RemoteItem) {
        if (_uiState.value.downloadStates[item.filename]?.isDownloading == true) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                downloadStates = _uiState.value.downloadStates +
                        (item.filename to DownloadState(isDownloading = true))
            )

            // Simulate download progress
            for (progress in 0..100 step 10) {
                kotlinx.coroutines.delay(200)
                _uiState.value = _uiState.value.copy(
                    downloadStates = _uiState.value.downloadStates +
                            (item.filename to DownloadState(isDownloading = true, progress = progress / 100f))
                )
            }

            val updatedRemoteItems = _uiState.value.remoteItems.map {
                if (it.filename == item.filename) it.copy(isDownloaded = true) else it
            }
            val updatedItem = updatedRemoteItems.find { it.filename == item.filename }

            _uiState.value = _uiState.value.copy(
                remoteItems = updatedRemoteItems,
                downloadStates = _uiState.value.downloadStates - item.filename,
                selectedRemoteItem = updatedItem
            )
        }
    }

    fun toggleWebsite() {
        _uiState.value = _uiState.value.copy(isWebsiteActive = !_uiState.value.isWebsiteActive)
    }

}

