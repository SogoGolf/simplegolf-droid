package com.sogo.golf.msl.shared_components.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sogo.golf.msl.R
import com.sogo.golf.msl.navigation.NavViewModel
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue

@Composable
fun AppDrawer(
    navController: NavController,
    onCloseDrawer: () -> Unit,
    drawerViewModel: DrawerViewModel = hiltViewModel(),
    navViewModel: NavViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val imageWidth = screenWidth * 0.28f

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    val versionName = packageInfo.versionName
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode
    } else {
        packageInfo.versionCode
    }

    // Get current golfer from the drawer view model
    val currentGolfer by drawerViewModel.currentGolfer.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MSLColors.Surface)
    ) {
        // Header with Blue Background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(mslBlue)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.simple_golf_transparent)
                        .memoryCacheKey("simple_golf_logo")
                        .build(),
                    contentDescription = "Logo",
                    modifier = Modifier.width(imageWidth).padding(top = 25.dp)
                )

                // Close Button
                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Drawer",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Golfer name with white text
            currentGolfer?.let { golfer ->
                Text(
                    text = "${golfer.firstName} ${golfer.surname}",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Text(
                    text = "Golf Link: ${golfer.golfLinkNo}",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } ?: run {
                Text(
                    text = "Welcome",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Logout Text
            Text(
                text = "Logout",
                color = Color.White,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
                        navViewModel.logout(navController)
                        onCloseDrawer()
                    }
            )
        }

        // Divider
        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // Navigation Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val isDarkTheme = false // Since we're using light theme primarily

        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Terms & Conditions",
                textDecoration = TextDecoration.Underline,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                modifier = Modifier.clickable {
                    // Add your terms and conditions URL here
                    // uriHandler.openUri(Constants.TERMS_AND_CONDITIONS_URL)
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Privacy Policy",
                textDecoration = TextDecoration.Underline,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                modifier = Modifier.clickable {
                    // Add your privacy policy URL here
                    // uriHandler.openUri(Constants.PRIVACY_POLICY_URL)
                }
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Version: ${versionName} #${versionCode}",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
            )
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, color: Color = Color.Black, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier
                .width(24.dp)
                .background(Color(0xFFE0E0E0), CircleShape)
                .padding(4.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}