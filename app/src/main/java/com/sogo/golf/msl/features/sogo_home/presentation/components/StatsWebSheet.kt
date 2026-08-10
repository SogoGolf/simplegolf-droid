package com.sogo.golf.msl.features.sogo_home.presentation.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.restorePurchasesWith
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.sogo.golf.msl.BuildConfig
import kotlinx.coroutines.delay

/** RevenueCat entitlement that unlocks the stats dashboard. */
private const val STATS_ENTITLEMENT_ID = "stats"

/** RevenueCat offering (monthly + annual packages) the stats paywall sells. */
private const val STATS_OFFERING_ID = "stats"

private fun CustomerInfo.hasStatsEntitlement(): Boolean =
    entitlements.all[STATS_ENTITLEMENT_ID]?.isActive == true

/**
 * Full-screen slide-up panel showing the golfer stats dashboard (sogo-golfer-stats
 * web app in ?embed=1 mode). The app owns identity: it passes the signed-in golfer's
 * GOLF Link number and theme in the URL, so the page has no login UI.
 *
 * Mirrors iOS StatsWebSheet: hidden entirely when STATS_WEB_URL is empty. The
 * purchase gate follows the RevenueCat "stats" entitlement — defensively, until
 * the store products + RC offering exist, the entitlement stays false and the
 * offering stays null, so the panel keeps its locked gate and the unlock CTA
 * explains subscriptions aren't available yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsWebSheet(
    golfLinkNo: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var retryKey by remember { mutableStateOf(0) }

    var hasEntitlement by remember { mutableStateOf(false) }
    var statsOffering by remember { mutableStateOf<Offering?>(null) }
    // The web load holds on this so the panel opens straight into the right
    // locked/price state instead of reloading mid-view.
    var subscriptionReady by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showUnavailable by remember { mutableStateOf(false) }
    var showRestoreFailed by remember { mutableStateOf(false) }
    var showRestoreError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var entitlementDone = false
        var offeringDone = false
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = {
                entitlementDone = true
                if (offeringDone) subscriptionReady = true
            },
            onSuccess = { info ->
                hasEntitlement = info.hasStatsEntitlement()
                entitlementDone = true
                if (offeringDone) subscriptionReady = true
            }
        )
        Purchases.sharedInstance.getOfferingsWith(
            onError = {
                offeringDone = true
                if (entitlementDone) subscriptionReady = true
            },
            onSuccess = { offerings ->
                statsOffering = offerings.all[STATS_OFFERING_ID]
                offeringDone = true
                if (entitlementDone) subscriptionReady = true
            }
        )
        // Fail open to the locked view rather than spinning forever offline.
        delay(2000)
        subscriptionReady = true
    }

    // Live store price once the RC offering has a monthly package (never label
    // another package's price as per-month); placeholder applies until then.
    val priceString = statsOffering?.monthly?.product?.price?.formatted?.let { "$it/mo" }

    val statsUrl = remember(golfLinkNo, retryKey, hasEntitlement, priceString) {
        Uri.parse(BuildConfig.STATS_WEB_URL).buildUpon()
            .appendQueryParameter("embed", "1")
            .appendQueryParameter("gl", golfLinkNo)
            .appendQueryParameter("theme", "light")
            .apply {
                if (!hasEntitlement) {
                    appendQueryParameter("locked", "1")
                    // Placeholder until the RC offering exists.
                    appendQueryParameter("price", priceString ?: "A$4.99/mo")
                }
            }
            .build()
            .toString()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!subscriptionReady) {
                // Hold the web load until the entitlement is resolved (spinner below).
            } else if (!loadFailed) {
                // Recreate (and so reload) when the entitlement flips after a
                // purchase or restore, dropping the locked overlay.
                key(hasEntitlement) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                @SuppressLint("SetJavaScriptEnabled")
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                // The bottom sheet otherwise intercepts vertical drags for
                                // drag-to-dismiss, so the page can never scroll. Keep touches
                                // on the WebView; the close button dismisses the sheet.
                                setOnTouchListener { v, _ ->
                                    v.parent.requestDisallowInterceptTouchEvent(true)
                                    false
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url ?: return false
                                        if (url.scheme == "simplegolf") {
                                            // The web gate's unlock/restore CTAs, not navigation.
                                            when (url.host) {
                                                "unlock-stats" -> {
                                                    if (statsOffering != null) {
                                                        showPaywall = true
                                                    } else {
                                                        showUnavailable = true
                                                    }
                                                }
                                                "restore-stats" -> {
                                                    Purchases.sharedInstance.restorePurchasesWith(
                                                        // Failed restore (offline, store error) is not the
                                                        // same as a restore that found no subscription.
                                                        onError = { showRestoreError = true },
                                                        onSuccess = { info ->
                                                            hasEntitlement = info.hasStatsEntitlement()
                                                            if (!info.hasStatsEntitlement()) {
                                                                showRestoreFailed = true
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                            return true
                                        }
                                        return false
                                    }

                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            isLoading = false
                                            loadFailed = true
                                        }
                                    }
                                }
                                loadUrl(statsUrl)
                            }
                        },
                        update = { webView ->
                            if (webView.url == null) webView.loadUrl(statsUrl)
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your stats aren't available right now.",
                        color = Color.Gray
                    )
                    TextButton(onClick = {
                        loadFailed = false
                        isLoading = true
                        retryKey++
                    }) {
                        Text("Try again")
                    }
                }
            }

            if ((isLoading || !subscriptionReady) && !loadFailed) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Floating close button over the page's hero, matching iOS.
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 16.dp)
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    val paywallOffering = statsOffering
    if (showPaywall && paywallOffering != null) {
        PaywallDialog(
            PaywallDialogOptions.Builder()
                .setOffering(paywallOffering)
                .setListener(object : PaywallListener {
                    override fun onPurchaseCompleted(
                        customerInfo: CustomerInfo,
                        storeTransaction: StoreTransaction
                    ) {
                        hasEntitlement = customerInfo.hasStatsEntitlement()
                        showPaywall = false
                    }

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                        hasEntitlement = customerInfo.hasStatsEntitlement()
                        // Keep the paywall up if the restore found nothing.
                        if (customerInfo.hasStatsEntitlement()) {
                            showPaywall = false
                        }
                    }
                })
                .setDismissRequest { showPaywall = false }
                .build()
        )
    }

    if (showUnavailable) {
        AlertDialog(
            onDismissRequest = { showUnavailable = false },
            confirmButton = {
                TextButton(onClick = { showUnavailable = false }) { Text("OK") }
            },
            title = { Text("Subscriptions coming soon") },
            text = { Text("Stats subscriptions aren't available just yet. Please check back soon.") }
        )
    }

    if (showRestoreFailed) {
        AlertDialog(
            onDismissRequest = { showRestoreFailed = false },
            confirmButton = {
                TextButton(onClick = { showRestoreFailed = false }) { Text("OK") }
            },
            title = { Text("Nothing to restore") },
            text = { Text("We couldn't find an active stats subscription for this account.") }
        )
    }

    if (showRestoreError) {
        AlertDialog(
            onDismissRequest = { showRestoreError = false },
            confirmButton = {
                TextButton(onClick = { showRestoreError = false }) { Text("OK") }
            },
            title = { Text("Restore failed") },
            text = { Text("We couldn't restore purchases. Please check your connection and try again.") }
        )
    }
}
