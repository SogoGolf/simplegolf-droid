package com.sogo.golf.msl.shared_components.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.domain.model.msl.MslPlayer
import com.sogo.golf.msl.domain.model.msl.MslHole

data class ScorecardData(
    val hole: String,
    val par: String,
    val golferScore: String,
    val playingPartnerScore: String,
    val golferStrokes: String,
    val playingPartnerStrokes: String
)

@Composable
fun GolferScorecard(
    golferData: MslPlayer?,
    playingPartnerData: MslPlayer?,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            golferData?.let { golfer ->
                GolferHeader(
                    golferName = "${golfer.firstName ?: ""} ${golfer.lastName ?: ""}".trim(),
                    teeName = golfer.teeName ?: "",
                    handicap = golfer.dailyHandicap.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            playingPartnerData?.let { partner ->
                GolferHeader(
                    golferName = "${partner.firstName ?: ""} ${partner.lastName ?: ""}".trim(),
                    teeName = partner.teeName ?: "",
                    handicap = partner.dailyHandicap.toString()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (golferData != null && playingPartnerData != null) {
                GolfScorecard(
                    golferData = golferData,
                    playingPartnerData = playingPartnerData
                )
            }
        }
    }
}

@Composable
private fun GolferHeader(
    golferName: String,
    teeName: String,
    handicap: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = golferName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row {
                Text(
                    text = "Tee: $teeName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Handicap: $handicap",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun GolfScorecard(
    golferData: MslPlayer,
    playingPartnerData: MslPlayer
) {
    val holes = golferData.holes.ifEmpty { playingPartnerData.holes }
    
    if (holes.isEmpty()) {
        Text(
            text = "No hole data available",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    val scorecardData = mutableListOf<ScorecardData>()
    
    scorecardData.add(
        ScorecardData(
            hole = "HOLE",
            par = "PAR",
            golferScore = "SCORE",
            playingPartnerScore = "SCORE",
            golferStrokes = "STROKES",
            playingPartnerStrokes = "STROKES"
        )
    )

    holes.forEach { hole ->
        val golferHole = golferData.holes.find { it.holeNumber == hole.holeNumber }
        val partnerHole = playingPartnerData.holes.find { it.holeNumber == hole.holeNumber }
        
        scorecardData.add(
            ScorecardData(
                hole = hole.holeNumber.toString(),
                par = hole.par.toString(),
                golferScore = golferHole?.strokes?.toString() ?: "-",
                playingPartnerScore = partnerHole?.strokes?.toString() ?: "-",
                golferStrokes = golferHole?.strokes?.toString() ?: "-",
                playingPartnerStrokes = partnerHole?.strokes?.toString() ?: "-"
            )
        )
    }

    val outHoles = holes.filter { it.holeNumber <= 9 }
    val inHoles = holes.filter { it.holeNumber > 9 }

    if (outHoles.isNotEmpty()) {
        val outPar = outHoles.sumOf { it.par }
        val golferOutScore = golferData.holes.filter { it.holeNumber <= 9 }.sumOf { it.strokes }
        val partnerOutScore = playingPartnerData.holes.filter { it.holeNumber <= 9 }.sumOf { it.strokes }
        
        scorecardData.add(
            ScorecardData(
                hole = "OUT",
                par = outPar.toString(),
                golferScore = golferOutScore.toString(),
                playingPartnerScore = partnerOutScore.toString(),
                golferStrokes = golferOutScore.toString(),
                playingPartnerStrokes = partnerOutScore.toString()
            )
        )
    }

    if (inHoles.isNotEmpty()) {
        val inPar = inHoles.sumOf { it.par }
        val golferInScore = golferData.holes.filter { it.holeNumber > 9 }.sumOf { it.strokes }
        val partnerInScore = playingPartnerData.holes.filter { it.holeNumber > 9 }.sumOf { it.strokes }
        
        scorecardData.add(
            ScorecardData(
                hole = "IN",
                par = inPar.toString(),
                golferScore = golferInScore.toString(),
                playingPartnerScore = partnerInScore.toString(),
                golferStrokes = golferInScore.toString(),
                playingPartnerStrokes = partnerInScore.toString()
            )
        )
    }

    val totalPar = holes.sumOf { it.par }
    val golferTotalScore = golferData.holes.sumOf { it.strokes }
    val partnerTotalScore = playingPartnerData.holes.sumOf { it.strokes }
    
    scorecardData.add(
        ScorecardData(
            hole = "TOTAL",
            par = totalPar.toString(),
            golferScore = golferTotalScore.toString(),
            playingPartnerScore = partnerTotalScore.toString(),
            golferStrokes = golferTotalScore.toString(),
            playingPartnerStrokes = partnerTotalScore.toString()
        )
    )

    val tableData = scorecardData.map { data ->
        listOf<@Composable () -> Unit>(
            { HoleScoreColumn(data.hole, isHeader = data.hole == "HOLE") },
            { HoleScoreColumn(data.par, isHeader = data.hole == "HOLE") },
            { HoleScoreColumn(data.golferScore, isHeader = data.hole == "HOLE") },
            { HoleScoreColumn(data.playingPartnerScore, isHeader = data.hole == "HOLE") }
        )
    }

    TableWithFixedFirstColumnSCORECARD(
        columnCount = 4,
        cellWidth = 80.dp,
        data = tableData,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun HoleScoreColumn(
    text: String,
    isHeader: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 14.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getPlayerTeeName(competitions: List<MslCompetition>, golfLinkNumber: String?): String? {
    return competitions.flatMap { it.players }
        .find { it.golfLinkNumber == golfLinkNumber }
        ?.teeName
}

fun findPlayerByGolfLinkNumber(competitions: List<MslCompetition>, golfLinkNumber: String?): MslPlayer? {
    return competitions.flatMap { it.players }
        .find { it.golfLinkNumber == golfLinkNumber }
}

@Preview(showBackground = true)
@Composable
private fun GolferScorecardPreview() {
    val sampleHoles = listOf(
        MslHole(par = 4, strokes = 5, strokeIndexes = listOf(1), distance = 350, holeNumber = 1, holeName = "Hole 1", holeAlias = "H1"),
        MslHole(par = 3, strokes = 3, strokeIndexes = listOf(2), distance = 150, holeNumber = 2, holeName = "Hole 2", holeAlias = "H2"),
        MslHole(par = 5, strokes = 6, strokeIndexes = listOf(3), distance = 500, holeNumber = 3, holeName = "Hole 3", holeAlias = "H3")
    )
    
    val golfer = MslPlayer(
        firstName = "John",
        lastName = "Doe",
        dailyHandicap = 18,
        golfLinkNumber = "12345",
        competitionName = "Test Competition",
        competitionType = "Stroke",
        teeName = "Blue Tee",
        teeColour = "Blue",
        teeColourName = "Blue",
        scoreType = "Gross",
        slopeRating = 113,
        scratchRating = 72.0,
        holes = sampleHoles
    )
    
    val partner = MslPlayer(
        firstName = "Jane",
        lastName = "Smith",
        dailyHandicap = 12,
        golfLinkNumber = "67890",
        competitionName = "Test Competition",
        competitionType = "Stroke",
        teeName = "Red Tee",
        teeColour = "Red",
        teeColourName = "Red",
        scoreType = "Gross",
        slopeRating = 113,
        scratchRating = 70.0,
        holes = sampleHoles.map { it.copy(strokes = it.strokes - 1) }
    )
    
    MaterialTheme {
        GolferScorecard(
            golferData = golfer,
            playingPartnerData = partner
        )
    }
}
