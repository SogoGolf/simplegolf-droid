package com.sogo.golf.msl.app.update

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sogo.golf.msl.domain.model.MobileAppVersionFeature

// Fixed dark palette from the design handoff — the prompt never renders light.
private object UpdateCardPalette {
    val Surface = Color(0xFF1E1C1C)
    val Title = Color(0xFFF4F3F1)
    val Detail = Color(0xFF9B9793)
    val FixRowText = Color(0xFFC4C0BC)
    val Footnote = Color(0xFF8D8985)
    val Accent = Color(0xFF4FA3D8)
    val OnAccent = Color(0xFF08222F)
    val IconTile = Accent.copy(alpha = 0.15f)
    val PillBackground = Accent.copy(alpha = 0.14f)
    val LaterBorder = Accent.copy(alpha = 0.55f)
}

// SF Symbol names from the shared config → Material icons.
private fun featureIcon(sfSymbolName: String?): ImageVector = when (sfSymbolName) {
    "flag.fill" -> Icons.Filled.Flag
    "chart.bar.fill" -> Icons.Filled.BarChart
    "arrow.down.circle.fill" -> Icons.Filled.ArrowCircleDown
    "bolt.fill" -> Icons.Filled.Bolt
    "person.2.fill" -> Icons.Filled.People
    "bell.badge.fill" -> Icons.Filled.NotificationsActive
    "wrench.fill" -> Icons.Filled.Build
    "checkmark.circle.fill" -> Icons.Filled.CheckCircle
    else -> Icons.Filled.Flag
}

/**
 * The in-app update prompt: a tiered "What's New" card driven by the server
 * config (web Release Manager). With no content it falls back to a
 * message-only card, matching the pre-tiered behaviour.
 */
@Composable
fun ForceUpdateOverlay(
    message: String,
    newVersion: String,
    isRequired: Boolean,
    features: List<MobileAppVersionFeature> = emptyList(),
    fixRows: List<String> = emptyList(),
    fixes: List<String> = emptyList(),
    footnote: String = "",
    onUpdate: () -> Unit,
    onDismiss: (() -> Unit)?
) {
    val isThin = features.isEmpty() && fixRows.isEmpty()
    val headline = when {
        features.isNotEmpty() -> "What's New"
        fixRows.isNotEmpty() -> "What's Fixed"
        isRequired -> "Update Required"
        else -> "Update Available"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { awaitPointerEventScope { while (true) { awaitPointerEvent() } } }
            .background(Color.Black.copy(alpha = 0.5f))
            .zIndex(100f),
        contentAlignment = Alignment.Center
    ) {
        // The feature list may take up to this fraction of the screen before it
        // scrolls — proportional so small and large phones both keep the
        // buttons comfortably on screen.
        val featureListCap = maxHeight * 0.42f

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = UpdateCardPalette.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (isThin) 20.dp else 18.dp,
                    bottom = 18.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isThin) 12.dp else 14.dp)
            ) {
                Text(
                    text = headline,
                    fontSize = if (isThin) 22.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = UpdateCardPalette.Title,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "VERSION $newVersion",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp,
                    color = UpdateCardPalette.Accent,
                    modifier = Modifier
                        .background(UpdateCardPalette.PillBackground, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )

                if (isThin && message.isNotEmpty()) {
                    Text(
                        text = message,
                        fontSize = 17.sp,
                        color = UpdateCardPalette.Footnote,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (features.isNotEmpty()) {
                    FeatureList(features = features, maxListHeight = featureListCap)
                }

                if (fixRows.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        fixRows.forEach { fix ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 9.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconTile(Icons.Filled.CheckCircle)
                                Text(
                                    text = fix,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = UpdateCardPalette.FixRowText
                                )
                            }
                        }
                    }
                }

                if (fixes.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        fixes.forEach { fix ->
                            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                Text(
                                    text = "—",
                                    fontSize = 15.5.sp,
                                    color = UpdateCardPalette.Accent
                                )
                                Text(
                                    text = fix,
                                    fontSize = 15.5.sp,
                                    color = UpdateCardPalette.Detail
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (onDismiss != null) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, UpdateCardPalette.LaterBorder),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Text(
                                text = "Later",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UpdateCardPalette.Accent
                            )
                        }
                    }

                    Button(
                        onClick = onUpdate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UpdateCardPalette.Accent),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Update",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UpdateCardPalette.OnAccent
                        )
                    }
                }

                if (footnote.isNotEmpty()) {
                    Text(
                        text = footnote,
                        fontSize = 15.sp,
                        color = UpdateCardPalette.Footnote,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureList(
    features: List<MobileAppVersionFeature>,
    maxListHeight: androidx.compose.ui.unit.Dp
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            // heightIn(max) hugs the rows; the list only scrolls past the cap.
            Column(
                modifier = Modifier
                    .heightIn(max = maxListHeight)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconTile(featureIcon(feature.icon))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = feature.title.orEmpty(),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UpdateCardPalette.Title
                            )
                            Text(
                                text = feature.detail.orEmpty(),
                                fontSize = 15.sp,
                                color = UpdateCardPalette.Detail
                            )
                        }
                    }
                }
            }

            // Bottom fade while there is more content below.
            if (scrollState.canScrollForward) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, UpdateCardPalette.Surface)
                            )
                        )
                )
            }
        }

        if (scrollState.canScrollForward) {
            Text(
                text = "Scroll for more ↓",
                fontSize = 13.sp,
                color = UpdateCardPalette.Footnote
            )
        }
    }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(UpdateCardPalette.IconTile, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UpdateCardPalette.Accent,
            modifier = Modifier.size(20.dp)
        )
    }
}
