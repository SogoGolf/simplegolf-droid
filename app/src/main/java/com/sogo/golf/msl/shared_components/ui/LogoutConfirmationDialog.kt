package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack
import com.sogo.golf.msl.ui.theme.MSLColors.mslRed
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else mslBlack
    val headingColor = if (isDarkTheme) Color.White else mslBlack

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Logout Confirmation",
                color = headingColor,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                style = TextStyle(color = headingColor),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Are you sure you want to logout?",
                    color = textColor,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "All your local data including rounds, scores, and transaction history will be permanently deleted from this device.",
                    color = mslRed,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = mslRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "Yes, Logout",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = textColor
                )
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
