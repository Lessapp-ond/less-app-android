package com.lessapp.less.ui.theme

import androidx.compose.ui.graphics.Color

data class AppColors(
    val isDark: Boolean
) {
    // Backgrounds
    val background: Color get() = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF5F5F5)
    val cardBackground: Color get() = if (isDark) Color(0xFF2C2C2E) else Color.White
    val headerBackground: Color get() = if (isDark) Color(0xFF1C1C1E) else Color.White

    // Text
    val textPrimary: Color get() = if (isDark) Color.White else Color.Black
    val textSecondary: Color get() = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.55f)
    val textTertiary: Color get() = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f)

    // UI Elements
    val buttonBackground: Color get() = if (isDark) Color(0xFF3A3A3C) else Color.White
    val buttonBackgroundActive: Color get() = if (isDark) Color.White else Color.Black
    val buttonText: Color get() = if (isDark) Color.White else Color.Black
    val buttonTextActive: Color get() = if (isDark) Color.Black else Color.White
    val border: Color get() = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)

    // Card specific
    val cardShadow: Color get() = if (isDark) Color.Transparent else Color.Black.copy(alpha = 0.08f)

    companion object {
        fun forDarkMode(isDark: Boolean) = AppColors(isDark)
    }
}
