// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeScreen.kt
package com.sogo.golf.msl.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslWhite
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow

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

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val view = LocalView.current

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

    ScreenWithDrawer(navController = navController) {
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

                Spacer(modifier = Modifier.height(36.dp))

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

                Spacer(modifier = Modifier.height(36.dp))

                // Start Competition Round button
                Button(
                    onClick = { navController.navigate(nextRoute) },
                    modifier = Modifier
                        .padding(horizontal = screenWidth * 0.15f)
                        .height(80.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mslYellow,
                        contentColor = Color.Black
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Home Club Competition Round",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            // SOGO banner at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable {
                        // Add navigation to SOGO home screen if needed
                        // navigationManager.navigateToSogoHomeScreen()
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
        }
    }
}

//// app/src/main/java/com/sogo/golf/msl/features/home/presentation/HomeScreen.kt
//package com.sogo.golf.msl.features.home.presentation
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.activity.compose.BackHandler
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
//
//@Composable
//fun HomeScreen(
//    navController: NavController,
//    title: String,
//    nextRoute: String,
//    homeViewModel: HomeViewModel = hiltViewModel()
//) {
//    // Prevent going back from home screen
//    BackHandler {
//        // Do nothing - home is the root screen
//    }
//
//    val currentGolfer by homeViewModel.currentGolfer.collectAsState()
//    val localGame by homeViewModel.localGame.collectAsState()
//    val localCompetition by homeViewModel.localCompetition.collectAsState()
//    val scrollState = rememberScrollState()
//
//    ScreenWithDrawer(navController = navController) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(scrollState)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(48.dp)) // Space for top bar icons
//
//            Text(
//                text = title,
//                style = MaterialTheme.typography.headlineMedium
//            )
//
//            // Welcome Card for current golfer
//            currentGolfer?.let { golfer ->
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.primaryContainer
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                    ) {
//                        Text(
//                            text = "Welcome back!",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Text(
//                            text = "${golfer.firstName} ${golfer.surname}",
//                            style = MaterialTheme.typography.headlineSmall
//                        )
//
//                        Text(
//                            text = "Golf Link: ${golfer.golfLinkNo}",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//
//            // Next button
//            Button(
//                onClick = { navController.navigate(nextRoute) },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Next")
//            }
//
//            // Add some bottom padding
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//    }
//}