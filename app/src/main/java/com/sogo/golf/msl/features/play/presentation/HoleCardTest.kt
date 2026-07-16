package com.sogo.golf.msl.features.play.presentation


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.em
import com.sogo.golf.msl.ui.theme.MSLColors
import kotlin.math.abs

@Composable
fun HoleCardTest(
    backgroundColor: Color = MSLColors.mslBlue,
    golferName: String = "Daniel Seymour",
    teeColor: String = "Black",
    competitionType: String = "Stableford",
    dailyHandicap: Int = 10,
    shotsReceived: Int = 0,
    strokes: Int = 3,
    currentPoints: Int = 2,
    par: Int = 5,
    distance: Int = 441,
    strokeIndex: String = "1/22/40",
    totalScore: Int = 0,
    onSwipeNext: () -> Unit = {},
    onSwipePrevious: () -> Unit = {},
    onStrokeButtonClick: () -> Unit = {},
    onPlusButtonClick: () -> Unit = {},
    onMinusButtonClick: () -> Unit = {},
    isBallPickedUp: Boolean = false,
    isDQ: Boolean = false,
    onPickupButtonClick: () -> Unit = {},
    showHoleStatsButton: Boolean = false,
    onHoleStatsButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    var totalDragX = 0f
                    var isDragging = false

                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                        totalDragX = 0f
                        isDragging = false

                        do {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val dragAmount = event.changes.firstOrNull()?.let { change ->
                                val currentPosition = change.position
                                val previousPosition = change.previousPosition
                                currentPosition.x - previousPosition.x
                            } ?: 0f

                            totalDragX += dragAmount

                            // Only consider it a drag if we've moved more than a small threshold
                            if (abs(totalDragX) > 20f && !isDragging) {
                                isDragging = true
                                // Consume the event to prevent child clicks
                                event.changes.forEach { it.consume() }
                            } else if (isDragging) {
                                // Continue consuming events during drag
                                event.changes.forEach { it.consume() }
                            }

                        } while (event.changes.any { it.pressed })

                        // Handle swipe on release
                        if (isDragging && abs(totalDragX) > swipeThreshold) {
                            if (totalDragX > 0) {
                                onSwipePrevious()
                            } else {
                                onSwipeNext()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            BoxWithConstraints {
                val minDimension = min(maxWidth, maxHeight)
                // Calculate scale factor based purely on available space, ignoring system font settings
                // This ensures content always fits regardless of user's font size preferences
                val baseScale = minDimension.value / 400f  // Adjusted base for better fit
                // Remove density scaling to ignore system font size settings
                val scaleFactor = baseScale.coerceIn(0.6f, 1.2f)  // Tighter bounds for consistent sizing

                // Build points label based on competition type
                val pointsText = if (isDQ) "DQ" else when {
                    competitionType.equals("par", ignoreCase = true) ||
                            competitionType.equals("stroke", ignoreCase = true) -> {
                        if (currentPoints > 0) "+$currentPoints" else "$currentPoints"
                    }
                    currentPoints == 1 -> "1 pt"
                    else -> "$currentPoints pts"
                }
                // Build total score label based on competition type
                val totalScoreText = if (isDQ) "DQ" else when {
                    competitionType.equals("par", ignoreCase = true) ||
                            competitionType.equals("stroke", ignoreCase = true) -> {
                        if (totalScore > 0) "+$totalScore" else "$totalScore"
                    }
                    else -> "$totalScore"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = (8 * scaleFactor).dp, bottom = (12 * scaleFactor).dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // Rows distribute evenly over the full card height (space-between) so nothing
                    // clusters at the top with a dead gap before the bottom stats.
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Name
                    Text(
                        text = golferName,
                        fontSize = (36 * scaleFactor).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    // Tee type / shots / handicap row (3 columns)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = teeColor,
                                color = Color.White,
                                fontSize = (24 * scaleFactor).sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Type: $competitionType",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp
                            )
                        }
                        // Shots received on this hole (handicap-derived) — plain 3rd column.
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = shotsReceived.toString(),
                                color = Color.White,
                                fontSize = (24 * scaleFactor).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (abs(shotsReceived) == 1) "Shot" else "Shots",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp,
                                maxLines = 1
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dailyHandicap.toString(),
                                color = Color.White,
                                fontSize = (24 * scaleFactor).sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daily Handicap",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp
                            )
                        }
                    }

                    // Score adjustment controls — centred row with a gap around the big circle.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((20 * scaleFactor).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = if (isBallPickedUp) { {} } else onMinusButtonClick,
                            enabled = !isBallPickedUp,
                            modifier = Modifier
                                .size(65.dp)
                                .clip(CircleShape)
                                .background(MSLColors.mslYellow.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f))
                        ) {
                            Text(
                                "-",
                                color = Color.Black.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f),
                                fontSize = (38 * scaleFactor).sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f))
                                .clickable(enabled = !isBallPickedUp) {
                                    if (!isBallPickedUp) onStrokeButtonClick()
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = strokes.toString(),
                                    color = Color.Black.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f),
                                    fontSize = (56 * scaleFactor).sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = pointsText,
                                    color = if (isDQ) Color.Red else Color.Gray.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f),
                                    fontSize = (19 * scaleFactor).sp,
                                    modifier = Modifier.offset(y = (-10 * scaleFactor).dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = if (isBallPickedUp) { {} } else onPlusButtonClick,
                            enabled = !isBallPickedUp,
                            modifier = Modifier
                                .size(65.dp)
                                .clip(CircleShape)
                                .background(MSLColors.mslYellow.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f))
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Plus",
                                tint = Color.Black.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f)
                            )
                        }
                    }

                    // Hole Stats (ghost pill) + Pickup, wrapped in ONE Row so the card's
                    // SpaceBetween layout treats them as a single centred, equal-height pair.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((12 * scaleFactor).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        if (showHoleStatsButton) {
                            HoleStatsPill(
                                scaleFactor = scaleFactor,
                                onClick = onHoleStatsButtonClick,
                                modifier = Modifier.fillMaxHeight()
                            )
                        }

                        // Pickup button — fills the row height so it matches the stats pill.
                        Button(
                            onClick = onPickupButtonClick,
                            modifier = Modifier.fillMaxHeight(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBallPickedUp) Color.Red else MSLColors.mslYellow,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Pickup",
                                color = if (isBallPickedUp) Color.White else Color.Black,
                                fontSize = (18 * scaleFactor).sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = (16 * scaleFactor).dp)
                            )
                        }
                    }

                    // Bottom stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = par.toString(),
                                color = Color.White,
                                fontSize = (30 * scaleFactor).sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Par",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = distance.toString(),
                                color = Color.White,
                                fontSize = (30 * scaleFactor).sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Meters",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = strokeIndex,
                                color = Color.White,
                                fontSize = (30 * scaleFactor).sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Index",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = totalScoreText,
                                color = if (isDQ) Color.Red else Color.White,
                                fontSize = (30 * scaleFactor).sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "Total",
                                color = Color.White,
                                fontSize = (18 * scaleFactor).sp
                            )
                        }
                    }
                }
            }
        }

    }
}

// Ghost pill matching the iOS HOLE STATS button: translucent white capsule with a white
// border, a 3-bar chart glyph (tallest bar yellow) and white "HOLE STATS" text.
@Composable
private fun HoleStatsPill(
    scaleFactor: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.55f)), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = (16 * scaleFactor).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((7 * scaleFactor).dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy((2.9f * scaleFactor).dp)
        ) {
            StatBar(6f, scaleFactor, Color.White)
            StatBar(10f, scaleFactor, Color.White)
            StatBar(14f, scaleFactor, MSLColors.mslYellow)
        }
        Text(
            text = "HOLE STATS",
            color = Color.White,
            fontSize = (11 * scaleFactor).sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.06.em
        )
    }
}

@Composable
private fun StatBar(h: Float, scaleFactor: Float, color: Color) {
    Box(
        modifier = Modifier
            .width((3.4f * scaleFactor).dp)
            .height((h * scaleFactor).dp)
            .clip(RoundedCornerShape((1 * scaleFactor).dp))
            .background(color)
    )
}

@Preview(showBackground = true)
@Composable
fun HoleCardTestPreview() {
    Box(
        modifier = Modifier
            .size(350.dp, 400.dp)
            .padding(16.dp)
    ) {
        HoleCardTest()
    }
}
