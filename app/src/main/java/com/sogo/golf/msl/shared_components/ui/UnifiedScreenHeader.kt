package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A unified header component for screens wrapped with ScreenWithDrawer.
 * This header provides proper spacing for the drawer menu icon and debug icon,
 * while centering the title.
 *
 * @param title The title text to display in the center
 * @param modifier Additional modifiers to apply to the header
 * @param backgroundColor Background color for the header (default: transparent)
 */
@Composable
fun UnifiedScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Standard toolbar height
            .background(backgroundColor)
            .padding(horizontal = 56.dp), // Space for menu icon (48dp + 8dp padding) on each side
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}