package com.eltonkola.quickplay.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Gamepad
import com.composables.icons.lucide.Gamepad2
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Server
import com.eltonkola.quickplay.R


// Navigation Drawer Implementation
@Composable
fun NavigationDrawer(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf("Games Library", "Download Center", "Web Server")
    val icons = listOf(
        Lucide.Gamepad2,
        Lucide.Download,
        Lucide.Server
    )

    Surface(
        modifier = modifier.width(280.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header

                Image(
                    painter = painterResource(R.drawable.tv_banner),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxWidth().aspectRatio(3.2f)
                )

            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Navigation Items
            tabTitles.forEachIndexed { index, title ->
                NavigationItem(
                    text = title,
                    icon = icons[index],
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    "v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

// Navigation Item Implementation
@Composable
fun NavigationItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
        }
    }
}
