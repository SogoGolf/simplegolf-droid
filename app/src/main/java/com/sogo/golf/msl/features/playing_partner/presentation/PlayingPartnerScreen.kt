package com.sogo.golf.msl.features.playing_partner.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.model.msl.MslPlayingPartner
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.shared_components.ui.UnifiedScreenHeader
import com.sogo.golf.msl.shared_components.ui.UserInfoSection
import com.sogo.golf.msl.shared_components.ui.components.ColoredSquare
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslGreen
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingPartnerScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    playingPartnerViewModel: PlayingPartnerViewModel = hiltViewModel()
) {
    val view = LocalView.current
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Get the data from the view model
    val currentGolfer by playingPartnerViewModel.currentGolfer.collectAsState()
    val localGame by playingPartnerViewModel.localGame.collectAsState()
    val mslGameState by playingPartnerViewModel.mslGameState.collectAsState()
    val selectedPartner by playingPartnerViewModel.selectedPartner.collectAsState()
    val uiState by playingPartnerViewModel.uiState.collectAsState()

    // Set status bar to white with black text and icons
    SideEffect {
        val window = (view.context as? androidx.activity.ComponentActivity)?.window ?: return@SideEffect
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        window.statusBarColor = Color.White.toArgb()
        windowInsetsController.isAppearanceLightStatusBars = true
    }
    
    // Track playing partner screen viewed only when game data is loaded
    LaunchedEffect(localGame) {
        localGame?.let {
            playingPartnerViewModel.trackPlayingPartnerScreenViewed()
        }
    }

    // Handle back button navigation to CompetitionScreen
    BackHandler {
        navController.navigate("competitionscreen") {
            popUpTo(0) { inclusive = true }
        }
    }

    ScreenWithDrawer(
        navController = navController,
        topBar = {
            UnifiedScreenHeader(
                title = "Playing Partner",
                backgroundColor = Color.White
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 56.dp) // Account for header height
        ) {
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
                    coroutineScope.launch {
                        isRefreshing = true
                        try {
                            android.util.Log.d("PlayingPartnerScreen", "🔄 Starting pull-to-refresh...")
                            val success = playingPartnerViewModel.refreshAllData()
                            android.util.Log.d("PlayingPartnerScreen", if (success) "✅ Refresh completed successfully" else "⚠️ Refresh completed with some errors")
                        } catch (e: Exception) {
                            android.util.Log.e("PlayingPartnerScreen", "❌ Refresh error", e)
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
                modifier = Modifier.weight(1f)
            ) {
                PlayingPartnersListSection(
                    mslGameState = mslGameState,
                    selectedPartner = selectedPartner,
                    onPartnerSelected = { partner -> playingPartnerViewModel.selectPartner(partner) }
                )
            }

            // "Let's Play" button with spinner - disabled until partner selected
            val isButtonEnabled = selectedPartner != null && !uiState.isLetsPlayLoading && !uiState.isRefreshLoading
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top border when disabled
                if (!isButtonEnabled) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = Color.Gray.copy(alpha = 0.1f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(if (isButtonEnabled) mslYellow else Color.LightGray)
                        .clickable(enabled = isButtonEnabled) {
                            if (isButtonEnabled) {
                                playingPartnerViewModel.onLetsPlayClicked {
                                    navController.navigate(nextRoute)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLetsPlayLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Let's Play",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Let's Play",
                            color = if (isButtonEnabled) Color.White else Color.DarkGray.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { error ->
        Box(modifier = Modifier.padding(top = 50.dp)) {
            NetworkMessageSnackbar (
                message = error,
                verticalAlignment = Alignment.CenterVertically,
                textColor = Color.White,
                backgroundColor = Color.Red,
                isError = false,
                onDismiss = {
                    playingPartnerViewModel.clearError()
                    playingPartnerViewModel.clearMessages()
                }
            )
        }
    }

}

data class MslGameState(
    val isLoading: Boolean = false,
    val game: MslGame? = null,
    val error: String = ""
)


@Composable
fun PlayingPartnersListSection(
    mslGameState: MslGameState,
    selectedPartner: MslPlayingPartner?,
    onPartnerSelected: (MslPlayingPartner) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
    ) {
        when {
            mslGameState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting for playing partner data...",
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }
            }

            mslGameState.game?.playingPartners?.isNotEmpty() == true -> {
                items(mslGameState.game!!.playingPartners.size) { index ->
                    val playingPartner = mslGameState.game!!.playingPartners[index]
                    PlayingPartnerCard(
                        title = "${playingPartner.firstName} ${playingPartner.lastName}",
                        dailyHandicap = playingPartner.dailyHandicap.toString(),
                        isAvailable = playingPartner.markedByGolfLinkNumber == null,
                        selected = selectedPartner?.golfLinkNumber == playingPartner.golfLinkNumber,
                        onCardClick = { onPartnerSelected(playingPartner) }
                    )
                }
            }

            mslGameState.error.isNotEmpty() -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize()
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mslGameState.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            else -> {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No playing partners available",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            )
                            Text(
                                text = "(Pull down to refresh)",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PlayingPartnerCard(
    title: String,
    dailyHandicap: String,
    isAvailable: Boolean,
    selected: Boolean,
    onCardClick: () -> Unit
) {
    val backgroundColor = if (isAvailable) Color.White else Color(0xFFB0B0B0)
    val availabilityText = if (isAvailable) "Available" else "Already Selected"
    val availabilityTextColor = if (isAvailable) mslGreen else mslYellow
    val availabilityBorderColor = if (isAvailable) mslGreen else mslYellow
    val titleAlpha = if (isAvailable) 1f else 0.4f
    val borderColor = if (selected) mslBlue else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .border(
                BorderStroke(3.dp, SolidColor(borderColor)),
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = isAvailable, onClick = onCardClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null, // null because we're handling clicks on the entire card
                enabled = isAvailable,
                colors = RadioButtonDefaults.colors(
                    selectedColor = mslBlue,
                    unselectedColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    color = mslBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(titleAlpha)
                )
                Text(
                    text = "Daily Handicap: $dailyHandicap",
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, availabilityBorderColor, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = availabilityText,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = availabilityTextColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
