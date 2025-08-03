package com.sogo.golf.msl.features.sogo_home.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue
import com.sogo.golf.msl.ui.theme.MSLColors.mslWhite

@Composable
fun SogoInfoSheet(onItemClicked: ((String) -> Unit)? = null) {
    val context = LocalContext.current

    fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .background(mslBlue.copy(alpha = 1f))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            "SOGO Information",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            color = mslWhite
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButtonItem("SOGO Terms and Conditions", "http://sogo.golf/conditions-of-entry") { url -> 
            onItemClicked?.invoke("terms")
            openWebPage(url) 
        }
        TextButtonItem("SOGO Privacy", "http://sogo.golf/privacy-policy") { url -> 
            onItemClicked?.invoke("privacy")
            openWebPage(url) 
        }
        TextButtonItem("SOGO Rules of Play", "http://sogo.golf/rules-of-play") { url -> 
            onItemClicked?.invoke("rules")
            openWebPage(url) 
        }
        TextButtonItem("SOGO Conditions of Entry", "http://sogo.golf/conditions-of-entry") { url -> 
            onItemClicked?.invoke("conditions")
            openWebPage(url) 
        }
    }
}


@Composable
fun TextButtonItem(text: String, url: String, onClick: (String) -> Unit) {
    TextButton(
        onClick = { onClick(url) },
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = mslWhite,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            textDecoration = TextDecoration.Underline
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SogoInfoSheetPreview() {
    SogoInfoSheet()
}
