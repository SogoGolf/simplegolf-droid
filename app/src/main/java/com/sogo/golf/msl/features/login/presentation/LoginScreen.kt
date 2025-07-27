package com.sogo.golf.msl.features.login.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.navigation.NavViewModel

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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SimpleGolf Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Club Selection Card
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
                    text = "Select Your Golf Club",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    uiState.isLoadingClubs -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading clubs...")
                        }
                    }

                    uiState.clubs.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No clubs available",
                                style = MaterialTheme.typography.bodyMedium
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
                        ClubDropdown(
                            clubs = uiState.clubs,
                            selectedClub = uiState.selectedClub,
                            onClubSelected = { club ->
                                loginViewModel.selectClub(club)
                            }
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
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Processing authentication...")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Exchanging tokens with MSL API",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        loginViewModel.startWebAuth()
                    },
                    enabled = uiState.selectedClub != null && !uiState.isLoadingClubs,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login with MSL")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        navViewModel.login()
                        navController.navigate("homescreen") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quick Login (Skip Auth)")
                }

                Spacer(modifier = Modifier.height(24.dp))

                val selectedClub = uiState.selectedClub
                if (selectedClub != null) {
                    Text(
                        text = "Selected: ${selectedClub.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubDropdown(
    clubs: List<com.sogo.golf.msl.domain.model.msl.MslClub>,
    selectedClub: com.sogo.golf.msl.domain.model.msl.MslClub?,
    onClubSelected: (com.sogo.golf.msl.domain.model.msl.MslClub) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // ✅ Sort clubs alphabetically by name
    val sortedClubs = remember(clubs) {
        clubs.sortedBy { it.name.lowercase() }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedClub?.name ?: "Select a club",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown arrow"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // ✅ Use sortedClubs instead of clubs
            sortedClubs.forEach { club ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(club.name)
                            Text(
                                text = "ID: ${club.clubId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    onClick = {
                        onClubSelected(club)
                        expanded = false
                    }
                )
            }
        }
    }
}