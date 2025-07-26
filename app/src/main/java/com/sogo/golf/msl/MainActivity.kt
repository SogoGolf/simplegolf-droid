package com.sogo.golf.msl

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sogo.golf.msl.domain.repository.AuthRepository
import com.sogo.golf.msl.features.choose_playing_partner.presentation.ChoosePlayingPartnerScreen
import com.sogo.golf.msl.features.competitions.presentation.CompetitionsScreen
import com.sogo.golf.msl.features.home.presentation.HomeScreen
import com.sogo.golf.msl.features.login.presentation.LoginScreen
import com.sogo.golf.msl.features.login.presentation.WebAuthScreen
import com.sogo.golf.msl.features.play.presentation.PlayRoundScreen
import com.sogo.golf.msl.features.review_scores.presentation.ReviewScoresScreen
import com.sogo.golf.msl.navigation.NavViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.sogo.golf.msl.ui.theme.MSLGolfTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MSLGolfTheme {
                val navController = rememberNavController()
                val viewModel: NavViewModel = hiltViewModel()
                val authState by viewModel.authState.collectAsState()

                // Only build back stack on cold start (savedInstanceState == null)
                val shouldBuildBackStack = remember { savedInstanceState == null }

                val startDestination = when {
                    !authState.isLoggedIn -> "login"
                    authState.hasFinishedRound -> "playroundscreen"
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
                        navController.navigate("choosepartnerscreen")
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
                        HomeScreen(navController, "Home", "competitionscreen")
                    }
                    composable("competitionscreen") {
                        SetPortraitOrientation()
                        CompetitionsScreen(navController, "Competitions", "choosepartnerscreen")
                    }
                    composable("choosepartnerscreen") {
                        SetPortraitOrientation()
                        ChoosePlayingPartnerScreen(navController, "Choose Partner", "playroundscreen")
                    }
                    composable("playroundscreen") {
                        SetUnspecifiedOrientation()
                        PlayRoundScreen(navController, viewModel)
                    }
                    composable("reviewscreen") {
                        SetUnspecifiedOrientation()
                        ReviewScoresScreen(navController, viewModel)
                    }
                    composable("webauth") {
                        SetPortraitOrientation()
                        WebAuthScreen(navController)
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