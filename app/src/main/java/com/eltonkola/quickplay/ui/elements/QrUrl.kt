package com.eltonkola.quickplay.ui.elements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun QrUrl(url: String) {
    Card(
        modifier = Modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.onPrimary)
    ) {
        Image(
            modifier = Modifier.size(320.dp),
            painter = rememberQrCodePainter(url),
            contentDescription = "QR code for $url"
        )
    }
}
