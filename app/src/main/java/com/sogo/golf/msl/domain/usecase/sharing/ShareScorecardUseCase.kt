package com.sogo.golf.msl.domain.usecase.sharing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ShareScorecardUseCase @Inject constructor() {
    
    suspend operator fun invoke(
        context: Context,
        scorecardBitmap: Bitmap,
        round: Round,
        playerName: String
    ): Intent {
        val shareText = buildShareText(round, playerName)
        val imageUri = saveBitmapToCache(context, scorecardBitmap)
        
        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun buildShareText(round: Round, playerName: String): String {
        val courseName = round.clubName ?: "Golf Course"
        val totalScore = round.holeScores.sumOf { it.score.toInt() }
        val totalPar = round.holeScores.sumOf { it.par }
        val scoreToPar = totalScore - totalPar
        val scoreToParString = when {
            scoreToPar > 0 -> "+$scoreToPar"
            scoreToPar < 0 -> "$scoreToPar"
            else -> "E"
        }
        
        return "Check out $playerName's scorecard from $courseName! Score: $totalScore ($scoreToParString) #Golf #Scorecard"
    }
    
    private suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val cacheDir = File(context.cacheDir, "shared_scorecards")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val file = File(cacheDir, "scorecard_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
