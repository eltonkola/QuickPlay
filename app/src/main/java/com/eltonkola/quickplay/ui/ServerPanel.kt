package com.eltonkola.quickplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Server
import com.eltonkola.quickplay.ui.elements.QrUrl

@Composable
fun ServerPanel(viewModel: TvAppViewModel) {
    val serverState by viewModel.serverState.collectAsState()

    val isRunning = serverState.running
    val ipAddress = serverState.ipAddress.orEmpty()


    Row() {


        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = Lucide.Server,
                contentDescription = "Server",
                modifier = Modifier.size(64.dp),
                tint = if (isRunning == true)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isRunning == true) "Web Server: ACTIVE" else "Web Server: INACTIVE",
                style = MaterialTheme.typography.headlineLarge,
                color = if (isRunning == true)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))


            if(isRunning == null ){
                CircularProgressIndicator()
            }else{
                Switch(
                    checked = isRunning,
                    onCheckedChange =  { viewModel.toggleServer(it) },
                    modifier = Modifier,
                )
            }



        }

        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRunning == true && ipAddress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.White, MaterialTheme.shapes.large)
                        .border(2.dp, Color.Black, MaterialTheme.shapes.large)
                ) {
                    QrUrl("http://$ipAddress:${serverState.port}")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scan to connect:}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "http://$ipAddress:${serverState.port}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

    }

}
