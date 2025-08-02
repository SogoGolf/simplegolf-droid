package com.sogo.golf.msl.shared_components.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PostRoundCard(
    playerName: String,
    competitionType: String,
    dailyHandicap: String,
    frontNineScore: String,
    backNineScore: String,
    grandTotal: String,
    signatureBase64: String?,
    onSignatureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = playerName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = competitionType,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Daily Handicap: $dailyHandicap",
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreColumn("Front 9", frontNineScore)
                ScoreColumn("Back 9", backNineScore)
                ScoreColumn("Grand Total", grandTotal)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Signature",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            SignatureBox(
                signatureBase64 = signatureBase64,
                onSignatureClick = onSignatureClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
private fun ScoreColumn(
    label: String,
    score: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SignatureBox(
    signatureBase64: String?,
    onSignatureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onSignatureClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (signatureBase64 != null && signatureBase64.isNotEmpty()) {
            val bitmap = decodeBase64ToBitmap(signatureBase64)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Signature",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SignaturePlaceholder()
            }
        } else {
            SignaturePlaceholder()
        }
    }
}

@Composable
private fun SignaturePlaceholder() {
    Text(
        text = "Tap to sign",
        fontSize = 14.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center
    )
}

private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    return try {
        val cleanBase64 = base64String.replace("data:image/png;base64,", "")
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun PostRoundCardPreview() {
    PostRoundCard(
        playerName = "John Doe",
        competitionType = "Stroke Play",
        dailyHandicap = "12.5",
        frontNineScore = "42",
        backNineScore = "38",
        grandTotal = "80",
        signatureBase64 = null,
        onSignatureClick = { }
    )
}
