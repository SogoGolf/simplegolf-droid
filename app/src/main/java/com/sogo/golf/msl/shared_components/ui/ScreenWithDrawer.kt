// app/src/main/java/com/sogo/golf/msl/shared_components/ui/ScreenWithDrawer.kt
package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sogo.golf.msl.features.debug.presentation.DebugScreen
import kotlinx.coroutines.launch

@Composable
fun ScreenWithDrawer(
    navController: NavController,
    topBar: @Composable () -> Unit = {}, // Optional custom top bar
    bottomBar: @Composable () -> Unit = {}, // Optional bottom bar
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showDebugScreen by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            content()

            // Bottom bar overlay (anchored to bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                bottomBar()
            }

            // Top bar overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Custom top bar if provided
                topBar()

                // Hamburger menu button (always on top left)
                IconButton(
                    onClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp, top = 4.dp) // Slight padding for visual balance
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Debug icon (always on top right)
                IconButton(
                    onClick = { showDebugScreen = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = 4.dp) // Slight padding for visual balance
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Debug Tools"
                    )
                }
            }
        }
    }

    // Show debug screen as modal
    if (showDebugScreen) {
        DebugScreen(
            onDismiss = { showDebugScreen = false }
        )
    }
}
