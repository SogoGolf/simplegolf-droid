package com.sogo.golf.msl.features.competitions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    competitionViewModel: CompetitionViewModel = hiltViewModel()
) {
    val view = LocalView.current
    var isRefreshing by remember { mutableStateOf(false) }
    var includeRound by remember { mutableStateOf(true) }
    val refreshState = rememberPullToRefreshState()

    // Get the data from the view model
    val currentGolfer by competitionViewModel.currentGolfer.collectAsState()
    val localGame by competitionViewModel.localGame.collectAsState()

    // Set status bar to white with black text and icons
    SideEffect {
        val window = (view.context as? androidx.activity.ComponentActivity)?.window ?: return@SideEffect
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        window.statusBarColor = Color.White.toArgb()
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    ScreenWithDrawer(navController = navController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Header with menu button and title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Menu button space (handled by ScreenWithDrawer)
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Competitions",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            color = Color.Black
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(6f)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }

                // User information section
                UserInfoSection(
                    golfer = currentGolfer,
                    game = localGame
                )

                // Competitions list with pull-to-refresh
                PullToRefreshBox(
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        // TODO: Implement refresh logic using competitionViewModel
                        isRefreshing = true
                        // Simulate refresh for now
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            delay(1500)
                            isRefreshing = false
                        }
                    },
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.Center),
                            isRefreshing = isRefreshing,
                            state = refreshState
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    CompetitionsListSection(
                        game = localGame
                    )
                }

                // Footer section
                FooterContent(
                    includeRound = includeRound,
                    golfer = currentGolfer,
                    onIncludeRoundChanged = { newValue ->
                        includeRound = newValue
                        // TODO: Track toggle
                    },
                    onNextClick = {
                        // TODO: Validate and navigate
                        navController.navigate(nextRoute)
                    },
                )
            }
        }
    }
}

@Composable
fun UserInfoSection(
    golfer: com.sogo.golf.msl.domain.model.msl.MslGolfer?,
    game: com.sogo.golf.msl.domain.model.msl.MslGame?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // Start time and hole
        val startTime = if (game?.startingHoleNumber != 0 && game?.startingHoleNumber != null) {
            // TODO: Format booking time when available
            "10:30 AM" // Placeholder for now
        } else {
            "-"
        }

        val startHole = if (game?.startingHoleNumber != 0 && game?.startingHoleNumber != null) {
            "${game.startingHoleNumber}"
        } else {
            "-"
        }

        Text(
            text = "$startTime Starting Hole $startHole",
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        )

        // Player name
        Text(
            text = "${golfer?.firstName ?: "-"} ${golfer?.surname ?: "-"}",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
            ),
        )

        // Golf link number
        Text(
            text = golfer?.golfLinkNo ?: "-",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Handicaps row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${game?.gaHandicap ?: "-"}",
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GA Handicap",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dailyHcap = if (game?.startingHoleNumber != 0 && game?.startingHoleNumber != null) {
                    "${game.dailyHandicap}"
                } else {
                    "-"
                }
                Text(
                    text = dailyHcap,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Daily Handicap",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tee color and name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Add ColoredSquare component when available
            // if (game?.teeColour != null) ColoredSquare(hexColor = "#${game.teeColour}")
            // Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = game?.teeName ?: "",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Competition section header
        Text(
            text = "Your competition(s):",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CompetitionsListSection(
    game: com.sogo.golf.msl.domain.model.msl.MslGame?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
    ) {
        when {
            game == null -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting for scorecard data...",
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        )
                    }
                }
            }

            game.competitions.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No competitions available",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                            )
                            Text(
                                text = "(Pull down to refresh)",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                }
            }

            else -> {
                items(game.competitions.size) { index ->
                    val competition = game.competitions[index]
                    CompetitionCard(
                        title = competition.name,
                        subtitle = game.teeName ?: ""
                    )
                }
            }
        }
    }
}

@Composable
fun CompetitionCard(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FooterContent(
    includeRound: Boolean,
    golfer: com.sogo.golf.msl.domain.model.msl.MslGolfer?,
    onIncludeRoundChanged: (Boolean) -> Unit,
    onNextClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onIncludeRoundChanged(!includeRound)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = includeRound,
                onCheckedChange = onIncludeRoundChanged,
            )
            Text(
                text = "Include this round on SOGO Golf",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Token cost display - TODO: Implement actual fee calculation
        Text(
            text = "Token cost: -", // TODO: Calculate based on fees
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        // Token balance display
        if (golfer != null) {
            Text(
                text = "Your Token Balance: ${golfer.primary.toInt()}", // TODO: Use actual token balance when available
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Text(
                text = "Your Token Balance: 0",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Next button - enable only if competitions are available
        val hasCompetitions = false // localGame?.competitions?.isNotEmpty() == true
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(if (hasCompetitions) mslYellow else Color.LightGray)
                .clickable(enabled = hasCompetitions) {
                    if (hasCompetitions) {
                        onNextClick()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Next",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
    }
}