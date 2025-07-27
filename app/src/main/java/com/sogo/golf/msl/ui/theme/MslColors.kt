package com.sogo.golf.msl.ui.theme

import androidx.compose.ui.graphics.Color


object MSLColors {
    // Primary Brand Colors
    val Primary = Color(0xFF054868)        // MSL Blue (from your colors.xml)
    val PrimaryLight = Color(0xFF2171A3)   // Lighter blue for accessibility
    val PrimaryDark = Color(0xFF032F45)    // Darker blue for contrast

    // Secondary Colors
    val Secondary = Color(0xFF34A853)      // Golf Green
    val SecondaryLight = Color(0xFF81C784) // Light green for success states
    val SecondaryDark = Color(0xFF1B5E20)  // Dark green for emphasis

    val mslBlue = Color(0xFF054868)
    val mslGrey = Color(0xFF808284)
    val mslGunMetal = Color(0xFF221F1F)
    val mslYellow = Color(0xFFF2C31A)
    val mslBlack = Color(0xFF000000)
    val mslWhite = Color(0xFFFFFFFF)
    val mslGreen = Color(0xFF25b862)
    val mslRed = Color(0xFFe34836)


    // Golf-Specific Colors
    val Fairway = Color(0xFF4CAF50)        // Fairway green
    val Rough = Color(0xFF689F38)          // Rough green
    val Sand = Color(0xFFD4AF37)           // Sand trap
    val Water = Color(0xFF2196F3)          // Water hazard
    val Pin = Color(0xFFFF5722)            // Pin/flag red

    // Score Colors (high contrast for accessibility)
    val Eagle = Color(0xFF4CAF50)          // Green for great scores
    val Birdie = Color(0xFF8BC34A)         // Light green for good scores
    val Par = Color(0xFF9E9E9E)            // Neutral gray for par
    val Bogey = Color(0xFFFF9800)          // Orange for over par
    val DoublePlus = Color(0xFFE53935)     // Red for bad scores

    // UI State Colors (WCAG AA compliant)
    val Success = Color(0xFF2E7D32)        // Dark green for success
    val Warning = Color(0xFFF57C00)        // Orange for warnings
    val Error = Color(0xFFD32F2F)          // Red for errors
    val Info = Color(0xFF1976D2)           // Blue for info

    // Background Colors
    val BackgroundPrimary = Color(0xFFFAFAFA)    // Off-white
    val BackgroundSecondary = Color(0xFFF5F5F5)  // Light gray
    val Surface = Color(0xFFFFFFFF)              // Pure white
    val SurfaceElevated = Color(0xFFFFFFFF)      // Cards, etc.

    // Text Colors (WCAG AA compliant - 4.5:1 contrast minimum)
    val TextPrimary = Color(0xFF212121)          // Dark gray (87% opacity)
    val TextSecondary = Color(0xFF757575)        // Medium gray (60% opacity)
    val TextTertiary = Color(0xFF9E9E9E)         // Light gray (38% opacity)
    val TextOnPrimary = Color.White              // White text on primary
    val TextOnSecondary = Color.White            // White text on secondary

    // Border & Divider Colors
    val Border = Color(0xFFE0E0E0)               // Light border
    val BorderFocus = Primary                    // Primary color for focused borders
    val Divider = Color(0xFFBDBDBD)              // Dividers

    // Accessibility Colors (high contrast variants)
    object HighContrast {
        val TextPrimary = Color(0xFF000000)      // Pure black for high contrast
        val TextSecondary = Color(0xFF424242)    // Dark gray for high contrast
        val Border = Color(0xFF424242)           // Darker border for visibility
        val Background = Color(0xFFFFFFFF)       // Pure white background
    }
}

// Extension for semantic color usage
object MSLSemanticColors {
    val handicapExcellent = MSLColors.Eagle      // Single digit handicaps
    val handicapGood = MSLColors.Birdie          // 10-18 handicaps
    val handicapAverage = MSLColors.Par          // 19-28 handicaps
    val handicapHigh = MSLColors.Bogey           // 29+ handicaps

    val scoreBelowPar = MSLColors.Eagle
    val scoreAtPar = MSLColors.Par
    val scoreAbovePar = MSLColors.Bogey
    val scoreWayAbovePar = MSLColors.DoublePlus
}