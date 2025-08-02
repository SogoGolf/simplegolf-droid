package com.sogo.golf.msl.features.review_scores.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    
    var showSignatureDialog by remember { mutableStateOf(false) }
    var currentSignaturePlayerId by remember { mutableStateOf("") }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            PostRoundCard(
                                playerName = "${uiState.round!!.golferFirstName ?: ""} ${uiState.round!!.golferLastName ?: ""}".trim(),
                                competitionType = uiState.round!!.compType ?: "Competition",
                                dailyHandicap = uiState.round!!.dailyHandicap?.toString() ?: "N/A",
                                frontNineScore = viewModel.calculateFrontNineScore(uiState.round!!).toString(),
                                backNineScore = viewModel.calculateBackNineScore(uiState.round!!).toString(),
                                grandTotal = viewModel.calculateGrandTotal(uiState.round!!).toString(),
                                signatureBase64 = playerSignatures[uiState.round!!.golferId ?: ""],
                                onSignatureClick = {
                                    currentSignaturePlayerId = uiState.round!!.golferId ?: ""
                                    showSignatureDialog = true
                                }
                            )
                        }

                        if (uiState.round!!.playingPartnerRound != null) {
                            item {
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
                                    grandTotal = uiState.round!!.playingPartnerRound!!.holeScores
                                        .sumOf { it.strokes }.toString(),
                                    signatureBase64 = playerSignatures[uiState.round!!.playingPartnerRound!!.golferId ?: ""],
                                    onSignatureClick = {
                                        currentSignaturePlayerId = uiState.round!!.playingPartnerRound!!.golferId ?: ""
                                        showSignatureDialog = true
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showSubmitDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !uiState.isSubmitting && !uiState.isSubmitted,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (uiState.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = if (uiState.isSubmitted) "Submitted" else "Submit Scores",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    SignatureDialog(
        isVisible = showSignatureDialog,
        onDismiss = { showSignatureDialog = false },
        onSignatureSaved = { signature ->
            viewModel.updatePlayerSignature(currentSignaturePlayerId, signature)
            showSignatureDialog = false
        },
        onClearSignature = {
            viewModel.clearPlayerSignature(currentSignaturePlayerId)
        }
    )

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Scores") },
            text = { Text("Are you sure you want to submit these scores? This action cannot be undone.") },
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
}
