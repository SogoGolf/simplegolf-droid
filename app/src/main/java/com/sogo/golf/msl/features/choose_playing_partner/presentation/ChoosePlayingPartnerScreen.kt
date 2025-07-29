package com.sogo.golf.msl.features.choose_playing_partner.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.shared_components.ui.UnifiedScreenHeader
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar

@Composable
fun ChoosePlayingPartnerScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    viewModel: ChoosePlayingPartnerViewModel = hiltViewModel(),
) {
    val localGame by viewModel.localGame.collectAsState()
    val currentGolfer by viewModel.currentGolfer.collectAsState()
    val selectedPartner by viewModel.selectedPartner.collectAsState()
    val markerUiState by viewModel.markerUiState.collectAsState()

    // 🔧 NEW: Clear selection when returning to screen
    LaunchedEffect(Unit) {
        viewModel.onScreenResumed()
    }

    // Handle navigation on successful marker selection
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            navController.navigate(nextRoute)
        }
    }

    ScreenWithDrawer(
        navController = navController,
        topBar = {
            UnifiedScreenHeader(
                title = "Competitions",
                backgroundColor = Color.White
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
//                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            // Current User Info
            currentGolfer?.let { golfer ->
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
                            text = "You",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${golfer.firstName} ${golfer.surname}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Golf Link: ${golfer.golfLinkNo}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Selection Status
            selectedPartner?.let { partner ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Partner",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        val partnerName = when {
                            partner.firstName != null && partner.lastName != null ->
                                "${partner.firstName} ${partner.lastName}"
                            partner.firstName != null -> partner.firstName!!
                            partner.lastName != null -> partner.lastName!!
                            else -> "Unknown Player"
                        }

                        Text(
                            text = partnerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { viewModel.clearSelection() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Clear Selection")
                        }
                    }
                }
            }

            // Playing Partners Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Playing Partners",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    when {
                        localGame == null -> {
                            Text(
                                text = "❌ No game data available.\nPlease fetch game data from the Home screen first.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        localGame!!.playingPartners.isEmpty() -> {
                            Text(
                                text = "🏌️ No playing partners found.\nYou'll be playing solo!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        else -> {
                            // Replace LazyColumn with regular Column for better scrolling behavior
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                localGame!!.playingPartners.forEach { partner ->
                                    val isSelected = viewModel.isPartnerSelected(partner)
                                    val isMarkedByMe = currentGolfer?.let { golfer ->
                                        partner.markedByGolfLinkNumber == golfer.golfLinkNo
                                    } ?: false

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectPartner(partner)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isSelected -> Color.Yellow
                                                isMarkedByMe -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            // Partner name with marker indicator
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                val partnerName = when {
                                                    partner.firstName != null && partner.lastName != null ->
                                                        "${partner.firstName} ${partner.lastName}"
                                                    partner.firstName != null -> partner.firstName!!
                                                    partner.lastName != null -> partner.lastName!!
                                                    else -> "Unknown Player"
                                                }

                                                Text(
                                                    text = partnerName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )

                                                if (isMarkedByMe) {
                                                    Text(
                                                        text = "✓ MARKED BY YOU",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // Partner details
                                            partner.golfLinkNumber?.let { golfLink ->
                                                Text(
                                                    text = "Golf Link: $golfLink",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = "Daily Handicap: ${partner.dailyHandicap}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            // Marked by info - always show
                                            Text(
                                                text = "Marked by: ${partner.markedByGolfLinkNumber ?: "Not assigned"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (partner.markedByGolfLinkNumber != null) {
                                                    MaterialTheme.colorScheme.secondary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Game Info Summary
            localGame?.let { game ->
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
                            text = "Game Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Competition ID: ${game.mainCompetitionId}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Starting Hole: ${game.startingHoleNumber}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        game.numberOfHoles?.let { holes ->
                            Text(
                                text = "Number of Holes: $holes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        game.teeName?.let { tee ->
                            Text(
                                text = "Tee: $tee",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Navigation buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (navController.previousBackStackEntry != null) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                }

                // NEW: Remove Marker button
                Button(
                    onClick = { viewModel.removeMarker() },
                    enabled = viewModel.hasPartnerMarkedByMe() && !markerUiState.isRemovingMarker && !markerUiState.isSelectingMarker,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (markerUiState.isRemovingMarker) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Removing...")
                        }
                    } else {
                        Text("Remove Marker")
                    }
                }

                // UPDATED: Next button waits for API success before navigating
                Button(
                    onClick = {
                        if (selectedPartner != null) {
                            viewModel.selectMarker() // Call API first, navigation handled by LaunchedEffect
                        } else {
                            // If no partner selected, proceed directly
                            viewModel.proceedWithoutMarker()
                        }
                    },
                    enabled = !markerUiState.isSelectingMarker && !markerUiState.isRemovingMarker,
                    modifier = Modifier.weight(1f)
                ) {
                    if (markerUiState.isSelectingMarker) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecting...")
                        }
                    } else {
                        Text("Next")
                    }
                }
            }

            // Add some bottom padding for better scrolling experience
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Network messages for marker API
        NetworkMessageSnackbar(
            message = markerUiState.markerErrorMessage,
            isError = true,
            onDismiss = { viewModel.clearMarkerMessages() }
        )

        NetworkMessageSnackbar(
            message = markerUiState.markerSuccessMessage,
            isError = false,
            onDismiss = { viewModel.clearMarkerMessages() }
        )
    }
}