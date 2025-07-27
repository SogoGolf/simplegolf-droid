package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import com.sogo.golf.msl.features.home.presentation.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun ScreenWithDrawer(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ✅ GET THE USE CASE
    val getCurrentGolferUseCase: GetMslGolferUseCase = hiltViewModel<HomeViewModel>().getMslGolferUseCase

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                getMslGolferUseCase = getCurrentGolferUseCase,
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()

            // Add hamburger button overlay (optional)
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
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    }
}