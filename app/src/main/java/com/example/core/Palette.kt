package com.example.core

import androidx.compose.ui.graphics.Color

/**
 * Master 32-Color Locked Palette for Desi Turbo Rush.
 * No color outside this 32-color specification reaches the screen.
 */
object Palette {
    // Neutrals / Ink
    val voidBlack = Color(0xFF0D0B1F)
    val ink       = Color(0xFF1C1A2E)
    val shadow    = Color(0xFF2E2B44)
    val slate     = Color(0xFF4A4460)
    val stone     = Color(0xFF6E6785)
    val ash       = Color(0xFF9A93AD)
    val bone      = Color(0xFFC8C2D4)
    val paper     = Color(0xFFF4F0E6)

    // Skin (South Asian range)
    val skinDeep  = Color(0xFF7A4A2B)
    val skinMid   = Color(0xFFA8683C)
    val skinLight = Color(0xFFC98B58)
    val skinPale  = Color(0xFFE0AB7A)

    // Reds / Oranges — Rickshaw paint, hood, brake lights
    val maroon    = Color(0xFF7A1327)
    val red       = Color(0xFFC02040)
    val coral     = Color(0xFFE84C3D)
    val orange    = Color(0xFFF5793A)
    val amber     = Color(0xFFFFB03B)

    // Golds — Taka, brass bell, headlamp
    val gold      = Color(0xFFFFD83D)
    val cream     = Color(0xFFF9F27A)

    // Greens — flag green, foliage, lungi
    val forest    = Color(0xFF0B4A34)
    val green     = Color(0xFF106B45)
    val leaf      = Color(0xFF2FA15A)
    val lime      = Color(0xFF7FD66F)

    // Blues / Cyans — sky, night, electric
    val navy      = Color(0xFF123A63)
    val blue      = Color(0xFF1F6FB2)
    val cyan      = Color(0xFF3FBFE8)
    val electric  = Color(0xFF8FF2FF)

    // Purples / Pinks — rickshaw folk art, sunset, neon
    val plum      = Color(0xFF4B1D6B)
    val violet    = Color(0xFF8C2FA8)
    val magenta   = Color(0xFFD94FA0)
    val blush     = Color(0xFFFF8FC9)

    // Earth — road, mud, wood
    val soil      = Color(0xFF3A2418)
    val clay      = Color(0xFF6B4A2A)

    // Array of all 32 colors for palette lookup / index dithering
    val allColors = arrayOf(
        voidBlack, ink, shadow, slate, stone, ash, bone, paper,
        skinDeep, skinMid, skinLight, skinPale,
        maroon, red, coral, orange, amber, gold, cream,
        forest, green, leaf, lime,
        navy, blue, cyan, electric,
        plum, violet, magenta, blush,
        soil, clay
    )

    // Bayer 2x2 Dither Matrix helper
    fun isDitherPixel(x: Int, y: Int, density: Float): Boolean {
        val threshold = BAYER_2X2[y % 2][x % 2]
        return density > threshold
    }

    private val BAYER_2X2 = arrayOf(
        floatArrayOf(0.2f, 0.6f),
        floatArrayOf(0.8f, 0.4f)
    )
}
