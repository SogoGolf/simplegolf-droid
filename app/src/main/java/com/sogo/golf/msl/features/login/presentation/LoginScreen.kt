package com.sogo.golf.msl.features.login.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import com.sogo.golf.msl.navigation.NavViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: NavViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login Screen", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.login()
                navController.navigate("homescreen") {
                    popUpTo("login") { inclusive = true }
                }
            }
        ) {
            Text("Quick Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("webauth")
            }
        ) {
            Text("Web Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Login to access the app",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
