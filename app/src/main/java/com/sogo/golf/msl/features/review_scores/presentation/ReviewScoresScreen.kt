package com.sogo.golf.msl.features.review_scores.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.features.play.presentation.ScorecardScreen
import com.sogo.golf.msl.navigation.NavViewModel
import com.sogo.golf.msl.shared_components.ui.PostRoundCard
import com.sogo.golf.msl.shared_components.ui.PostRoundHeader
import com.sogo.golf.msl.shared_components.ui.SignatureDialog
import com.sogo.golf.msl.ui.theme.MSLColors

@Composable
fun ReviewScoresScreen(
    navController: NavController, 
    navViewModel: NavViewModel,
    roundId: String = ""
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        ScorecardScreen()
    } else {
        ReviewScoresPortrait(
            navController = navController,
            navViewModel = navViewModel,
            roundId = roundId
        )
    }
}

@Composable
private fun ReviewScoresPortrait(
    navController: NavController,
    navViewModel: NavViewModel,
    roundId: String
) {
    val viewModel: ReviewScoresViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val playerSignatures by viewModel.playerSignatures.collectAsState()
    val roundSubmitState by viewModel.roundSubmitState.collectAsState()
    
    var showSignatureDialog by remember { mutableStateOf(false) }
    var currentSignaturePlayerId by remember { mutableStateOf("") }
    var currentSignaturePlayerFirstName by remember { mutableStateOf("") }
    var currentSignaturePlayerLastName by remember { mutableStateOf("") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showSignatureErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(roundId) {
        if (roundId.isNotEmpty()) {
            viewModel.loadRound(roundId)
        }
    }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            PostRoundHeader(
                title = "Review Scores",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                uiState.round != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cards container with weight to fill available space
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Playing partner card first
                            if (uiState.round!!.playingPartnerRound != null) {
                                PostRoundCard(
                                    playerName = "${uiState.round!!.playingPartnerRound!!.golferFirstName ?: ""} ${uiState.round!!.playingPartnerRound!!.golferLastName ?: ""}".trim(),
                                    competitionType = uiState.round!!.playingPartnerRound!!.compType ?: "Competition",
                                    dailyHandicap = uiState.round!!.playingPartnerRound!!.dailyHandicap?.toString() ?: "N/A",
                                    frontNineScore = uiState.round!!.playingPartnerRound!!.holeScores
                                        .filter { it.holeNumber in 1..9 }
                                        .sumOf { it.strokes }.toString(),
                                    backNineScore = uiState.round!!.playingPartnerRound!!.holeScores
                                        .filter { it.holeNumber in 10..18 }
                                        .sumOf { it.strokes }.toString(),
                                    grandTotalStrokes = uiState.round!!.playingPartnerRound!!.holeScores
                                        .sumOf { it.strokes }.toString(),
                                    compScoreTotal = 99.toString(),

                                    signatureBase64 = playerSignatures[uiState.round!!.playingPartnerRound!!.golferId ?: ""],
                                    onSignatureClick = {
                                        currentSignaturePlayerId = uiState.round!!.playingPartnerRound!!.golferId ?: ""
                                        currentSignaturePlayerFirstName = uiState.round!!.golferFirstName ?: ""
                                        currentSignaturePlayerLastName = uiState.round!!.golferLastName ?: ""
                                        showSignatureDialog = true
                                    },
                                    backgroundColor = MSLColors.mslBlue,
                                    signerName = "${uiState.round!!.golferFirstName ?: ""} ${uiState.round!!.golferLastName ?: ""}".trim(),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Golfer card second
                            PostRoundCard(
                                playerName = "${uiState.round!!.golferFirstName ?: ""} ${uiState.round!!.golferLastName ?: ""}".trim(),
                                competitionType = uiState.round!!.compType ?: "Competition",
                                dailyHandicap = uiState.round!!.dailyHandicap?.toString() ?: "N/A",
                                frontNineScore = viewModel.calculateFrontNineScore(uiState.round!!).toString(),
                                backNineScore = viewModel.calculateBackNineScore(uiState.round!!).toString(),
                                grandTotalStrokes = viewModel.calculateGrandTotal(uiState.round!!).toString(),
                                compScoreTotal = 99.toString(),

                                signatureBase64 = playerSignatures[uiState.round!!.golferId ?: ""],
                                onSignatureClick = {
                                    currentSignaturePlayerId = uiState.round!!.golferId ?: ""
                                    currentSignaturePlayerFirstName = if (uiState.round!!.playingPartnerRound != null) {
                                        uiState.round!!.playingPartnerRound!!.golferFirstName ?: ""
                                    } else {
                                        uiState.round!!.golferFirstName ?: ""
                                    }
                                    currentSignaturePlayerLastName = if (uiState.round!!.playingPartnerRound != null) {
                                        uiState.round!!.playingPartnerRound!!.golferLastName ?: ""
                                    } else {
                                        uiState.round!!.golferLastName ?: ""
                                    }
                                    showSignatureDialog = true
                                },
                                backgroundColor = MSLColors.mslGrey,
                                signerName = if (uiState.round!!.playingPartnerRound != null) {
                                    "${uiState.round!!.playingPartnerRound!!.golferFirstName ?: ""} ${uiState.round!!.playingPartnerRound!!.golferLastName ?: ""}".trim()
                                } else {
                                    "${uiState.round!!.golferFirstName ?: ""} ${uiState.round!!.golferLastName ?: ""}".trim()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Submit button at bottom matching other screens
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(if (!uiState.isSubmitting && !uiState.isSubmitted) MSLColors.mslYellow else Color.LightGray)
                                .clickable(enabled = !uiState.isSubmitting && !uiState.isSubmitted) {
                                    // Check if both signatures are present
                                    val golferSignature = playerSignatures[uiState.round!!.golferId ?: ""]
                                    val playingPartnerSignature = if (uiState.round!!.playingPartnerRound != null) {
                                        playerSignatures[uiState.round!!.playingPartnerRound!!.golferId ?: ""]
                                    } else null
                                    
                                    val hasGolferSignature = !golferSignature.isNullOrEmpty()
                                    val hasPlayingPartnerSignature = uiState.round!!.playingPartnerRound == null || !playingPartnerSignature.isNullOrEmpty()
                                    
                                    if (hasGolferSignature && hasPlayingPartnerSignature) {
                                        showSubmitDialog = true
                                    } else {
                                        showSignatureErrorDialog = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSubmitting) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Submit Scores",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = if (uiState.isSubmitted) "Submitted" else "Submit Scores",
                                    color = if (!uiState.isSubmitting && !uiState.isSubmitted) Color.White else Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Loading overlay during MSL submission
            if (roundSubmitState.isSending) {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Submitting scores...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showSignatureDialog) {
        SignatureDialog(
            firstName = currentSignaturePlayerFirstName,
            lastName = currentSignaturePlayerLastName,
            onDismiss = { showSignatureDialog = false },
            onSignatureCaptured = { signature ->
                viewModel.updatePlayerSignature(currentSignaturePlayerId, signature)
                showSignatureDialog = false
            }
        )
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Scores") },
            text = { Text("Are you sure you want to submit ${uiState.round!!.playingPartnerRound!!.golferFirstName ?: ""}'s score?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitRound()
                        showSubmitDialog = false
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = { Text("Success") },
            text = { Text("Scores have been submitted successfully!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showSignatureErrorDialog) {
        AlertDialog(
            onDismissRequest = { showSignatureErrorDialog = false },
            title = { Text("Signatures Required") },
            text = { Text("Please sign both cards") },
            confirmButton = {
                Button(
                    onClick = { showSignatureErrorDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
