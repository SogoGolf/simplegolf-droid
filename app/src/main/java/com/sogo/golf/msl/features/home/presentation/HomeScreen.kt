// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeScreen.kt
package com.sogo.golf.msl.features.home.presentation

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslWhite
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow
import com.sogo.golf.msl.features.sogo_home.presentation.components.GolferDataConfirmationSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    skipDataFetch: Boolean = false,
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCompetition: () -> Unit
) {
    // Prevent going back from home screen
    BackHandler {
        // Do nothing - home is the root screen
    }

    val currentGolfer by homeViewModel.currentGolfer.collectAsState()
    val localGame by homeViewModel.localGame.collectAsState()
    val localCompetition by homeViewModel.localCompetition.collectAsState()
    val sogoGolfer by homeViewModel.sogoGolfer.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    // Collect the updateState properly
    val updateState by homeViewModel.updateState.collectAsState()

    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val view = LocalView.current

    val context = LocalContext.current
    val activity = context as Activity

    var shouldStartCompetition by remember { mutableStateOf(false) }
    var showGolferDataConfirmationSheet by remember { mutableStateOf(false) }

    // Initialize the ViewModel with skipDataFetch parameter
    LaunchedEffect(skipDataFetch) {
        homeViewModel.setSkipDataFetch(skipDataFetch)
    }

    val updateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        homeViewModel.handleUpdateResult(result)
    }

    // Handle navigation after update check
    LaunchedEffect(shouldStartCompetition, updateState) {
        if (shouldStartCompetition) {
            when {
                updateState.isCheckingForUpdate -> {
                    // Still checking, wait...
                }
                updateState.updateAvailable -> {
                    // Update required - AppUpdateManager handles this
                    // Reset flag since we're not navigating
                    shouldStartCompetition = false
                }
                !updateState.isCheckingForUpdate && !updateState.updateAvailable -> {
                    // No update needed - navigate
                    shouldStartCompetition = false
                    onNavigateToCompetition()
                }
                updateState.updateError != null -> {
                    // Error occurred - reset flag and let user see error
                    shouldStartCompetition = false
                }
            }
        }
    }

    // Set status bar color to match blue background
    SideEffect {
        val window = (view.context as? androidx.activity.ComponentActivity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        window.decorView.setBackgroundColor(mslBlue.toArgb())
        @Suppress("DEPRECATION")
        window.statusBarColor = mslBlue.toArgb()
        windowInsetsController.isAppearanceLightStatusBars = false
    }

    ScreenWithDrawer(navController = navController, buttonColor = Color.White) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mslBlue)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(start = 0.dp, end = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                // Space for the hamburger menu from ScreenWithDrawer
                Spacer(modifier = Modifier.height(76.dp))

                // Logo
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.simple_golf_transparent)
                        .memoryCacheKey("simple_golf_logo_home")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .width(200.dp)
                        .height(100.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Let's Play title
                Text(
                    text = "Let's Play",
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = mslWhite,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Start a competition round. Tap SOGO Golf for leaderboards and round history",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    color = mslWhite,
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                        .align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Start Competition Round button
                Button(
                    onClick = {
        // ✅ NEW: Check terms acceptance before version update
        Log.d("HomeScreen", "=== BUTTON CLICK DEBUG ===")
        Log.d("HomeScreen", "currentGolfer: ${currentGolfer}")
        Log.d("HomeScreen", "currentGolfer.golfLinkNo: ${currentGolfer?.golfLinkNo}")
        Log.d("HomeScreen", "localGame: ${localGame}")
        Log.d("HomeScreen", "localGame.golflinkNumber: ${localGame?.golflinkNumber}")
        Log.d("HomeScreen", "sogoGolfer: ${sogoGolfer}")
        Log.d("HomeScreen", "sogoGolfer.appSettings: ${sogoGolfer?.appSettings}")
        Log.d("HomeScreen", "sogoGolfer.appSettings.isAcceptedSogoTermsAndConditions: ${sogoGolfer?.appSettings?.isAcceptedSogoTermsAndConditions}")
        
        val termsAccepted = sogoGolfer?.appSettings?.isAcceptedSogoTermsAndConditions ?: false
        Log.d("HomeScreen", "termsAccepted final value: $termsAccepted")
        
        if (!termsAccepted) {
            Log.d("HomeScreen", "Terms not accepted - showing confirmation sheet")
            showGolferDataConfirmationSheet = true
        } else {
            Log.d("HomeScreen", "Terms accepted - proceeding with competition start")
            shouldStartCompetition = true
                            // Call the modified method with both callbacks
                            homeViewModel.checkForUpdatesAndStartCompetition(
                                activity = activity,
                                activityResultLauncher = updateLauncher,
                                onUpdateCheckComplete = {
                                    // Update check completed - if we reach here, update was required
                                    // UI should show loading state until update finishes
                                },
                                onNoUpdateRequired = {
                                    // This is handled in LaunchedEffect above
                                }
                            )
                        }
                    },
                    enabled = !updateState.isCheckingForUpdate && !homeUiState.isLoading && homeViewModel.hasRequiredData(), // Disable while checking or loading initial data
                    modifier = Modifier
                        .padding(horizontal = screenWidth * 0.15f)
                        .height(80.dp)
                        .fillMaxWidth()
                        .then(
                            if (updateState.isCheckingForUpdate) {
                                Modifier.border(
                                    width = 0.5.dp,
                                    color = Color.Gray.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                Modifier
                            }
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mslYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    // Show different text based on state
                    if (updateState.isCheckingForUpdate) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Checking for updates...",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "Start Home Club Competition Round",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Show error message if update fails
                updateState.updateError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Update Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Dismiss button
                                OutlinedButton(
                                    onClick = { homeViewModel.clearUpdateError() }
                                ) {
                                    Text("Dismiss")
                                }
                                // Retry button
                                Button(
                                    onClick = {
                                        homeViewModel.clearUpdateError()
                                        shouldStartCompetition = true
                                        homeViewModel.checkForUpdatesAndStartCompetition(
                                            activity = activity,
                                            activityResultLauncher = updateLauncher,
                                            onUpdateCheckComplete = { },
                                            onNoUpdateRequired = { }
                                        )
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // Global loading overlay with spinner and progress message
            if (homeUiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        homeUiState.progressMessage?.let { msg ->
                            Text(text = msg, color = Color.White)
                        }
                        homeUiState.progressPercent?.let { pct ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$pct%", color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }

            // SOGO banner at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable {
                        // Add navigation to SOGO home screen if needed
                        navController.navigate("sogogolfhomescreen")
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.sogo_banner_block)
                        .memoryCacheKey("sogo_banner")
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth().height(screenWidth * 0.65f)
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.sogo_logo_with_tag_line)
                        .memoryCacheKey("sogo_logo_tagline")
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .width(screenWidth * 0.9f)
                        .padding(top = 15.dp)
                )
            }

            // ✅ NEW: Show GolferDataConfirmationSheet when terms not accepted
            @OptIn(ExperimentalMaterial3Api::class)
            currentGolfer?.let { golfer ->
                if (showGolferDataConfirmationSheet) {
                    val bottomSheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )

                    ModalBottomSheet(
                        onDismissRequest = {
                            showGolferDataConfirmationSheet = false
                        },
                        sheetState = bottomSheetState,
                    ) {
                        GolferDataConfirmationSheet(
                            viewModel = homeViewModel,
                            mslGolfer = golfer,
                            sogoGolfer = sogoGolfer, // Pass existing SOGO golfer data
                            onDismiss = {
                                showGolferDataConfirmationSheet = false
                                // After terms are accepted, proceed with competition start
                                shouldStartCompetition = true
                                homeViewModel.checkForUpdatesAndStartCompetition(
                                    activity = activity,
                                    activityResultLauncher = updateLauncher,
                                    onUpdateCheckComplete = { },
                                    onNoUpdateRequired = { }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
