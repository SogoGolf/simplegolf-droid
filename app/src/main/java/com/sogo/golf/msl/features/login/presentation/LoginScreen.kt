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
import androidx.compose.ui.platform.LocalConfiguration
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

    // Get screen dimensions for responsive sizing
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp

    // Calculate responsive image size (15% of screen width, min 80dp, max 200dp)
    val imageSize = (screenWidthDp * 0.45f).coerceIn(80.dp, 200.dp)

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

    // Set system bar colors - this runs every time the screen is displayed
    LaunchedEffect(view) {
        val window = (view.context as androidx.activity.ComponentActivity).window
        val windowInsetsController = WindowCompat.getInsetsController(window, view)

        // Set status bar and navigation bar to MSL blue (#054868)
        window.statusBarColor = android.graphics.Color.parseColor("#032F45")
        window.navigationBarColor = android.graphics.Color.parseColor("#032F45")

        // Set status bar content to light (white icons) since we're using dark background
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false
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

    Scaffold(
        containerColor = MSLColors.PrimaryDark,
        bottomBar = {
            if (!uiState.isProcessingAuth) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 30.dp, vertical = 30.dp)
                ) {
                    CenteredYellowButton(
                        text = "Continue",
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        enabled = uiState.selectedClub != null && !uiState.isLoadingClubs,
                        onClick = { loginViewModel.startWebAuth() }

                    )
                }
            }
        }
    ) { innerPadding ->
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
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Move image to top with responsive sizing
            Spacer(modifier = Modifier.height(32.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.simple_golf_transparent)
                    .memoryCacheKey("top_logo")
                    .build(),
                contentDescription = "SimpleGolf Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(imageSize) // Responsive size based on screen width
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Welcome",
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .align(Alignment.Start)
            )
            Text(
                "Get started by finding your home club",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 5.dp, bottom = 10.dp)
                    .align(Alignment.Start),
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

            // Authentication progress content
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
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = mslYellow),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = textStyle,
            color = mslBlack,
            fontWeight = FontWeight.Normal,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1.0f else 0.5f),
            textAlign = TextAlign.Center
        )
    }
}