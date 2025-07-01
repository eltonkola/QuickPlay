package com.eltonkola.quickplay.ui

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
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> GamesPanel(viewModel)
                1 -> DownloadPanel(viewModel)
                2 -> ServerPanel(viewModel)
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

