package com.sogo.golf.msl.features.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.features.login.components.SearchableClubDropdown
import com.sogo.golf.msl.navigation.NavViewModel
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow
import com.sogo.golf.msl.ui.theme.MSLTypography

@Composable
fun LoginScreen(
    navController: NavController,
    navViewModel: NavViewModel,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current


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

    // Set status bar and navigation bar colors to match the screen background
    LaunchedEffect(Unit) {
        val window = (view.context as androidx.activity.ComponentActivity).window
        val windowInsetsController = WindowCompat.getInsetsController(window, view)

        // Set status bar and navigation bar to match MSLColors.PrimaryDark (#054868)
        window.statusBarColor = android.graphics.Color.parseColor("#032F45")
        window.navigationBarColor = android.graphics.Color.parseColor("#032F45")

        // Set status bar content to light (white icons) since we're using dark background
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false
    }

    // Reset system bar colors when leaving this screen
    DisposableEffect(Unit) {
        onDispose {
            val window = (view.context as androidx.activity.ComponentActivity).window
            val windowInsetsController = WindowCompat.getInsetsController(window, view)

            // Reset to transparent/system default
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            windowInsetsController.isAppearanceLightStatusBars = true
            windowInsetsController.isAppearanceLightNavigationBars = true
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
            .background(MSLColors.PrimaryDark)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Dismiss keyboard and clear focus when tapping anywhere
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Move image to top and make it smaller
        Spacer(modifier = Modifier.height(32.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.simple_golf_transparent)
                .memoryCacheKey("top_logo")
                .build(),
            contentDescription = "SimpleGolf Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(125.dp) // 50% smaller than original size
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Welcome",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            //fontSize = MaterialTheme.typography.headlineLarge.fontSize.clampFontScale(minScale = 1f, maxScale = 1.1f),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth() // Make text fill the width of its container
                .padding(start = 16.dp)
                .align(Alignment.Start) // Align the text to the start (left) of the screen
                //.padding(start = 0.dp)
        )
        Text(
            "Get started by finding your home club",
            modifier = Modifier
                .fillMaxWidth() // Make text fill the width of its container
                .padding(start = 16.dp, top = 5.dp, bottom = 10.dp)
                .align(Alignment.Start), // Align the text to the start (left) of the screen

            //fontSize = MaterialTheme.typography.headlineSmall.fontSize.clampFontScale(minScale = 1f, maxScale = 1.1f),
            color = Color.White
        )

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
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        loginViewModel.startWebAuth()
                    },
                    enabled = uiState.selectedClub != null && !uiState.isLoadingClubs,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // Yellow button
                        contentColor = Color.Black
                    )
                ) {
                    Text("Continue")
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

//                val selectedClub = uiState.selectedClub
//                if (selectedClub != null) {
//                    Text(
//                        text = "Selected: ${selectedClub.name}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.White.copy(alpha = 0.8f)
//                    )
//                }
            }
        }
    }
}

@Composable
fun CenteredYellowButton(
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth(), // Ensures the button takes up full width
        colors = ButtonDefaults.buttonColors(containerColor = mslYellow),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp) // Adjust padding to center text vertically
    ) {
        Text(
            text = text,
            style = textStyle,
            color = mslBlack,
            fontWeight = FontWeight.Normal,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize, //.clampFontScale(minScale = 1f, maxScale = 1.1f),
            modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1.0f else 0.5f), // Makes the Text span the button's full width
            textAlign = TextAlign.Center // Centers text horizontally within the button

        )
    }
}