package com.sogo.golf.msl.features.sogo_home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.R

@Composable
fun SogoTopicButton(
    onClick: () -> Unit,
    imageResId: Int? = null,
    icon: ImageVector? = null,
    title: String = "ABOUT SOGO GOLF & PRIZES",
    subTitle: String? = null
) {
    Surface(
        modifier = Modifier
            .width(180.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.65f),
        onClick = onClick,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            if (imageResId != null) {
                Icon(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(24.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                textAlign = TextAlign.Center
            )

            if (!subTitle.isNullOrEmpty()) {
                Text(
                    text = subTitle,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutButtonPreview() {
    SogoTopicButton(
        onClick = { },
        subTitle = ""
    )
}

@Preview(showBackground = true)
@Composable
fun AboutButtonPreview2() {
    SogoTopicButton(
        title = "TOKENS & ACCOUNT",
        subTitle = "",
        onClick = { },
        imageResId = R.drawable.simple_golf_transparent
    )
}
