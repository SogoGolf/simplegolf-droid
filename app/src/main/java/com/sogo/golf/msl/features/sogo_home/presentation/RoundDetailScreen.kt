package com.sogo.golf.msl.features.sogo_home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.mongodb.RoundDetail
import com.sogo.golf.msl.domain.model.mongodb.RoundDetailHoleScore
import com.sogo.golf.msl.ui.theme.MSLColors
import org.threeten.bp.format.DateTimeFormatter

data class RoundTotal(
    val label: String,
    val par: Int,
    val strokes: Int,
    val score: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundDetailScreen(
    navController: NavController,
    roundId: String,
    viewModel: RoundDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(roundId) {
        viewModel.fetchRoundDetail(roundId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Round Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MSLColors.mslBlue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MSLColors.mslBlue
                        )
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Failed to load round details",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error ?: "Unknown error",
                                fontSize = 14.sp,
                                color = Color.Gray.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.fetchRoundDetail(roundId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MSLColors.mslBlue
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.roundDetail != null -> {
                    val roundDetail = uiState.roundDetail!!
                    val holeScores = roundDetail.holeScores.sortedBy { it.holeNumber }
                    val isNineHoles = holeScores.size == 9
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Pinned header and table header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5))
                                .padding(16.dp)
                        ) {
                            RoundSummaryHeader(roundDetail)
                            Spacer(modifier = Modifier.height(16.dp))
                            HoleScoreTableHeader()
                        }
                        
                        // Scrollable content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            if (isNineHoles) {
                                items(holeScores) { hole ->
                                    HoleScoreRow(hole)
                                }
                                item {
                                    val total = calculateTotal(holeScores, "TOTAL")
                                    TotalRow(total)
                                }
                            } else {
                                items(holeScores.take(9)) { hole ->
                                    HoleScoreRow(hole)
                                }
                                
                                item {
                                    val outTotal = calculateTotal(holeScores.take(9), "OUT")
                                    TotalRow(outTotal)
                                }
                                
                                items(holeScores.drop(9).take(9)) { hole ->
                                    HoleScoreRow(hole)
                                }
                                
                                item {
                                    val inTotal = calculateTotal(holeScores.drop(9).take(9), "IN")
                                    TotalRow(inTotal)
                                }
                                
                                item {
                                    val grandTotal = calculateTotal(holeScores, "TOTAL")
                                    TotalRow(grandTotal, isGrandTotal = true)
                                }
                            }
                            
                            // Add bottom padding
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundSummaryHeader(roundDetail: RoundDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = roundDetail.clubName ?: "Unknown Club",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MSLColors.mslBlue
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = roundDetail.roundDate?.format(
                    DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
                ) ?: "No date",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            roundDetail.compType?.let { compType ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Competition: ${compType.replaceFirstChar { it.uppercase() }}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            
            // Split into two rows to handle longer tee names
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                roundDetail.scratchRating?.let { rating ->
                    Text(
                        text = "Scratch Rating: $rating",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                roundDetail.slopeRating?.let { rating ->
                    Text(
                        text = "Slope Rating: $rating",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            roundDetail.teeColor?.let { color ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tee: $color",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Hole-stats summary — only when the golfer tracked stats this round.
            val summary = remember(roundDetail) { RoundStatSummary(roundDetail.holeScores) }
            if (summary.anyTracked) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color.Black.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))
                RoundStatsBand(summary)
            }
        }
    }
}

// Aggregate of the hole stats entered across the round.
private class RoundStatSummary(holes: List<RoundDetailHoleScore>) {
    var fairwaysHit = 0
    var fairwaysTracked = 0
    var greensHit = 0
    var greensTracked = 0
    var putts = 0
    var puttsTracked = false
    var bunker = 0
    var penalties = 0
    var anyTracked = false

    init {
        for (h in holes) {
            val s = h.stats ?: continue
            if (!s.hasAnyEntry()) continue
            anyTracked = true
            s.fairwayHit?.let { fairwaysTracked++; if (it) fairwaysHit++ }
            s.greenHit?.let { greensTracked++; if (it) greensHit++ }
            s.putts?.let { putts += it; puttsTracked = true }
            s.bunkerShots?.let { bunker += it }
            s.penalties?.let { penalties += it }
        }
    }
}

private fun HoleStats.hasAnyEntry(): Boolean =
    teeClub != null || approachClub != null ||
    fairwayHit != null || greenHit != null ||
    !fairwayMiss.isNullOrEmpty() || !approachMiss.isNullOrEmpty() ||
    putts != null || bunkerShots != null || penalties != null

@Composable
private fun RoundStatsBand(summary: RoundStatSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (summary.fairwaysTracked > 0) {
                BandStat(Icons.Default.Flag, "FW", "${summary.fairwaysHit}/${summary.fairwaysTracked}")
            }
            if (summary.greensTracked > 0) {
                BandStat(Icons.Default.Adjust, "GIR", "${summary.greensHit}/${summary.greensTracked}")
            }
            if (summary.puttsTracked) {
                BandStat(Icons.Default.SportsGolf, "Putts", "${summary.putts}")
            }
        }
        if (summary.bunker > 0 || summary.penalties > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (summary.bunker > 0) {
                    Text("${summary.bunker} bunker", fontSize = 12.sp, color = Color.Gray)
                }
                if (summary.penalties > 0) {
                    val word = if (summary.penalties == 1) "penalty" else "penalties"
                    Text("${summary.penalties} $word", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun BandStat(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MSLColors.mslBlue,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(5.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MSLColors.mslBlue)
    }
}

@Composable
private fun HoleScoreTableHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MSLColors.mslBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HeaderCell("Hole", Modifier.weight(1f))
            HeaderCell("Par", Modifier.weight(1f))
            HeaderCell("Strokes", Modifier.weight(1f))
            HeaderCell("Score", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun HoleScoreRow(hole: RoundDetailHoleScore) {
    val hasStats = hole.stats?.hasAnyEntry() == true
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .then(if (hasStats) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hole cell carries a green dot + chevron when stats exist.
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = hole.holeNumber.toString(),
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                if (hasStats) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MSLColors.mslGreen)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Hide stats" else "Show stats",
                            tint = MSLColors.mslGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            DataCell(hole.par.toString(), Modifier.weight(1f))
            DataCell(hole.strokes.toString(), Modifier.weight(1f))
            DataCell(hole.score.toString(), Modifier.weight(1f))
        }

        AnimatedVisibility(visible = expanded) {
            hole.stats?.let { HoleStatsDetail(it) }
        }
    }
}

@Composable
private fun HoleStatsDetail(stats: HoleStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MSLColors.mslBlue.copy(alpha = 0.04f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatDetailRow(
            label = "FAIRWAY (FROM TEE)",
            result = stats.fairwayHit,
            direction = directionText(stats.fairwayMiss),
            club = stats.teeClub
        )
        StatDetailRow(
            label = "GREEN",
            result = stats.greenHit,
            direction = directionText(stats.approachMiss),
            club = stats.approachClub
        )

        val chips = countChips(stats)
        if (chips.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach { chip ->
                    Text(
                        text = chip,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MSLColors.mslBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MSLColors.mslBlue.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatDetailRow(label: String, result: Boolean?, direction: String?, club: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.weight(1f))
        if (result != null) {
            Text(
                text = if (result) "Hit" else "Miss",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (result) MSLColors.mslGreen else MSLColors.mslRed
            )
            if (!result && direction != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = direction,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MSLColors.mslRed
                )
            }
        }
        if (!club.isNullOrEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "· $club", fontSize = 13.sp, color = Color.Black)
        }
        if (result == null && club.isNullOrEmpty()) {
            Text(text = "—", fontSize = 13.sp, color = Color.Black.copy(alpha = 0.4f))
        }
    }
}

private fun countChips(stats: HoleStats): List<String> {
    val out = mutableListOf<String>()
    stats.putts?.let { out.add("$it ${if (it == 1) "putt" else "putts"}") }
    stats.bunkerShots?.let { if (it > 0) out.add("$it ${if (it == 1) "bunker shot" else "bunker shots"}") }
    stats.penalties?.let { if (it > 0) out.add("$it ${if (it == 1) "penalty" else "penalties"}") }
    return out
}

private fun directionText(raw: String?): String? {
    if (raw.isNullOrEmpty()) return null
    val map = mapOf("left" to "←", "right" to "→", "short" to "↓", "long" to "↑")
    val arrows = raw.split(",").mapNotNull { map[it.trim()] }
    return if (arrows.isEmpty()) null else arrows.joinToString(" ")
}

@Composable
private fun TotalRow(total: RoundTotal, isGrandTotal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isGrandTotal) MSLColors.mslYellow else MSLColors.mslBlue.copy(alpha = 0.1f)
            )
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DataCell(
            total.label, 
            Modifier.weight(1f), 
            fontWeight = FontWeight.Bold,
            color = if (isGrandTotal) Color.White else MSLColors.mslBlue
        )
        DataCell(
            total.par.toString(), 
            Modifier.weight(1f), 
            fontWeight = FontWeight.Bold,
            color = if (isGrandTotal) Color.White else MSLColors.mslBlue
        )
        DataCell(
            total.strokes.toString(), 
            Modifier.weight(1f), 
            fontWeight = FontWeight.Bold,
            color = if (isGrandTotal) Color.White else MSLColors.mslBlue
        )
        DataCell(
            total.score.toString(), 
            Modifier.weight(1f), 
            fontWeight = FontWeight.Bold,
            color = if (isGrandTotal) Color.White else MSLColors.mslBlue
        )
    }
}

@Composable
private fun DataCell(
    text: String, 
    modifier: Modifier = Modifier, 
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Black
) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = TextAlign.Center
    )
}

private fun calculateTotal(holes: List<RoundDetailHoleScore>, label: String): RoundTotal {
    return RoundTotal(
        label = label,
        par = holes.sumOf { it.par },
        strokes = holes.sumOf { it.strokes },
        score = holes.sumOf { it.score }
    )
}