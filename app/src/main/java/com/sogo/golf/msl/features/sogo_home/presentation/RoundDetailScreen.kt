package com.sogo.golf.msl.features.sogo_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
        viewModel.fetchRoundDetail(roundId) // initapi - fetches round detail data on screen load
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
        }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DataCell(hole.holeNumber.toString(), Modifier.weight(1f))
        DataCell(hole.par.toString(), Modifier.weight(1f))
        DataCell(hole.strokes.toString(), Modifier.weight(1f))
        DataCell(hole.score.toString(), Modifier.weight(1f))
    }
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
