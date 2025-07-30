package com.sogo.golf.msl.shared_components.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NetworkMessageSnackbar(
    message: String?,
    isError: Boolean = false,
    textColor: Color? = null,
    backgroundColor: Color? = null,
    verticalAlignment: Alignment.Vertical = Alignment.Bottom,
    onDismiss: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = when (verticalAlignment) {
            Alignment.Top -> Alignment.TopCenter
            Alignment.CenterVertically -> Alignment.Center
            else -> Alignment.BottomCenter
        }
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp),
            snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = backgroundColor ?: if (isError)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    contentColor = textColor ?: if (isError)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )
    }
}
