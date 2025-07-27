package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.ui.theme.AccessibleText
import com.sogo.golf.msl.ui.theme.GolferNameText
import com.sogo.golf.msl.ui.theme.MSLTypography


@Composable
fun AppDrawer(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    drawerViewModel: DrawerViewModel = hiltViewModel() // Own ViewModel
) {

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
        drawerViewModel.currentGolfer.let { golfer ->
            GolferNameText(
                firstName = golfer.value?.firstName ?: "-",
                lastName = golfer.value?.surname ?: "-",
                isCurrentPlayer = true
            )

            // ✅ Use accessible text with proper styling
            AccessibleText(
                text = "Golf Link: ${golfer.value?.golfLinkNo ?: "-"}",
                style = MSLTypography.caption,
                contentDescription = "Golf Link Number: ${golfer.value?.golfLinkNo ?: "-"}"
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

