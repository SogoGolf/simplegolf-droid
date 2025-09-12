package com.sogo.golf.msl.shared_components.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ScorecardSharingUtils {
    
    suspend fun captureComposableAsBitmap(
        context: Context,
        width: Int = 1080,
        height: Int = 1350,
        content: @Composable () -> Unit
    ): Bitmap = withContext(Dispatchers.Main) {
        val composeView = ComposeView(context).apply {
            setContent {
                com.sogo.golf.msl.ui.theme.MSLGolfTheme {
                    content()
                }
            }
        }
        
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        
        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, width, height)
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        composeView.draw(canvas)
        
        bitmap
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
