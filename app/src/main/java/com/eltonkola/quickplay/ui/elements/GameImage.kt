package com.eltonkola.quickplay.ui.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.eltonkola.quickplay.R
import java.io.File

@Composable
fun GameImage(gameId: String) {
    val context = LocalContext.current
    val imagePath = File(context.getExternalFilesDir("images"), "$gameId.png").absolutePath
    val imageFile = File(imagePath)

    if (imageFile.exists()) {
        AsyncImage(
            model = imageFile,
            contentDescription = "Game Image"
        )
    } else {
        AsyncImage(
            model = R.drawable.placeholder_game,
            contentDescription = "Fallback Image"
        )
    }
}
