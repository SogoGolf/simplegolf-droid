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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun ChoosePlayingPartnerScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    viewModel: ChoosePlayingPartnerViewModel = hiltViewModel()
) {
    val localGame by viewModel.localGame.collectAsState()
    val currentGolfer by viewModel.currentGolfer.collectAsState()
    val selectedPartner by viewModel.selectedPartner.collectAsState()

    ScreenWithDrawer(navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                        Text(
                            text = "Handicap: ${golfer.primary}",
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
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(localGame!!.playingPartners) { partner ->
                                    val isSelected = viewModel.isPartnerSelected(partner)

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectPartner(partner)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                Color.Yellow
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            // Partner name
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

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons
            Row {
                if (navController.previousBackStackEntry != null) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Button(onClick = { navController.navigate(nextRoute) }) {
                    Text("Next")
                }
            }
        }
    }
}