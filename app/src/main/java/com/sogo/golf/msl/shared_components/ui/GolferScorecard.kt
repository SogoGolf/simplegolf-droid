package com.sogo.golf.msl.shared_components.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.shared_components.ui.scorecard.TableWithFixedFirstColumnSCORECARD
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGrey

data class ScorecardData(
    val holeNumber: String,
    val meters: String,
    val index: String,
    val par: String,
    val golferStrokes: String,
    val golferScore: String,
    val partnerStrokes: String,
    val partnerScore: String
)

@Composable
fun GolferScorecard(
    round: Round?,
    mslCompetition: MslCompetition?,
    onPlayingPartnerClicked: () -> Unit,
    onGolferClicked: () -> Unit,
    isNineHoles: Boolean,
    onScorecardViewed: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    LaunchedEffect(Unit) {
        activity?.let {
            val window = it.window
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
        onScorecardViewed()
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    if (round == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No round data available",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )
        }
        return
    }

    val holeScores = round.holeScores
    val partnerHoleScores = round.playingPartnerRound?.holeScores ?: emptyList()

    val maxHoles = if (isNineHoles) 9 else 18
    val filteredHoleScores = holeScores.filter { it.holeNumber <= maxHoles }
    val filteredPartnerHoleScores = partnerHoleScores.filter { it.holeNumber <= maxHoles }

    val scorecardData = mutableListOf<ScorecardData>()

    for (holeNum in 1..maxHoles) {
        val golferHole = filteredHoleScores.find { it.holeNumber == holeNum }
        val partnerHole = filteredPartnerHoleScores.find { it.holeNumber == holeNum }

        scorecardData.add(
            ScorecardData(
                holeNumber = holeNum.toString(),
                meters = golferHole?.meters?.toString() ?: "0",
                index = "${golferHole?.index1 ?: 0}/${golferHole?.index2 ?: 0}/${golferHole?.index3 ?: "-"}",
                par = golferHole?.par?.toString() ?: "0",
                golferStrokes = golferHole?.strokes?.toString() ?: "0",
                golferScore = golferHole?.score?.toInt()?.toString() ?: "0",
                partnerStrokes = partnerHole?.strokes?.toString() ?: "0",
                partnerScore = partnerHole?.score?.toInt()?.toString() ?: "0"
            )
        )
    }

    if (isNineHoles) {
        scorecardData.add(
            ScorecardData(
                holeNumber = "OUT",
                meters = calculateOutDistance(filteredHoleScores).toString(),
                index = "",
                par = calculateOutPar(filteredHoleScores).toString(),
                golferStrokes = calculateOutStrokes(filteredHoleScores).toString(),
                golferScore = calculateOutScore(filteredHoleScores).toString(),
                partnerStrokes = calculateOutStrokes(filteredPartnerHoleScores).toString(),
                partnerScore = calculateOutScore(filteredPartnerHoleScores).toString()
            )
        )
    } else {
        scorecardData.add(
            ScorecardData(
                holeNumber = "OUT",
                meters = calculateOutDistance(filteredHoleScores).toString(),
                index = "",
                par = calculateOutPar(filteredHoleScores).toString(),
                golferStrokes = calculateOutStrokes(filteredHoleScores).toString(),
                golferScore = calculateOutScore(filteredHoleScores).toString(),
                partnerStrokes = calculateOutStrokes(filteredPartnerHoleScores).toString(),
                partnerScore = calculateOutScore(filteredPartnerHoleScores).toString()
            )
        )

        for (holeNum in 10..18) {
            val golferHole = filteredHoleScores.find { it.holeNumber == holeNum }
            val partnerHole = filteredPartnerHoleScores.find { it.holeNumber == holeNum }

            scorecardData.add(
                ScorecardData(
                    holeNumber = holeNum.toString(),
                    meters = golferHole?.meters?.toString() ?: "0",
                    index = "${golferHole?.index1 ?: 0}/${golferHole?.index2 ?: 0}/${golferHole?.index3 ?: "-"}",
                    par = golferHole?.par?.toString() ?: "0",
                    golferStrokes = golferHole?.strokes?.toString() ?: "0",
                    golferScore = golferHole?.score?.toInt()?.toString() ?: "0",
                    partnerStrokes = partnerHole?.strokes?.toString() ?: "0",
                    partnerScore = partnerHole?.score?.toInt()?.toString() ?: "0"
                )
            )
        }

        scorecardData.add(
            ScorecardData(
                holeNumber = "IN",
                meters = calculateInDistance(filteredHoleScores).toString(),
                index = "",
                par = calculateInPar(filteredHoleScores).toString(),
                golferStrokes = calculateInStrokes(filteredHoleScores).toString(),
                golferScore = calculateInScore(filteredHoleScores).toString(),
                partnerStrokes = calculateInStrokes(filteredPartnerHoleScores).toString(),
                partnerScore = calculateInScore(filteredPartnerHoleScores).toString()
            )
        )

        scorecardData.add(
            ScorecardData(
                holeNumber = "TOTAL",
                meters = calculateTotalDistance(filteredHoleScores).toString(),
                index = "",
                par = calculateTotalPar(filteredHoleScores).toString(),
                golferStrokes = calculateTotalStrokes(filteredHoleScores).toString(),
                golferScore = calculateTotalScore(filteredHoleScores).toString(),
                partnerStrokes = calculateTotalStrokes(filteredPartnerHoleScores).toString(),
                partnerScore = calculateTotalScore(filteredPartnerHoleScores).toString()
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            val golferTeeName = getTeeName(mslCompetition, round.golferGLNumber)
            val partnerTeeName = getTeeName(mslCompetition, round.playingPartnerRound?.golferGLNumber)

            GolferHeader(
                golferFirstName = round.golferFirstName ?: "",
                golferLastName = round.golferLastName ?: "",
                dailyHandicap = round.dailyHandicap?.toString() ?: "0.0",
                golfLinkHandicap = round.golfLinkHandicap?.toString() ?: "0.0",
                teeName = golferTeeName,
                onGolferClicked = onGolferClicked
            )

            Spacer(modifier = Modifier.height(8.dp))

            round.playingPartnerRound?.let { partner ->
                GolferHeader(
                    golferFirstName = partner.golferFirstName ?: "",
                    golferLastName = partner.golferLastName ?: "",
                    dailyHandicap = partner.dailyHandicap?.toString() ?: "0.0",
                    golfLinkHandicap = partner.golfLinkHandicap?.toString() ?: "0.0",
                    teeName = partnerTeeName,
                    onGolferClicked = onPlayingPartnerClicked
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            val columnCount = scorecardData.size + 1
            val outColumnIndex = scorecardData.indexOfFirst { it.holeNumber.lowercase() == "out" }
            val inColumnIndex = scorecardData.indexOfFirst { it.holeNumber.lowercase() == "in" }
            val totalColumnIndex = scorecardData.indexOfFirst { it.holeNumber.lowercase() == "total" }

            TableWithFixedFirstColumnSCORECARD(
                columnCount = columnCount,
                cellWidth = { (screenWidth * 0.10).dp },
                firstColumnWidth = { 100.dp },
                data = listOf("Hole", "Meters", "Index", "Par", "Golfer", "Score", "Partner", "Score"),
                headerCellContent = { columnIndex ->
                    if (columnIndex == 0) {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        val dataIndex = columnIndex - 1
                        if (dataIndex < scorecardData.size) {
                            val data = scorecardData[dataIndex]
                            val backgroundColor = when {
                                dataIndex == outColumnIndex || dataIndex == inColumnIndex || dataIndex == totalColumnIndex -> mslBlue
                                else -> Color.White
                            }
                            val textColor = when {
                                dataIndex == outColumnIndex || dataIndex == inColumnIndex || dataIndex == totalColumnIndex -> Color.White
                                else -> Color.Black
                            }

                            Box(
                                modifier = Modifier
                                    .background(backgroundColor)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = data.holeNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                },
                cellContent = { columnIndex, rowData ->
                    if (columnIndex == 0) {
                        Text(
                            text = rowData,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        val dataIndex = columnIndex - 1
                        if (dataIndex < scorecardData.size) {
                            val data = scorecardData[dataIndex]
                            val cellValue = when (rowData) {
                                "Hole" -> data.holeNumber
                                "Meters" -> data.meters
                                "Index" -> data.index
                                "Par" -> data.par
                                "Golfer" -> data.golferStrokes
                                "Score" -> data.golferScore
                                "Partner" -> data.partnerStrokes
                                else -> data.partnerScore
                            }

                            val backgroundColor = when {
                                dataIndex == outColumnIndex || dataIndex == inColumnIndex || dataIndex == totalColumnIndex -> mslBlue
                                else -> Color.White
                            }
                            val textColor = when {
                                dataIndex == outColumnIndex || dataIndex == inColumnIndex || dataIndex == totalColumnIndex -> Color.White
                                else -> Color.Black
                            }

                            Box(
                                modifier = Modifier
                                    .background(backgroundColor)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cellValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun getTeeName(mslCompetition: MslCompetition?, golfLinkNumber: String?): String {
    return mslCompetition?.players?.find { 
        it.golfLinkNumber == golfLinkNumber 
    }?.teeName ?: "--"
}

@Composable
fun GolferHeader(
    golferFirstName: String,
    golferLastName: String,
    dailyHandicap: String,
    golfLinkHandicap: String,
    teeName: String,
    onGolferClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(mslGrey)
            .clickable { onGolferClicked() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$golferFirstName $golferLastName",
                style = MaterialTheme.typography.titleMedium,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Daily HC: $dailyHandicap | GL HC: $golfLinkHandicap",
                style = MaterialTheme.typography.bodySmall,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = teeName,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tee",
                style = MaterialTheme.typography.bodySmall,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

fun calculateOutScore(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 1..9 }.sumOf { it.score.toInt() }
}

fun calculateOutDistance(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 1..9 }.sumOf { it.meters }
}

fun calculateOutPar(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 1..9 }.sumOf { it.par }
}

fun calculateOutStrokes(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 1..9 }.sumOf { it.strokes }
}

fun calculateInScore(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 10..18 }.sumOf { it.score.toInt() }
}

fun calculateInDistance(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 10..18 }.sumOf { it.meters }
}

fun calculateInPar(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 10..18 }.sumOf { it.par }
}

fun calculateInStrokes(holeScores: List<HoleScore>): Int {
    return holeScores.filter { it.holeNumber in 10..18 }.sumOf { it.strokes }
}

fun calculateTotalScore(holeScores: List<HoleScore>): Int {
    return calculateOutScore(holeScores) + calculateInScore(holeScores)
}

fun calculateTotalDistance(holeScores: List<HoleScore>): Int {
    return calculateOutDistance(holeScores) + calculateInDistance(holeScores)
}

fun calculateTotalPar(holeScores: List<HoleScore>): Int {
    return calculateOutPar(holeScores) + calculateInPar(holeScores)
}

fun calculateTotalStrokes(holeScores: List<HoleScore>): Int {
    return calculateOutStrokes(holeScores) + calculateInStrokes(holeScores)
}

@Preview(showBackground = true)
@Composable
fun GolferScorecardPreview() {
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

    GolferScorecard(
        round = round,
        mslCompetition = null,
        onPlayingPartnerClicked = {},
        onGolferClicked = {},
        isNineHoles = false
    )
}
