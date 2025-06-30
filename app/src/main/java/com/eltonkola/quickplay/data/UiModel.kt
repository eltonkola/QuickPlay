package com.eltonkola.quickplay.data


// Data Models
data class RemoteItem(
    val name: String,
    val filename: String,
    val downloadUrl: String,
    val mediaId: String = "",
    val isDownloaded: Boolean = false,
    val imageUrl: String = "",
    val downloadFileName: String = "",
    val isFavorite: Boolean = false
)

data class LocalItem(
    val name: String,
    val filename: String,
    val imageUrl: String = "",
    val isFavorite: Boolean = false
)

data class DownloadState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f
)

