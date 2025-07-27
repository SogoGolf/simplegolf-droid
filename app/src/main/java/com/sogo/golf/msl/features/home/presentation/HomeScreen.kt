package com.sogo.golf.msl.features.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.sogo.golf.msl.features.competitions.presentation.CompetitionViewModel
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar

@Composable
fun HomeScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    competitionViewModel: CompetitionViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Prevent going back from home screen
    BackHandler {
        // Do nothing - home is the root screen
    }

    val competitionUiState by competitionViewModel.uiState.collectAsState()
    val currentCompetition by competitionViewModel.currentCompetition.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    // DEBUG: Log what we're actually getting
    LaunchedEffect(currentCompetition) {
        android.util.Log.d("HomeScreen", "=== UI DEBUG ===")
        android.util.Log.d("HomeScreen", "currentCompetition: $currentCompetition")
        android.util.Log.d("HomeScreen", "currentCompetition == null: ${currentCompetition == null}")
        if (currentCompetition != null) {
            android.util.Log.d("HomeScreen", "players.size: ${currentCompetition!!.players.size}")
            android.util.Log.d("HomeScreen", "players.isEmpty(): ${currentCompetition!!.players.isEmpty()}")
        }
    }

    ScreenWithDrawer(navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Competition status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Competition Status",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // SIMPLE DEBUG: Show exactly what we have
                    Text(
                        text = when {
                            currentCompetition == null -> "❌ currentCompetition is NULL"
                            currentCompetition!!.players.isEmpty() -> "⚠️ currentCompetition exists but players is EMPTY"
                            else -> "✅ currentCompetition exists with ${currentCompetition!!.players.size} players"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    // Show the actual summary from ViewModel
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = competitionViewModel.getCompetitionSummary(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    // Show player names if available
                    currentCompetition?.let { competition ->
                        if (competition.players.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Players: ${competition.players.joinToString(", ") { "${it.firstName} ${it.lastName}".trim() }}",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Data Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Game Data",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    homeUiState.gameData?.let { game ->
                        Text(
                            text = "✅ Game loaded: Competition ${game.mainCompetitionId}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Starting Hole: ${game.startingHoleNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Partners: ${game.playingPartners.size}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    } ?: run {
                        Text(
                            text = "No game data loaded",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Get Competition Data button
            Button(
                onClick = {
                    competitionViewModel.fetchCompetitionData()
                },
                enabled = !competitionUiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (competitionUiState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading...")
                    }
                } else {
                    Text("Get Competition Data")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Get Game button
            Button(
                onClick = {
                    homeViewModel.getGame()
                },
                enabled = !homeUiState.isLoadingGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (homeUiState.isLoadingGame) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading...")
                    }
                } else {
                    Text("Get Game")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation button
            Button(
                onClick = { navController.navigate(nextRoute) }
            ) {
                Text("Next")
            }
        }

        // Network messages for competition
        NetworkMessageSnackbar(
            message = competitionUiState.errorMessage,
            isError = true,
            onDismiss = { competitionViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = competitionUiState.successMessage,
            isError = false,
            onDismiss = { competitionViewModel.clearMessages() }
        )

        // Network messages for game
        NetworkMessageSnackbar(
            message = homeUiState.gameErrorMessage,
            isError = true,
            onDismiss = { homeViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = homeUiState.gameSuccessMessage,
            isError = false,
            onDismiss = { homeViewModel.clearMessages() }
        )
    }
}