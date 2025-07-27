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
    competitionViewModel: CompetitionViewModel = hiltViewModel()
) {
    // Prevent going back from home screen
    BackHandler {
        // Do nothing - home is the root screen
    }

    val uiState by competitionViewModel.uiState.collectAsState()
    val currentCompetition by competitionViewModel.currentCompetition.collectAsState()

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

                    Text(
                        text = competitionViewModel.getCompetitionSummary(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    // Show player names if available
                    val playerNames = competitionViewModel.getPlayerNames()
                    if (playerNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Players: ${playerNames.take(3).joinToString(", ")}${if (playerNames.size > 3) " and ${playerNames.size - 3} more..." else ""}",
                            style = MaterialTheme.typography.bodySmall,
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
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
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

            // Additional action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Refresh button
                OutlinedButton(
                    onClick = { competitionViewModel.refreshCompetition() },
                    enabled = !uiState.isLoading
                ) {
                    Text("Refresh")
                }

                // Clear button (for testing)
                OutlinedButton(
                    onClick = { competitionViewModel.clearAllCompetitions() },
                    enabled = !uiState.isLoading
                ) {
                    Text("Clear Data")
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

        // Network messages (only show when user takes action)
        NetworkMessageSnackbar(
            message = uiState.errorMessage,
            isError = true,
            onDismiss = { competitionViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = uiState.successMessage,
            isError = false,
            onDismiss = { competitionViewModel.clearMessages() }
        )
    }
}