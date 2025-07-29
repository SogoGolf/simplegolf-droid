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
import com.sogo.golf.msl.data.network.dto.mongodb.SogoGolferDto
import com.sogo.golf.msl.domain.model.mongodb.Fee
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.shared_components.ui.UnifiedScreenHeader
import com.sogo.golf.msl.shared_components.ui.components.ColoredSquare
import com.sogo.golf.msl.shared.utils.TimeFormatUtils
import com.sogo.golf.msl.shared_components.ui.UserInfoSection
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
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
    val coroutineScope = rememberCoroutineScope()


    // Get the data from the view model
    val currentGolfer by competitionViewModel.currentGolfer.collectAsState()
    val localGame by competitionViewModel.localGame.collectAsState()
    val sogoGolfer by competitionViewModel.sogoGolfer.collectAsState()
    val mslFees by competitionViewModel.mslFees.collectAsState()
    val tokenCost by competitionViewModel.tokenCost.collectAsState()
    val canProceed by competitionViewModel.canProceed.collectAsState()

    // Set status bar to white with black text and icons
    SideEffect {
        val window = (view.context as? androidx.activity.ComponentActivity)?.window ?: return@SideEffect
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        window.statusBarColor = Color.White.toArgb()
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    ScreenWithDrawer(
        navController = navController,
        topBar = {
            UnifiedScreenHeader(
                title = "Competitions",
                backgroundColor = Color.White
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 56.dp) // Account for header height
            ) {
                UserInfoSection(
                    golfer = currentGolfer,
                    game = localGame
                )

                PullToRefreshBox(
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        coroutineScope.launch {
                            isRefreshing = true
                            try {
                                val result = competitionViewModel.refreshMslData()
                                if (result.isFailure) {
                                    android.util.Log.e("CompetitionsScreen", "MSL refresh failed: ${result.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("CompetitionsScreen", "MSL refresh error", e)
                            } finally {
                                isRefreshing = false
                            }
                        }
                    },
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.Center),
                            isRefreshing = isRefreshing,
                            state = refreshState
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                ) {
                    CompetitionsListSection(
                        game = localGame
                    )
                }
            }

            // Footer pinned to bottom
            FooterContent(
                includeRound = includeRound,
                golfer = currentGolfer,
                sogoGolfer = sogoGolfer,
                tokenCost = tokenCost,
                canProceed = canProceed,
                onIncludeRoundChanged = { newValue ->
                    includeRound = newValue
                    competitionViewModel.setIncludeRound(newValue)
                },
                onNextClick = {
                    navController.navigate(nextRoute)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }
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
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
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
        }
    }
}

@Composable
fun FooterContent(
    includeRound: Boolean,
    golfer: com.sogo.golf.msl.domain.model.msl.MslGolfer?,
    sogoGolfer: SogoGolfer?,
    tokenCost: Double,
    canProceed: Boolean,
    onIncludeRoundChanged: (Boolean) -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
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

        Text(
            text = "Token cost: $tokenCost",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        // Token balance display
        Text(
            text = "Your Token Balance: ${sogoGolfer?.tokenBalance ?: 0}",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Next button - enable only if competitions are available
        val hasCompetitions = true //localGame?.competitions?.isNotEmpty() == true
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