package com.sogo.golf.msl.features.play.presentation.components

import android.app.Activity
import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.LifecycleOwner
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.features.play.presentation.components.scorecard.TableWithFixedFirstColumnSCORECARD
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGrey

// New class for rendering the scorecard data
data class ScorecardData(
    val holeNumber: String,
    val distance: String,
    val index: String,
    val par: String,
    val strokes: String,
    val score: String
)

@Composable
fun GolfScorecard(
    round: Round, // Data model for the round
    mslPlayingPartnerTeeName: String?, // Name of the playing partner's tee1
    mslGolferTeeName: String?, // Name of the golfer's tee
    onPlayingPartnerClicked: () -> Unit,
    onGolferClicked: () -> Unit,
    isNineHoles: Boolean, // Boolean flag to determine 9 holes or 18 holes round
    onScorecardViewed: () -> Unit = {} // Callback for analytics tracking when scorecard is viewed
) {
    val context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val screenWidth = LocalConfiguration.current.screenWidthDp

    val view = LocalView.current
    val activity = view.context as? Activity
    val orientation = LocalConfiguration.current.orientation

    DisposableEffect(activity, orientation) {
        val window = activity?.window
        val controller = window?.let { getInsetsController(it, window.decorView) }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            controller?.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // Track scorecard viewing when orientation changes to landscape
            onScorecardViewed()
        } else {
            controller?.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        }
        onDispose {
            controller?.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        }
    }

    val isPlayingPartnerEnabled = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Header for Playing Partner and Golfer - Top left and right sections
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            // Playing Partner Header (Left Side - Blue Background)
            round.playingPartnerRound?.let {
                GolferHeader(
                    golferFirstName = it.golferFirstName ?: "--",
                    golferLastName = it.golferLastName ?: "--",
                    dailyHandicap = it.dailyHandicap?.toInt() ?: 0,
                    gaHandicap = it.golfLinkHandicap ?: 0f,
                    teeColor = it.teeColor?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            java.util.Locale.ROOT
                        ) else it.toString()
                    } ?: "",
                    courseName = mslPlayingPartnerTeeName ?: "--",
                    slope =  it.slopeRating ?: 0f,
                    scratch = it.scratchRating ?: 0f,
                    par = it.holeScores.sumOf { it.par },
                    onGolferClicked = {
                        isPlayingPartnerEnabled.value = true
                    },
                    backgroundColor = if (isPlayingPartnerEnabled.value) mslBlue else Color.Gray.copy(alpha = 0.4f),
                    textOpacity = if (isPlayingPartnerEnabled.value) 1f else 0.5f,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isPlayingPartnerEnabled.value = true
                        }
                )
            }

            // Golfer Header (Right Side - Green Background)
            GolferHeader(
                golferFirstName = round.golferFirstName ?: "--",
                golferLastName = round.golferLastName ?: "--",
                dailyHandicap = round.dailyHandicap?.toInt() ?: 0,
                gaHandicap = round.golfLinkHandicap?.toFloat() ?: 0f,
                teeColor = round.teeColor?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        java.util.Locale.ROOT
                    ) else it.toString()
                } ?: "",
                courseName = mslGolferTeeName ?: "--",
                slope =  round.slopeRating ?: 0f,
                scratch = round.scratchRating ?: 0f,
                par = round.holeScores.sumOf { it.par },
                onGolferClicked = {
                    isPlayingPartnerEnabled.value = false
                },
                backgroundColor = if (isPlayingPartnerEnabled.value) Color.Gray.copy(alpha = 0.4f) else mslGrey,
                textOpacity = if (isPlayingPartnerEnabled.value) 0.5f else 1f,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        isPlayingPartnerEnabled.value = false
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {

            val useTheseHoleScores = if (isPlayingPartnerEnabled.value) round.playingPartnerRound?.holeScores ?: round.holeScores else round.holeScores

            val scorecardData = mutableListOf<ScorecardData>().apply {
                // Add hole scores
                useTheseHoleScores.slice(0..8).forEach { hole ->
                    add(
                        ScorecardData(
                            holeNumber = "${hole.holeNumber}",
                            distance = hole.meters.toString(),
                            index = "${hole.index1}/${hole.index2}/${hole.index3 ?: "-"}",
                            par = hole.par.toString(),
                            strokes = hole.strokes.toString(),
                            score = hole.score.toInt().toString()
                        )
                    )
                }
                // Add OUT
                if (useTheseHoleScores.count() == 18 || (isNineHoles && useTheseHoleScores.first().holeNumber == 1)) {
                    add(
                        ScorecardData(
                            holeNumber = "OUT",
                            distance = useTheseHoleScores.take(9).sumOf { it.meters }.toString(),
                            index = "",
                            par = useTheseHoleScores.take(9).sumOf { it.par }.toString(),
                            strokes = useTheseHoleScores.take(9).sumOf { it.strokes }.toString(),
                            score = useTheseHoleScores.take(9).sumOf { it.score.toInt() }.toString()
                        )
                    )
                }
                // Add holes 10-18
                if (useTheseHoleScores.count() == 18 || (isNineHoles && useTheseHoleScores.first().holeNumber == 10)) {
                    useTheseHoleScores.slice(9..17).forEach { hole ->
                        add(
                            ScorecardData(
                                holeNumber = "${hole.holeNumber}",
                                distance = hole.meters.toString(),
                                index = "${hole.index1}/${hole.index2}/${hole.index3 ?: "-"}",
                                par = hole.par.toString(),
                                strokes = hole.strokes.toString(),
                                score = hole.score.toInt().toString()
                            )
                        )
                    }
                }
                // Add IN
                if (useTheseHoleScores.count() == 18 || (isNineHoles && useTheseHoleScores.first().holeNumber == 10)) {
                    add(
                        ScorecardData(
                            holeNumber = "IN",
                            distance = useTheseHoleScores.slice(9..17).sumOf { it.meters }.toString(),
                            index = "",
                            par = useTheseHoleScores.slice(9..17).sumOf { it.par }.toString(),
                            strokes = useTheseHoleScores.slice(9..17).sumOf { it.strokes }.toString(),
                            score = useTheseHoleScores.slice(9..17).sumOf { it.score.toInt() }.toString()
                        )
                    )
                }
                // Add TOTAL
                add(
                    ScorecardData(
                        holeNumber = "TOTAL",
                        distance = useTheseHoleScores.sumOf { it.meters }.toString(),
                        index = "",
                        par = useTheseHoleScores.sumOf { it.par }.toString(),
                        strokes = useTheseHoleScores.sumOf { it.strokes }.toString(),
                        score = useTheseHoleScores.sumOf { it.score.toInt() }.toString()
                    )
                )
            }

            val reformattedData = listOf(
                "Distance" to scorecardData.map { it.distance },
                "Index" to scorecardData.map { it.index },
                "Par" to scorecardData.map { it.par },
                "Strokes" to scorecardData.map { it.strokes },
                (if (round.compType?.lowercase() == "stroke") "To Par" else "Score") to scorecardData.map { it.score }
            )

            val columnCount = scorecardData.size + 1

            val outColumnIndex = scorecardData.indexOfFirst { it.holeNumber.lowercase() == "out" }
            val inColumnIndex = scorecardData.indexOfFirst { it.holeNumber.lowercase() == "in" }
            val totalColumnIndex = scorecardData.indexOfFirst { it.holeNumber.lowercase() == "total" }

            TableWithFixedFirstColumnSCORECARD(
                columnCount = columnCount,
                cellWidth = { (screenWidth * 0.10).dp },
                firstColumnWidth = { 100.dp },
                data = reformattedData,
                headerCellContent = { index ->
                    if (index == 0) {
                        Text("Hole", Modifier
                            .padding(10.dp),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            color = mslBlue,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                    } else {
                        Text(scorecardData.getOrNull(index - 1)?.holeNumber ?: "", Modifier
                            .padding(10.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = mslBlue,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,

                        )
                    }
                },
                cellContent = { columnIndex, item ->
                    when (columnIndex) {
                        0 -> Text(
                            item.first,
                            Modifier.padding(10.dp).fillMaxWidth(),
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            color = mslBlue,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                        outColumnIndex + 1,
                        inColumnIndex + 1,
                        totalColumnIndex + 1 -> Text(
                            item.second.getOrNull(columnIndex - 1) ?: "",
                            Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = mslBlue,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                        else -> Text(
                            item.second.getOrNull(columnIndex - 1) ?: "",
                            Modifier.padding(10.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    }
                }
            )

            // DO NOT DELETE THIS !!!!!!!!!

        }
    }
}

// Header for golfer information (clickable)
@Composable
fun GolferHeader(
    golferFirstName: String,
    golferLastName: String,
    dailyHandicap: Int,
    gaHandicap: Float,
    teeColor: String,
    courseName: String,
    slope: Float,
    scratch: Float,
    par: Int,
    onGolferClicked: () -> Unit,
    backgroundColor: Color,
    textOpacity: Float,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .clickable { onGolferClicked() }
            .background(backgroundColor)
            .padding(16.dp),

    ) {

        Text(
            text = "Tee Color: $teeColor",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = Color.White.copy(alpha = textOpacity)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$golferFirstName $golferLastName",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = Color.White.copy(alpha = textOpacity),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "GA Handicap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = Color.White.copy(alpha = textOpacity)
                    )
                    Text(
                        text = "$gaHandicap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = Color.White.copy(alpha = textOpacity)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Daily Handicap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = Color.White.copy(alpha = textOpacity)
                    )
                    Text(
                        text = "$dailyHandicap",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        color = Color.White.copy(alpha = textOpacity)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = Color.White.copy(alpha = textOpacity)
                )
                Text(
                    text = "Course",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    color = Color.White.copy(alpha = textOpacity)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$slope",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = Color.White.copy(alpha = textOpacity)
                        )
                        Text(
                            text = "Slope",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = Color.White.copy(alpha = textOpacity)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$scratch",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = Color.White.copy(alpha = textOpacity)
                        )
                        Text(
                            text = "Scratch",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = Color.White.copy(alpha = textOpacity)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$par",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = Color.White.copy(alpha = textOpacity)
                        )
                        Text(
                            text = "Par",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = Color.White.copy(alpha = textOpacity)
                        )
                    }
                }
            }
        }
    }
}

// Hole Score Column
@Composable
fun HoleScoreColumn(hole: HoleScore) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = hole.holeNumber.toString(), style = MaterialTheme.typography.bodyLarge, fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        Text(text = hole.meters.toString(), fontSize = MaterialTheme.typography.bodySmall.fontSize,)
        Text(text = "${hole.index1}/${hole.index2}/${hole.index3 ?: "-"}", fontSize = MaterialTheme.typography.bodySmall.fontSize,)
        Text(text = hole.par.toString(), fontSize = MaterialTheme.typography.bodySmall.fontSize,)
        Text(text = hole.strokes.toString(), fontSize = MaterialTheme.typography.bodySmall.fontSize,)
        Text(text = hole.score.toString(), fontSize = MaterialTheme.typography.bodySmall.fontSize,)
    }
}

// Functions to calculate OUT, IN, and TOTAL scores
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

// Preview Function
@Preview(showBackground = true)
@Composable
fun GolfScorecardPreview() {
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
        // Add more holes...
    )

    // Create a placeholder instance of PlayingPartnerRound
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

    GolfScorecard(
        round = round,
        mslPlayingPartnerTeeName = "Test Tee",
        mslGolferTeeName = "Golfer Tee",
        onPlayingPartnerClicked = {},
        onGolferClicked = {},
        isNineHoles = false // Example for 18 holes round
    )
}

@Composable
fun LazyHorizontalGridWithFixedFirstColumn() {
    val itemsList = (1..126).toList() // 18 columns x 7 rows = 126 items
    val firstColumnItems = itemsList.chunked(18).map { it.first() }
    val remainingItems = itemsList.chunked(18).flatMap { it.drop(1) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Fixed First Column
        Column(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight()
                .background(Color.Gray),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            firstColumnItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .height(19.dp)
                        .fillMaxWidth()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.toString())
                }
            }
        }

        // Scrollable Grid for Remaining Items
        LazyRow(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            items(17) { columnIndex ->
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (rowIndex in 0 until 7) {
                        val itemIndex = columnIndex + rowIndex * 17
                        if (itemIndex < remainingItems.size) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = remainingItems[itemIndex].toString(), fontSize = MaterialTheme.typography.bodySmall.fontSize )
                            }
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Divider(thickness = 1.dp, color = Color.Gray)
}
