package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack
import com.sogo.golf.msl.ui.theme.MSLColors.mslGrey

enum class PlayerType {
    GOLFER,
    PLAYING_PARTNER
}

@Composable
fun VerticalScorecardForSharing(
    round: Round,
    mslCompetition: MslCompetition?,
    selectedPlayer: PlayerType = PlayerType.GOLFER,
    isNineHoles: Boolean,
    modifier: Modifier = Modifier
) {
    val playerHoleScores = when (selectedPlayer) {
        PlayerType.GOLFER -> round.holeScores
        PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.holeScores ?: emptyList()
    }
    
    val playerFirstName = when (selectedPlayer) {
        PlayerType.GOLFER -> round.golferFirstName ?: ""
        PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.golferFirstName ?: ""
    }
    
    val playerLastName = when (selectedPlayer) {
        PlayerType.GOLFER -> round.golferLastName ?: ""
        PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.golferLastName ?: ""
    }
    
    val dailyHandicap = when (selectedPlayer) {
        PlayerType.GOLFER -> round.dailyHandicap?.toString() ?: "--"
        PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.dailyHandicap?.toString() ?: "--"
    }
    
    val teeName = getTeeName(mslCompetition, when (selectedPlayer) {
        PlayerType.GOLFER -> round.golferGLNumber
        PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.golferGLNumber
    })
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Player Header
        VerticalScorecardHeader(
            playerName = "$playerFirstName $playerLastName",
            dailyHandicap = dailyHandicap,
            teeName = teeName,
            courseName = round.clubName ?: "Golf Course"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Column Headers
        VerticalScorecardColumnHeaders()
        
        // Front 9 Holes
        playerHoleScores.filter { it.holeNumber in 1..9 }.forEach { holeScore ->
            VerticalScorecardHoleRow(holeScore = holeScore)
        }
        
        // OUT Summary
        VerticalScorecardSummaryRow(
            label = "OUT",
            holeScores = playerHoleScores.filter { it.holeNumber in 1..9 }
        )
        
        // Back 9 Holes (if 18-hole round)
        if (!isNineHoles) {
            Spacer(modifier = Modifier.height(8.dp))
            
            playerHoleScores.filter { it.holeNumber in 10..18 }.forEach { holeScore ->
                VerticalScorecardHoleRow(holeScore = holeScore)
            }
            
            // IN Summary
            VerticalScorecardSummaryRow(
                label = "IN",
                holeScores = playerHoleScores.filter { it.holeNumber in 10..18 }
            )
        }
        
        // TOTAL Summary
        VerticalScorecardSummaryRow(
            label = "TOTAL",
            holeScores = playerHoleScores,
            isTotal = true
        )
    }
}

@Composable
private fun VerticalScorecardHeader(
    playerName: String,
    dailyHandicap: String,
    teeName: String,
    courseName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(mslBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = playerName,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = courseName,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Daily HC: $dailyHandicap",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Text(
                text = "$teeName Tee",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun VerticalScorecardColumnHeaders() {
    Surface(
        border = BorderStroke(1.dp, Color.LightGray),
        color = mslGrey.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HeaderCell("Hole", modifier = Modifier.weight(1f))
            HeaderCell("Meters", modifier = Modifier.weight(1.2f))
            HeaderCell("Index", modifier = Modifier.weight(1f))
            HeaderCell("Par", modifier = Modifier.weight(1f))
            HeaderCell("Strokes", modifier = Modifier.weight(1.2f))
            HeaderCell("Score", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = mslBlack,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VerticalScorecardHoleRow(holeScore: HoleScore) {
    Surface(
        border = BorderStroke(0.5.dp, Color.LightGray),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DataCell(holeScore.holeNumber.toString(), modifier = Modifier.weight(1f))
            DataCell(holeScore.meters.toString(), modifier = Modifier.weight(1.2f))
            DataCell(formatIndex(holeScore.index1, holeScore.index2), modifier = Modifier.weight(1f))
            DataCell(holeScore.par.toString(), modifier = Modifier.weight(1f))
            DataCell(holeScore.strokes.toString(), modifier = Modifier.weight(1.2f))
            DataCell(holeScore.score.toInt().toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun VerticalScorecardSummaryRow(
    label: String,
    holeScores: List<HoleScore>,
    isTotal: Boolean = false
) {
    val backgroundColor = if (isTotal) mslBlue else mslGrey.copy(alpha = 0.6f)
    val textColor = Color.White
    
    Surface(
        border = BorderStroke(1.dp, Color.LightGray),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryCell(label, modifier = Modifier.weight(1f), textColor = textColor, isBold = true)
            SummaryCell(holeScores.sumOf { it.meters }.toString(), modifier = Modifier.weight(1.2f), textColor = textColor)
            SummaryCell("--", modifier = Modifier.weight(1f), textColor = textColor)
            SummaryCell(holeScores.sumOf { it.par }.toString(), modifier = Modifier.weight(1f), textColor = textColor)
            SummaryCell(holeScores.sumOf { it.strokes }.toString(), modifier = Modifier.weight(1.2f), textColor = textColor)
            SummaryCell(holeScores.sumOf { it.score.toInt() }.toString(), modifier = Modifier.weight(1f), textColor = textColor, isBold = true)
        }
    }
}

@Composable
private fun DataCell(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            color = mslBlack,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryCell(text: String, modifier: Modifier = Modifier, textColor: Color, isBold: Boolean = false) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatIndex(index1: Int, index2: Int): String {
    return if (index2 > 0) "$index1/$index2" else index1.toString()
}

private fun getTeeName(mslCompetition: MslCompetition?, golfLinkNumber: String?): String {
    return mslCompetition?.players?.find { 
        it.golfLinkNumber == golfLinkNumber 
    }?.teeName ?: "--"
}
