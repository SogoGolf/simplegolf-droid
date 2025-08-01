package com.sogo.golf.msl.features.play.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGrey

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
    val showDialog by playRoundViewModel.showGoToHoleDialog.collectAsState()
    val isAbandoningRound by playRoundViewModel.isAbandoningRound.collectAsState()
    val abandonError by playRoundViewModel.abandonError.collectAsState()

    var showBackConfirmDialog by remember { mutableStateOf(false) }
    var showAbandonDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (showBackButton) {
            // Check if we're on the starting hole (same logic as header back button)
            val startingHoleNumber = localGame?.startingHoleNumber ?: 1
            if (currentHoleNumber == startingHoleNumber) {
                // On starting hole - show confirmation dialog
                showBackConfirmDialog = true
            } else {
                // Not on starting hole - navigate normally
                playRoundViewModel.navigateToPreviousHole()
            }
        }
        // If showBackButton is false, do nothing (completely block back navigation)
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        if (showBackButton) {
                            IconButton(
                                onClick = { 
                                    // Check if we're on the starting hole
                                    val startingHoleNumber = localGame?.startingHoleNumber ?: 1
                                    if (currentHoleNumber == startingHoleNumber) {
                                        // On starting hole - show confirmation dialog
                                        showBackConfirmDialog = true
                                    } else {
                                        // Not on starting hole - navigate normally
                                        playRoundViewModel.navigateToPreviousHole()
                                    }
                                },
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
                            modifier = Modifier.clickable {
                                playRoundViewModel.showGoToHoleDialog()
                            }
                        )
                        Text(
                            " $currentHoleNumber",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            modifier = Modifier.clickable {
                                playRoundViewModel.showGoToHoleDialog()
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                        //.background(Color.Red),
                    ) {

                        IconButton(
                            onClick = { showAbandonDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MSLColors.mslGunMetal
                            )
                        }

                        IconButton(
                            onClick = { playRoundViewModel.navigateToNextHole(navController) },
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
            }?.takeIf { it.isNotBlank() } ?: "--"
            
            val partnerDailyHandicap = playingPartner?.dailyHandicap ?: 0
            
            // Extract game data
            val teeColor = localGameValue?.teeColourName ?: "Black"
            
            // Extract competition data
            val competitionType = localCompetitionValue?.players?.firstOrNull()?.scoreType ?: "Stableford"
            
            // Extract hole data for current hole
            android.util.Log.d("PlayRoundScreen", "🔍 Looking for hole data: currentHoleNumber=$currentHoleNumber")
            android.util.Log.d("PlayRoundScreen", "🔍 Competition data: ${localCompetitionValue != null}")
            localCompetitionValue?.players?.firstOrNull()?.holes?.forEach { hole ->
                android.util.Log.d("PlayRoundScreen", "  - Available hole: ${hole.holeNumber} (alias: ${hole.holeAlias})")
            }
            
            val currentHole = localCompetitionValue?.players?.firstOrNull()?.holes?.find { 
                it.holeNumber == currentHoleNumber 
            }
            
            android.util.Log.d("PlayRoundScreen", "🔍 Found hole data: ${currentHole != null}")
            if (currentHole != null) {
                android.util.Log.d("PlayRoundScreen", "  - Hole ${currentHole.holeNumber}: par=${currentHole.par}, distance=${currentHole.distance}")
            } else {
                android.util.Log.w("PlayRoundScreen", "⚠️ Using fallback values for hole $currentHoleNumber")
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

            // Extract pickup states for current hole
            val mainGolferPickedUp = currentRoundValue?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }?.isBallPickedUp ?: false
            
            val partnerPickedUp = currentRoundValue?.playingPartnerRound?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }?.isBallPickedUp ?: false

            // Calculate current points for display
            val mainGolferCurrentPoints = if (mainGolferStrokes > 0 && currentHole != null) {
                playRoundViewModel.calculateCurrentPoints(
                    strokes = mainGolferStrokes,
                    par = par,
                    index1 = currentHole.strokeIndexes.getOrNull(0) ?: 0,
                    index2 = currentHole.strokeIndexes.getOrNull(1) ?: 0,
                    index3 = currentHole.strokeIndexes.getOrNull(2) ?: 0,
                    dailyHandicap = mainGolferDailyHandicap.toDouble(),
                    scoreType = competitionType
                )
            } else 0

            val partnerCurrentPoints = if (partnerStrokes > 0 && currentHole != null) {
                playRoundViewModel.calculateCurrentPoints(
                    strokes = partnerStrokes,
                    par = par,
                    index1 = currentHole.strokeIndexes.getOrNull(0) ?: 0,
                    index2 = currentHole.strokeIndexes.getOrNull(1) ?: 0,
                    index3 = currentHole.strokeIndexes.getOrNull(2) ?: 0,
                    dailyHandicap = partnerDailyHandicap.toDouble(),
                    scoreType = competitionType
                )
            } else 0

            // Top card - Playing Partner
            HoleCardTest(
                golferName = partnerDisplayName,
                backgroundColor = mslBlue,
                teeColor = teeColor,
                competitionType = competitionType,
                dailyHandicap = partnerDailyHandicap,
                strokes = partnerStrokes,
                currentPoints = partnerCurrentPoints,
                par = par,
                distance = distance,
                strokeIndex = strokeIndexes,
                totalScore = currentRoundValue?.playingPartnerRound?.holeScores?.sumOf { it.score.toInt() } ?: 0,
                onSwipeNext = { playRoundViewModel.navigateToNextHole(navController) },
                onSwipePrevious = { 
                    if (showBackButton) {
                        playRoundViewModel.navigateToPreviousHole()
                    }
                },
                onStrokeButtonClick = { playRoundViewModel.onPartnerStrokeButtonClick() },
                onPlusButtonClick = { playRoundViewModel.onPartnerPlusButtonClick() },
                onMinusButtonClick = { playRoundViewModel.onPartnerMinusButtonClick() },
                isBallPickedUp = partnerPickedUp,
                onPickupButtonClick = { playRoundViewModel.onPartnerPickupButtonClick() },
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
                backgroundColor = mslGrey,
                teeColor = teeColor,
                competitionType = competitionType,
                dailyHandicap = mainGolferDailyHandicap,
                strokes = mainGolferStrokes,
                currentPoints = mainGolferCurrentPoints,
                par = par,
                distance = distance,
                strokeIndex = strokeIndexes,
                totalScore = currentRoundValue?.holeScores?.sumOf { it.score.toInt() } ?: 0,
                onSwipeNext = { playRoundViewModel.navigateToNextHole(navController) },
                onSwipePrevious = { 
                    if (showBackButton) {
                        playRoundViewModel.navigateToPreviousHole()
                    }
                },
                onStrokeButtonClick = { playRoundViewModel.onMainGolferStrokeButtonClick() },
                onPlusButtonClick = { playRoundViewModel.onMainGolferPlusButtonClick() },
                onMinusButtonClick = { playRoundViewModel.onMainGolferMinusButtonClick() },
                isBallPickedUp = mainGolferPickedUp,
                onPickupButtonClick = { playRoundViewModel.onMainGolferPickupButtonClick() },
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

        // Abandon round confirmation dialog
        if (showAbandonDialog) {
            AlertDialog(
                onDismissRequest = { showAbandonDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showAbandonDialog = false
                            playRoundViewModel.abandonRound(navController)
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showAbandonDialog = false }
                    ) {
                        Text("No")
                    }
                },
                title = { Text("Abandon Round") },
                text = {
                    Text("Do you want to abandon your round? This will delete all your progress and you'll need to start over.")
                }
            )
        }

        // Abandon error dialog
        abandonError?.let { error ->
            AlertDialog(
                onDismissRequest = { playRoundViewModel.clearAbandonError() },
                confirmButton = {
                    Button(onClick = { playRoundViewModel.clearAbandonError() }) {
                        Text("OK")
                    }
                },
                title = { Text("Abandon Round Error") },
                text = { Text(error) }
            )
        }

        // Keep loading states
        if (isRemovingMarker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Removing marker...", color = Color.White)
                }
            }
        }

        // Abandon loading state
        if (isAbandoningRound) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Abandoning round...", color = Color.White)
                }
            }
        }

        currentRound?.let { round ->
            if (showDialog && round.holeScores.isNotEmpty() && round.playingPartnerRound?.holeScores?.isNotEmpty() == true) {
                GoToHoleAlertDialog(
                    holeScores = round.holeScores,
                    holeScoresPlayingPartner = round.playingPartnerRound.holeScores,
                showDialog = showDialog,
                onDismiss = { playRoundViewModel.hideGoToHoleDialog() },
                onConfirm = { holeNumber ->
                    playRoundViewModel.hideGoToHoleDialog()
                    playRoundViewModel.navigateToHole(holeNumber)
                }
            )
            }
        }
    }
}
