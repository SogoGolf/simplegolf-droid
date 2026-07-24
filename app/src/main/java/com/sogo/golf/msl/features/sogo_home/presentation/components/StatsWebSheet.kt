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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sogo.golf.msl.BuildConfig

/**
 * Full-screen slide-up panel showing the golfer stats dashboard (sogo-golfer-stats
 * web app in ?embed=1 mode). The app owns identity: it passes the signed-in golfer's
 * GOLF Link number and theme in the URL, so the page has no login UI.
 *
 * Mirrors iOS StatsWebSheet: hidden entirely when STATS_WEB_URL is empty, and the
 * purchase gate is hard-coded on (locked=1) until the RevenueCat stats entitlement
 * exists.
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

    // TODO: replace with the real RevenueCat stats entitlement check once the
    // product exists. Hard-coded "unpaid" for now so the blur gate is visible.
    val hasStatsEntitlement = false

    val statsUrl = remember(golfLinkNo, retryKey) {
        Uri.parse(BuildConfig.STATS_WEB_URL).buildUpon()
            .appendQueryParameter("embed", "1")
            .appendQueryParameter("gl", golfLinkNo)
            .appendQueryParameter("theme", "light")
            .apply {
                if (!hasStatsEntitlement) {
                    appendQueryParameter("locked", "1")
                    // TODO: use the RevenueCat package's localized price once the product exists.
                    appendQueryParameter("price", "A$4.99/mo")
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
            if (!loadFailed) {
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
                                        // TODO: unlock-stats deep link — present the
                                        // RevenueCat paywall once the product exists.
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

            if (isLoading && !loadFailed) {
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
}
