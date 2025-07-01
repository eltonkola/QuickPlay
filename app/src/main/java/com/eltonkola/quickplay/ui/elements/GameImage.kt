package com.eltonkola.quickplay.ui.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.eltonkola.quickplay.R
import java.io.File

@Composable
fun GameImage(
    gameName: String,
    modifier : Modifier,
    contentScale:  ContentScale
    ) {
    val context = LocalContext.current
    val imagePath = File(context.getExternalFilesDir("images"), "$gameName.png").absolutePath
    val imageFile = File(imagePath)

    if (imageFile.exists()) {
        AsyncImage(
            modifier = modifier,
            model = imageFile,
            contentDescription = "Game Image",
            contentScale = contentScale
        )
    } else {
        AsyncImage(
            modifier = modifier,
            model = R.drawable.placeholder_game,
            contentDescription = "Fallback Image",
            contentScale = contentScale
        )
    }
}
