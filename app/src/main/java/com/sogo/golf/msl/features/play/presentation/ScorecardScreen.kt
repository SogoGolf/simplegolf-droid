package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.features.play.presentation.components.GolfScorecard

@Composable
fun ScorecardScreen(
    round: Round? = null,
    mslPlayingPartnerTeeName: String? = null,
    mslGolferTeeName: String? = null,
    onPlayingPartnerClicked: () -> Unit = {},
    onGolferClicked: () -> Unit = {},
    isNineHoles: Boolean = false,
    onScorecardViewed: () -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape && round != null) {
        // Show scorecard in landscape mode
        GolfScorecard(
            round = round,
            mslPlayingPartnerTeeName = mslPlayingPartnerTeeName,
            mslGolferTeeName = mslGolferTeeName,
            onPlayingPartnerClicked = onPlayingPartnerClicked,
            onGolferClicked = onGolferClicked,
            isNineHoles = isNineHoles,
            onScorecardViewed = onScorecardViewed
        )
    } else if (isLandscape && round == null) {
        // Show placeholder when no round data
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
                    text = "No Round Data Available",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black
                )
            }
        }
    }
    // Don't render anything in portrait mode
}

@Preview(showBackground = true)
@Composable
fun ScorecardScreenPreview() {
    val holeScores = listOf(
        HoleScore(
            holeNumber = 1,
            meters = 450,
            index1 = 1,
            index2 = 19,
            par = 4,
            strokes = 5,
            score = 1f
        ),
        HoleScore(
            holeNumber = 2,
            meters = 315,
            index1 = 2,
            index2 = 20,
            par = 3,
            strokes = 4,
            score = 0f
        )
    )

    val partnerRound = PlayingPartnerRound(
        golferFirstName = "Julius",
        golferLastName = "Wire",
        dailyHandicap = 16.2f,
        holeScores = holeScores
    )

    val round = Round(
        golferFirstName = "Peter",
        golferLastName = "Farrier",
        dailyHandicap = 28.0,
        playingPartnerRound = partnerRound,
        holeScores = holeScores
    )

    ScorecardScreen(
        round = round,
        mslPlayingPartnerTeeName = "Test Tee",
        mslGolferTeeName = "Golfer Tee",
        isNineHoles = false
    )
}
