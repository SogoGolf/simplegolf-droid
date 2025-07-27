package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase

@Composable
fun AppDrawer(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    getMslGolferUseCase: GetMslGolferUseCase
) {
    val currentGolfer by getMslGolferUseCase()
        .collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f) // 60% of screen width
            .background(Color.Red)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // ✅ DISPLAY GOLFER NAME
        currentGolfer?.let { golfer ->
            Text(
                text = "${golfer.firstName} ${golfer.surname}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Menu Items
        DrawerMenuItem(
            text = "Home",
            onClick = {
                navController.navigate("homescreen") {
                    popUpTo("homescreen") { inclusive = true }
                }
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            text = "Competitions",
            onClick = {
                navController.navigate("competitionscreen")
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            text = "Choose Partner",
            onClick = {
                navController.navigate("choosepartnerscreen")
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            text = "Play Round",
            onClick = {
                navController.navigate("playroundscreen")
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            text = "Review Scores",
            onClick = {
                navController.navigate("reviewscreen")
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Logout at bottom
        DrawerMenuItem(
            text = "Logout",
            onClick = {
                // Handle logout - you'll need to pass the viewModel or create a logout callback
                onCloseDrawer()
            }
        )
    }
}

