package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.ui.theme.MSLColors

@Composable
fun GoToHole(
    holeNumber: Int,
    holeScore: HoleScore?,
    holeScorePlayingPartner: HoleScore?,
    onHoleClick: (Int) -> Unit
){
    val topHalfColor = when {
        holeScorePlayingPartner?.isBallPickedUp == true -> Color.Red
        holeScorePlayingPartner?.strokes != null && holeScorePlayingPartner.strokes > 0 -> Color.Gray
        else -> Color.White
    }

    val bottomHalfColor = when {
        holeScore?.isBallPickedUp == true -> Color.Red
        holeScore?.strokes != null && holeScore.strokes > 0 -> Color.Gray
        else -> Color.White
    }

    val textColor = when {
        topHalfColor == Color.White && bottomHalfColor == Color.White -> Color.Black
        topHalfColor == Color.Gray && bottomHalfColor == Color.Gray -> Color.White
        topHalfColor == Color.Red && bottomHalfColor == Color.Red -> Color.White
        topHalfColor == Color.White && bottomHalfColor == Color.Gray -> Color.Black
        topHalfColor == Color.Gray && bottomHalfColor == Color.White -> Color.Black
        topHalfColor == Color.White && bottomHalfColor == Color.Red -> Color.Black
        topHalfColor == Color.Red && bottomHalfColor == Color.White -> Color.Black
        topHalfColor == Color.Gray && bottomHalfColor == Color.Red -> Color.White
        topHalfColor == Color.Red && bottomHalfColor == Color.Gray -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .border(1.dp, Color.Gray, CircleShape)
            .clickable { onHoleClick(holeNumber) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(topHalfColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(bottomHalfColor)
            )
        }

        Text(
            text = holeNumber.toString(),
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
