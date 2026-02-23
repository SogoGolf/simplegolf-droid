package com.sogo.golf.msl.shared_components.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = mslYellow,
    textColor: Color = Color.Black,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 12.dp,
    scrollSpeedDpPerSec: Float = 50f,
    gapWidth: Dp = 80.dp
) {
    val density = LocalDensity.current
    val speedPxPerSec = with(density) { scrollSpeedDpPerSec.dp.toPx() }
    val gapPx = with(density) { gapWidth.toPx() }
    val hPaddingPx = with(density) { horizontalPadding.toPx() }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(fontSize = fontSize, fontWeight = fontWeight)
    val measuredTextWidth = remember(text, textStyle) {
        textMeasurer.measure(text = text, style = textStyle, maxLines = 1, softWrap = false)
            .size.width.toFloat()
    }

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val availableWidth = containerWidthPx - 2 * hPaddingPx
    val needsScrolling = measuredTextWidth > availableWidth && availableWidth > 0
    val loopPointPx = measuredTextWidth + gapPx

    val scrollState = rememberScrollState()
    var isTouching by remember { mutableStateOf(false) }

    // Auto-scroll: continuous marquee loop
    LaunchedEffect(needsScrolling, loopPointPx, isTouching) {
        if (!needsScrolling || isTouching || loopPointPx <= 0) return@LaunchedEffect

        delay(1000)

        while (isActive) {
            val remaining = loopPointPx - scrollState.value
            val durationMs = (remaining / speedPxPerSec * 1000).toInt().coerceAtLeast(0)

            scrollState.animateScrollTo(
                loopPointPx.toInt(),
                animationSpec = tween(durationMs, easing = LinearEasing)
            )

            // Seamless snap — second copy is now where first copy was
            scrollState.scrollTo(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
    ) {
        if (needsScrolling) {
            Row(
                modifier = Modifier
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            isTouching = true
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })
                            isTouching = false
                        }
                    }
                    .horizontalScroll(scrollState)
            ) {
                Spacer(Modifier.width(horizontalPadding))
                Text(
                    text = text,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
                Spacer(Modifier.width(gapWidth))
                Text(
                    text = text,
                    color = textColor,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(vertical = verticalPadding)
                )
                Spacer(Modifier.width(horizontalPadding))
            }
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
            )
        }
    }
}
