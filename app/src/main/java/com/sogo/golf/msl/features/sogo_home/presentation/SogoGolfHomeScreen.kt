package com.sogo.golf.msl.features.sogo_home.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.sogo.golf.msl.R
import com.sogo.golf.msl.features.sogo_home.presentation.components.LeaderboardsButton
import com.sogo.golf.msl.features.sogo_home.presentation.components.SogoInfoSheet
import com.sogo.golf.msl.features.sogo_home.presentation.components.SogoTopicButton
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SogoGolfHomeScreen(
    navController: NavController,
    viewModel: SogoGolfHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val purchaseTokenState by viewModel.purchaseTokenState.collectAsState()
    val currentGolfer by viewModel.currentGolfer.collectAsState()
    val sogoGolfer by viewModel.sogoGolfer.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }
    val purchaseTokensState by viewModel.purchaseTokenState.collectAsState()

    var showSogoInfoDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mslBlue)
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.home_background_image)
                .memoryCacheKey("home_background")
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.sogo_logo_with_tag_line)
                    .memoryCacheKey("sogo_logo_tagline")
                    .build(),
                contentDescription = "SOGO Logo",
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            LeaderboardsButton(
                onClick = {
                    viewModel.trackLeaderboardsButtonClicked()
                    //navController.navigate("leaderboards")
                },
                title = "NATIONAL LEADERBOARDS"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SogoTopicButton(
                title = "MY ROUNDS",
                subTitle = "",
                icon = Icons.Filled.SportsGolf,
                onClick = {
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentGolfer?.let { golfer ->
                    sogoGolfer?.let { sogo ->
                        SogoTopicButton(
                            title = "PURCHASE TOKENS",
                            subTitle = "Balance: ${sogo.tokenBalance} Tokens",
                            imageResId = R.drawable.token_icon,
                            onClick = {
                                viewModel.setPurchaseTokensState(isInProgress = true)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                SogoTopicButton(
                    title = "SOGO T&C",
                    subTitle = "",
                    imageResId = R.drawable.info_icon,
                    onClick = {
                        showSogoInfoDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (purchaseTokensState.isLoading) {
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

                            viewModel.viewModelScope.launch {
                                viewModel.updateTokenBalance(storeTransaction)
                            }
                        }
                        override fun onPurchaseError(error: PurchasesError) {
                            super.onPurchaseError(error)
                            Log.d("PaywallDialog", "onPurchaseError: $error")
                            viewModel.setPurchaseTokensState(isInProgress = false)
                        }
                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            Log.d("PaywallDialog", "onRestoreCompleted: $customerInfo")
                            viewModel.setPurchaseTokensState(isInProgress = false)
                        }
                    }
                )
                .setDismissRequest {
                    Log.d("PaywallDialog", "setDismissRequest: close the dialog now")
                    viewModel.setPurchaseTokensState(isInProgress = false)
                    showPaywall = false
                }
                .build()
        )
    }

    if (showSogoInfoDialog) {
        ModalBottomSheet(
            onDismissRequest = { showSogoInfoDialog = false },
            sheetState = bottomSheetState
        ) {
            SogoInfoSheet(
                onItemClicked = { itemType ->
                    viewModel.trackSogoTandCItemClicked(itemType)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SogoGolfHomeScreenPreview() {
    SogoGolfHomeScreen(navController = rememberNavController())
}
