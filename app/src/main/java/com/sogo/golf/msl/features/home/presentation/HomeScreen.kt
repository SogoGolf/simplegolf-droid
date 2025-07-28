// Update to app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeScreen.kt
package com.sogo.golf.msl.features.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.sogo.golf.msl.shared.utils.DateUtils
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar

@Composable
fun HomeScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    // Prevent going back from home screen
    BackHandler {
        // Do nothing - home is the root screen
    }

    val homeUiState by homeViewModel.uiState.collectAsState()
    val localGame by homeViewModel.localGame.collectAsState()
    val scrollState = rememberScrollState()

    ScreenWithDrawer(navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState) // Add scroll capability
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Consistent spacing
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            // Competition status card - NOW USING API DATA
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
                        text = "Competition Data (Single Record)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show API competition data
                    Text(
                        text = when {
                            homeUiState.competitionData == null -> "❌ No competition data from API"
                            homeUiState.competitionData!!.players.isEmpty() -> "⚠️ Competition exists but players is EMPTY"
                            else -> "✅ API: Competition loaded with ${homeUiState.competitionData!!.players.size} players"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Show local competition data using the StateFlow
                    val localCompetition by homeViewModel.localCompetition.collectAsState()
                    Text(
                        text = when {
                            localCompetition != null -> "✅ DB: Single competition record with ${localCompetition!!.players.size} players"
                            else -> "❌ No competition in database"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status indicator
                    Text(
                        text = "🔄 Each API fetch replaces the database record",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )

                    // Show the actual summary from ViewModel
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = homeViewModel.getCompetitionSummary(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    // Show player names if available
                    (homeUiState.competitionData ?: localCompetition)?.let { competition ->
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
                        text = "Game Data (Single Record)",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show both API and local game data
                    Text(
                        text = when {
                            homeUiState.gameData != null -> "✅ API: Game loaded - Competition ${homeUiState.gameData!!.mainCompetitionId}"
                            else -> "❌ No game data from API"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Show local game data using the StateFlow
                    Text(
                        text = when {
                            localGame != null -> "✅ DB: Single game record - Competition ${localGame!!.mainCompetitionId}"
                            else -> "❌ No game in database"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status indicator
                    Text(
                        text = "🔄 Each API fetch replaces the database record",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )

                    // Show the game summary
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = homeViewModel.getGameSummary(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    // Show game details if available
                    (homeUiState.gameData ?: localGame)?.let { game ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Starting Hole: ${game.startingHoleNumber} | Partners: ${game.playingPartners.size} | Competitions: ${game.competitions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔧 DEBUG: Date Testing",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            // Simulate having yesterday's data stored
                            homeViewModel.setDebugStoredDate("2025-07-27") // Set stored date to yesterday
                            DateUtils.clearDebugDate() // Keep today as real today
                            homeViewModel.clearMessages()
                            homeViewModel.testDateValidation()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🕐 Simulate YESTERDAY'S data (should trigger reset)")
                    }

                    Button(
                        onClick = {
                            // Simulate having today's data stored
                            homeViewModel.setDebugStoredDate("2025-07-28") // Set stored date to today
                            DateUtils.clearDebugDate() // Keep today as real today
                            homeViewModel.clearMessages()
                            homeViewModel.testDateValidation()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📅 Simulate TODAY'S data (should be fresh)")
                    }

                    Button(
                        onClick = {
                            DateUtils.clearDebugDate()
                            homeViewModel.clearMessages()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔄 Reset to Real Date")
                    }
                }
            }


            // Competition API Buttons Section
            Text(
                text = "Competition Actions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Get Competition Data button - NOW USING API
            Button(
                onClick = {
                    homeViewModel.getCompetitionAndSaveToDatabase()  // ← EXACT same pattern as game
                },
                enabled = !homeUiState.isLoadingCompetition,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (homeUiState.isLoadingCompetition) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    }
                } else {
                    Text("📥 Get Competition (API) & Replace in Database")                }
            }

            // Get Competition from Local Database button - EXACT same as game
            Button(
                onClick = {
                    homeViewModel.getCompetitionFromLocalDatabase()  // ← EXACT same pattern as game
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("🏆 Get Competition from LOCAL DATABASE")  // ← EXACT same text pattern as game
            }

            // Game API Buttons Section
            Text(
                text = "Game Actions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Get Game button (API only)
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
                    Text("🎮 Get Game (API Only)")
                }
            }

            // Get Game and Save to Database button
            Button(
                onClick = {
                    homeViewModel.getGameAndSaveToDatabase()
                },
                enabled = !homeUiState.isLoadingGame,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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
                        Text("Saving...")
                    }
                } else {
                    Text("📥 Get Game (API) & Replace in Database")
                }
            }

            // Local Database Test Section
            Text(
                text = "Local Database Tests",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            // Get Golfer from LOCAL DATABASE button
            Button(
                onClick = {
                    homeViewModel.getGolferFromLocalDatabase()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("👤 Get Golfer from LOCAL DATABASE")
            }

            // Get Game from Local Database button
            Button(
                onClick = {
                    homeViewModel.getGameFromLocalDatabase()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("📱 Get Game from LOCAL DATABASE")
            }

            // Debug Section
            Text(
                text = "Debug Tools",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )

            // Test Club Storage button
            Button(
                onClick = {
                    homeViewModel.testClubStorage()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("🔧 TEST: Club Storage Debug")
            }

            // Navigation Section
            Text(
                text = "Navigation",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            // Navigation button
            Button(
                onClick = { navController.navigate(nextRoute) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("➡️ Next: $nextRoute")
            }

            // Add some bottom padding so the last button isn't cut off
            Spacer(modifier = Modifier.height(16.dp))
        }

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

        // NEW: Network messages for competition
        NetworkMessageSnackbar(
            message = homeUiState.competitionErrorMessage,
            isError = true,
            onDismiss = { homeViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = homeUiState.competitionSuccessMessage,
            isError = false,
            onDismiss = { homeViewModel.clearMessages() }
        )
    }
}