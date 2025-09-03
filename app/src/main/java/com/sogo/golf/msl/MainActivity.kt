package com.sogo.golf.msl

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sogo.golf.msl.app.lifecycle.AppLifecycleManager
import com.sogo.golf.msl.app.lifecycle.AppResumeAction
import com.sogo.golf.msl.domain.repository.remote.AuthRepository
import com.sogo.golf.msl.features.playing_partner.presentation.PlayingPartnerScreen
import com.sogo.golf.msl.features.competitions.presentation.CompetitionsScreen
import com.sogo.golf.msl.features.home.presentation.HomeScreen
import com.sogo.golf.msl.features.sogo_home.presentation.SogoGolfHomeScreen
import com.sogo.golf.msl.features.sogo_home.presentation.RoundsSummaryScreen
import com.sogo.golf.msl.features.sogo_home.presentation.RoundDetailScreen
import com.sogo.golf.msl.features.sogo_home.presentation.NationalLeaderboardsScreen
import com.sogo.golf.msl.features.sogo_home.presentation.CompetitionLeaderboardScreen
import com.sogo.golf.msl.features.login.presentation.LoginScreen
import com.sogo.golf.msl.features.login.presentation.WebAuthScreen
import com.sogo.golf.msl.features.play.presentation.PlayRoundScreen
import com.sogo.golf.msl.features.review_scores.presentation.ReviewScoresScreen
import com.sogo.golf.msl.features.splashscreen.presentation.SplashScreen
import com.sogo.golf.msl.navigation.NavViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.sogo.golf.msl.ui.theme.MSLGolfTheme
import kotlinx.coroutines.launch
import io.sentry.Sentry

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var appLifecycleManager: AppLifecycleManager

    private var navController: NavController? = null // ✅ Keep reference to NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ CONDITIONAL SPLASH SCREEN HANDLING based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ - use splash screen API to disable it
            installSplashScreen().apply {
                setKeepOnScreenCondition { false }
                setOnExitAnimationListener { it.remove() }
            }
        }
        // For Android 11 and below, no splash screen API exists, so nothing to disable

        // ✅ FORCE FONT SCALE TO 1.0 AND NORMALIZE DENSITY - ignores device font scaling and screen zoom
        enforceNormalFontScale()


        super.onCreate(savedInstanceState)

        setContent {
            MSLGolfTheme {
                val navController = rememberNavController()
                this@MainActivity.navController = navController

                val viewModel: NavViewModel = hiltViewModel()
                val authState by viewModel.authState.collectAsState()

                // State to control splash visibility
                var showSplash by rememberSaveable { mutableStateOf(true) }


                if (showSplash) {
                    // Show splash screen first
                    SplashScreen(
                        navController = navController,
                        onSplashComplete = {
                            showSplash = false // Hide splash, show main app
                        }
                    )
                } else {

                    // Only build back stack on cold start (savedInstanceState == null)
                    val shouldBuildBackStack = remember { savedInstanceState == null }

                    val startDestination = when {
                        !authState.isLoggedIn -> "login"
                        authState.hasActiveRound -> "playroundscreen"
                        else -> "homescreen"
                    }

                    var hasBuiltBackStack by remember { mutableStateOf(false) }

                    // Build the "virtual" back stack if starting at play round screen
                    LaunchedEffect(startDestination, shouldBuildBackStack) {
                        if (shouldBuildBackStack && startDestination == "playroundscreen" && !hasBuiltBackStack) {
                            navController.navigate("homescreen") {
                                popUpTo(0) { inclusive = true }
                            }
                            navController.navigate("competitionscreen")
                            navController.navigate("playingpartnerscreen")
                            navController.navigate("playroundscreen")
                            hasBuiltBackStack = true
                        }
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            SetPortraitOrientation()
                            LoginScreen(navController, viewModel)
                        }
                        composable("homescreen") {
                            SetPortraitOrientation()
                            HomeScreen(
                                navController = navController, 
                                title = "Home", 
                                nextRoute = "competitionscreen",
                                skipDataFetch = false,
                                onNavigateToCompetition = {
                                    navController.navigate("competitionscreen")
                                }
                            )
                        }
                        composable(
                            route = "homescreen?skipDataFetch={skipDataFetch}",
                            arguments = listOf(
                                navArgument("skipDataFetch") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val skipDataFetch = backStackEntry.arguments?.getBoolean("skipDataFetch") ?: false
                            SetPortraitOrientation()
                            HomeScreen(
                                navController = navController, 
                                title = "Home", 
                                nextRoute = "competitionscreen",
                                skipDataFetch = skipDataFetch,
                                onNavigateToCompetition = {
                                    navController.navigate("competitionscreen")
                                }
                            )
                        }
                        composable("competitionscreen") {
                            SetPortraitOrientation()
                            CompetitionsScreen(navController, "Competitions", "playingpartnerscreen")
                        }
                        composable("playingpartnerscreen") {
                            SetPortraitOrientation()
                            PlayingPartnerScreen(
                                navController,
                                "Playing Partner",
                                "playroundscreen"
                            )
                        }
                        composable("playroundscreen") {
                            SetUnspecifiedOrientation()
                            PlayRoundScreen(navController, viewModel)
                        }
                        composable("reviewscreen/{roundId}") { backStackEntry ->
                            SetUnspecifiedOrientation()
                            val roundId = backStackEntry.arguments?.getString("roundId") ?: ""
                            ReviewScoresScreen(navController, viewModel, roundId)
                        }
                        composable("webauth") {
                            SetPortraitOrientation()
                            WebAuthScreen(navController)
                        }
                        composable("sogogolfhomescreen") {
                            SetPortraitOrientation()
                            SogoGolfHomeScreen(navController)
                        }
                        composable("roundssummaryscreen") {
                            SetPortraitOrientation()
                            RoundsSummaryScreen(navController)
                        }
                        composable("rounddetailscreen/{roundId}") { backStackEntry ->
                            SetPortraitOrientation()
                            val roundId = backStackEntry.arguments?.getString("roundId") ?: ""
                            RoundDetailScreen(navController, roundId)
                        }
                        composable("nationalleaderboardsscreen") {
                            SetPortraitOrientation()
                            NationalLeaderboardsScreen(navController)
                        }
                        composable(
                            route = "competition_leaderboard/{competitionId}/{competitionName}",
                            arguments = listOf(
                                navArgument("competitionId") { type = NavType.StringType },
                                navArgument("competitionName") { type = NavType.StringType }
                            )
                        ) {
                            SetPortraitOrientation()
                            CompetitionLeaderboardScreen(navController)
                        }
                    }
                }
            }
        }
    }

    private fun enforceNormalFontScale() {
        val configuration = resources.configuration
        val metrics = resources.displayMetrics
        var configChanged = false
        
        if (configuration.fontScale != 1.0f) {
            configuration.fontScale = 1.0f
            configChanged = true
        }
        
        // Also normalize display density to prevent screen zoom from affecting layouts
        if (metrics.density != 1.0f) {
            metrics.density = 1.0f
            configChanged = true
        }
        
        if (configChanged) {
            metrics.scaledDensity = metrics.density * configuration.fontScale

            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, metrics)
        }
    }

    override fun onResume() {
        super.onResume()

        // ✅ ADD APP LIFECYCLE DATE CHECKING
        lifecycleScope.launch {
            android.util.Log.d("MainActivity", "=== ON RESUME - CHECKING DATE ===")

            when (appLifecycleManager.onAppResumed()) {
                AppResumeAction.Continue -> {
                    android.util.Log.d("MainActivity", "✅ Continuing normally - data is fresh")
                    // Continue normally, no action needed
                }

                AppResumeAction.NavigateToHome -> {
                    android.util.Log.d(
                        "MainActivity",
                        "🏠 Navigating to home - data was stale and refreshed"
                    )

                    // ✅ SAFE NAVIGATION - Check if NavController is ready
                    navController?.let { navCtrl ->
                        try {
                            // Check if the current destination exists (means NavHost is set up)
                            if (navCtrl.currentDestination != null) {
                                navCtrl.navigate("homescreen") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                android.util.Log.d("MainActivity", "NavController not ready yet, skipping navigation")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Navigation failed", e)
                        }
                    } ?: run {
                        android.util.Log.d("MainActivity", "NavController is null, skipping navigation")
                    }
                }
            }
        }
    }
}


@Composable
fun SetUnspecifiedOrientation() {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}

@Composable
fun SetPortraitOrientation() {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
