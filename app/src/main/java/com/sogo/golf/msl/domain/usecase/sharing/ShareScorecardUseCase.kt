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
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class ShareScorecardUseCase @Inject constructor() {
    
    suspend operator fun invoke(
        context: Context,
        scorecardBitmap: Bitmap,
        round: Round,
        mslCompetition: MslCompetition? = null
    ): Intent {
        val shareText = buildShareText(round, mslCompetition)
        val imageUri = saveBitmapToCache(context, scorecardBitmap)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(
                context.contentResolver,
                "scorecard",
                imageUri
            )
        }
        
        // Grant read URI permission to all potential receivers
        val pm = context.packageManager
        val resInfoList = pm.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        resInfoList.forEach { resolveInfo ->
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName,
                imageUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        
        return intent
    }
    
    private fun buildShareText(round: Round, mslCompetition: MslCompetition?): String {
        val clubName = round.clubName ?: "Golf Course"
        
        val dateString = round.roundDate?.let { date ->
            date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        } ?: "today"
        
        val compType = round.compType ?: mslCompetition?.players?.firstOrNull()?.competitionType ?: "Social"
        
        val numberOfHoles = round.holeScores.distinctBy { it.holeNumber }.size
        
        return "Here's my round at $clubName on $dateString. $compType, $numberOfHoles holes."
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
