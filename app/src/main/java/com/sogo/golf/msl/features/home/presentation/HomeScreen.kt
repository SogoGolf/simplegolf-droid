// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeScreen.kt
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
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer

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

    val currentGolfer by homeViewModel.currentGolfer.collectAsState()
    val localGame by homeViewModel.localGame.collectAsState()
    val localCompetition by homeViewModel.localCompetition.collectAsState()
    val scrollState = rememberScrollState()

    ScreenWithDrawer(navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp)) // Space for top bar icons

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            // Welcome Card for current golfer
            currentGolfer?.let { golfer ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${golfer.firstName} ${golfer.surname}",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = "Golf Link: ${golfer.golfLinkNo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Next button
            Button(
                onClick = { navController.navigate(nextRoute) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Next")
            }

            // Add some bottom padding
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}