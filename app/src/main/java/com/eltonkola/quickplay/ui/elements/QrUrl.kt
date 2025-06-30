package com.eltonkola.quickplay.ui.elements

import androidx.compose.foundation.Image
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import androidx.compose.runtime.Composable

@Composable
fun QrUrl(url: String) {
    Image(
        painter = rememberQrCodePainter(url),
        contentDescription = "QR code for $url"
    )
}
