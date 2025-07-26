package com.sogo.golf.msl.features.review_scores.presentation


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sogo.golf.msl.features.play.presentation.ScorecardScreen
import com.sogo.golf.msl.navigation.NavViewModel

@Composable
fun ReviewScoresScreen(navController: NavController, viewModel: NavViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Show scorecard in landscape
        ScorecardScreen()
    } else {
        // Show normal Screen5 content in portrait
        ReviewScoresPortrait(navController, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewScoresPortrait(navController: NavController, viewModel: NavViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ReviewScores") },
                actions = {
                    // Logout button
                    Button(
                        onClick = { viewModel.logout(navController) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Welcome to ReviewScores")

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { navController.popBackStack() }) {
                    Text("Back to Play Round")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "You can logout from the top-right button",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "💡 Rotate to landscape to see Scorecard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}