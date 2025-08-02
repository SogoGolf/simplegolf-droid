package com.sogo.golf.msl.features.competitions.presentation

import android.net.Network
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.domain.model.mongodb.SogoGolfer
import com.sogo.golf.msl.shared_components.ui.ScreenWithDrawer
import com.sogo.golf.msl.shared_components.ui.UnifiedScreenHeader
import com.sogo.golf.msl.shared_components.ui.UserInfoSection
import com.sogo.golf.msl.shared_components.ui.components.NetworkMessageSnackbar
import com.sogo.golf.msl.ui.theme.MSLColors.mslYellow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import androidx.lifecycle.viewModelScope
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(
    navController: NavController,
    title: String,
    nextRoute: String,
    competitionViewModel: CompetitionViewModel = hiltViewModel()
) {
    val view = LocalView.current
    var includeRound by remember { mutableStateOf(true) }
    val refreshState = rememberPullToRefreshState()


    // Get the data from the view model
    val currentGolfer by competitionViewModel.currentGolfer.collectAsState()
    val localGame by competitionViewModel.localGame.collectAsState()
    val sogoGolfer by competitionViewModel.sogoGolfer.collectAsState()
    val mslFees by competitionViewModel.mslFees.collectAsState()
    val tokenCost by competitionViewModel.tokenCost.collectAsState()
    val canProceed by competitionViewModel.canProceed.collectAsState()
    val uiState by competitionViewModel.uiState.collectAsState()
    val purchaseTokensState by competitionViewModel.purchaseTokensState.collectAsState()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .padding(top = 56.dp) // Account for header height
        ) {
            UserInfoSection(
                golfer = currentGolfer,
                game = localGame
            )

            PullToRefreshBox(
                state = refreshState,
                isRefreshing = uiState.isLoading,
                onRefresh = {
                    competitionViewModel.triggerRefresh()
                },
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.Center),
                        isRefreshing = uiState.isLoading,
                        state = refreshState
                    )
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                val game = localGame
                when {
                    game == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Waiting for scorecard data...",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            )
                        }
                    }
                        game.competitions.isEmpty() -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
//                                    .background(Color.Red)
                            ) {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No Competitions Registrations or Entries found, try registering in a competition or printing a scorecard\n\n(Pull down to refresh)",
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 40.dp)
                                        )
                                    }
                                }
                            }
                        }
                    else -> {
                        CompetitionsListSection(
                            game = game
                        )
                    }
                }
            }

            // Footer is now a direct child of the Column
            FooterContent(
                includeRound = includeRound,
                golfer = currentGolfer,
                sogoGolfer = sogoGolfer,
                tokenCost = tokenCost,
                onIncludeRoundChanged = { newValue ->
                    includeRound = newValue
                    competitionViewModel.setIncludeRound(newValue)
                },
                onNextClick = {
                    val hasInsufficientTokens = includeRound && 
                        tokenCost > 0 && 
                        (sogoGolfer?.tokenBalance ?: 0) < tokenCost
                    
                    if (hasInsufficientTokens) {
                        competitionViewModel.purchaseTokens()
                    } else {
                        navController.navigate(nextRoute)
                    }
                }
            )
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
                    competitionViewModel.clearError()
                    competitionViewModel.clearMessages()
                }
            )
        }
    }

    if (purchaseTokensState.isInProgress) {
        PaywallDialog(
            PaywallDialogOptions.Builder()
                .setRequiredEntitlementIdentifier("all_features")
                .setShouldDisplayBlock {
                    true
                }
                .setListener(
                    object : PaywallListener {
                        override fun onPurchaseStarted(rcPackage: com.revenuecat.purchases.Package) {
                            super.onPurchaseStarted(rcPackage)
                            Log.d("PaywallDialog", "onPurchaseStarted: $rcPackage")
                        }
                        override fun onPurchaseCompleted(customerInfo: CustomerInfo, storeTransaction: StoreTransaction) {
                            super.onPurchaseCompleted(customerInfo, storeTransaction)
                            Log.d("PaywallDialog", "onPurchaseCompleted: $customerInfo")

                            competitionViewModel.viewModelScope.launch {
                                competitionViewModel.updateTokenBalanceForGolfer(storeTransaction) {
                                    navController.navigate(nextRoute)
                                }
                            }
                        }
                        override fun onPurchaseError(error: PurchasesError) {
                            super.onPurchaseError(error)
                            Log.d("PaywallDialog", "onPurchaseError: $error")

                            competitionViewModel.setPurchaseTokensState(isInProgress = false)
                        }
                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            Log.d("PaywallDialog", "onRestoreCompleted: $customerInfo")

                            competitionViewModel.setPurchaseTokensState(isInProgress = false)
                        }
                    }
                )
                .setDismissRequest {
                    Log.d("PaywallDialog", "setDismissRequest: close the dialog now")
                    competitionViewModel.setPurchaseTokensState(isInProgress = false)
                }
                .build()
        )
    }
}

@Composable
fun CompetitionsListSection(
    game: com.sogo.golf.msl.domain.model.msl.MslGame?
) {
    game?.let { nonNullGame ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(nonNullGame.competitions.size) { index ->
                val competition = nonNullGame.competitions[index]
                CompetitionCard(
                    title = competition.name,
                    subtitle = nonNullGame.teeName ?: ""
                )
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
            )
        }

        Text(
            text = "Token cost: $tokenCost",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
        )

        // Token balance display
        Text(
            text = "Your Token Balance: ${sogoGolfer?.tokenBalance ?: 0}",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(6.dp))

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
