package com.sogo.golf.msl.features.sogo_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.mongodb.RoundSummary
import com.sogo.golf.msl.features.sogo_home.presentation.components.RoundSummaryCard
import com.sogo.golf.msl.ui.theme.MSLColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundsSummaryScreen(
    navController: NavController,
    viewModel: SogoGolfHomeViewModel = hiltViewModel()
) {
    val roundsSummaryState by viewModel.roundsSummaryState.collectAsState()
    val currentGolfer by viewModel.currentGolfer.collectAsState()
    val localGame by viewModel.localGame.collectAsState()
    
    // Set status bar to have light icons (white) for dark background
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? androidx.activity.ComponentActivity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }
    
    // Fetch rounds when golfer data is available
    LaunchedEffect(currentGolfer, localGame) { // initapi - fetches rounds data when golfer data becomes available
        // Wait for either currentGolfer or localGame to have a golf link number
        val golfLinkNo = currentGolfer?.golfLinkNo?.takeIf { it.isNotBlank() }
            ?: localGame?.golflinkNumber
        
        if (!golfLinkNo.isNullOrBlank()) {
            viewModel.fetchRoundsSummary()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Rounds",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MSLColors.mslBlue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                roundsSummaryState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MSLColors.mslBlue
                        )
                    }
                }
                
                roundsSummaryState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Failed to load rounds",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = roundsSummaryState.error ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color.Gray.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.fetchRoundsSummary() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MSLColors.mslBlue
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                roundsSummaryState.rounds.filter { it.isSubmitted == true }.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No rounds found",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your completed rounds will appear here",
                                fontSize = 14.sp,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(roundsSummaryState.rounds.filter { it.isSubmitted == true }) { round ->
                            RoundSummaryCard(
                                roundDate = round.roundDate,
                                clubName = round.clubName,
                                countOfHoleScores = round.countOfHoleScores,
                                score = round.score,
                                playingPartnerFirstName = round.playingPartnerGolferFirstName,
                                playingPartnerLastName = round.playingPartnerGolferLastName,
                                compType = round.compType,
                                isSubmitted = round.isSubmitted,
                                scratchRating = round.scratchRating,
                                slopeRating = round.slopeRating,
                                golfLinkHandicap = round.golfLinkHandicap,
onClick = {
                                    // Navigate to round detail screen using the round ID
                                    round.id?.let { roundId ->
                                        navController.navigate("rounddetailscreen/$roundId")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
