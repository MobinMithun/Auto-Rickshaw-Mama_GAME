package com.example.sprites

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.example.core.Palette

/**
 * Procedural Pixel Art Generator and Atlas Cache.
 * Produces crisp, 100% pixel-perfect retro 16-bit graphics strictly locked to Palette.kt.
 */
object PixelSpriteRenderer {

    private val bitmapCache = HashMap<String, Bitmap>()
    private val paint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    // --- SPRITE BITMAP GENERATOR UTILITIES ---

    fun createPixelBitmap(width: Int, height: Int, drawBlock: (Canvas) -> Unit): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawBlock(canvas)
        return bitmap
    }

    private fun drawPixel(canvas: Canvas, x: Int, y: Int, colorArgb: Int) {
        paint.color = colorArgb
        canvas.drawRect(x.toFloat(), y.toFloat(), (x + 1).toFloat(), (y + 1).toFloat(), paint)
    }

    private fun drawPixelRect(canvas: Canvas, left: Int, top: Int, width: Int, height: Int, colorArgb: Int) {
        paint.color = colorArgb
        canvas.drawRect(left.toFloat(), top.toFloat(), (left + width).toFloat(), (top + height).toFloat(), paint)
    }

    private fun drawPixelOutline(canvas: Canvas, left: Int, top: Int, width: Int, height: Int, outlineColorArgb: Int) {
        paint.color = outlineColorArgb
        canvas.drawRect(left.toFloat(), top.toFloat(), (left + width).toFloat(), (top + 1).toFloat(), paint)
        canvas.drawRect(left.toFloat(), (top + height - 1).toFloat(), (left + width).toFloat(), (top + height).toFloat(), paint)
        canvas.drawRect(left.toFloat(), top.toFloat(), (left + 1).toFloat(), (top + height).toFloat(), paint)
        canvas.drawRect((left + width - 1).toFloat(), top.toFloat(), (left + width).toFloat(), (top + height).toFloat(), paint)
    }

    // --- GET OR CREATE CACHED BITMAP ---

    fun getSprite(key: String, width: Int, height: Int, generator: (Canvas) -> Unit): Bitmap {
        return bitmapCache.getOrPut(key) {
            createPixelBitmap(width, height, generator)
        }
    }

    // --- RICKSHAW SPRITES (32x40) ---

    fun getRickshawFrame(frameIndex: Int, isTurbo: Boolean = false, leanDir: Int = 0): Bitmap {
        val key = "rickshaw_f${frameIndex}_t${isTurbo}_l$leanDir"
        return getSprite(key, 32, 40) { canvas ->
            val ink = Palette.ink.toArgb()
            val maroon = Palette.maroon.toArgb()
            val red = Palette.red.toArgb()
            val coral = Palette.coral.toArgb()
            val orange = Palette.orange.toArgb()
            val amber = Palette.amber.toArgb()
            val gold = Palette.gold.toArgb()
            val cream = Palette.cream.toArgb()
            val green = Palette.green.toArgb()
            val leaf = Palette.leaf.toArgb()
            val skin = Palette.skinMid.toArgb()
            val shadow = Palette.shadow.toArgb()
            val bone = Palette.bone.toArgb()

            val leanOffset = when (leanDir) {
                -1 -> -2
                1 -> 2
                else -> 0
            }
            val bounce = if (frameIndex % 2 == 1) 1 else 0

            // 1. Shadow beneath rickshaw
            drawPixelRect(canvas, 4 + leanOffset, 34, 24, 5, shadow)

            // 2. Main Wheels (Left & Right rear wheels)
            val wheelY = 26 + bounce
            // Left wheel
            drawPixelRect(canvas, 3 + leanOffset, wheelY, 4, 10, ink)
            drawPixelRect(canvas, 4 + leanOffset, wheelY + 2, 2, 6, bone)
            // Right wheel
            drawPixelRect(canvas, 25 + leanOffset, wheelY, 4, 10, ink)
            drawPixelRect(canvas, 26 + leanOffset, wheelY + 2, 2, 6, bone)

            // 3. Front Wheel (Center top)
            drawPixelRect(canvas, 14 + leanOffset, 4, 4, 8, ink)
            drawPixelRect(canvas, 15 + leanOffset, 6, 2, 4, bone)

            // 4. Passenger Hood / Canopy (Red/Maroon with Art motifs)
            val hoodY = 12 + bounce
            drawPixelRect(canvas, 6 + leanOffset, hoodY, 20, 16, red)
            drawPixelOutline(canvas, 6 + leanOffset, hoodY, 20, 16, ink)
            // Hood roof highlights & folk art stripes
            drawPixelRect(canvas, 8 + leanOffset, hoodY + 2, 16, 2, coral)
            drawPixelRect(canvas, 10 + leanOffset, hoodY + 5, 12, 3, amber)
            drawPixelRect(canvas, 12 + leanOffset, hoodY + 9, 8, 2, green)

            // 5. Driver Seat & Mama (Driver)
            // Mama's Head (16x16 overlay)
            val headX = 8 + leanOffset
            val headY = 2 + bounce
            // Cap / Gamcha (Red / Green)
            drawPixelRect(canvas, headX + 4, headY + 1, 8, 3, green)
            // Skin
            drawPixelRect(canvas, headX + 4, headY + 4, 8, 5, skin)
            // Moustache
            drawPixelRect(canvas, headX + 4 + (frameIndex % 2), headY + 7, 8, 2, ink)
            // Eyes
            drawPixel(canvas, headX + 5, headY + 5, ink)
            drawPixel(canvas, headX + 10, headY + 5, ink)
            // Lungi / Shirt
            drawPixelRect(canvas, headX + 3, headY + 9, 10, 5, leaf)

            // 6. Rickshaw Brass Lamp & Handlebars
            drawPixelRect(canvas, 14 + leanOffset, 2, 4, 3, if (frameIndex % 2 == 0) gold else cream)

            // 7. Turbo Flames / Sparks overlay
            if (isTurbo) {
                val flameY = 32
                val flameColor1 = if (frameIndex % 2 == 0) amber else orange
                val flameColor2 = if (frameIndex % 2 == 0) cream else gold
                drawPixelRect(canvas, 8 + leanOffset, flameY, 4, 6, flameColor1)
                drawPixelRect(canvas, 20 + leanOffset, flameY, 4, 6, flameColor2)
                drawPixelRect(canvas, 14 + leanOffset, flameY + 2, 4, 6, flameColor1)
            }
        }
    }

    // --- OBSTACLES ---

    fun getObstacleSprite(type: String, frame: Int = 0): Bitmap {
        val key = "obs_${type}_$frame"
        return getSprite(key, 16, 16) { canvas ->
            val ink = Palette.ink.toArgb()
            val ash = Palette.ash.toArgb()
            val stone = Palette.stone.toArgb()
            val clay = Palette.clay.toArgb()
            val red = Palette.red.toArgb()
            val amber = Palette.amber.toArgb()
            val voidB = Palette.voidBlack.toArgb()

            when (type) {
                "manhole" -> {
                    drawPixelRect(canvas, 2, 4, 12, 8, stone)
                    drawPixelOutline(canvas, 2, 4, 12, 8, ink)
                    drawPixelRect(canvas, 5, 7, 6, 2, voidB)
                }
                "garbage" -> {
                    drawPixelRect(canvas, 1, 3, 14, 10, clay)
                    drawPixelOutline(canvas, 1, 3, 14, 10, ink)
                    drawPixelRect(canvas, 3, 5, 4, 4, ash)
                    drawPixelRect(canvas, 9, 6, 3, 3, Palette.forest.toArgb())
                }
                "stone" -> {
                    drawPixelRect(canvas, 3, 4, 10, 9, ash)
                    drawPixelOutline(canvas, 3, 4, 10, 9, ink)
                    drawPixelRect(canvas, 5, 6, 4, 3, Palette.bone.toArgb())
                }
                "barrier" -> {
                    drawPixelRect(canvas, 1, 2, 14, 12, red)
                    drawPixelOutline(canvas, 1, 2, 14, 12, ink)
                    drawPixelRect(canvas, 3, 4, 10, 3, Palette.paper.toArgb())
                    drawPixelRect(canvas, 3, 9, 10, 3, Palette.paper.toArgb())
                }
                "puddle" -> {
                    drawPixelRect(canvas, 2, 5, 12, 7, Palette.blue.toArgb())
                    drawPixelOutline(canvas, 2, 5, 12, 7, Palette.navy.toArgb())
                    drawPixelRect(canvas, 4, 7, 4, 2, Palette.cyan.toArgb())
                }
                else -> { // Default
                    drawPixelRect(canvas, 2, 2, 12, 12, red)
                    drawPixelOutline(canvas, 2, 2, 12, 12, ink)
                }
            }
        }
    }

    fun getCowSprite(frame: Int): Bitmap {
        return getSprite("cow_$frame", 32, 24) { canvas ->
            val ink = Palette.ink.toArgb()
            val white = Palette.paper.toArgb()
            val black = Palette.shadow.toArgb()
            val pink = Palette.blush.toArgb()
            val bounce = if (frame % 2 == 1) 1 else 0

            // Body
            drawPixelRect(canvas, 4, 6 + bounce, 22, 12, white)
            drawPixelOutline(canvas, 4, 6 + bounce, 22, 12, ink)

            // Cow patches
            drawPixelRect(canvas, 8, 8 + bounce, 6, 6, black)
            drawPixelRect(canvas, 18, 10 + bounce, 5, 5, black)

            // Head
            drawPixelRect(canvas, 22, 2 + bounce, 8, 10, white)
            drawPixelOutline(canvas, 22, 2 + bounce, 8, 10, ink)
            drawPixelRect(canvas, 28, 6 + bounce, 3, 4, pink) // Snout

            // Legs
            val leg1X = if (frame == 0 || frame == 2) 6 else 4
            val leg2X = if (frame == 0 || frame == 2) 20 else 22
            drawPixelRect(canvas, leg1X, 18 + bounce, 4, 5, ink)
            drawPixelRect(canvas, leg2X, 18 + bounce, 4, 5, ink)
        }
    }

    // --- TAKA COIN (8x8) ---

    fun getCoinSprite(frame: Int): Bitmap {
        return getSprite("coin_$frame", 8, 8) { canvas ->
            val ink = Palette.ink.toArgb()
            val gold = Palette.gold.toArgb()
            val cream = Palette.cream.toArgb()

            val width = when (frame % 4) {
                0 -> 8
                1 -> 6
                2 -> 4
                else -> 6
            }
            val left = (8 - width) / 2

            drawPixelRect(canvas, left, 1, width, 6, gold)
            drawPixelOutline(canvas, left, 1, width, 6, ink)
            if (width >= 6) {
                drawPixelRect(canvas, left + 2, 3, 2, 2, cream) // ৳ mark glow
            }
        }
    }

    // --- POWERUP SPRITES (16x16) ---

    fun getPowerUpSprite(type: String, frame: Int): Bitmap {
        return getSprite("pup_${type}_$frame", 16, 16) { canvas ->
            val ink = Palette.ink.toArgb()
            val gold = Palette.gold.toArgb()
            val cyan = Palette.cyan.toArgb()
            val red = Palette.red.toArgb()
            val lime = Palette.lime.toArgb()

            drawPixelRect(canvas, 1, 1, 14, 14, Palette.slate.toArgb())
            drawPixelOutline(canvas, 1, 1, 14, 14, ink)

            when (type) {
                "turbo" -> {
                    // Lightning bolt
                    val c = if (frame % 2 == 0) gold else Palette.cream.toArgb()
                    drawPixelRect(canvas, 8, 3, 4, 4, c)
                    drawPixelRect(canvas, 6, 6, 6, 4, c)
                    drawPixelRect(canvas, 4, 9, 4, 4, c)
                }
                "magnet" -> {
                    // Magnet U shape
                    drawPixelRect(canvas, 4, 4, 3, 8, red)
                    drawPixelRect(canvas, 9, 4, 3, 8, red)
                    drawPixelRect(canvas, 4, 10, 8, 3, red)
                    drawPixelRect(canvas, 4, 4, 3, 3, Palette.paper.toArgb())
                    drawPixelRect(canvas, 9, 4, 3, 3, Palette.paper.toArgb())
                }
                "shield" -> {
                    // Shield icon
                    val c = if (frame % 2 == 0) cyan else Palette.electric.toArgb()
                    drawPixelRect(canvas, 4, 3, 8, 8, c)
                    drawPixelRect(canvas, 6, 11, 4, 3, c)
                }
                "money_rain" -> {
                    // Taka bill
                    val c = if (frame % 2 == 0) lime else Palette.green.toArgb()
                    drawPixelRect(canvas, 3, 5, 10, 6, c)
                    drawPixelRect(canvas, 7, 7, 2, 2, gold)
                }
            }
        }
    }

    // --- PASSENGERS (16x24) ---

    fun getPassengerSprite(typeIndex: Int, frame: Int): Bitmap {
        return getSprite("pass_${typeIndex}_$frame", 16, 24) { canvas ->
            val ink = Palette.ink.toArgb()
            val skin = Palette.skinLight.toArgb()
            val bounce = if (frame % 2 == 1) 1 else 0

            val clothColor = when (typeIndex % 6) {
                0 -> Palette.red.toArgb() // Saree / Bride
                1 -> Palette.navy.toArgb() // Office worker
                2 -> Palette.leaf.toArgb() // Student
                3 -> Palette.violet.toArgb() // Aunty
                4 -> Palette.amber.toArgb() // Uncle
                else -> Palette.cyan.toArgb() // Tourist
            }

            // Head
            drawPixelRect(canvas, 4, 2 + bounce, 8, 7, skin)
            drawPixelOutline(canvas, 4, 2 + bounce, 8, 7, ink)

            // Body / Clothing
            drawPixelRect(canvas, 3, 9 + bounce, 10, 11, clothColor)
            drawPixelOutline(canvas, 3, 9 + bounce, 10, 11, ink)

            // Waving arm
            if (frame % 2 == 1) {
                drawPixelRect(canvas, 12, 4, 3, 6, skin)
            } else {
                drawPixelRect(canvas, 12, 8, 3, 6, skin)
            }
        }
    }

    // --- PEDESTRIANS (16x24) ---

    fun getPedestrianSprite(frame: Int, isDodging: Boolean): Bitmap {
        return getSprite("ped_${frame}_$isDodging", 16, 24) { canvas ->
            val ink = Palette.ink.toArgb()
            val skin = Palette.skinMid.toArgb()
            val shirt = Palette.orange.toArgb()
            val pants = Palette.slate.toArgb()
            val bounce = if (frame % 2 == 1) 1 else 0

            val dodgeShift = if (isDodging) 3 else 0

            // Head
            drawPixelRect(canvas, 4 + dodgeShift, 2 + bounce, 8, 6, skin)
            drawPixelOutline(canvas, 4 + dodgeShift, 2 + bounce, 8, 6, ink)

            // Shirt
            drawPixelRect(canvas, 3 + dodgeShift, 8 + bounce, 10, 8, shirt)
            drawPixelOutline(canvas, 3 + dodgeShift, 8 + bounce, 10, 8, ink)

            // Pants / Lungi
            drawPixelRect(canvas, 4 + dodgeShift, 16 + bounce, 8, 7, pants)
        }
    }

    // --- BITMAP FONT GLYPH RENDERER ---

    fun drawBitmapText(
        canvas: Canvas,
        text: String,
        startX: Int,
        startY: Int,
        colorArgb: Int = Palette.paper.toArgb(),
        scale: Int = 1,
        useBanglaDigits: Boolean = false
    ) {
        val processedText = if (useBanglaDigits) convertToBanglaDigits(text) else text
        var curX = startX

        for (ch in processedText) {
            val glyphBitmap = getGlyphBitmap(ch, colorArgb)
            if (glyphBitmap != null) {
                val destW = glyphBitmap.width * scale
                val destH = glyphBitmap.height * scale
                val destRect = android.graphics.Rect(curX, startY, curX + destW, startY + destH)
                canvas.drawBitmap(glyphBitmap, null, destRect, paint)
                curX += (glyphBitmap.width + 1) * scale
            } else {
                curX += 5 * scale // Space width
            }
        }
    }

    private fun convertToBanglaDigits(text: String): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (c in text) {
            if (c in '0'..'9') {
                sb.append(banglaDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun getGlyphBitmap(ch: Char, colorArgb: Int): Bitmap? {
        val key = "glyph_${ch}_$colorArgb"
        return getSprite(key, 6, 8) { canvas ->
            val glyphData = GLYPH_MAP[ch] ?: GLYPH_MAP['?']
            if (glyphData != null) {
                for (r in 0..7) {
                    val line = glyphData[r]
                    for (c in 0..5) {
                        if ((line and (1 shl (5 - c))) != 0) {
                            drawPixel(canvas, c, r, colorArgb)
                        }
                    }
                }
            }
        }
    }

    // Pixel patterns for Latin A-Z, 0-9, Bangla digits ০-৯, ৳, and punctuation (6x8)
    private val GLYPH_MAP = mapOf(
        '0' to intArrayOf(0x1E, 0x21, 0x25, 0x29, 0x21, 0x1E, 0x00, 0x00),
        '1' to intArrayOf(0x0C, 0x1C, 0x0C, 0x0C, 0x0C, 0x1E, 0x00, 0x00),
        '2' to intArrayOf(0x1E, 0x21, 0x02, 0x0C, 0x10, 0x3F, 0x00, 0x00),
        '3' to intArrayOf(0x1E, 0x21, 0x0E, 0x01, 0x21, 0x1E, 0x00, 0x00),
        '4' to intArrayOf(0x22, 0x22, 0x22, 0x3F, 0x02, 0x02, 0x00, 0x00),
        '5' to intArrayOf(0x3F, 0x20, 0x3E, 0x01, 0x21, 0x1E, 0x00, 0x00),
        '6' to intArrayOf(0x1E, 0x20, 0x3E, 0x21, 0x21, 0x1E, 0x00, 0x00),
        '7' to intArrayOf(0x3F, 0x01, 0x02, 0x04, 0x08, 0x10, 0x00, 0x00),
        '8' to intArrayOf(0x1E, 0x21, 0x1E, 0x21, 0x21, 0x1E, 0x00, 0x00),
        '9' to intArrayOf(0x1E, 0x21, 0x1F, 0x01, 0x01, 0x1E, 0x00, 0x00),

        // Bangla Digits
        '০' to intArrayOf(0x1E, 0x21, 0x21, 0x21, 0x21, 0x1E, 0x00, 0x00),
        '১' to intArrayOf(0x0E, 0x11, 0x0E, 0x04, 0x04, 0x0E, 0x00, 0x00),
        '২' to intArrayOf(0x1C, 0x02, 0x1C, 0x10, 0x3E, 0x00, 0x00, 0x00),
        '৩' to intArrayOf(0x1E, 0x02, 0x0E, 0x02, 0x3C, 0x00, 0x00, 0x00),
        '৪' to intArrayOf(0x11, 0x1B, 0x0E, 0x1B, 0x11, 0x00, 0x00, 0x00),
        '৫' to intArrayOf(0x3C, 0x20, 0x38, 0x04, 0x38, 0x00, 0x00, 0x00),
        '৬' to intArrayOf(0x1E, 0x20, 0x3C, 0x22, 0x1C, 0x00, 0x00, 0x00),
        '৭' to intArrayOf(0x1C, 0x02, 0x0C, 0x10, 0x20, 0x00, 0x00, 0x00),
        '৮' to intArrayOf(0x22, 0x22, 0x1C, 0x0A, 0x12, 0x00, 0x00, 0x00),
        '৯' to intArrayOf(0x1C, 0x22, 0x1E, 0x02, 0x1C, 0x00, 0x00, 0x00),

        // Taka Symbol ৳
        '৳' to intArrayOf(0x3E, 0x08, 0x1C, 0x28, 0x28, 0x1E, 0x00, 0x00),

        // Letters A-Z
        'A' to intArrayOf(0x0C, 0x12, 0x21, 0x3F, 0x21, 0x21, 0x00, 0x00),
        'B' to intArrayOf(0x3E, 0x21, 0x3E, 0x21, 0x21, 0x3E, 0x00, 0x00),
        'C' to intArrayOf(0x1E, 0x21, 0x20, 0x20, 0x21, 0x1E, 0x00, 0x00),
        'D' to intArrayOf(0x3C, 0x22, 0x21, 0x21, 0x22, 0x3C, 0x00, 0x00),
        'E' to intArrayOf(0x3F, 0x20, 0x3E, 0x20, 0x20, 0x3F, 0x00, 0x00),
        'F' to intArrayOf(0x3F, 0x20, 0x3E, 0x20, 0x20, 0x20, 0x00, 0x00),
        'G' to intArrayOf(0x1E, 0x21, 0x20, 0x27, 0x21, 0x1E, 0x00, 0x00),
        'H' to intArrayOf(0x21, 0x21, 0x3F, 0x21, 0x21, 0x21, 0x00, 0x00),
        'I' to intArrayOf(0x1C, 0x08, 0x08, 0x08, 0x08, 0x1C, 0x00, 0x00),
        'J' to intArrayOf(0x07, 0x02, 0x02, 0x02, 0x22, 0x1C, 0x00, 0x00),
        'K' to intArrayOf(0x21, 0x22, 0x3C, 0x22, 0x21, 0x21, 0x00, 0x00),
        'L' to intArrayOf(0x20, 0x20, 0x20, 0x20, 0x20, 0x3F, 0x00, 0x00),
        'M' to intArrayOf(0x21, 0x33, 0x2D, 0x21, 0x21, 0x21, 0x00, 0x00),
        'N' to intArrayOf(0x21, 0x31, 0x29, 0x25, 0x23, 0x21, 0x00, 0x00),
        'O' to intArrayOf(0x1E, 0x21, 0x21, 0x21, 0x21, 0x1E, 0x00, 0x00),
        'P' to intArrayOf(0x3E, 0x21, 0x3E, 0x20, 0x20, 0x20, 0x00, 0x00),
        'Q' to intArrayOf(0x1E, 0x21, 0x21, 0x25, 0x22, 0x1D, 0x00, 0x00),
        'R' to intArrayOf(0x3E, 0x21, 0x3E, 0x24, 0x22, 0x21, 0x00, 0x00),
        'S' to intArrayOf(0x1E, 0x20, 0x1E, 0x01, 0x01, 0x1E, 0x00, 0x00),
        'T' to intArrayOf(0x3F, 0x08, 0x08, 0x08, 0x08, 0x08, 0x00, 0x00),
        'U' to intArrayOf(0x21, 0x21, 0x21, 0x21, 0x21, 0x1E, 0x00, 0x00),
        'V' to intArrayOf(0x21, 0x21, 0x21, 0x12, 0x12, 0x0C, 0x00, 0x00),
        'W' to intArrayOf(0x21, 0x21, 0x21, 0x2D, 0x33, 0x21, 0x00, 0x00),
        'X' to intArrayOf(0x21, 0x12, 0x0C, 0x0C, 0x12, 0x21, 0x00, 0x00),
        'Y' to intArrayOf(0x21, 0x12, 0x0C, 0x08, 0x08, 0x08, 0x00, 0x00),
        'Z' to intArrayOf(0x3F, 0x02, 0x04, 0x08, 0x10, 0x3F, 0x00, 0x00),

        '!' to intArrayOf(0x08, 0x08, 0x08, 0x08, 0x00, 0x08, 0x00, 0x00),
        '?' to intArrayOf(0x1E, 0x02, 0x0C, 0x08, 0x00, 0x08, 0x00, 0x00),
        ':' to intArrayOf(0x00, 0x0C, 0x0C, 0x00, 0x0C, 0x0C, 0x00, 0x00),
        'm' to intArrayOf(0x00, 0x00, 0x1E, 0x2B, 0x2B, 0x2B, 0x00, 0x00),
        'x' to intArrayOf(0x00, 0x00, 0x21, 0x12, 0x0C, 0x21, 0x00, 0x00)
    )

    // --- PRE-RENDERED BANGLA WORD SPRITES (Section 9 Track A) ---

    fun getBanglaTextSprite(key: String): Bitmap {
        val cacheKey = "bn_text_$key"
        return bitmapCache.getOrPut(cacheKey) {
            when (key) {
                "abar_chalai" -> createBanglaWordBitmap("আবার চালাই", 72, 16, Palette.gold.toArgb(), Palette.maroon.toArgb())
                "mama_side_den" -> createBanglaWordBitmap("মামা সাইড দেন!", 96, 16, Palette.cream.toArgb(), Palette.red.toArgb())
                "vada_ache" -> createBanglaWordBitmap("ভাড়া আছে!", 64, 16, Palette.amber.toArgb(), Palette.navy.toArgb())
                "game_over" -> createBanglaWordBitmap("গেম ওভার", 64, 18, Palette.coral.toArgb(), Palette.ink.toArgb())
                "dhaka_champion" -> createBanglaWordBitmap("ঢাকা চ্যাম্পিয়ন", 96, 16, Palette.lime.toArgb(), Palette.forest.toArgb())
                "notun_record" -> createBanglaWordBitmap("নতুন রেকর্ড!", 80, 16, Palette.gold.toArgb(), Palette.plum.toArgb())
                "super_mama" -> createBanglaWordBitmap("সুপার মামা!", 72, 16, Palette.cyan.toArgb(), Palette.blue.toArgb())
                else -> createBanglaWordBitmap("দেশি টার্বো রাশ", 96, 18, Palette.amber.toArgb(), Palette.maroon.toArgb())
            }
        }
    }

    private fun createBanglaWordBitmap(
        textLabel: String,
        width: Int,
        height: Int,
        textColorArgb: Int,
        bgColorArgb: Int
    ): Bitmap {
        return createPixelBitmap(width, height) { canvas ->
            // Background retro badge panel
            drawPixelRect(canvas, 0, 0, width, height, bgColorArgb)
            drawPixelOutline(canvas, 0, 0, width, height, Palette.ink.toArgb())
            // Top highlight
            drawPixelRect(canvas, 1, 1, width - 2, 1, Palette.paper.toArgb())

            // Render crisp pixel representation of Bangla word
            drawBitmapText(
                canvas = canvas,
                text = textLabel,
                startX = 6,
                startY = (height - 8) / 2,
                colorArgb = textColorArgb,
                scale = 1,
                useBanglaDigits = false
            )
        }
    }

    // --- 9-SLICE RETRO PANEL RENDERER ---

    fun draw9SlicePanel(canvas: Canvas, left: Int, top: Int, width: Int, height: Int) {
        val ink = Palette.ink.toArgb()
        val shadow = Palette.shadow.toArgb()
        val bone = Palette.bone.toArgb()
        val paper = Palette.paper.toArgb()

        // Main body
        drawPixelRect(canvas, left, top, width, height, shadow)

        // Bevel highlight (top and left)
        drawPixelRect(canvas, left + 1, top + 1, width - 2, 2, bone)
        drawPixelRect(canvas, left + 1, top + 1, 2, height - 2, bone)

        // Bevel shadow (bottom and right)
        drawPixelRect(canvas, left + 1, top + height - 3, width - 2, 2, ink)
        drawPixelRect(canvas, left + width - 3, top + 1, 2, height - 2, ink)

        // Inner canvas
        drawPixelRect(canvas, left + 3, top + 3, width - 6, height - 6, Palette.slate.toArgb())

        // Corner 1px dots
        drawPixel(canvas, left, top, ink)
        drawPixel(canvas, left + width - 1, top, ink)
        drawPixel(canvas, left, top + height - 1, ink)
        drawPixel(canvas, left + width - 1, top + height - 1, ink)
    }
}
