package com.sogo.golf.msl.features.splashscreen.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    onSplashComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 second splash
        onSplashComplete() // Just signal that splash is done
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.simplegolf_splashlogo),
            contentDescription = "SimpleGolf Logo",
            modifier = Modifier
                .size(200.dp)
                .padding(32.dp),
            contentScale = ContentScale.Fit
        )
    }
}