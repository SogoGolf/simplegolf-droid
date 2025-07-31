package com.sogo.golf.msl.features.play.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.navigation.NavViewModel
import com.sogo.golf.msl.ui.theme.MSLColors

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
    val localCompetition by playRoundViewModel.localCompetition.collectAsState()
    val currentGolfer by playRoundViewModel.currentGolfer.collectAsState()
    val currentRound by playRoundViewModel.currentRound.collectAsState()
    val currentHoleNumber by playRoundViewModel.currentHoleNumber.collectAsState()
    val showBackButton by playRoundViewModel.showBackButton.collectAsState()

    var showBackConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = showBackButton) {
        if (!showBackButton) {
            android.util.Log.d("PlayRoundScreen", "Back navigation blocked - strokes exist on first hole")
            return@BackHandler
        }

        // Show confirmation dialog before navigating back
        showBackConfirmDialog = true
    }

    Scaffold(
        topBar = {
//            HoleHeader(
//                holeNumber = 1, // TODO: Get from viewmodel
//                onBack = {
//                    // Show confirmation dialog before navigating back
//                    showBackConfirmDialog = true
//                },
//                onClose = {
//                    // TODO: Implement close functionality
//                    // viewModel.logout(navController)
//                },
//                onNext = {
//                    // TODO: Implement hole navigation
//                    // playRoundViewModel.incrementHoleNumber()
//                },
//                onTapHoleNumber = {
//                    // TODO: Implement hole selection dialog
//                    // playRoundViewModel.showGoToHoleDialog()
//                },
//                showBackButton = showBackButton
//            )

            Column(modifier = Modifier.padding(top = 6.dp)) {

                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
//                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
//                            .background(Color.Red),
                    ) {
                        IconButton(
                            onClick = { playRoundViewModel.navigateToPreviousHole() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Hole",
                                tint = MSLColors.mslGunMetal,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            //.background(Color.Yellow)
                            .weight(3f),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Text(
                            "HOLE",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            modifier = Modifier
                        )
                        Text(
                            " $currentHoleNumber",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                        //.background(Color.Red),
                    ) {

                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MSLColors.mslGunMetal
                            )
                        }

                        IconButton(
                            onClick = { playRoundViewModel.navigateToNextHole() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Hole",
                                tint = MSLColors.mslGunMetal,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                }

//                Text("123")
            }
        }
        // No bottomBar parameter = no bottom bar
    ) { paddingValues ->
        // Your screen content

        Column(
            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
                .navigationBarsPadding()
                .statusBarsPadding()
//                .padding(vertical = 6.dp)
                .padding(
                    top = paddingValues.calculateTopPadding(), // Custom top spacing
                )
        ) {
            // Top card - Main Golfer
//            Column() {
//                Text("123", modifier = Modifier.statusBarsPadding())
//                Text("123", modifier = Modifier.statusBarsPadding())
//                Text("123", modifier = Modifier.statusBarsPadding())
//            }

             Spacer(modifier = Modifier.height(5.dp))

            // Extract golfer data from Room database
            val currentGolferValue = currentGolfer
            val localGameValue = localGame
            val localCompetitionValue = localCompetition
            val currentRoundValue = currentRound
            
            // Extract main golfer data
            val mainGolferName = currentGolferValue?.let { golfer ->
                "${golfer.firstName} ${golfer.surname}".trim()
            } ?: "Main Golfer"
            
            val mainGolferHandicap = currentGolferValue?.primary?.toInt() ?: 0
            val mainGolferDailyHandicap = localGameValue?.dailyHandicap ?: 0
            
            // Extract playing partner data
            val playingPartner = if (currentGolferValue != null && localGameValue != null) {
                localGameValue.playingPartners.find { partner ->
                    partner.markedByGolfLinkNumber == currentGolferValue.golfLinkNo
                }
            } else null
            
            val partnerDisplayName = playingPartner?.let { 
                "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() 
            }?.takeIf { it.isNotBlank() } ?: "Playing Partner"
            
            val partnerDailyHandicap = playingPartner?.dailyHandicap ?: 0
            
            // Extract game data
            val teeColor = localGameValue?.teeColourName ?: "Black"
            
            // Extract competition data
            val competitionType = localCompetitionValue?.players?.firstOrNull()?.scoreType ?: "Stableford"
            
            // Extract hole data for current hole
            val currentHole = localCompetitionValue?.players?.firstOrNull()?.holes?.find { 
                it.holeNumber == currentHoleNumber 
            }
            val par = currentHole?.par ?: 5
            val distance = currentHole?.distance ?: 441
            val strokeIndexes = currentHole?.strokeIndexes?.joinToString("/") ?: "1/22/40"
            
            // Extract stroke data from Round object for current hole
            val mainGolferStrokes = currentRoundValue?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }?.strokes ?: 0
            
            val partnerStrokes = currentRoundValue?.playingPartnerRound?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }?.strokes ?: 0

            // Top card - Playing Partner
            HoleCardTest(
                golferName = partnerDisplayName,
                backgroundColor = Color.Green,
                teeColor = teeColor,
                competitionType = competitionType,
                dailyHandicap = partnerDailyHandicap,
                strokes = partnerStrokes,
                currentPoints = 0,
                par = par,
                distance = distance,
                strokeIndex = strokeIndexes,
                totalScore = 0, // TODO: Get from round data
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)  // This makes it take up half the available space
                    .padding(horizontal = 10.dp)
            )

            // Gap between cards
            Spacer(modifier = Modifier.height(10.dp))

            // Bottom card - Main Golfer
            HoleCardTest(
                golferName = mainGolferName,
                backgroundColor = Color.Blue,
                teeColor = teeColor,
                competitionType = competitionType,
                dailyHandicap = mainGolferDailyHandicap,
                strokes = mainGolferStrokes,
                currentPoints = 0,
                par = par,
                distance = distance,
                strokeIndex = strokeIndexes,
                totalScore = 0, // TODO: Get from round data
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)  // This makes it take up the other half
                    .padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))
        }


//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues) // <-- APPLY THE PADDING HERE
//                .navigationBarsPadding()
//              .padding(vertical = 6.dp)
//        ) {
        // Top card (green background)
//            HoleCard(
//                backgroundColor = Color.Green,
//                golferName = "Main Golfer",
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//                    .padding(horizontal = 10.dp)
//            )

        // Gap between cards
//            Spacer(modifier = Modifier.height(10.dp))

        // Bottom card (blue background)
//            HoleCard(
//                backgroundColor = Color.Blue,
//                golferName = "Playing Partner",
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//                    .padding(horizontal = 10.dp)
//            )
//        }
//    }

//    Column(
//        modifier = Modifier.fillMaxSize().navigationBarsPadding()
//    ) {
        // HoleHeader as top bar
//        HoleHeader(
//            holeNumber = 1, // TODO: Get from viewmodel
//            onBack = {
//                // Show confirmation dialog before navigating back
//                showBackConfirmDialog = true
//            },
//            onClose = {
//                // TODO: Implement close functionality
//                // viewModel.logout(navController)
//            },
//            onNext = {
//                // TODO: Implement hole navigation
//                // playRoundViewModel.incrementHoleNumber()
//            },
//            onTapHoleNumber = {
//                // TODO: Implement hole selection dialog
//                // playRoundViewModel.showGoToHoleDialog()
//            },
//            showBackButton = showBackButton
//        )

        // Two HoleCard components taking up remaining vertical space
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .navigationBarsPadding()
////                .padding(horizontal = 16.dp)
//        ) {
//            // Top card (green background)
//            HoleCard(
//                backgroundColor = Color.Green,
//                golferName = "Main Golfer",
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//            )
//
//            // Gap between cards
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // Bottom card (blue background)
//            HoleCard(
//                backgroundColor = Color.Blue,
//                golferName = "Playing Partner",
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//            )
//        }
        // }

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
}
