package com.sogo.golf.msl.shared_components.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.PlayingPartnerRound
import com.sogo.golf.msl.domain.model.StrokeIndexEntry
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.shared_components.ui.scorecard.TableWithFixedFirstColumnSCORECARD
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGrey

data class ScorecardData(
    val holeNumber: String,
    val meters: String,
    val index: String,
    val indexValues: List<Int> = emptyList(),
    val par: String,
    val strokes: String,
    val score: String
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

    // Tab state management
    val selectedTab = remember { mutableStateOf("golfer") } // "golfer" or "partner"
    
    val holeScores = round.holeScores
    val partnerHoleScores = round.playingPartnerRound?.holeScores ?: emptyList()

    // For 18-hole rounds, show all holes. For 9-hole rounds, show all available holes (don't filter)
    val filteredHoleScores = if (isNineHoles) holeScores else holeScores.filter { it.holeNumber <= 18 }
    val filteredPartnerHoleScores = if (isNineHoles) partnerHoleScores else partnerHoleScores.filter { it.holeNumber <= 18 }

    // Get active player's data based on selected tab
    val activeHoleScores = if (selectedTab.value == "golfer") filteredHoleScores else filteredPartnerHoleScores

    // Derive DQ state: stroke round + any hole picked up = disqualified
    val isGolferDQ = round.compType.equals("stroke", ignoreCase = true)
        && holeScores.any { it.isBallPickedUp == true }
    val isPartnerDQ = round.playingPartnerRound?.compType.equals("stroke", ignoreCase = true)
        && partnerHoleScores.any { it.isBallPickedUp == true }
    val isActiveDQ = if (selectedTab.value == "golfer") isGolferDQ else isPartnerDQ
    
    // Create column data for the grid
    val columnData = mutableListOf<ScorecardData>()

    if (!isNineHoles) {
        // 18-hole round: Show holes 1-9, OUT, holes 10-18, IN, TOTAL
        
        // Add holes 1-9
        for (holeNum in 1..9) {
            val activeHole = activeHoleScores.find { it.holeNumber == holeNum }
            val idxValues = activeHole?.let { getIndexValues(it) } ?: emptyList()
            columnData.add(
                ScorecardData(
                    holeNumber = holeNum.toString(),
                    meters = activeHole?.meters?.toString() ?: "0",
                    index = idxValues.joinToString("/").ifEmpty { "-" },
                    indexValues = idxValues,
                    par = activeHole?.par?.toString() ?: "0",
                    strokes = activeHole?.strokes?.toString() ?: "0",
                    score = activeHole?.score?.toInt()?.toString() ?: "0"
                )
            )
        }

        // Add OUT column after hole 9
        columnData.add(
            ScorecardData(
                holeNumber = "OUT",
                meters = calculateOutDistance(activeHoleScores).toString(),
                index = "",
                par = calculateOutPar(activeHoleScores).toString(),
                strokes = calculateOutStrokes(activeHoleScores).toString(),
                score = calculateOutScore(activeHoleScores).toString()
            )
        )

        // Add holes 10-18
        for (holeNum in 10..18) {
            val activeHole = activeHoleScores.find { it.holeNumber == holeNum }
            val idxValues = activeHole?.let { getIndexValues(it) } ?: emptyList()
            columnData.add(
                ScorecardData(
                    holeNumber = holeNum.toString(),
                    meters = activeHole?.meters?.toString() ?: "0",
                    index = idxValues.joinToString("/").ifEmpty { "-" },
                    indexValues = idxValues,
                    par = activeHole?.par?.toString() ?: "0",
                    strokes = activeHole?.strokes?.toString() ?: "0",
                    score = activeHole?.score?.toInt()?.toString() ?: "0"
                )
            )
        }

        // Add IN column after hole 18
        columnData.add(
            ScorecardData(
                holeNumber = "IN",
                meters = calculateInDistance(activeHoleScores).toString(),
                index = "",
                par = calculateInPar(activeHoleScores).toString(),
                strokes = calculateInStrokes(activeHoleScores).toString(),
                score = calculateInScore(activeHoleScores).toString()
            )
        )

        // Add TOTAL column at the end
        columnData.add(
            ScorecardData(
                holeNumber = "TOTAL",
                meters = calculateTotalDistance(activeHoleScores).toString(),
                index = "",
                par = calculateTotalPar(activeHoleScores).toString(),
                strokes = calculateTotalStrokes(activeHoleScores).toString(),
                score = calculateTotalScore(activeHoleScores).toString()
            )
        )
    } else {
        // 9-hole round: Show only the holes that were actually played
        
        // DEBUG: Log what hole scores are available
        android.util.Log.d("GolferScorecard", "=== 9-HOLE SCORECARD DEBUG ===")
        android.util.Log.d("GolferScorecard", "activeHoleScores.size: ${activeHoleScores.size}")
        android.util.Log.d("GolferScorecard", "activeHoleScores: ${activeHoleScores.map { "Hole ${it.holeNumber}: ${it.strokes} strokes" }}")
        
        // Get the actual holes played and sort them
        val holesPlayed = activeHoleScores.map { it.holeNumber }.sorted()
        android.util.Log.d("GolferScorecard", "holesPlayed: $holesPlayed")
        
        // Add each hole that was played
        for (holeNum in holesPlayed) {
            val activeHole = activeHoleScores.find { it.holeNumber == holeNum }
            if (activeHole != null) {
                val idxValues = getIndexValues(activeHole)
                columnData.add(
                    ScorecardData(
                        holeNumber = holeNum.toString(),
                        meters = activeHole.meters.toString(),
                        index = idxValues.joinToString("/").ifEmpty { "-" },
                        indexValues = idxValues,
                        par = activeHole.par.toString(),
                        strokes = activeHole.strokes.toString(),
                        score = activeHole.score.toInt().toString()
                    )
                )
            }
        }
        
        // Add subtotal column - use appropriate name based on holes played
        val subtotalName = when {
            holesPlayed.all { it in 1..9 } -> "OUT"
            holesPlayed.all { it in 10..18 } -> "IN" 
            else -> "SUBTOTAL"
        }
        
        columnData.add(
            ScorecardData(
                holeNumber = subtotalName,
                meters = activeHoleScores.sumOf { it.meters }.toString(),
                index = "",
                par = activeHoleScores.sumOf { it.par }.toString(),
                strokes = activeHoleScores.sumOf { it.strokes }.toString(),
                score = activeHoleScores.sumOf { it.score.toInt() }.toString()
            )
        )

        // Add TOTAL column at the end for 9-hole rounds (same as subtotal)
        columnData.add(
            ScorecardData(
                holeNumber = "TOTAL",
                meters = activeHoleScores.sumOf { it.meters }.toString(),
                index = "",
                par = activeHoleScores.sumOf { it.par }.toString(),
                strokes = activeHoleScores.sumOf { it.strokes }.toString(),
                score = activeHoleScores.sumOf { it.score.toInt() }.toString()
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            var tabsHeight by remember { mutableStateOf(80.dp) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                val golferTeeName = getTeeName(mslCompetition, round.golferGLNumber)
                val partnerTeeName = getTeeName(mslCompetition, round.playingPartnerRound?.golferGLNumber)

                // Tab Headers (measure height)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            tabsHeight = with(density) { coords.size.height.toDp() }
                        },
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Playing Partner Tab (Left)
                    round.playingPartnerRound?.let { partner ->
                        TabHeader(
                            title = "${partner.golferFirstName ?: ""} ${partner.golferLastName ?: ""}",
                            subtitle = "Daily HC: ${partner.dailyHandicap?.toString() ?: "0.0"} | GL HC: ${partner.golfLinkHandicap?.toString() ?: "0.0"}",
                            teeName = partnerTeeName,
                            isActive = selectedTab.value == "partner",
                            onClick = { 
                                selectedTab.value = "partner"
                                onPlayingPartnerClicked()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Golfer Tab (Right)
                    TabHeader(
                        title = "${round.golferFirstName ?: ""} ${round.golferLastName ?: ""}",
                        subtitle = "Daily HC: ${round.dailyHandicap?.toString() ?: "0.0"} | GL HC: ${round.golfLinkHandicap?.toString() ?: "0.0"}",
                        teeName = golferTeeName,
                        isActive = selectedTab.value == "golfer",
                        onClick = { 
                            selectedTab.value = "golfer"
                            onGolferClicked()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Prepare data rows once so we can derive totalRows too
                val rowLabels = listOf("Meters", "Index", "Par", "Strokes", "Score")
                val totalRows = 1 + rowLabels.size // header + data rows

                // Available height for the grid is the BoxWithConstraints maxHeight minus measured header+spacer
                val availableForTable = (this@BoxWithConstraints.maxHeight - tabsHeight - 3.dp).coerceAtLeast(0.dp)
                val responsiveCellHeight = (availableForTable / totalRows).coerceAtLeast(32.dp)

                // Determine text sizes based on cell height - maximized for better readability
                val cellTextSize = when {
                    responsiveCellHeight >= 72.dp -> 26.sp
                    responsiveCellHeight >= 64.dp -> 24.sp
                    responsiveCellHeight >= 56.dp -> 22.sp
                    responsiveCellHeight >= 48.dp -> 20.sp
                    responsiveCellHeight >= 40.dp -> 18.sp
                    else -> 16.sp
                }
                val headerTextSize = when {
                    cellTextSize.value >= 22f -> 24.sp
                    cellTextSize.value >= 20f -> 22.sp
                    cellTextSize.value >= 18f -> 20.sp
                    cellTextSize.value >= 16f -> 18.sp
                    cellTextSize.value >= 14f -> 16.sp
                    else -> 14.sp
                }
                // Larger font size for first column/row labels
                val firstColumnTextSize = when {
                    cellTextSize.value >= 22f -> 24.sp
                    cellTextSize.value >= 20f -> 22.sp
                    cellTextSize.value >= 18f -> 20.sp
                    cellTextSize.value >= 16f -> 18.sp
                    cellTextSize.value >= 14f -> 16.sp
                    else -> 14.sp
                }

                val columnCount = columnData.size + 1
                val outColumnIndex = columnData.indexOfFirst { it.holeNumber.lowercase() == "out" }
                val inColumnIndex = columnData.indexOfFirst { it.holeNumber.lowercase() == "in" }
                val totalColumnIndex = columnData.indexOfFirst { it.holeNumber.lowercase() == "total" }

                key(responsiveCellHeight) {
                    TableWithFixedFirstColumnSCORECARD(
                        columnCount = columnCount,
                        cellWidth = { columnIndex ->
                            val dataIndex = columnIndex - 1
                            if (dataIndex == totalColumnIndex) {
                                (screenWidth * 0.10).dp  // TOTAL column keeps original width
                            } else {
                                (screenWidth * 0.088).dp  // Other columns use 12% reduced width
                            }
                        },
                        firstColumnWidth = { 100.dp },
                        data = rowLabels,
                        cellHeight = responsiveCellHeight,
                        headerCellContent = { columnIndex ->
                            if (columnIndex == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "HOLE",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = headerTextSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                val dataIndex = columnIndex - 1
                                if (dataIndex < columnData.size) {
                                    val data = columnData[dataIndex]
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
                                            fontSize = headerTextSize,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        },
                        cellContent = { columnIndex, rowData ->
                            if (columnIndex == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = rowData,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = firstColumnTextSize,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                val dataIndex = columnIndex - 1
                                if (dataIndex < columnData.size) {
                                    val data = columnData[dataIndex]
                                    val isScoreRow = rowData == "Score"
                                    val isStrokesRow = rowData == "Strokes"
                                    val isSummaryColumn = dataIndex == outColumnIndex || dataIndex == inColumnIndex || dataIndex == totalColumnIndex
                                    val cellValue = when {
                                        isScoreRow && isActiveDQ -> "DQ"
                                        rowData == "Meters" -> data.meters
                                        rowData == "Index" -> data.index
                                        rowData == "Par" -> data.par
                                        rowData == "Strokes" -> data.strokes
                                        else -> data.score
                                    }

                                    val backgroundColor = when {
                                        isSummaryColumn -> mslBlue
                                        else -> Color.White
                                    }
                                    val textColor = when {
                                        isScoreRow && isActiveDQ -> Color.Red
                                        isSummaryColumn -> Color.White
                                        else -> Color.Black
                                    }
                                    val strokeMarkerStyle = if (
                                        isStrokesRow &&
                                        !isSummaryColumn &&
                                        data.strokes != "0"
                                    ) {
                                        scoreReferenceMarkerStyle(
                                            strokes = data.strokes.toIntOrNull(),
                                            par = data.par.toIntOrNull()
                                        )
                                    } else {
                                        null
                                    }
                                    val displayTextColor = if (strokeMarkerStyle?.usesWhiteText == true) {
                                        Color.White
                                    } else {
                                        textColor
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(backgroundColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        strokeMarkerStyle?.let { markerStyle ->
                                            ScoreReferenceMarker(
                                                markerStyle = markerStyle,
                                                cellHeight = responsiveCellHeight,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }

                                        if (rowData == "Index" && data.indexValues.isNotEmpty()) {
                                            val idxSize = when {
                                                cellTextSize.value >= 26f -> 22.sp
                                                cellTextSize.value >= 24f -> 20.sp
                                                cellTextSize.value >= 22f -> 18.sp
                                                cellTextSize.value >= 20f -> 16.sp
                                                else -> 14.sp
                                            }

                                            when (data.indexValues.size) {
                                                1 -> {
                                                    Text(
                                                        text = data.indexValues[0].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.Center).padding(4.dp)
                                                    )
                                                }
                                                2 -> {
                                                    Text(
                                                        text = data.indexValues[0].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp, top = 4.dp)
                                                    )
                                                    Text(
                                                        text = data.indexValues[1].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp, bottom = 4.dp)
                                                    )
                                                }
                                                3 -> {
                                                    Text(
                                                        text = data.indexValues[0].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp, top = 4.dp)
                                                    )
                                                    Text(
                                                        text = data.indexValues[1].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.Center).padding(4.dp)
                                                    )
                                                    Text(
                                                        text = data.indexValues[2].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp, bottom = 4.dp)
                                                    )
                                                }
                                                else -> {
                                                    // 4+ values: four corners
                                                    Text(
                                                        text = data.indexValues[0].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp, top = 4.dp)
                                                    )
                                                    Text(
                                                        text = data.indexValues[1].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.TopEnd).padding(end = 5.dp, top = 4.dp)
                                                    )
                                                    Text(
                                                        text = data.indexValues[2].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 5.dp, bottom = 4.dp)
                                                    )
                                                    Text(
                                                        text = data.indexValues[3].toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontSize = idxSize,
                                                        color = textColor,
                                                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 5.dp, bottom = 4.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            // Non-Index rows or summary columns: original single value
                                            Text(
                                                text = cellValue,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontSize = cellTextSize,
                                                color = displayTextColor,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun getIndexValues(hole: HoleScore): List<Int> {
    return hole.strokeIndexes?.map { it.stroke }
        ?: listOfNotNull(
            hole.index1.takeIf { it > 0 },
            hole.index2.takeIf { it > 0 },
            hole.index3?.takeIf { it > 0 }
        )
}

private fun getTeeName(mslCompetition: MslCompetition?, golfLinkNumber: String?): String {
    return mslCompetition?.players?.find { 
        it.golfLinkNumber == golfLinkNumber 
    }?.teeName ?: "--"
}

private enum class ScoreReferenceMarkerStyle {
    OutlineCircle,
    FilledCircle,
    FilledDoubleCircle,
    OutlineSquare,
    FilledSquare,
    FilledDoubleSquare
}

private val ScoreReferenceMarkerStyle.usesWhiteText: Boolean
    get() = when (this) {
        ScoreReferenceMarkerStyle.FilledCircle,
        ScoreReferenceMarkerStyle.FilledDoubleCircle,
        ScoreReferenceMarkerStyle.FilledSquare,
        ScoreReferenceMarkerStyle.FilledDoubleSquare -> true
        else -> false
    }

private fun scoreReferenceMarkerStyle(strokes: Int?, par: Int?): ScoreReferenceMarkerStyle? {
    if (strokes == null || par == null || par <= 0 || strokes == 0) return null

    return when (strokes - par) {
        in Int.MIN_VALUE until -2 -> ScoreReferenceMarkerStyle.FilledDoubleCircle
        -2 -> ScoreReferenceMarkerStyle.FilledCircle
        -1 -> ScoreReferenceMarkerStyle.OutlineCircle
        1 -> ScoreReferenceMarkerStyle.OutlineSquare
        2 -> ScoreReferenceMarkerStyle.FilledSquare
        in 3..Int.MAX_VALUE -> ScoreReferenceMarkerStyle.FilledDoubleSquare
        else -> null
    }
}

@Composable
private fun ScoreReferenceMarker(
    markerStyle: ScoreReferenceMarkerStyle,
    cellHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val markerSize = (cellHeight * 0.70f).coerceIn(26.dp, 44.dp)
    val doublePadding = (markerSize * 0.14f).coerceIn(3.dp, 6.dp)
    val strokeWidth = if (markerSize >= 34.dp) 2.dp else 1.5.dp
    val markerColor = Color.Black

    Box(
        modifier = modifier.size(markerSize),
        contentAlignment = Alignment.Center
    ) {
        when (markerStyle) {
            ScoreReferenceMarkerStyle.OutlineCircle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(strokeWidth, markerColor, CircleShape)
                )
            }
            ScoreReferenceMarkerStyle.FilledCircle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(markerColor, CircleShape)
                )
            }
            ScoreReferenceMarkerStyle.FilledDoubleCircle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(strokeWidth, markerColor, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(doublePadding)
                        .background(markerColor, CircleShape)
                )
            }
            ScoreReferenceMarkerStyle.OutlineSquare -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(strokeWidth, markerColor, RoundedCornerShape(0.dp))
                )
            }
            ScoreReferenceMarkerStyle.FilledSquare -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(markerColor, RoundedCornerShape(0.dp))
                )
            }
            ScoreReferenceMarkerStyle.FilledDoubleSquare -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(strokeWidth, markerColor, RoundedCornerShape(0.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(doublePadding)
                        .background(markerColor, RoundedCornerShape(0.dp))
                )
            }
        }
    }
}

@Composable
fun TabHeader(
    title: String,
    subtitle: String,
    teeName: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) mslBlue else mslGrey.copy(alpha = 0.6f)
    val textColor = Color.White
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "$teeName Tee",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$golferFirstName $golferLastName",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Daily HC: $dailyHandicap | GL HC: $golfLinkHandicap",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = teeName,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Tee",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
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
        compType = "stableford",
        holeScores = holeScores
    )

    val round = Round(
        golferFirstName = "Peter",
        golferLastName = "Farrier",
        dailyHandicap = 28.0,
        compType = "stableford",
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
