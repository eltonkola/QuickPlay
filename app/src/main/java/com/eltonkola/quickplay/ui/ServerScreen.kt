package com.eltonkola.quickplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ServerCog
import com.eltonkola.quickplay.ui.elements.QrUrl

@Composable
fun ServerScreen(viewModel: TvAppViewModel) {
    val serverState by viewModel.serverState.collectAsState()
    val isRunning = serverState.running
    val ipAddress = serverState.ipAddress.orEmpty()

    Row (
        modifier = Modifier
            .fillMaxWidth()
    ){


    Column(
        modifier = Modifier
            .weight(0.5f).fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Lucide.ServerCog,
            contentDescription = "Server",
            modifier = Modifier.size(72.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Local Web Server",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isRunning == true) "Status: ACTIVE" else "Status: INACTIVE",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isRunning == null) {
            CircularProgressIndicator()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRunning) "Turn Off" else "Turn On",
                    modifier = Modifier.padding(end = 8.dp),
//                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isRunning,
                    onCheckedChange = { viewModel.toggleServer(it) }
                )
            }
        }
    }
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .weight(0.5f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRunning == true && ipAddress.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, MaterialTheme.shapes.medium)
                        .border(2.dp, Color.Black, MaterialTheme.shapes.medium)
                ) {
                    QrUrl("http://$ipAddress:${serverState.port}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scan this QR code to access the server",
//                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Or open this address on another device:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "http://$ipAddress:${serverState.port}",
//                    style = MaterialTheme.typography.bodyLarge.copy(
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = MaterialTheme.colorScheme.primary
//                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
