package com.sogo.golf.msl.shared_components.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1976D2),
    textColor: Color = Color.White,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp,
    scrollDurationMs: Int = 10000
) {
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0f) }
    var textWidthPx by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "marquee")

    val needsScrolling = textWidthPx > containerWidthPx && containerWidthPx > 0

    val offset by infiniteTransition.animateFloat(
        initialValue = if (needsScrolling) containerWidthPx else 0f,
        targetValue = if (needsScrolling) -textWidthPx else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = scrollDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "marqueeOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clipToBounds()
            .onSizeChanged { size ->
                containerWidthPx = size.width.toFloat()
            }
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            softWrap = false,
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .graphicsLayer {
                    translationX = if (needsScrolling) offset else 0f
                }
                .onSizeChanged { size ->
                    textWidthPx = size.width.toFloat()
                }
        )
    }
}
