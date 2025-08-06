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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sogo.golf.msl.shared.utils.DateUtils
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onDismiss: () -> Unit,
    debugViewModel: DebugViewModel = hiltViewModel()
) {
    val debugUiState by debugViewModel.uiState.collectAsState()
    val localGame by debugViewModel.localGame.collectAsState()
    val currentGolfer by debugViewModel.currentGolfer.collectAsState()
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
                // NEW: Current User & Club Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current User & Club",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Golfer info
                        currentGolfer?.let { golfer ->
                            Text(
                                text = "👤 ${golfer.firstName} ${golfer.surname}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Golf Link: ${golfer.golfLinkNo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } ?: run {
                            Text(
                                text = "❌ No golfer logged in",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Club info button
                        Button(
                            onClick = { debugViewModel.debugClubInfo() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("🏌️ Show Current Club Info")
                        }
                    }
                }

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
                                color = mslBlack
                            )

                            game.teeName?.let { tee ->
                                Text(
                                    text = "Tee: $tee",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = mslBlack
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
                                text = "Use the buttons below to fetch game data for your club",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = mslBlack
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
                                        color = mslBlack
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
                                text = "Use the buttons below to fetch competition data for your club",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = mslBlack
                            )
                        }
                    }
                }

                // Game API Buttons Section
                Text(
                    text = "Game Actions (Your Club)",
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
                        Text("🎮 Get Game (API Only) - Your Club")
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
                        Text("📥 Get Game (API) & Save to Database - Your Club")
                    }
                }

                // Competition API Buttons Section
                Text(
                    text = "Competition Actions (Your Club)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                // Get Competition Data button
                Button(
                    onClick = {
                        debugViewModel.getCompetitionAndSaveToDatabase()
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
                        Text("📥 Get Competition (API) & Save to Database - Your Club")
                    }
                }

                // Get Competition from Local Database button
                Button(
                    onClick = {
                        debugViewModel.getCompetitionFromLocalDatabase()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("🏆 Get Competition from LOCAL DATABASE")
                }

                // Fees API Section
                Text(
                    text = "Fees Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Get Fees button
                Button(
                    onClick = {
                        debugViewModel.getFees()
                    },
                    enabled = !debugUiState.isLoadingFees,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (debugUiState.isLoadingFees) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading...")
                        }
                    } else {
                        Text("💰 Get Fees (API) & Save to Database")
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

                // Date testing section (keep your existing date testing card)
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
                                debugViewModel.setDebugStoredDate("2025-07-27")
                                DateUtils.clearDebugDate()
                                debugViewModel.clearMessages()
                                debugViewModel.testDateValidation()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🕐 Simulate YESTERDAY'S data (should trigger reset)")
                        }

                        Button(
                            onClick = {
                                debugViewModel.setDebugStoredDate("2025-07-28")
                                DateUtils.clearDebugDate()
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

                // Add some bottom padding so the last button isn't cut off
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Network messages
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

        NetworkMessageSnackbar(
            message = debugUiState.feesErrorMessage,
            isError = true,
            onDismiss = { debugViewModel.clearMessages() }
        )

        NetworkMessageSnackbar(
            message = debugUiState.feesSuccessMessage,
            isError = false,
            onDismiss = { debugViewModel.clearMessages() }
        )
    }
}