package com.sogo.golf.msl.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Accessibility-aware spacing that scales with user preferences
@Composable
fun AccessibleSpacer(
    height: Dp = 0.dp,
    width: Dp = 0.dp
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    // Scale spacers with font size for better layout
    val scaledHeight = if (height > 0.dp) height * fontScale else height
    val scaledWidth = if (width > 0.dp) width * fontScale else width

    Spacer(
        modifier = Modifier
            .height(scaledHeight)
            .width(scaledWidth)
    )
}

// Text that automatically scales and provides good contrast
@Composable
fun AccessibleText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    // Limit extreme font scaling to keep UI usable
    val cappedFontScale = fontScale.coerceIn(0.8f, 1.4f)

    val scaledStyle = style.copy(
        fontSize = style.fontSize * cappedFontScale,
        lineHeight = style.lineHeight * cappedFontScale
    )

    Text(
        text = text,
        style = scaledStyle,
        maxLines = maxLines,
        color = MSLColors.TextPrimary,
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        }
    )
}

// Score display that's always readable
@Composable
fun ScoreText(
    score: String,
    par: Int? = null,
    isCurrentHole: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scoreInt = score.toIntOrNull()
    val scoreColor = when {
        scoreInt == null -> MSLColors.TextSecondary
        par == null -> MSLColors.TextPrimary
        scoreInt < par -> MSLColors.Eagle
        scoreInt == par -> MSLColors.Par
        scoreInt == par + 1 -> MSLColors.Bogey
        else -> MSLColors.DoublePlus
    }

    val textStyle = if (isCurrentHole) {
        MSLTypography.scoreboardLarge
    } else {
        MSLTypography.scoreboardMedium
    }

    val weight = if (isCurrentHole) FontWeight.Bold else FontWeight.SemiBold

    Text(
        text = score,
        style = textStyle.copy(
            color = scoreColor,
            fontWeight = weight
        ),
        modifier = modifier.semantics {
            contentDescription = when {
                scoreInt == null -> "Score: $score"
                par == null -> "Score: $score"
                scoreInt < par -> "Score: $score, ${par - scoreInt} under par"
                scoreInt == par -> "Score: $score, par"
                else -> "Score: $score, ${scoreInt - par} over par"
            }
        }
    )
}

// Golfer name that scales well
@Composable
fun GolferNameText(
    firstName: String?,
    lastName: String?,
    isCurrentPlayer: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayName = when {
        firstName != null && lastName != null -> "$firstName $lastName"
        firstName != null -> firstName
        lastName != null -> lastName
        else -> "Unknown Player"
    }

    val textStyle = if (isCurrentPlayer) {
        MSLTypography.playerNameLarge
    } else {
        MSLTypography.playerNameMedium
    }

    AccessibleText(
        text = displayName,
        style = textStyle.copy(
            color = if (isCurrentPlayer) MSLColors.Primary else MSLColors.TextPrimary,
            fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Medium
        ),
        contentDescription = if (isCurrentPlayer) "Current player: $displayName" else "Player: $displayName",
        modifier = modifier
    )
}

// Hole information that's compact but readable
@Composable
fun HoleInfoCard(
    holeNumber: Int,
    par: Int,
    distance: Int? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "HOLE",
            style = MSLTypography.overline,
            color = MSLColors.TextSecondary
        )

        Text(
            text = holeNumber.toString(),
            style = MSLTypography.holeNumber,
            color = MSLColors.Primary
        )

        AccessibleSpacer(height = 4.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PAR",
                style = MSLTypography.caption,
                color = MSLColors.TextSecondary
            )
            AccessibleSpacer(width = 4.dp)
            Text(
                text = par.toString(),
                style = MSLTypography.holeInfo.copy(fontWeight = FontWeight.SemiBold),
                color = MSLColors.TextPrimary
            )
        }

        distance?.let {
            AccessibleSpacer(height = 2.dp)
            Text(
                text = "${it}m",
                style = MSLTypography.caption,
                color = MSLColors.TextSecondary
            )
        }
    }
}

// Responsive layout that adapts to font scaling
@Composable
fun ResponsiveLayout(
    compactContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    // Switch to compact layout when font is very large
    if (fontScale > 1.2f) {
        compactContent()
    } else {
        expandedContent()
    }
}