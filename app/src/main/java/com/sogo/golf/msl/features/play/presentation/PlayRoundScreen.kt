package com.sogo.golf.msl.features.play.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.navigation.NavViewModel

@Composable
fun PlayRoundScreen(
    navController: NavController,
    viewModel: NavViewModel = hiltViewModel(),
    playRoundViewModel: PlayRoundViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Show scorecard in landscape
        ScorecardScreen()
    } else {
        // Show normal Screen4 content in portrait
        Screen4Portrait(navController, viewModel, playRoundViewModel)
    }
}

@Composable
private fun Screen4Portrait(
    navController: NavController,
    viewModel: NavViewModel,
    playRoundViewModel: PlayRoundViewModel
) {
    val deleteMarkerEnabled by playRoundViewModel.deleteMarkerEnabled.collectAsState()
    val isRemovingMarker by playRoundViewModel.isRemovingMarker.collectAsState()
    val markerError by playRoundViewModel.markerError.collectAsState()
    val localGame by playRoundViewModel.localGame.collectAsState()
    val currentGolfer by playRoundViewModel.currentGolfer.collectAsState()
    
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        // Show confirmation dialog before navigating back
        showBackConfirmDialog = true
    }

    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding()
    ) {
        // HoleHeader as top bar
        HoleHeader(
            holeNumber = 1, // TODO: Get from viewmodel
            onBack = {
                // Show confirmation dialog before navigating back
                showBackConfirmDialog = true
            },
            onClose = {
                // TODO: Implement close functionality
                // viewModel.logout(navController)
            },
            onNext = {
                // TODO: Implement hole navigation
                // playRoundViewModel.incrementHoleNumber()
            },
            onTapHoleNumber = {
                // TODO: Implement hole selection dialog
                // playRoundViewModel.showGoToHoleDialog()
            },
            showBackButton = true // TODO: Get from viewmodel state
        )

        // Main content area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Play Round Content",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "HoleHeader integration complete",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Back confirmation dialog
    if (showBackConfirmDialog) {
        // Extract StateFlow values to local variables for null checking
        val currentGolferValue = currentGolfer
        val localGameValue = localGame
        
        // Find the partner marked by current user
        val markerName = if (currentGolferValue != null && localGameValue != null) {
            val partner = localGameValue.playingPartners.find { partner ->
                partner.markedByGolfLinkNumber == currentGolferValue.golfLinkNo
            }
            if (partner != null) {
                "${partner.firstName} ${partner.lastName}".trim()
            } else {
                "Unknown"
            }
        } else {
            "Unknown"
        }
        
        AlertDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showBackConfirmDialog = false
                        playRoundViewModel.removeMarkerAndNavigateBack(navController)
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showBackConfirmDialog = false }
                ) {
                    Text("No")
                }
            },
            title = { Text("Remove Marker") },
            text = { 
                Text("This will remove your marker ($markerName) and you will need to choose again. Are you sure?") 
            }
        )
    }

    // Keep error dialogs
    markerError?.let { error ->
        AlertDialog(
            onDismissRequest = { playRoundViewModel.clearMarkerError() },
            confirmButton = {
                Button(onClick = { playRoundViewModel.clearMarkerError() }) {
                    Text("OK")
                }
            },
            title = { Text("Marker Removal Error") },
            text = { Text(error) }
        )
    }

    // Keep loading states
    if (isRemovingMarker) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
