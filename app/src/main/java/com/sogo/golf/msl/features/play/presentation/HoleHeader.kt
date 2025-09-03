package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.rememberNormalizedDensity

@Composable
fun HoleHeader(
    modifier: Modifier = Modifier,
    headerHeightPercentage: Float = 0.125f,
    holeNumber: Int,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onNext: () -> Unit,
    onTapHoleNumber: () -> Unit,
    showBackButton: Boolean
) {
    // Get the screen height using normalized density to prevent zoom issues
    val configuration = LocalConfiguration.current
    val normalizedDensity = rememberNormalizedDensity()
    val screenHeightPx = with(normalizedDensity) { configuration.screenHeightDp.dp.toPx() }
    val headerHeightDp = with(normalizedDensity) { (screenHeightPx * headerHeightPercentage).toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeightDp),
            //.background(mslBlue),
        contentAlignment = Alignment.Center
    ) {
        // First Row for left and right icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MSLColors.mslGunMetal,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MSLColors.mslGunMetal
                )
            }
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = MSLColors.mslGunMetal,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        // Second Box for centered text
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOLE",
                    color = MSLColors.mslGunMetal,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .border(0.5.dp, MSLColors.mslGunMetal, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .width(60.dp)
                        .clickable(onClick = onTapHoleNumber)
                ) {
                    Text(
                        text = if (holeNumber == 99) "--" else holeNumber.toString(),
                        color = MSLColors.mslGunMetal,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HoleHeaderPreview() {
    HoleHeader(
        holeNumber = 1,
        onBack = {},
        onClose = {},
        onNext = {},
        onTapHoleNumber = {},
        showBackButton = true
    )
}
