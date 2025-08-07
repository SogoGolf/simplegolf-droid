package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sogo.golf.msl.domain.model.HoleScore

@Composable
fun GoToHoleAlertDialog(
    holeScores: List<HoleScore>,
    holeScoresPlayingPartner: List<HoleScore>,
    validHoleRange: IntRange?,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
){
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Go to Hole",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        // Filter holes to only show ones that exist on the course
                        val filteredHoleScores = if (validHoleRange != null) {
                            holeScores.filter { it.holeNumber in validHoleRange }
                        } else {
                            holeScores
                        }
                        
                        items(filteredHoleScores) { holeScore ->
                            val holeScorePartner = holeScoresPlayingPartner.find { 
                                it.holeNumber == holeScore.holeNumber 
                            }
                            
                            GoToHole(
                                holeNumber = holeScore.holeNumber,
                                holeScore = holeScore,
                                holeScorePlayingPartner = holeScorePartner,
                                onHoleClick = { holeNumber ->
                                    onConfirm(holeNumber)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
