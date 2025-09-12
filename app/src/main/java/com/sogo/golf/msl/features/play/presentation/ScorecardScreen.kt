package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.shared_components.ui.GolferScorecard

@Composable
fun ScorecardScreen(
    round: Round? = null,
    mslCompetition: MslCompetition? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val sharingViewModel: ScorecardSharingViewModel = hiltViewModel()
    val sharingState by sharingViewModel.state.collectAsState()

    if (isLandscape) {
        if (round != null) {
            // Determine if this is a 9-hole round by checking the total number of unique holes
            val allHoles = (round.holeScores.map { it.holeNumber } + 
                           (round.playingPartnerRound?.holeScores?.map { it.holeNumber } ?: emptyList())).toSet()
            val isNineHoles = allHoles.size <= 9
            
            // DEBUG: Log scorecard detection
            android.util.Log.d("ScorecardScreen", "=== SCORECARD DETECTION DEBUG ===")
            android.util.Log.d("ScorecardScreen", "round.holeScores.size: ${round.holeScores.size}")
            android.util.Log.d("ScorecardScreen", "round.holeScores hole numbers: ${round.holeScores.map { it.holeNumber }}")
            android.util.Log.d("ScorecardScreen", "partner hole scores: ${round.playingPartnerRound?.holeScores?.map { it.holeNumber } ?: "none"}")
            android.util.Log.d("ScorecardScreen", "allHoles: $allHoles")
            android.util.Log.d("ScorecardScreen", "allHoles.size: ${allHoles.size}")
            android.util.Log.d("ScorecardScreen", "isNineHoles: $isNineHoles")
            
            Box(modifier = Modifier.fillMaxSize()) {
                GolferScorecard(
                    round = round,
                    mslCompetition = mslCompetition,
                    onPlayingPartnerClicked = {},
                    onGolferClicked = {},
                    isNineHoles = isNineHoles
                )

                // Share button overlay (top-end)
                IconButton(
                    onClick = {
                        sharingViewModel.shareScorecard(
                            context = context,
                            round = round,
                            mslCompetition = mslCompetition,
                            isNineHoles = isNineHoles
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(
                            color = Color(0x80000000),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share scorecard",
                        tint = Color.White
                    )
                }

                // Optional simple progress indicator while generating image
                if (sharingState.isGeneratingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No round data available",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
