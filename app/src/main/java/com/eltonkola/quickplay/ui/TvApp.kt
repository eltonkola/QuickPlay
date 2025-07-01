package com.eltonkola.quickplay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvApp(
    viewModel: TvAppViewModel = viewModel()
) {

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Persistent Navigation Drawer (Left Panel)
        NavigationDrawer(
            selectedTab = selectedTab,
            onTabSelected = { viewModel.selectTab(it) },
            modifier = Modifier.fillMaxHeight()
        )

        // Center Content Panel
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(0.dp)
        ) {
            when (selectedTab) {
                0 -> GamesScreen(viewModel)
                1 -> DownloadScreen(viewModel)
                2 -> ServerScreen(viewModel)
            }


            if (selectedItem != null) {
                DetailsPanel(
                    item = selectedItem!!,
                    viewModel = viewModel,
                    onClose = { viewModel.closeDetails() },
                    modifier = Modifier.width(400.dp).align(Alignment.TopEnd)
                )
            }

        }


    }
}

