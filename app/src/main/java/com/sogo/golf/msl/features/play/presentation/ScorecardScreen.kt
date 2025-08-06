package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.shared_components.ui.components.GolferScorecard
import com.sogo.golf.msl.shared_components.ui.components.findPlayerByGolfLinkNumber
import com.sogo.golf.msl.domain.model.msl.MslCompetition

@Composable
fun ScorecardScreen(
    competitions: List<MslCompetition> = emptyList(),
    golferGolfLinkNumber: String? = null,
    playingPartnerGolfLinkNumber: String? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        val golferData = findPlayerByGolfLinkNumber(competitions, golferGolfLinkNumber)
        val playingPartnerData = findPlayerByGolfLinkNumber(competitions, playingPartnerGolfLinkNumber)

        if (golferData != null || playingPartnerData != null) {
            GolferScorecard(
                golferData = golferData,
                playingPartnerData = playingPartnerData,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No scorecard data available",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
