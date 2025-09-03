package com.sogo.golf.msl.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Provides a normalized density that prevents screen zoom from affecting UI layouts
 * while preserving legitimate device density differences.
 */
@Composable
fun rememberNormalizedDensity(): Density {
    val systemDensity = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    // Get the system density value
    val systemDensityValue = systemDensity.density
    
    // Android's standard density buckets (LDPI=0.75, MDPI=1.0, HDPI=1.5, XHDPI=2.0, XXHDPI=3.0, XXXHDPI=4.0)
    // Screen zoom typically creates non-standard density values
    val standardDensities = listOf(0.75f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f)
    
    // Find the closest standard density
    val closestStandardDensity = standardDensities.minByOrNull { 
        kotlin.math.abs(it - systemDensityValue) 
    } ?: 1.0f
    
    // If the system density is very close to a standard density (within 10%), use the standard density
    // This helps normalize zoom-induced density changes while preserving device characteristics
    val normalizedDensityValue = if (kotlin.math.abs(systemDensityValue - closestStandardDensity) / closestStandardDensity < 0.1f) {
        closestStandardDensity
    } else {
        // For non-standard densities that might be zoom-induced, clamp to nearest standard
        closestStandardDensity
    }
    
    return Density(
        density = normalizedDensityValue,
        fontScale = 1.0f // Always use 1.0f font scale as per app requirements
    )
}
