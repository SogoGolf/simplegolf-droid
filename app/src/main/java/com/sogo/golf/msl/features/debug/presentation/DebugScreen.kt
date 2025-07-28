// app/src/main/java/com/sogo/golf/msl/features/debug/presentation/DebugScreen.kt
package com.sogo.golf.msl.features.debug.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sogo.golf.msl.shared.utils.DateUtils
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onDismiss: () -> Unit,
    debugViewModel: DebugViewModel = hiltViewModel()
) {
    val debugUiState by debugViewModel.uiState.collectAsState()
    val localGame by debugViewModel.localGame.collectAsState()
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Debug Tools",
                    style = MaterialTheme.typography.headlineMedium
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Debug"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Status Card
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
                            text = "Today's Game",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        localGame?.let { game ->
                            Text(
                                text = "✅ Game ready for Competition ${game.mainCompetitionId}",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Starting Hole: ${game.startingHoleNumber} | ${game.playingPartners.size} playing partners",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            game.teeName?.let { tee ->
                                Text(
                                    text = "Tee: $tee",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } ?: run {
                            Text(
                                text = "❌ No game data available",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = "Use the buttons below to fetch game data",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Competition Status Card
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

                        val localCompetition by debugViewModel.localCompetition.collectAsState()
                        localCompetition?.let { competition ->
                            Text(
                                text = "✅ Competition loaded with ${competition.players.size} players",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )

                            competition.players.firstOrNull()?.let { firstPlayer ->
                                firstPlayer.competitionName?.let { name ->
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } ?: run {
                            Text(
                                text = "❌ No competition data available",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = "Use the buttons below to fetch competition data",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                                debugUiState.competitionData == null -> "❌ No competition data from API"
                                debugUiState.competitionData!!.players.isEmpty() -> "⚠️ Competition exists but players is EMPTY"
                                else -> "✅ API: Competition loaded with ${debugUiState.competitionData!!.players.size} players"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Show local competition data using the StateFlow
                        val localCompetition by debugViewModel.localCompetition.collectAsState()
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
                            text = debugViewModel.getCompetitionSummary(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        // Show player names if available
                        (debugUiState.competitionData ?: localCompetition)?.let { competition ->
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
                                debugUiState.gameData != null -> "✅ API: Game loaded - Competition ${debugUiState.gameData!!.mainCompetitionId}"
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
                            text = debugViewModel.getGameSummary(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        // Show game details if available
                        (debugUiState.gameData ?: localGame)?.let { game ->
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
                                debugViewModel.setDebugStoredDate("2025-07-27") // Set stored date to yesterday
                                DateUtils.clearDebugDate() // Keep today as real today
                                debugViewModel.clearMessages()
                                debugViewModel.testDateValidation()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🕐 Simulate YESTERDAY'S data (should trigger reset)")
                        }

                        Button(
                            onClick = {
                                // Simulate having today's data stored
                                debugViewModel.setDebugStoredDate("2025-07-28") // Set stored date to today
                                DateUtils.clearDebugDate() // Keep today as real today
                                debugViewModel.clearMessages()
                                debugViewModel.testDateValidation()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📅 Simulate TODAY'S data (should be fresh)")
                        }

                        Button(
                            onClick = {
                                DateUtils.clearDebugDate()
                                debugViewModel.clearMessages()
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
                        debugViewModel.getCompetitionAndSaveToDatabase()  // ← EXACT same pattern as game
                    },
                    enabled = !debugUiState.isLoadingCompetition,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (debugUiState.isLoadingCompetition) {
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
                        Text("📥 Get Competition (API) & Replace in Database")
                    }
                }

                // Get Competition from Local Database button - EXACT same as game
                Button(
                    onClick = {
                        debugViewModel.getCompetitionFromLocalDatabase()  // ← EXACT same pattern as game
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
                        debugViewModel.getGame()
                    },
                    enabled = !debugUiState.isLoadingGame,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (debugUiState.isLoadingGame) {
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
                        debugViewModel.getGameAndSaveToDatabase()
                    },
                    enabled = !debugUiState.isLoadingGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (debugUiState.isLoadingGame) {
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
                        debugViewModel.getGolferFromLocalDatabase()
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
                        debugViewModel.getGameFromLocalDatabase()
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
                        debugViewModel.testClubStorage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("🔧 TEST: Club Storage Debug")
                }

                // Quick Actions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Quick Navigation",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Debug & Start Playing Golf")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Debug Panel")
                        }
                    }
                }

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💡 Debug Info",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This debug panel is available on any screen via the settings icon (⚙️). All API calls and data operations are logged to the console for debugging.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Add some bottom padding so the last button isn't cut off
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Network messages for game
        NetworkMessageSnackbar(
            message = debugUiState.gameErrorMessage,
            isError = true,
            onDismiss = { debugViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = debugUiState.gameSuccessMessage,
            isError = false,
            onDismiss = { debugViewModel.clearMessages() }
        )

        // Network messages for competition
        NetworkMessageSnackbar(
            message = debugUiState.competitionErrorMessage,
            isError = true,
            onDismiss = { debugViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = debugUiState.competitionSuccessMessage,
            isError = false,
            onDismiss = { debugViewModel.clearMessages() }
        )
    }
}