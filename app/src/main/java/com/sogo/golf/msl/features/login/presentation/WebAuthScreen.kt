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

@Composable
fun WebAuthScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    val uiState by loginViewModel.uiState.collectAsState()
    val selectedClub = uiState.selectedClub

    // Observe auth success event
    LaunchedEffect(Unit) {
        loginViewModel.authSuccessEvent.collect {
            navController.navigate("homescreen") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Get the auth URL for the selected club
    val authUrl = remember(selectedClub) {
        if (selectedClub != null && selectedClub.name.isNotBlank()) {
            // Derive auth path from club name

            val tenantId = selectedClub.tenantId

            val url = "https://id.micropower.com.au/$tenantId?returnUrl=msl://success"
            android.util.Log.d("WebAuthScreen", "Generated auth URL: $url for club: ${selectedClub.name}")
            url
        } else {
            // Fallback to default
            val url = "https://id.micropower.com.au/goldencreekgolfclub?returnUrl=msl://success"
            android.util.Log.w("WebAuthScreen", "No club selected, using fallback URL: $url")
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
                        }

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url.toString()

                                // Let the LoginViewModel handle the URL parsing and MSL auth flow
                                loginViewModel.handleUrlRedirect(url)

                                // Return true if this is our success redirect to prevent WebView from loading it
                                return url.startsWith("msl://success")
                            }
                        }

                        if (BuildConfig.DEBUG) {
                            WebView.setWebContentsDebuggingEnabled(true)
                        }

                        // Load the auth URL for the selected club
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
                    Text("Loading ${selectedClub?.name ?: "club"} login...")
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
                    if (selectedClub != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Club: ${selectedClub.name}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}



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