package com.sogo.golf.msl.features.splashscreen.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.sogo.golf.msl.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    onSplashComplete: () -> Unit
) {
    val view = LocalView.current

    // Set status bar and navigation bar to match blue background
    LaunchedEffect(Unit) {
        val window = (view.context as androidx.activity.ComponentActivity).window
        val windowInsetsController = WindowCompat.getInsetsController(window, view)

        // Make status bar and navigation bar match the blue background
        window.statusBarColor = android.graphics.Color.parseColor("#054868") // Blue color
        window.navigationBarColor = android.graphics.Color.parseColor("#054868") // Blue color

        // Set status bar content to light (white icons)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false

        delay(2000) // 2 second splash
        onSplashComplete() // Signal that splash is done

        // Reset status bar and navigation bar colors after splash
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF054868)) // Blue background to match status/nav bars
            .systemBarsPadding(), // Handle system bar padding
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.simple_golf_transparent),
            contentDescription = "SimpleGolf Logo",
            modifier = Modifier
                .size(250.dp)
                .padding(32.dp),
            contentScale = ContentScale.Fit
        )
    }
}