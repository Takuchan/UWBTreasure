package com.takuchan.uwbviaserial.ui.theme

import androidx.compose.ui.graphics.Color

// Light Colors
data object LightColors {
    val Primary = Color(0xFFFF6B35) // オレンジ
    val Secondary = Color(0xFF4ECDC4) // ターコイズ
    val Accent = Color(0xFFFFE66D) // イエロー
    val Warning = Color(0xFFFF8A80) // レッド
    val Background = Color(0xFFF7F9FC) // ライトグレー
    val Surface = Color.White
    val onBackGround = Color(0xFF1C1B1F)



}
data object ComponentsColor{
    val Success = Color(0xFF95E1A3) // グリーン

    val Primary = Color(0xFFFF6B35) // オレンジ
    val Anchor0 = Color(0xFFFF6B6B) // 明るい赤
    val Anchor1 = Color(0xFF4ECDC4) // ターコイズ
    val Anchor2 = Color(0xFF45B7D1) // 青
    val Tag = Color(0xFFFFD93D) // 明るい黄色
    val HiddenTag = Color(0xFFE74C3C) // 隠されたUWB
}

// Dark Colors
data object DarkColors {
    val Primary = Color(0xFF8E6BFF) // より明るいパープル
    val Secondary = Color(0xFF6BFFF2) // 明るいターコイズ
    val Accent = Color(0xFFFFF176) // ソフトイエロー
    val Warning = Color(0xFFFF9E80) // ソフトレッド
    val Background = Color(0xFF121212) // ダークグレー（Material Design標準）
    val Surface = Color(0xFF1E1E1E) // ダークサーフェス
}