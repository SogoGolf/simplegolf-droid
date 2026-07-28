// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeScreen.kt
package com.sogo.golf.msl.features.home.presentation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.features.sogo_home.presentation.components.GolferDataConfirmationSheet
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslWhite
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow

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

    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val view = LocalView.current

    val context = LocalContext.current
    val activity = context as Activity

    var showGolferDataConfirmationSheet by remember { mutableStateOf(false) }

    // Initialize the ViewModel with skipDataFetch parameter
    LaunchedEffect(skipDataFetch) {
        homeViewModel.setSkipDataFetch(skipDataFetch)
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
            onNavigateToCompetition()
                        }
                    },
                    enabled = (!homeUiState.isLoading && homeViewModel.hasRequiredData()).also { enabled ->
                        Log.d("HomeScreen", "🔴 BUTTON STATE: enabled=$enabled, currentGolfer.golfLinkNo='${currentGolfer?.golfLinkNo}', localGame.golflinkNumber='${localGame?.golflinkNumber}'")
                    }, // Disable while loading initial data
                    modifier = Modifier
                        .padding(horizontal = screenWidth * 0.15f)
                        .height(80.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mslYellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Home Club Competition Round",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }

                // Show message when button is disabled due to missing golflink number
                if (!homeUiState.isLoading &&
                    currentGolfer?.golfLinkNo.isNullOrBlank() == true) {
                    Text(
                        text = "Your GolfLink number was not provided to the app. Please contact your club to ensure this is set up on your profile.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 18.dp)
                    )
                }

                // Show error message when SOGO data fetch fails
                if (!homeUiState.isLoading && homeUiState.errorMessage != null) {
                    Text(
                        text = homeUiState.errorMessage ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 18.dp)
                    )
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
                    .clickable(enabled = !homeUiState.isLoading) {
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
                    // Track when the confirmation sheet is displayed
                    LaunchedEffect(showGolferDataConfirmationSheet) {
                        homeViewModel.trackConfirmGolferDataDisplayed(golfer, sogoGolfer)
                    }
                    
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
                                // After terms are accepted, navigate to competition
                                onNavigateToCompetition()
                            }
                        )
                    }
                }
            }
        }
    }
}
