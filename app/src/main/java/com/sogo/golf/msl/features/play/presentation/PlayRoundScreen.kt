package com.sogo.golf.msl.features.play.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.navigation.NavViewModel
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGrey
import kotlinx.coroutines.delay
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun PlayRoundScreen(
    navController: NavController,
    viewModel: NavViewModel = hiltViewModel(),
    playRoundViewModel: PlayRoundViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Collect the state data needed for the scorecard
        val localCompetition by playRoundViewModel.localCompetition.collectAsState()
        val currentRound by playRoundViewModel.currentRound.collectAsState()
        
        // Track scorecard viewed event when switching to landscape
        LaunchedEffect(isLandscape) {
            playRoundViewModel.trackScorecardViewed()
        }
        
        // Show scorecard in landscape
        ScorecardScreen(
            round = currentRound,
            mslCompetition = localCompetition
        )
    } else {
        // Show normal Screen4 content in portrait
        Screen4Portrait(navController, viewModel, playRoundViewModel)
    }
}



private data class PaceStatus(
    val currentHoleNumber: Int,
    val currentHoleExpectedMinutes: Int,
    val expectedElapsedMinutes: Int,
    val actualElapsedMinutes: Int,
    val actualElapsedSeconds: Int,
    val minutesBehind: Int,
    val startsInMinutes: Int,
    val startMillis: Long?
) {
    val isBehind: Boolean
        get() = minutesBehind > 0

    val hasStarted: Boolean
        get() = startsInMinutes <= 0

    /** Minutes of slack before falling behind on the current hole. Negative once behind. */
    val bufferMinutes: Int
        get() = expectedElapsedMinutes - actualElapsedMinutes

    /**
     * The pill always shows a live number: a countdown of remaining slack while
     * on pace (green), the minutes behind while behind (red), or a countdown to
     * the booked tee time before the round has started (neutral).
     */
    val pillText: String
        get() = when {
            !hasStarted -> "${startsInMinutes}m"
            isBehind -> "${minutesBehind}m"
            else -> "${maxOf(0, bufferMinutes)}m"
        }

    /** Running stopwatch since the booked tee time (H:MM:SS / M:SS). */
    val elapsedClock: String
        get() = formatPaceClock(if (hasStarted) actualElapsedSeconds else 0, includeSeconds = true)

    /** Total expected play time to the current hole (H:MM). */
    val targetClock: String
        get() = formatPaceClock(expectedElapsedMinutes * 60, includeSeconds = false)

    /** One-line summary for the expanded view. */
    val deltaText: String
        get() = when {
            !hasStarted -> if (startsInMinutes == 1) "Tees off in 1 min" else "Tees off in $startsInMinutes min"
            isBehind -> if (minutesBehind == 1) "1 min behind pace" else "$minutesBehind min behind pace"
            else -> when (val ahead = maxOf(0, bufferMinutes)) {
                0 -> "On pace"
                1 -> "1 min ahead of pace"
                else -> "$ahead min ahead of pace"
            }
        }

    val teeTimeText: String
        get() = startMillis?.let { millis ->
            Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("h:mm a"))
        } ?: "—"
}

private fun formatPaceClock(seconds: Int, includeSeconds: Boolean): String {
    val total = maxOf(0, seconds)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val secs = total % 60
    return if (includeSeconds) {
        if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs)
        else "%d:%02d".format(minutes, secs)
    } else {
        "%d:%02d".format(hours, minutes)
    }
}

@Composable
private fun PacePill(
    status: PaceStatus,
    onClick: () -> Unit
) {
    // Green through the pre-tee countdown too: the golfer is still on time, so a
    // green pill reads clearly as "all good". Only red when behind.
    val pillColor = if (status.isBehind) MSLColors.mslRed else MSLColors.mslGreen
    Row(
        modifier = Modifier
            // The pill lives in a weight-constrained header slot; let it grow to
            // its content so a wide number (e.g. a long countdown) isn't squeezed
            // into wrapping onto a second line.
            .wrapContentWidth(unbounded = true)
            .offset(x = 5.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(pillColor)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaceClockIcon(
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = status.pillText,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            // Drop the extra font padding so the number sits at the true vertical
            // centre of the pill, matching the clock icon.
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

@Composable
private fun PaceClockIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.12f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.42f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = stroke
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.48f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.46f, center.y + radius * 0.30f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PaceOfPlayPopover(
    status: PaceStatus,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerColor = when {
        !status.hasStarted -> PacePopoverColors.mutedText
        status.isBehind -> PacePopoverColors.behindRed
        else -> PacePopoverColors.green
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(width = 34.dp, height = 20.dp)
                .offset(x = 64.dp, y = (-16).dp)
                .zIndex(1f)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = PacePopoverColors.panel)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(PacePopoverColors.panel)
                .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "Pace of play",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss pace details",
                        tint = PacePopoverColors.mutedText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Running stopwatch from the booked tee time.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = status.elapsedClock,
                    color = timerColor,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status.deltaText,
                    color = timerColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PacePopoverColors.mutedText.copy(alpha = 0.3f))
            )

            PaceDetailRow(title = "Target to hole ${status.currentHoleNumber}", value = status.targetClock)
            PaceDetailRow(title = "Tee time", value = status.teeTimeText)
        }
    }
}

@Composable
private fun PaceDetailRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PacePopoverColors.mutedText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private object PacePopoverColors {
    val panel = Color(0xFF2B2E29)
    val tile = Color(0xFF1F211C)
    val mutedText = Color(0xFFB3B3A8)
    val gold = Color(0xFFA87A05)
    val green = Color(0xFF3D8F40)
    val behindRed = Color(0xFFCC3833)
    val notice = Color(0xFFFFEDB8)
    val noticeText = Color(0xFF6B4705)
}

private const val DEFAULT_HOLE_MINUTES = 14

private fun calculatePaceStatus(
    game: MslGame?,
    round: com.sogo.golf.msl.domain.model.Round?,
    currentHoleNumber: Int,
    expectedMinutesByHole: Map<Int, Int>,
    nowMillis: Long
): PaceStatus {
    fun expectedMinutes(holeNumber: Int): Int {
        val minutes = expectedMinutesByHole[holeNumber] ?: 0
        return if (minutes > 0) minutes else DEFAULT_HOLE_MINUTES
    }

    val currentHoleExpected = expectedMinutes(currentHoleNumber)
    val holeCycle = buildPaceHoleCycle(
        startingHole = game?.startingHoleNumber ?: 1,
        numberOfHoles = game?.numberOfHoles ?: 18
    )
    val currentIndex = holeCycle.indexOf(currentHoleNumber)
    val holesThroughCurrent = if (currentIndex >= 0) {
        holeCycle.take(currentIndex + 1)
    } else {
        listOf(currentHoleNumber)
    }
    val expectedElapsed = holesThroughCurrent.sumOf { holeNumber ->
        expectedMinutes(holeNumber)
    }

    val startMillis = resolvePaceStartMillis(game, round)

    if (startMillis == null) {
        return PaceStatus(
            currentHoleNumber = currentHoleNumber,
            currentHoleExpectedMinutes = currentHoleExpected,
            expectedElapsedMinutes = expectedElapsed,
            actualElapsedMinutes = 0,
            actualElapsedSeconds = 0,
            minutesBehind = 0,
            startsInMinutes = 0,
            startMillis = null
        )
    }

    val startsInMinutes = if (nowMillis < startMillis) {
        kotlin.math.ceil((startMillis - nowMillis) / 60_000.0).toInt()
    } else {
        0
    }
    val actualElapsedSeconds = if (nowMillis > startMillis) {
        ((nowMillis - startMillis) / 1_000L).toInt()
    } else {
        0
    }
    val actualElapsed = actualElapsedSeconds / 60
    val minutesBehind = maxOf(0, actualElapsed - expectedElapsed)

    return PaceStatus(
        currentHoleNumber = currentHoleNumber,
        currentHoleExpectedMinutes = currentHoleExpected,
        expectedElapsedMinutes = expectedElapsed,
        actualElapsedMinutes = actualElapsed,
        actualElapsedSeconds = actualElapsedSeconds,
        minutesBehind = minutesBehind,
        startsInMinutes = startsInMinutes,
        startMillis = startMillis
    )
}

private fun resolvePaceStartMillis(
    game: MslGame?,
    round: com.sogo.golf.msl.domain.model.Round?
): Long? {
    val actualStartMillis = round?.startTime?.toEpochMillis()
    val bookingTime = game?.bookingTime
    val teeMillis = if (bookingTime != null) {
        val roundDate = round?.roundDate?.toLocalDate() ?: LocalDate.now()
        LocalDateTime.of(roundDate, bookingTime.toLocalTime()).toEpochMillis()
    } else {
        null
    }

    // The pace clock runs from the booked tee time — the tester expects a
    // stopwatch from the tee time, so a late start correctly counts against pace.
    // (actualStart is the "Let's Play" tap in the pro shop, before the tee time,
    // so it must NOT pull the clock earlier.) Fall back to the actual start only
    // when there is no booked tee time.
    return teeMillis ?: actualStartMillis
}

private fun LocalDateTime.toEpochMillis(): Long {
    return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun buildPaceHoleCycle(startingHole: Int, numberOfHoles: Int): List<Int> {
    // Must match PlayRoundViewModel.getCycleIndices so the "expected time to this
    // hole" is summed over the SAME hole order the golfer actually plays.
    // getCycleIndices wraps a front-nine (1-9) 9-hole round at 9->1 and a
    // back-nine (10-18) 9-hole round at 18->10; only 18-hole rounds wrap 18->1.
    // A plain 18->1 wrap here diverged for 9-hole/shotgun starts and made the
    // pill read wildly behind after the wrap.
    val start = startingHole.coerceIn(1, 18)
    val count = numberOfHoles.takeIf { it > 0 } ?: 18
    val maxHole = when {
        count == 18 -> 18
        start >= 10 && count == 9 -> 18
        start in 1..9 && count == 9 -> 9
        else -> start + count - 1
    }
    val holes = mutableListOf<Int>()
    var currentHole = start

    repeat(count) {
        holes.add(currentHole)
        currentHole += 1
        if (currentHole > maxHole) {
            currentHole = if (start >= 10 && count == 9) 10 else 1
        }
    }

    return holes
}

@Composable
private fun Screen4Portrait(
    navController: NavController,
    viewModel: NavViewModel,
    playRoundViewModel: PlayRoundViewModel
) {
    val context = LocalContext.current

    val deleteMarkerEnabled by playRoundViewModel.deleteMarkerEnabled.collectAsState()
    val isRemovingMarker by playRoundViewModel.isRemovingMarker.collectAsState()
    val markerError by playRoundViewModel.markerError.collectAsState()
    val localGame by playRoundViewModel.localGame.collectAsState()
    val localCompetition by playRoundViewModel.localCompetition.collectAsState()
    val currentGolfer by playRoundViewModel.currentGolfer.collectAsState()
    val currentRound by playRoundViewModel.currentRound.collectAsState()
    val currentHoleNumber by playRoundViewModel.currentHoleNumber.collectAsState()
    val showBackButton by playRoundViewModel.showBackButton.collectAsState()
    val showDialog by playRoundViewModel.showGoToHoleDialog.collectAsState()
    val isAbandoningRound by playRoundViewModel.isAbandoningRound.collectAsState()
    val abandonError by playRoundViewModel.abandonError.collectAsState()

    var showBackConfirmDialog by remember { mutableStateOf(false) }
    var showAbandonDialog by remember { mutableStateOf(false) }
    var showPacePopover by remember { mutableStateOf(false) }
    var paceNowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            paceNowMillis = System.currentTimeMillis()
        }
    }

    val paceStatus = remember(localGame, localCompetition, currentRound, currentHoleNumber, paceNowMillis) {
        val expectedMinutesByHole = localCompetition?.players?.firstOrNull()?.holes
            ?.associate { it.holeNumber to it.playTimeMinutes }
            ?: emptyMap()
        calculatePaceStatus(
            game = localGame,
            round = currentRound,
            currentHoleNumber = currentHoleNumber,
            expectedMinutesByHole = expectedMinutesByHole,
            nowMillis = paceNowMillis
        )
    }

    BackHandler(enabled = true) {
        if (showBackButton) {
            // Check if we're on the starting hole (same logic as header back button)
            val startingHoleNumber = localGame?.startingHoleNumber ?: 1
            if (currentHoleNumber == startingHoleNumber) {
                // On starting hole - show confirmation dialog
                showBackConfirmDialog = true
            } else {
                // Not on starting hole - navigate normally
                playRoundViewModel.navigateToPreviousHole()
            }
        }
        // If showBackButton is false, do nothing (completely block back navigation)
    }

    SideEffect {
        val window = (context as? androidx.activity.ComponentActivity)?.window
            ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        window.statusBarColor = Color.White.toArgb()
        insetsController.isAppearanceLightStatusBars = true
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(top = 6.dp)) {

                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    //LEFT ARROW SECTION
                    Row(
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (showBackButton) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clickable(
                                            // Let Compose manage its own InteractionSource for perf
                                            interactionSource = null,
                                            // Use new Material3 ripple; or set indication = null if you want no ripple
                                            indication = ripple(bounded = true)
                                        ) {
                                            // Check if we're on the starting hole
                                            val startingHoleNumber = localGame?.startingHoleNumber ?: 1
                                            if (currentHoleNumber == startingHoleNumber) {
                                                // On starting hole - show confirmation dialog
                                                showBackConfirmDialog = true
                                            } else {
                                                // Not on starting hole - navigate normally
                                                playRoundViewModel.navigateToPreviousHole()
                                            }
                                        },
                                    contentAlignment = Alignment.CenterStart // keep icon anchored right
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous Hole",
                                        tint = MSLColors.mslGunMetal,
                                        modifier = Modifier.size(46.dp)
                                    )
                                }
                            }
                        }

                        PacePill(
                            status = paceStatus,
                            onClick = { showPacePopover = !showPacePopover }
                        )
                    }

                    //HOLE SECTION
                    Row(
                        modifier = Modifier
                            //.background(Color.Yellow)
                            .weight(3f),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Text(
                            "HOLE",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            modifier = Modifier.clickable {
                                playRoundViewModel.showGoToHoleDialog()
                            }
                        )
                        Text(
                            " $currentHoleNumber",
                            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                            modifier = Modifier.clickable {
                                playRoundViewModel.showGoToHoleDialog()
                            }
                        )
                    }

                    //RIGHT ARROW SECTION
                    Row(
                        modifier = Modifier
                            .weight(2f)
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        IconButton(
                            onClick = { showAbandonDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = MSLColors.mslRed,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Bigger hit area for the next button, extending left
                        Box(
                            modifier = Modifier
                                //.background(Color.Red)
                                .height(48.dp)
                                .fillMaxWidth()
                                .clickable(
                                    // Let Compose manage its own InteractionSource for perf
                                    interactionSource = null,
                                    // Use new Material3 ripple; or set indication = null if you want no ripple
                                    indication = ripple(bounded = true)
                                ) { playRoundViewModel.navigateToNextHole(navController) },
                            contentAlignment = Alignment.CenterEnd // keep icon anchored right
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Hole",
                                tint = MSLColors.mslGunMetal,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

            }
        }
        // No bottomBar parameter = no bottom bar
    ) { paddingValues ->
        // Your screen content

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)  // This properly respects the Scaffold's content padding
        ) {
            val cardSpacing = with(LocalDensity.current) {
                (2 * density).dp
            }
            val topCardTop = 10.dp

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Add spacing between nav bar and first card
            Spacer(modifier = Modifier.height(10.dp))
            
            // Extract golfer data from Room database
            val currentGolferValue = currentGolfer
            val localGameValue = localGame
            val localCompetitionValue = localCompetition
            val currentRoundValue = currentRound
            
            // Extract main golfer data
            val mainGolferName = currentGolferValue?.let { golfer ->
                "${golfer.firstName} ${golfer.surname}".trim()
            } ?: "Main Golfer"
            
            val mainGolferHandicap = currentGolferValue?.primary?.toInt() ?: 0
            val mainGolferDailyHandicap = localGameValue?.dailyHandicap ?: 0
            
            // Extract playing partner data
            val playingPartner = if (currentGolferValue != null && localGameValue != null) {
                localGameValue.playingPartners.find { partner ->
                    partner.markedByGolfLinkNumber == currentGolferValue.golfLinkNo
                }
            } else null
            
            val partnerDisplayName = playingPartner?.let { 
                "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() 
            }?.takeIf { it.isNotBlank() } ?: "--"
            
            val partnerDailyHandicap = playingPartner?.dailyHandicap ?: 0
            
            // Extract tee colors from round data (each golfer has their own tee)
            val mainGolferTeeColor = currentRoundValue?.teeColor?.capitalize() ?: localGameValue?.teeColourName ?: "Black"
            val partnerTeeColor = currentRoundValue?.playingPartnerRound?.teeColor?.capitalize() ?: localGameValue?.teeColourName ?: "Black"

            // Extract scoreType for EACH player from competition data
            val mainGolferScoreType = localCompetitionValue?.players
                ?.find { it.golfLinkNumber == currentGolferValue?.golfLinkNo }
                ?.scoreType ?: "Stableford"

            val partnerScoreType = localCompetitionValue?.players
                ?.find { it.golfLinkNumber == playingPartner?.golfLinkNumber }
                ?.scoreType ?: "Stableford"

            // Get hole data from the actual round data instead of competition
            // Main golfer's hole data
            val mainGolferHoleData = currentRoundValue?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }
            val mainGolferPar = mainGolferHoleData?.par ?: 0
            val mainGolferDistance = mainGolferHoleData?.meters ?: 0
            val mainGolferStrokeIndexes = mainGolferHoleData?.strokeIndexes
                ?.map { it.stroke }
                ?.joinToString("/")
                ?.ifEmpty { null }
                ?: listOfNotNull(
                    mainGolferHoleData?.index1?.takeIf { it > 0 },
                    mainGolferHoleData?.index2?.takeIf { it > 0 },
                    mainGolferHoleData?.index3?.takeIf { it > 0 }
                ).joinToString("/").ifEmpty { "-" }
            
            // Playing partner's hole data
            val partnerHoleData = currentRoundValue?.playingPartnerRound?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }
            val partnerPar = partnerHoleData?.par ?: 0
            val partnerDistance = partnerHoleData?.meters ?: 0
            val partnerStrokeIndexes = partnerHoleData?.strokeIndexes
                ?.map { it.stroke }
                ?.joinToString("/")
                ?.ifEmpty { null }
                ?: listOfNotNull(
                    partnerHoleData?.index1?.takeIf { it > 0 },
                    partnerHoleData?.index2?.takeIf { it > 0 },
                    partnerHoleData?.index3?.takeIf { it > 0 }
                ).joinToString("/").ifEmpty { "-" }
            
            // Extract stroke data from Round object for current hole
            val mainGolferStrokes = currentRoundValue?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }?.strokes ?: 0
            
            val partnerStrokes = currentRoundValue?.playingPartnerRound?.holeScores?.find { 
                it.holeNumber == currentHoleNumber 
            }?.strokes ?: 0

            // Extract pickup states for current hole
            val mainGolferPickedUp = currentRoundValue?.holeScores?.find {
                it.holeNumber == currentHoleNumber
            }?.isBallPickedUp ?: false

            val partnerPickedUp = currentRoundValue?.playingPartnerRound?.holeScores?.find {
                it.holeNumber == currentHoleNumber
            }?.isBallPickedUp ?: false

            // Derive DQ state: stroke round + any hole picked up = disqualified
            val isMainGolferDQ = mainGolferScoreType.equals("stroke", ignoreCase = true)
                && currentRoundValue?.holeScores?.any { it.isBallPickedUp == true } == true
            val isPartnerDQ = partnerScoreType.equals("stroke", ignoreCase = true)
                && currentRoundValue?.playingPartnerRound?.holeScores?.any { it.isBallPickedUp == true } == true

            // Look up extraStrokes from competition data for EACH player
            val mainGolferExtraStrokes = localCompetitionValue?.players
                ?.find { it.golfLinkNumber == currentGolferValue?.golfLinkNo }
                ?.holes?.find { it.holeNumber == currentHoleNumber }
                ?.extraStrokes

            val partnerExtraStrokes = localCompetitionValue?.players
                ?.find { it.golfLinkNumber == playingPartner?.golfLinkNumber }
                ?.holes?.find { it.holeNumber == currentHoleNumber }
                ?.extraStrokes

            // Calculate current points for display
            val mainGolferCurrentPoints = if (mainGolferStrokes > 0 && mainGolferHoleData != null) {
                playRoundViewModel.calculateCurrentPoints(
                    strokes = mainGolferStrokes,
                    par = mainGolferPar,
                    index1 = mainGolferHoleData.index1,
                    index2 = mainGolferHoleData.index2,
                    index3 = mainGolferHoleData.index3 ?: 0,
                    dailyHandicap = mainGolferDailyHandicap.toDouble(),
                    scoreType = mainGolferScoreType,
                    extraStrokes = mainGolferExtraStrokes
                )
            } else 0

            val partnerCurrentPoints = if (partnerStrokes > 0 && partnerHoleData != null) {
                playRoundViewModel.calculateCurrentPoints(
                    strokes = partnerStrokes,
                    par = partnerPar,
                    index1 = partnerHoleData.index1,
                    index2 = partnerHoleData.index2,
                    index3 = partnerHoleData.index3 ?: 0,
                    dailyHandicap = partnerDailyHandicap.toDouble(),
                    scoreType = partnerScoreType,
                    extraStrokes = partnerExtraStrokes
                )
            } else 0

            // Top card - Playing Partner
            HoleCardTest(
                golferName = partnerDisplayName,
                backgroundColor = mslBlue,
                teeColor = partnerTeeColor,
                competitionType = partnerScoreType,
                dailyHandicap = partnerDailyHandicap,
                shotsReceived = partnerExtraStrokes ?: 0,
                strokes = partnerStrokes,
                currentPoints = partnerCurrentPoints,
                par = partnerPar,
                distance = partnerDistance,
                strokeIndex = partnerStrokeIndexes,
                totalScore = currentRoundValue?.playingPartnerRound?.holeScores?.sumOf { it.score.toInt() } ?: 0,
                onSwipeNext = { playRoundViewModel.navigateToNextHole(navController) },
                onSwipePrevious = { 
                    if (showBackButton) {
                        playRoundViewModel.navigateToPreviousHole()
                    }
                },
                onStrokeButtonClick = { playRoundViewModel.onPartnerStrokeButtonClick() },
                onPlusButtonClick = { playRoundViewModel.onPartnerPlusButtonClick() },
                onMinusButtonClick = { playRoundViewModel.onPartnerMinusButtonClick() },
                isBallPickedUp = partnerPickedUp,
                isDQ = isPartnerDQ,
                onPickupButtonClick = { playRoundViewModel.onPartnerPickupButtonClick() },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)  // This makes it take up half the available space
                    .padding(horizontal = 10.dp)
            )

            // Gap between cards
            //Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.height(cardSpacing))

            // Bottom card - Main Golfer
            HoleCardTest(
                golferName = mainGolferName,
                backgroundColor = mslGrey,
                teeColor = mainGolferTeeColor,
                competitionType = mainGolferScoreType,
                dailyHandicap = mainGolferDailyHandicap,
                shotsReceived = mainGolferExtraStrokes ?: 0,
                strokes = mainGolferStrokes,
                currentPoints = mainGolferCurrentPoints,
                par = mainGolferPar,
                distance = mainGolferDistance,
                strokeIndex = mainGolferStrokeIndexes,
                totalScore = currentRoundValue?.holeScores?.sumOf { it.score.toInt() } ?: 0,
                onSwipeNext = { playRoundViewModel.navigateToNextHole(navController) },
                onSwipePrevious = { 
                    if (showBackButton) {
                        playRoundViewModel.navigateToPreviousHole()
                    }
                },
                onStrokeButtonClick = { playRoundViewModel.onMainGolferStrokeButtonClick() },
                onPlusButtonClick = { playRoundViewModel.onMainGolferPlusButtonClick() },
                onMinusButtonClick = { playRoundViewModel.onMainGolferMinusButtonClick() },
                isBallPickedUp = mainGolferPickedUp,
                isDQ = isMainGolferDQ,
                onPickupButtonClick = { playRoundViewModel.onMainGolferPickupButtonClick() },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)  // This makes it take up the other half
                    .padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))
        }

            if (showPacePopover) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.34f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showPacePopover = false
                        }
                        .zIndex(1f)
                )

                PaceOfPlayPopover(
                    status = paceStatus,
                    onDismiss = { showPacePopover = false },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = topCardTop)
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                        .zIndex(2f)
                )
            }

        // Back confirmation dialog
        if (showBackConfirmDialog) {
            // Extract StateFlow values to local variables for null checking
            val currentGolferValue = currentGolfer
            val localGameValue = localGame

            // Find the partner marked by current user
            val markerName = if (currentGolferValue != null && localGameValue != null) {
                val partner = localGameValue.playingPartners.find { partner ->
                    partner.markedByGolfLinkNumber == currentGolferValue.golfLinkNo
                }
                if (partner != null) {
                    "${partner.firstName} ${partner.lastName}".trim()
                } else {
                    "Unknown"
                }
            } else {
                "Unknown"
            }

            AlertDialog(
                onDismissRequest = { showBackConfirmDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showBackConfirmDialog = false
                            playRoundViewModel.removeMarkerAndNavigateBack(navController)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MSLColors.mslGreen
                        )
                    ) {
                        Text("Yes", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showBackConfirmDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MSLColors.mslRed
                        )
                    ) {
                        Text("No", color = Color.White)
                    }
                },
                title = { Text("Remove Marker") },
                text = {
                    Text("This will remove your marker ($markerName) and you will need to choose again. Are you sure?")
                }
            )
        }

        // Keep error dialogs
        markerError?.let { error ->
            AlertDialog(
                onDismissRequest = { playRoundViewModel.clearMarkerError() },
                confirmButton = {
                    Button(onClick = { playRoundViewModel.clearMarkerError() }) {
                        Text("OK")
                    }
                },
                title = { Text("Marker Removal Error") },
                text = { Text(error) }
            )
        }

        // Abandon round confirmation dialog
        if (showAbandonDialog) {
            AlertDialog(
                onDismissRequest = { showAbandonDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showAbandonDialog = false
                            playRoundViewModel.abandonRound(navController)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MSLColors.mslGreen
                        )
                    ) {
                        Text("Yes", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showAbandonDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MSLColors.mslRed
                        )
                    ) {
                        Text("No", color = Color.White)
                    }
                },
                title = { Text("Abandon Round") },
                text = {
                    Text("Do you want to abandon your round? This will delete all your progress and you'll need to start over.")
                }
            )
        }

        // Abandon error dialog
        abandonError?.let { error ->
            AlertDialog(
                onDismissRequest = { playRoundViewModel.clearAbandonError() },
                confirmButton = {
                    Button(onClick = { playRoundViewModel.clearAbandonError() }) {
                        Text("OK")
                    }
                },
                title = { Text("Abandon Round Error") },
                text = { Text(error) }
            )
        }

        // Keep loading states
        if (isRemovingMarker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Removing marker...", color = Color.White)
                }
            }
        }

        // Abandon loading state
        if (isAbandoningRound) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Abandoning round...", color = Color.White)
                }
            }
        }

        currentRound?.let { round ->
            if (showDialog && round.holeScores.isNotEmpty() && round.playingPartnerRound?.holeScores?.isNotEmpty() == true) {
                GoToHoleAlertDialog(
                    holeScores = round.holeScores,
                    holeScoresPlayingPartner = round.playingPartnerRound.holeScores,
                    validHoleRange = playRoundViewModel.getValidHoleRange(),
                    showDialog = showDialog,
                    onDismiss = { playRoundViewModel.hideGoToHoleDialog() },
                    onConfirm = { holeNumber ->
                        playRoundViewModel.hideGoToHoleDialog()
                        playRoundViewModel.navigateToHole(holeNumber)
                    }
                )
            }
        }
    }
}

}
