package com.nav.testing


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen4(
    navController: NavController,
    viewModel: NavViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Show scorecard in landscape
        ScorecardScreen()
    } else {
        // Show normal Screen4 content in portrait
        Screen4Portrait(navController, viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Screen4Portrait(
    navController: NavController,
    viewModel: NavViewModel
) {
    val backNavDisabled by viewModel.backNavDisabled.collectAsState()
    val simulateError by viewModel.simulateError.collectAsState()
    val finishedRound by viewModel.finishedRound.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (backNavDisabled) return@BackHandler

        // Normal back navigation - will go to Screen3, then Screen2, etc.
        if (!isLoading) {
            isLoading = true
            coroutineScope.launch {
                delay(2000) // Simulate API delay

                if (simulateError) {
                    isLoading = false
                    showDialog = true
                } else {
                    isLoading = false
                    navController.popBackStack() // This will go to Screen3!
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Error") },
            text = { Text("❌ Failed to go back.") }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen 4") },
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
                Text("Back navigation is ${if (backNavDisabled) "disabled" else "enabled"}")

                Spacer(modifier = Modifier.height(16.dp))

                Switch(
                    checked = backNavDisabled,
                    onCheckedChange = { viewModel.setBackNavDisabled(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Toggle to block or allow back navigation")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Simulated error is ${if (simulateError) "enabled" else "disabled"}")

                Spacer(modifier = Modifier.height(16.dp))

                Switch(
                    checked = simulateError,
                    onCheckedChange = { viewModel.setSimulateError(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Toggle to simulate API error or success")

                Spacer(modifier = Modifier.height(16.dp))

                // Finished Round toggle
                Text("Finished Round is ${if (finishedRound) "ON" else "OFF"}")

                Spacer(modifier = Modifier.height(16.dp))

                Switch(
                    checked = finishedRound,
                    onCheckedChange = { viewModel.setFinishedRound(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("When ON: App restarts at Screen 4")
                Text("When OFF: App restarts at Screen 1", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "💡 Rotate to landscape to see Scorecard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate("screen5") }
                ) {
                    Text("Go to Screen 5")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Try the back button - it should go to Screen 3!",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp))
            }
        }
    }
}