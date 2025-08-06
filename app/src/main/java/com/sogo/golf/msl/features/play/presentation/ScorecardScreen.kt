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
            GolferScorecard(
                round = round,
                mslCompetition = mslCompetition,
                onPlayingPartnerClicked = {},
                onGolferClicked = {},
                isNineHoles = false
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
