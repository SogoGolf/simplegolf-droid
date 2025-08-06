

package com.sogo.golf.msl.features.login.presentation

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sogo.golf.msl.BuildConfig
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack

@Composable
fun WebAuthScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    val uiState by loginViewModel.uiState.collectAsState()

    // Use remember to capture the club at screen creation time
    val selectedClub = remember { uiState.selectedClub }

    // Also monitor current state for debugging
    val currentSelectedClub = uiState.selectedClub

    // Debug logging
    LaunchedEffect(Unit) {
        android.util.Log.d("WebAuthScreen", "=== WEBAUTH SCREEN LAUNCHED ===")
        android.util.Log.d("WebAuthScreen", "Remembered club: ${selectedClub?.name}")
        android.util.Log.d("WebAuthScreen", "Current UI state club: ${currentSelectedClub?.name}")
    }

    // Monitor changes to current state
    LaunchedEffect(currentSelectedClub) {
        android.util.Log.d("WebAuthScreen", "UI state changed - new club: ${currentSelectedClub?.name}")
        if (selectedClub != null && currentSelectedClub == null) {
            android.util.Log.w("WebAuthScreen", "⚠️ WARNING: Club selection was lost after screen creation!")
        }
    }

    // Observe auth success event
    LaunchedEffect(Unit) {
        loginViewModel.authSuccessEvent.collect {
            navController.navigate("homescreen") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Use the remembered club (from screen creation) instead of current state
    val clubToUse = selectedClub ?: currentSelectedClub

    if (clubToUse == null) {
        android.util.Log.e("WebAuthScreen", "❌ NO CLUB AVAILABLE")

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "No club selected",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Please go back and select a club",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Go Back")
                }

                // Debug info
                if (BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Debug: Remembered=${selectedClub?.name}, Current=${currentSelectedClub?.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = mslBlack
                    )
                }
            }
        }
        return
    }

    // Generate auth URL
    val authUrl = remember(clubToUse) {
        val tenantId = clubToUse.tenantId
        if (tenantId.isBlank()) {
            android.util.Log.e("WebAuthScreen", "❌ TenantID is blank for club: ${clubToUse.name}")
            "https://id.micropower.com.au/goldencreekgolfclub?returnUrl=msl://success"
        } else {
            val url = "https://id.micropower.com.au/$tenantId?returnUrl=msl://success"
            android.util.Log.d("WebAuthScreen", "✅ Generated auth URL: $url for club: ${clubToUse.name}")
            url
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!uiState.isProcessingAuth) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportMultipleWindows(false)
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            allowFileAccess = false
                            allowContentAccess = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                android.util.Log.d("WebAuthScreen", "Page finished loading: $url")
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                android.util.Log.d("WebAuthScreen", "Page started loading: $url")

                                if (url != null && url.startsWith("msl://success")) {
                                    android.util.Log.d("WebAuthScreen", "SUCCESS REDIRECT DETECTED: $url")
                                    loginViewModel.handleUrlRedirect(url)
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: ""
                                android.util.Log.d("WebAuthScreen", "URL Loading: $url")

                                if (url.startsWith("msl://success")) {
                                    android.util.Log.d("WebAuthScreen", "SUCCESS REDIRECT in shouldOverride: $url")
                                    loginViewModel.handleUrlRedirect(url)
                                    return true
                                }

                                return false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                super.onReceivedError(view, errorCode, description, failingUrl)
                                android.util.Log.e("WebAuthScreen", "WebView error: $errorCode - $description for URL: $failingUrl")
                            }
                        }

                        if (BuildConfig.DEBUG) {
                            WebView.setWebContentsDebuggingEnabled(true)
                        }

                        android.util.Log.d("WebAuthScreen", "Loading auth URL: $authUrl")
                        loadUrl(authUrl)
                    }
                }
            )
        }

        // Loading indicators
        if (isLoading && !uiState.isProcessingAuth) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading ${clubToUse.name} login...")
                }
            }
        }

        if (uiState.isProcessingAuth) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Processing authentication...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Exchanging tokens with MSL API",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Club: ${clubToUse.name}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

//@Composable
//fun WebAuthScreen(
//    navController: NavController,
//    loginViewModel: LoginViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    var isLoading by remember { mutableStateOf(true) }
//    val uiState by loginViewModel.uiState.collectAsState()
//    val selectedClub = uiState.selectedClub
//
//    // Observe auth success event
//    LaunchedEffect(Unit) {
//        loginViewModel.authSuccessEvent.collect {
//            navController.navigate("homescreen") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    }
//
//    // Get the auth URL for the selected club
//    val authUrl = remember(selectedClub) {
//        if (selectedClub != null && selectedClub.name.isNotBlank()) {
//            // Derive auth path from club name
//
//            val tenantId = selectedClub.tenantId
//
//            val url = "https://id.micropower.com.au/$tenantId?returnUrl=msl://success"
//            android.util.Log.d("WebAuthScreen", "Generated auth URL: $url for club: ${selectedClub.name}")
//            url
//        } else {
//            // Fallback to default
//            val url = "https://id.micropower.com.au/goldencreekgolfclub?returnUrl=msl://success"
//            android.util.Log.w("WebAuthScreen", "No club selected, using fallback URL: $url")
//            url
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        if (!uiState.isProcessingAuth) {
//            AndroidView(
//                modifier = Modifier.fillMaxSize(),
//                factory = {
//                    WebView(context).apply {
//                        layoutParams = ViewGroup.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.MATCH_PARENT
//                        )
//
//                        settings.apply {
//                            javaScriptEnabled = true
//                            domStorageEnabled = true
//                            cacheMode = WebSettings.LOAD_DEFAULT
//                            setSupportMultipleWindows(false)
//                            loadWithOverviewMode = true
//                            useWideViewPort = true
//                            allowFileAccess = false
//                            allowContentAccess = false
//                        }
//
//                        val cookieManager = CookieManager.getInstance()
//                        cookieManager.setAcceptCookie(true)
//                        cookieManager.setAcceptThirdPartyCookies(this, true)
//
//                        webViewClient = object : WebViewClient() {
//                            override fun onPageFinished(view: WebView?, url: String?) {
//                                super.onPageFinished(view, url)
//                                isLoading = false
//                            }
//
//                            override fun shouldOverrideUrlLoading(
//                                view: WebView?,
//                                request: WebResourceRequest?
//                            ): Boolean {
//                                val url = request?.url.toString()
//
//                                // Let the LoginViewModel handle the URL parsing and MSL auth flow
//                                loginViewModel.handleUrlRedirect(url)
//
//                                // Return true if this is our success redirect to prevent WebView from loading it
//                                return url.startsWith("msl://success")
//                            }
//                        }
//
//                        if (BuildConfig.DEBUG) {
//                            WebView.setWebContentsDebuggingEnabled(true)
//                        }
//
//                        // Load the auth URL for the selected club
//                        android.util.Log.d("WebAuthScreen", "Loading auth URL: $authUrl")
//                        loadUrl(authUrl)
//                    }
//                }
//            )
//        }
//
//        // Loading indicators
//        if (isLoading && !uiState.isProcessingAuth) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    CircularProgressIndicator()
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text("Loading ${selectedClub?.name ?: "club"} login...")
//                }
//            }
//        }
//
//        if (uiState.isProcessingAuth) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    CircularProgressIndicator()
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        "Processing authentication...",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        "Exchanging tokens with MSL API",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    if (selectedClub != null) {
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            "Club: ${selectedClub.name}",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
//            }
//        }
//    }
//}



//    package com.sogo.golf.msl.features.login.presentation
//
//import android.view.ViewGroup
//import android.webkit.CookieManager
//import android.webkit.WebResourceRequest
//import android.webkit.WebSettings
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//
//    @Composable
//fun WebAuthScreen(
//    navController: NavController,
//    viewModel: LoginViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//
//    // Observe auth success event
//    LaunchedEffect(Unit) {
//        viewModel.authSuccessEvent.collect {
//            navController.navigate("homescreen") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    }
//
//    AndroidView(
//        modifier = Modifier.fillMaxSize(),
//        factory = {
//            WebView(context).apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//
//                settings.javaScriptEnabled = true
//                settings.domStorageEnabled = true
//                settings.cacheMode = WebSettings.LOAD_DEFAULT
//                settings.setSupportMultipleWindows(false)
//                settings.loadWithOverviewMode = true
//                settings.useWideViewPort = true
//
//                CookieManager.getInstance().setAcceptCookie(true)
//                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
//
//                webViewClient = object : WebViewClient() {
//                    override fun shouldOverrideUrlLoading(
//                        view: WebView?,
//                        request: WebResourceRequest?
//                    ): Boolean {
//                        val url = request?.url.toString()
//                        viewModel.handleUrlRedirect(url)
//                        return url.startsWith("msl://success")
//                    }
//                }
//
//                WebView.setWebContentsDebuggingEnabled(true)
//
////                loadUrl("https://id.micropower.com.au/murwillumbahgolfclub?returnUrl=msl://success")
//                  loadUrl("https://id.micropower.com.au/goldencreekgolfclub?returnUrl=msl://success")
//            }
//        }
//    )
//}