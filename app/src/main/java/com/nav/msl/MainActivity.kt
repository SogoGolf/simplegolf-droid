package com.nav.msl

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val viewModel: NavViewModel = hiltViewModel()

                // Determine start destination based on login status and finished round
                val startDestination = when {
                    !authManager.isUserLoggedIn() -> "login"
                    authManager.isFinishedRound() -> "screen4"
                    else -> "screen1"
                }

                var hasBuiltBackStack by remember { mutableStateOf(false) }

                // Build the virtual back stack if starting at screen4
                LaunchedEffect(startDestination) {
                    if (startDestination == "screen4" && !hasBuiltBackStack) {
                        // User is logged in and finished round, build full back stack for screen4
                        // Navigate through the flow to build proper back stack
                        navController.navigate("screen1") {
                            popUpTo("screen4") { inclusive = true }
                        }
                        navController.navigate("screen2")
                        navController.navigate("screen3")
                        navController.navigate("screen4")

                        hasBuiltBackStack = true
                    } else if (startDestination == "screen1" && !hasBuiltBackStack) {
                        // User is logged in but not finished round, start at screen1 normally
                        hasBuiltBackStack = true
                    }
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        SetPortraitOrientation()
                        LoginScreen(navController, viewModel)
                    }
                    composable("screen1") {
                        SetPortraitOrientation()
                        Screen(navController, "Screen 1", "screen2")
                    }
                    composable("screen2") {
                        SetPortraitOrientation()
                        Screen(navController, "Screen 2", "screen3")
                    }
                    composable("screen3") {
                        SetPortraitOrientation()
                        Screen(navController, "Screen 3", "screen4")
                    }
                    composable("screen4") {
                        SetUnspecifiedOrientation()
                        Screen4(navController, viewModel)
                    }
                    composable("screen5") {
                        SetUnspecifiedOrientation()
                        Screen5(navController, viewModel)
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