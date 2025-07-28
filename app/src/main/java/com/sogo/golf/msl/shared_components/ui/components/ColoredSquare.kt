package com.sogo.golf.msl.shared_components.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColoredSquare(hexColor: String) {
    val color = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: IllegalArgumentException) {
        Color.Gray // Default color if parsing fails
    }

    Box(
        modifier = Modifier
            .width(20.dp)
            .height(20.dp)
            .background(color)
    )
}
