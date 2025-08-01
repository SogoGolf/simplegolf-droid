package com.sogo.golf.msl.features.play.presentation


import androidx.compose.foundation.background
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
import com.sogo.golf.msl.ui.theme.MSLColors
import kotlin.math.abs

@Composable
fun HoleCardTest(
    backgroundColor: Color = MSLColors.mslBlue,
    golferName: String = "Daniel Seymour",
    teeColor: String = "Black",
    competitionType: String = "Stableford",
    dailyHandicap: Int = 10,
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
    onPickupButtonClick: () -> Unit = {},
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with name
                Text(
                    text = golferName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 5.dp)
                )

                // Tee type and handicap row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = teeColor,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = "Type: $competitionType",
                            color = Color.White,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dailyHandicap.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily Handicap",
                            color = Color.White,
                        )
                    }
                }

                // Score adjustment controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = if (isBallPickedUp) { {} } else onMinusButtonClick,
                        enabled = !isBallPickedUp,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MSLColors.mslYellow.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f))
                    ) {
                        Text(
                            "-",
                            color = Color.Black.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f),
                            style = MaterialTheme.typography.displaySmall,
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
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
                                style = MaterialTheme.typography.displayMedium,
                            )
                            Text(
                                text = "$currentPoints pts",
                                color = Color.Gray.copy(alpha = if (isBallPickedUp) 0.5f else 1.0f),
                                modifier = Modifier.offset(y = (-10).dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = if (isBallPickedUp) { {} } else onPlusButtonClick,
                        enabled = !isBallPickedUp,
                        modifier = Modifier
                            .size(56.dp)
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

                // Pickup button
                Button(
                    onClick = onPickupButtonClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBallPickedUp) Color.Red else MSLColors.mslYellow
                    )
                ) {
                    Text(
                        text = "Pickup",
                        color = if (isBallPickedUp) Color.White else Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
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
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Par",
                            color = Color.White,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = distance.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Meters",
                            color = Color.White,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strokeIndex,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Index",
                            color = Color.White,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalScore.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Total",
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
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

