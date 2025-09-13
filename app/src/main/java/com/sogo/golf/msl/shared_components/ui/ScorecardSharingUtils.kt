package com.sogo.golf.msl.shared_components.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.msl.MslCompetition
import com.sogo.golf.msl.ui.theme.MSLColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ScorecardSharingUtils {
    
    @Deprecated("Use generateVerticalScorecardBitmap instead")
    suspend fun captureComposableAsBitmap(
        context: Context,
        width: Int = 1080,
        height: Int = 1350
    ): Bitmap = withContext(Dispatchers.Main) {
        throw UnsupportedOperationException("Use generateVerticalScorecardBitmap instead")
    }
    
    suspend fun generateVerticalScorecardBitmap(
        context: Context,
        round: Round,
        mslCompetition: MslCompetition?,
        selectedPlayer: PlayerType = PlayerType.GOLFER,
        isNineHoles: Boolean,
        width: Int = 1080,
        height: Int = if (isNineHoles) 1200 else 1600
    ): Bitmap = withContext(Dispatchers.Main) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val playerHoleScores = when (selectedPlayer) {
            PlayerType.GOLFER -> round.holeScores
            PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.holeScores ?: emptyList()
        }
        
        val playerFirstName = when (selectedPlayer) {
            PlayerType.GOLFER -> round.golferFirstName ?: ""
            PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.golferFirstName ?: ""
        }
        
        val playerLastName = when (selectedPlayer) {
            PlayerType.GOLFER -> round.golferLastName ?: ""
            PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.golferLastName ?: ""
        }
        
        val dailyHandicap = when (selectedPlayer) {
            PlayerType.GOLFER -> round.dailyHandicap?.toString() ?: "--"
            PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.dailyHandicap?.toString() ?: "--"
        }
        
        val teeName = getTeeName(mslCompetition, when (selectedPlayer) {
            PlayerType.GOLFER -> round.golferGLNumber
            PlayerType.PLAYING_PARTNER -> round.playingPartnerRound?.golferGLNumber
        })
        
        var yPos = 60f
        val margin = 50f
        val cellHeight = 60f
        val headerHeight = 120f
        
        // Header
        paint.apply {
            color = Color(MSLColors.mslGunMetal.value).toArgb() // mslGunMetal
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
        }
        val headerRect = Rect(margin.toInt(), yPos.toInt(), (width - margin).toInt(), (yPos + headerHeight).toInt())
        canvas.drawRect(headerRect, paint)
        
        paint.color = android.graphics.Color.WHITE
        canvas.drawText("$playerFirstName $playerLastName", width / 2f, yPos + 40f, paint)
        
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(round.clubName ?: "Golf Course", width / 2f, yPos + 80f, paint)
        
        paint.textSize = 28f
        canvas.drawText("Daily HC: $dailyHandicap    Tee: $teeName", width / 2f, yPos + 110f, paint)
        
        yPos += headerHeight + 20f
        
        // Column headers
        paint.apply {
            color = android.graphics.Color.BLACK
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
        }
        
        val colWidth = (width - 2 * margin) / 6f
        val headers = listOf("Hole", "Meters", "Index", "Par", "Strokes", "Score")
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, margin + colWidth * (index + 0.5f), yPos + 30f, paint)
        }
        yPos += cellHeight
        
        // Draw holes
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 24f
        
        // Front 9
        playerHoleScores.filter { it.holeNumber in 1..9 }.forEach { holeScore ->
            drawHoleRow(canvas, paint, holeScore, margin, colWidth, yPos)
            yPos += cellHeight
        }
        
        // OUT summary
        val frontNine = playerHoleScores.filter { it.holeNumber in 1..9 }
        drawSummaryRow(canvas, paint, "OUT", frontNine, margin, colWidth, yPos, true)
        yPos += cellHeight + 20f
        
        // Back 9 (if 18-hole)
        if (!isNineHoles) {
            playerHoleScores.filter { it.holeNumber in 10..18 }.forEach { holeScore ->
                drawHoleRow(canvas, paint, holeScore, margin, colWidth, yPos)
                yPos += cellHeight
            }
            
            // IN summary
            val backNine = playerHoleScores.filter { it.holeNumber in 10..18 }
            drawSummaryRow(canvas, paint, "IN", backNine, margin, colWidth, yPos, true)
            yPos += cellHeight + 20f
        }
        
        // TOTAL summary
        drawSummaryRow(canvas, paint, "TOTAL", playerHoleScores, margin, colWidth, yPos, true, isTotal = true)
        
        bitmap
    }
    
    private fun drawHoleRow(canvas: Canvas, paint: Paint, holeScore: HoleScore, margin: Float, colWidth: Float, yPos: Float) {
        val values = listOf(
            holeScore.holeNumber.toString(),
            holeScore.meters.toString(),
            formatIndex(holeScore.index1, holeScore.index2),
            holeScore.par.toString(),
            holeScore.strokes.toString(),
            holeScore.score.toInt().toString()
        )
        
        values.forEachIndexed { index, value ->
            canvas.drawText(value, margin + colWidth * (index + 0.5f), yPos + 35f, paint)
        }
    }
    
    private fun drawSummaryRow(canvas: Canvas, paint: Paint, label: String, holeScores: List<HoleScore>, 
                              margin: Float, colWidth: Float, yPos: Float, isHighlighted: Boolean, isTotal: Boolean = false) {
        val originalColor = paint.color
        val originalTypeface = paint.typeface
        
        if (isHighlighted) {
            val bgColor = if (isTotal) Color(MSLColors.mslGunMetal.value).toArgb() else Color(0xFFE0E0E0).toArgb()
            val bgRect = Rect(margin.toInt(), yPos.toInt(), (margin + colWidth * 6).toInt(), (yPos + 60).toInt())
            val bgPaint = Paint().apply { color = bgColor }
            canvas.drawRect(bgRect, bgPaint)
            
            paint.color = if (isTotal) android.graphics.Color.WHITE else android.graphics.Color.BLACK
            paint.typeface = Typeface.DEFAULT_BOLD
        }
        
        val values = listOf(
            label,
            holeScores.sumOf { it.meters }.toString(),
            "--",
            holeScores.sumOf { it.par }.toString(),
            holeScores.sumOf { it.strokes }.toString(),
            holeScores.sumOf { it.score.toInt() }.toString()
        )
        
        values.forEachIndexed { index, value ->
            canvas.drawText(value, margin + colWidth * (index + 0.5f), yPos + 35f, paint)
        }
        
        paint.color = originalColor
        paint.typeface = originalTypeface
    }
    
    private fun formatIndex(index1: Int, index2: Int): String {
        return if (index2 > 0) "$index1/$index2" else index1.toString()
    }
    
    private fun getTeeName(mslCompetition: MslCompetition?, golfLinkNumber: String?): String {
        return mslCompetition?.players?.find { 
            it.golfLinkNumber == golfLinkNumber 
        }?.teeName ?: "--"
    }
    
    fun optimizeBitmapForSharing(bitmap: Bitmap, maxWidth: Int = 1080, quality: Int = 85): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth) {
            return bitmap
        }
        
        val scale = maxWidth.toFloat() / width
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
