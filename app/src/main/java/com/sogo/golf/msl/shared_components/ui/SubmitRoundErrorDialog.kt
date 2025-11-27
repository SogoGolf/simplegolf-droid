package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.ui.theme.MSLColors

@Composable
fun SubmitRoundErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Submission Error",
                color = MSLColors.mslBlue,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                style = TextStyle(color = MSLColors.mslBlue),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    color = MSLColors.mslGunMetal,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MSLColors.mslYellow,
                    contentColor = MSLColors.mslBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = null
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewSubmitRoundErrorDialog() {
    MaterialTheme {
        SubmitRoundErrorDialog(
            errorMessage = "Score submission failed. Please try again.",
            onDismiss = { }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewSubmitRoundErrorDialogLongMessage() {
    MaterialTheme {
        SubmitRoundErrorDialog(
            errorMessage = "The round could not be saved to SimpleGolf. Please check your internet connection and try again. If the problem persists, please contact support.",
            onDismiss = { }
        )
    }
}
