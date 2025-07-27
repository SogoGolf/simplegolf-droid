package com.sogo.golf.msl.features.competitions.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer

@Composable
fun CompetitionsScreen(navController: NavController, title: String, nextRoute: String,
                       competitionViewModel: CompetitionViewModel = hiltViewModel()) {

    val currentMslGolfer by competitionViewModel.currentGolfer.collectAsState()

    ScreenWithDrawer(navController = navController) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✅ DISPLAY GOLFER NAME
            currentMslGolfer?.let { golfer ->
                Text(
                    text = "Welcome ${golfer.firstName} ${golfer.surname}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(text = title)
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                if (navController.previousBackStackEntry != null) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Button(onClick = { navController.navigate(nextRoute) }) {
                    Text("Next")
                }
            }
        }
    }
}