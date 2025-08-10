package com.sogo.golf.msl.features.sogo_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.mongodb.LeaderboardEntry
import com.sogo.golf.msl.ui.theme.MSLColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionLeaderboardScreen(
    navController: NavController,
    viewModel: CompetitionLeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.competitionName,
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
                                text = "Failed to load leaderboard",
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
                                onClick = { viewModel.fetchLeaderboard() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MSLColors.mslBlue
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.leaderboardEntries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No leaderboard data available",
                                fontSize = 18.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check back later for updated results",
                                fontSize = 14.sp,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Fixed date range and best rounds header at top
                        if (uiState.dateRangeText.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MSLColors.mslBlue.copy(alpha = 0.1f))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = uiState.dateRangeText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MSLColors.mslBlue,
                                    textAlign = TextAlign.Center
                                )
                                if (uiState.bestRoundsText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.bestRoundsText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MSLColors.mslBlue.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        
                        // Scrollable list below the fixed header
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.leaderboardEntries) { entry ->
                                LeaderboardEntryCard(
                                    entry = entry,
                                    isFirstPlace = entry.rank == 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    isFirstPlace: Boolean
) {
    val cardModifier = if (isFirstPlace) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD700), // Gold
                        Color(0xFFFFA500)  // Orange
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    }
    
    val cardColors = if (isFirstPlace) {
        CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.98f)
        )
    } else {
        CardDefaults.cardColors(
            containerColor = Color.White
        )
    }
    
    val elevation = if (isFirstPlace) 8.dp else 2.dp
    val shape = if (isFirstPlace) RoundedCornerShape(12.dp) else RoundedCornerShape(8.dp)
    
    Card(
        modifier = cardModifier,
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank section with special styling for first place
            Box(
                modifier = Modifier.width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isFirstPlace) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "First Place",
                            tint = Color(0xFFB8860B),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "#${entry.rank}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB8860B)
                        )
                    }
                } else {
                    Text(
                        text = "#${entry.rank}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MSLColors.mslBlue,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Golfer info section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Golfer name
                Text(
                    text = entry.golferName,
                    fontSize = if (isFirstPlace) 18.sp else 16.sp,
                    fontWeight = if (isFirstPlace) FontWeight.ExtraBold else FontWeight.Bold,
                    color = if (isFirstPlace) Color(0xFFB8860B) else Color.Black
                )
                
                // Club/State info if available
                if (entry.golferState.isNotBlank()) {
                    Text(
                        text = entry.golferState,
                        fontSize = 12.sp,
                        color = if (isFirstPlace) Color(0xFFB8860B).copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Individual scores
                val scoresDisplay = if (entry.roundScores.isEmpty()) {
                    "[-]"
                } else {
                    entry.roundScores.joinToString(", ") { it.toString() }
                        .let { "[$it]" }
                }
                
                Text(
                    text = scoresDisplay,
                    fontSize = 14.sp,
                    color = if (isFirstPlace) Color(0xFFB8860B).copy(alpha = 0.8f) else Color.Gray,
                    fontWeight = if (isFirstPlace) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Total points section
            Box(
                modifier = Modifier
                    .background(
                        color = if (isFirstPlace) 
                            Color(0xFFFFD700).copy(alpha = 0.2f) 
                        else 
                            MSLColors.mslBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${entry.compScoreTotal}",
                    fontSize = if (isFirstPlace) 20.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFirstPlace) Color(0xFFB8860B) else MSLColors.mslBlue
                )
            }
        }
    }
}