package com.eltonkola.quickplay.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File


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

data class DownloadState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f
)



