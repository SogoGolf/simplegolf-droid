package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow

@Composable
fun SubmitRoundSuccessDialog(
    playingPartnerName: String,
    onDone: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val imageSize = screenWidth * 0.5f

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else mslBlack
    val headingColor = if (isDarkTheme) Color.White else mslBlack

    AlertDialog(
        onDismissRequest = { },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Submit Round",
                    color = headingColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    style = TextStyle(color = headingColor),
                    modifier = Modifier.padding(bottom = 26.dp, top = 10.dp)
                )
                Text(
                    text = "The round for $playingPartnerName has been successfully submitted.",
                    color = textColor,
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.greentick)
                        .memoryCacheKey("success_greentick")
                        .build(),
                    contentDescription = "Success",
                    modifier = Modifier
                        .size(imageSize),
                    contentScale = ContentScale.Fit,
                )
                Button(
                    onClick = { onDone() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mslYellow,
                        contentColor = mslBlack
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Done")
                }
            }
        },
        confirmButton = { },
        dismissButton = null
    )
}

@Composable
@Preview(showBackground = true)
fun PreviewSubmitRoundSuccessDialogLight() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color.White,
            surface = Color.White,
            onSurface = mslBlack
        )
    ) {
        SubmitRoundSuccessDialog(
            playingPartnerName = "Arnold Palmer",
            onDone = { }
        )
    }
}

@Composable
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
fun PreviewSubmitRoundSuccessDialogDark() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = mslBlack,
            surface = Color.DarkGray,
            onSurface = Color.White
        )
    ) {
        SubmitRoundSuccessDialog(
            playingPartnerName = "Arnold Palmer",
            onDone = { }
        )
    }
}
