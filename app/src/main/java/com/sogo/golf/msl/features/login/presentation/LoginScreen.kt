package com.sogo.golf.msl.features.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.features.login.components.SearchableClubDropdown
import com.sogo.golf.msl.navigation.NavViewModel
import com.sogo.golf.msl.ui.theme.MSLColors

@Composable
fun LoginScreen(
    navController: NavController,
    navViewModel: NavViewModel,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()

    // Handle auth success
    LaunchedEffect(Unit) {
        loginViewModel.authSuccessEvent.collect {
            navController.navigate("homescreen") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Handle navigation to web auth
    LaunchedEffect(Unit) {
        loginViewModel.navigateToWebAuth.collect { authUrl ->
            navController.navigate("webauth")
        }
    }

    // Show error dialog
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { loginViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { loginViewModel.clearError() }) {
                    Text("OK")
                }
            },
            dismissButton = if (error.contains("Failed to load clubs")) {
                {
                    Button(onClick = {
                        loginViewModel.clearError()
                        loginViewModel.retryLoadClubs()
                    }) {
                        Text("Retry")
                    }
                }
            } else null
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MSLColors.PrimaryDark) // NEW: Set background to primary dark
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SimpleGolf Login",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White // NEW: White text for dark background
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Club Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MSLColors.PrimaryDark
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Your Golf Club",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    uiState.isLoadingClubs -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Loading clubs...",
                                color = Color.White
                            )
                        }
                    }

                    uiState.clubs.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No clubs available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { loginViewModel.retryLoadClubs() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }

                    else -> {
                        SearchableClubDropdown(
                            clubs = uiState.clubs,
                            selectedClub = uiState.selectedClub,
                            onClubSelected = { club ->
                                loginViewModel.selectClub(club)
                            },
                            isLoading = uiState.isLoadingClubs
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Authentication buttons
        if (uiState.isProcessingAuth) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Processing authentication...",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Exchanging tokens with MSL API",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.simple_golf_transparent)
                        .memoryCacheKey("center_logo")
                        .build(),
                    contentDescription = "Your Image Description",
                    contentScale = ContentScale.Fit,
                )

                Button(
                    onClick = {
                        loginViewModel.startWebAuth()
                    },
                    enabled = uiState.selectedClub != null && !uiState.isLoadingClubs,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // Yellow button
                        contentColor = Color.Black
                    )
                ) {
                    Text("Login with MSL")
                }

                Spacer(modifier = Modifier.height(16.dp))

//                OutlinedButton(
//                    onClick = {
//                        navViewModel.login()
//                        navController.navigate("homescreen") {
//                            popUpTo("login") { inclusive = true }
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        contentColor = Color.White
//                    ),
//                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
//                ) {
//                    Text("Quick Login (Skip Auth)")
//                }

                Spacer(modifier = Modifier.height(24.dp))

                val selectedClub = uiState.selectedClub
                if (selectedClub != null) {
                    Text(
                        text = "Selected: ${selectedClub.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}