package com.sogo.golf.msl.shared_components.ui

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sogo.golf.msl.ui.theme.MSLColors
import com.sogo.golf.msl.ui.theme.MSLColors.mslWhite
import com.sogo.golf.msl.ui.theme.MSLGolfTheme
import se.warting.signaturepad.SignaturePadAdapter
import se.warting.signaturepad.SignaturePadView
import java.io.ByteArrayOutputStream

@Composable
fun SignatureDialog(
    firstName: String?,
    lastName: String?,
    onDismiss: () -> Unit,
    onSignatureCaptured: (String) -> Unit
) {
    var signatureBase64 by remember { mutableStateOf<String?>(null) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val signatureBoxWidth = screenWidth - 16.dp

    var signaturePadAdapter: SignaturePadAdapter? = null

    val dialogBackgroundColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5) {
        Color.White
    } else {
        MSLColors.mslGunMetal
    }

    val textColor = if (MaterialTheme.colorScheme.background.luminance() > 0.5) {
        mslWhite
    } else {
        Color.White
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .width(signatureBoxWidth)
                .height(300.dp)
                .padding(0.dp),
            shape = RoundedCornerShape(8.dp),
            color = dialogBackgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Please sign below (${firstName} ${lastName}):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.LightGray)
                        .border(2.dp, MSLColors.mslBlack)
                ) {
                    SignaturePadView(onReady = {
                        signaturePadAdapter = it
                    })
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            signaturePadAdapter?.clear()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MSLColors.mslRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Clear")
                    }
                    Button(
                        onClick = {
                            signaturePadAdapter?.getSignatureBitmap()?.let { bitmap ->
                                signatureBase64 = bitmapToBase64(bitmap)

                                Log.d("signaturePadAdapter", signatureBase64 ?: "null")

                                onSignatureCaptured(signatureBase64!!)

                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MSLColors.mslGreen,
                            contentColor = MSLColors.mslWhite
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

private fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

@Composable
@Preview(showBackground = true)
fun PreviewSignatureDialogLight() {
    MSLGolfTheme(darkTheme = false) {
        SignatureDialog(
            firstName = "John",
            lastName = "Doe",
            onDismiss = { },
            onSignatureCaptured = { }
        )
    }
}

@Composable
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
fun PreviewSignatureDialogDark() {
    MSLGolfTheme(darkTheme = true) {
        SignatureDialog(
            firstName = "John",
            lastName = "Doe",
            onDismiss = { },
            onSignatureCaptured = { }
        )
    }
}
