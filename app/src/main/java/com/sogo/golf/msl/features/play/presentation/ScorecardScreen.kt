package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
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
            
            GolferScorecard(
                round = round,
                mslCompetition = mslCompetition,
                onPlayingPartnerClicked = {},
                onGolferClicked = {},
                isNineHoles = isNineHoles
            )
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
