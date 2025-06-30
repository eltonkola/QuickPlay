package com.eltonkola.quickplay.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

data class RomItem(
    val name: String,
    val filename: String,
    val downloadUrl: String,
    val mediaId: String = "",
    val isDownloaded: Boolean = false,
    val imageUrl: String = "",
    val downloadFileName: String = "",
    val isFavorite: Boolean = false
)

enum class RomFilter {
    ALL, DOWNLOADED, FAVORITES
}

data class RomBrowserUiState(
    val roms: List<RomItem> = emptyList(),
    val filteredRoms: List<RomItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentFilter: RomFilter = RomFilter.ALL
)

class RomBrowserViewModel(private val context: Context) : ViewModel() {
    private val _uiState = MutableStateFlow(RomBrowserUiState())
    val uiState: StateFlow<RomBrowserUiState> = _uiState.asStateFlow()

    private val romRepository = RomRepository(context)

    fun loadRoms() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val roms = romRepository.fetchRomsFromWebsite()
                val romsWithStatus = roms.map { rom ->
                    rom.copy(
                        isDownloaded = romRepository.isRomDownloaded(rom.filename),
                        isFavorite = romRepository.isRomFavorite(rom.filename)
                    )
                }

                _uiState.value = _uiState.value.copy(
                    roms = romsWithStatus,
                    filteredRoms = filterRoms(romsWithStatus, _uiState.value.currentFilter),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load ROMs: ${e.message}"
                )
            }
        }
    }

    fun setFilter(filter: RomFilter) {
        val filteredRoms = filterRoms(_uiState.value.roms, filter)
        _uiState.value = _uiState.value.copy(
            currentFilter = filter,
            filteredRoms = filteredRoms
        )
    }

    private fun filterRoms(roms: List<RomItem>, filter: RomFilter): List<RomItem> {
        return when (filter) {
            RomFilter.ALL -> roms
            RomFilter.DOWNLOADED -> roms.filter { it.isDownloaded }
            RomFilter.FAVORITES -> roms.filter { it.isFavorite }
        }
    }

    suspend fun downloadRom(rom: RomItem) {
        try {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            val downloadedRom = romRepository.downloadRom(rom)

            // Update the ROM status
            val updatedRoms = _uiState.value.roms.map { r ->
                if (r.filename == rom.filename) {
                    r.copy(isDownloaded = true, downloadFileName = downloadedRom.downloadFileName)
                } else r
            }
            _uiState.value = _uiState.value.copy(
                roms = updatedRoms,
                filteredRoms = filterRoms(updatedRoms, _uiState.value.currentFilter)
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to download ROM: ${e.message}"
            )
        }
    }

    fun deleteRom(rom: RomItem) {
        try {
            romRepository.deleteRom(rom.downloadFileName)

            // Update the ROM status
            val updatedRoms = _uiState.value.roms.map { r ->
                if (r.filename == rom.filename) r.copy(isDownloaded = false) else r
            }
            _uiState.value = _uiState.value.copy(
                roms = updatedRoms,
                filteredRoms = filterRoms(updatedRoms, _uiState.value.currentFilter)
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Failed to delete ROM: ${e.message}"
            )
        }
    }

    fun toggleFavorite(rom: RomItem) {
        val newFavoriteStatus = !rom.isFavorite
        romRepository.setRomFavorite(rom.filename, newFavoriteStatus)

        // Update the ROM status
        val updatedRoms = _uiState.value.roms.map { r ->
            if (r.filename == rom.filename) r.copy(isFavorite = newFavoriteStatus) else r
        }
        _uiState.value = _uiState.value.copy(
            roms = updatedRoms,
            filteredRoms = filterRoms(updatedRoms, _uiState.value.currentFilter)
        )
    }

    companion object {
        fun provideFactory(context: Context) = viewModelFactory {
            initializer {
                RomBrowserViewModel(context)
            }
        }
    }
}