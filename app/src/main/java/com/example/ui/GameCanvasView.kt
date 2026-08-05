package com.example.ui

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import com.example.core.Constants
import com.example.core.GameZone
import com.example.core.Palette
import com.example.game.GameEngine
import com.example.game.GameScreenState
import com.example.sprites.PixelSpriteRenderer
import kotlin.math.floor
import kotlin.math.min

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent

@Composable
fun GameCanvasView(
    engine: GameEngine,
    onPauseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var frameTick by remember { mutableStateOf(0L) }

    // 60 FPS Game Loop Driver
    LaunchedEffect(Unit) {
        var lastNanos = System.nanoTime()
        while (true) {
            withFrameNanos { nowNanos ->
                val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nowNanos
                engine.update(dt)
                frameTick = nowNanos
            }
        }
    }

    val nativePaint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    var totalDragX by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onKeyEvent { keyEvent ->
                if (engine.state == GameScreenState.PLAYING) {
                    when (keyEvent.key) {
                        Key.DirectionLeft, Key.A -> {
                            engine.handleSwipe(-20f)
                            true
                        }
                        Key.DirectionRight, Key.D -> {
                            engine.handleSwipe(20f)
                            true
                        }
                        Key.Spacebar -> {
                            engine.handleTapBell()
                            true
                        }
                        Key.Escape, Key.P -> {
                            onPauseClicked()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { totalDragX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (engine.state == GameScreenState.PLAYING) {
                                totalDragX += dragAmount.x
                                if (totalDragX > 20f) {
                                    engine.handleSwipe(20f)
                                    totalDragX = 0f
                                } else if (totalDragX < -20f) {
                                    engine.handleSwipe(-20f)
                                    totalDragX = 0f
                                }
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (engine.state == GameScreenState.PLAYING) {
                                val canvasW = size.width.toFloat()
                                if (offset.x < canvasW / 2f) {
                                    engine.handleSwipe(-20f)
                                } else {
                                    engine.handleSwipe(20f)
                                }
                            }
                        },
                        onDoubleTap = {
                            if (engine.state == GameScreenState.PLAYING) {
                                engine.handleTapBell()
                            }
                        }
                    )
                }
        ) {
            val tick = frameTick
            val deviceW = size.width
            val deviceH = size.height

            // Integer-only upscaling calculation
            val scaleFloat = floor(min(deviceW / Constants.VIRTUAL_WIDTH, deviceH / Constants.VIRTUAL_HEIGHT)).coerceAtLeast(1f)
            val scale = scaleFloat.toInt()

            val virtualDrawW = Constants.VIRTUAL_WIDTH * scale
            val virtualDrawH = Constants.VIRTUAL_HEIGHT * scale

            // Letterbox / Pillarbox Offsets
            val letterboxX = ((deviceW - virtualDrawW) / 2f).toInt()
            val letterboxY = ((deviceH - virtualDrawH) / 2f).toInt()

            drawIntoCanvas { canvas ->
                val nCanvas = canvas.nativeCanvas

                // Fill letterbox area with voidBlack (#0D0B1F)
                nCanvas.drawColor(Palette.voidBlack.toArgb())

                // Apply Integer Viewport Translation & Screen Shake
                nCanvas.save()
                nCanvas.translate(
                    (letterboxX + engine.shakeOffsetX * scale).toFloat(),
                    (letterboxY + engine.shakeOffsetY * scale).toFloat()
                )

                // Clip bounds to exactly 180x320 virtual pixels
                nCanvas.clipRect(0, 0, virtualDrawW.toInt(), virtualDrawH.toInt())

                // --- 1. PARALLAX SKYLINE & BACKGROUND ---
                drawParallaxBackground(nCanvas, engine, scale, nativePaint)

                // --- 2. ROAD & KERBS ---
                drawRoadAndKerbs(nCanvas, engine, scale, nativePaint)

                // --- 3. ENTITIES ---
                drawEntities(nCanvas, engine, scale, nativePaint)

                // --- 4. WEATHER & ATMOSPHERIC DITHERING ---
                drawWeatherAndAtmosphere(nCanvas, engine, scale, nativePaint)

                // --- 5. IN-GAME HUD ---
                drawHud(nCanvas, engine, scale, nativePaint)

                nCanvas.restore()
            }
        }

        // --- ON-SCREEN TOUCH CONTROLS (WHEN PLAYING) ---
        if (engine.state == GameScreenState.PLAYING) {
            // Top Right Mute & Pause Controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isMusicOn = engine.audioDirector.bgmEnabled
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Palette.ink)
                        .border(2.dp, if (isMusicOn) Palette.gold else Palette.ash, RoundedCornerShape(6.dp))
                        .clickable { engine.audioDirector.bgmEnabled = !isMusicOn }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("in_game_mute_button")
                ) {
                    Text(
                        text = if (isMusicOn) "🔊 MUSIC" else "🔇 MUTED",
                        color = Palette.paper,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Palette.ink)
                        .border(2.dp, Palette.coral, RoundedCornerShape(6.dp))
                        .clickable { onPauseClicked() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("in_game_pause_button")
                ) {
                    Text(
                        text = "⏸ PAUSE",
                        color = Palette.paper,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Bottom On-Screen Arcade Steering & Bell Controls
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Steering Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Palette.maroon)
                        .border(2.dp, Palette.amber, RoundedCornerShape(8.dp))
                        .clickable { engine.handleSwipe(-20f) }
                        .testTag("left_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀ LEFT",
                        color = Palette.gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Bell Button
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Palette.leaf)
                        .border(2.dp, Palette.gold, RoundedCornerShape(8.dp))
                        .clickable { engine.handleTapBell() }
                        .testTag("bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔔",
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Right Steering Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Palette.maroon)
                        .border(2.dp, Palette.amber, RoundedCornerShape(8.dp))
                        .clickable { engine.handleSwipe(20f) }
                        .testTag("right_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RIGHT ▶",
                        color = Palette.gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- PAUSE OVERLAY ---
        if (engine.state == GameScreenState.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Palette.voidBlack.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "P A U S E D",
                        color = Palette.gold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RetroArcadeButton(
                        text = "▶  R E S U M E",
                        bgColor = Palette.amber,
                        textColor = Palette.ink,
                        testTag = "resume_button",
                        onClick = { engine.resumeGame() }
                    )
                    val bgmActive = engine.audioDirector.bgmEnabled
                    RetroArcadeButton(
                        text = if (bgmActive) "🔊  M U T E  M U S I C" else "🔇  U N M U T E  M U S I C",
                        bgColor = if (bgmActive) Palette.slate else Palette.leaf,
                        textColor = Palette.paper,
                        testTag = "pause_mute_button",
                        onClick = { engine.audioDirector.bgmEnabled = !bgmActive }
                    )
                    RetroArcadeButton(
                        text = "🏠  Q U I T  T O  M E N U",
                        bgColor = Palette.coral,
                        textColor = Palette.paper,
                        testTag = "quit_button",
                        onClick = { engine.goToHome() }
                    )
                }
            }
        }
    }
}

private fun drawParallaxBackground(nCanvas: NativeCanvas, engine: GameEngine, scale: Int, paint: Paint) {
    val distance = engine.distanceMeters

    // Top Sky (0..40 virtual px)
    val skyColor = when (engine.currentZone) {
        GameZone.NIGHT_STREET -> Palette.voidBlack.toArgb()
        GameZone.RAINY_ROAD -> Palette.slate.toArgb()
        GameZone.OLD_DHAKA -> Palette.plum.toArgb()
        else -> Palette.navy.toArgb()
    }
    paint.color = skyColor
    nCanvas.drawRect(0f, 0f, 180f * scale, 40f * scale, paint)

    // Far Skyline Parallax (scrolling at 0.2x speed)
    val skyScroll = ((distance * 0.2f) % 64f).toInt()
    val bldgColor = when (engine.currentZone) {
        GameZone.NIGHT_STREET -> Palette.ink.toArgb()
        else -> Palette.shadow.toArgb()
    }
    paint.color = bldgColor

    for (x in -64..180 step 32) {
        val drawX = (x - skyScroll) * scale
        nCanvas.drawRect(drawX.toFloat(), 10f * scale, (drawX + 24 * scale).toFloat(), 40f * scale, paint)
    }
}

private fun drawRoadAndKerbs(nCanvas: NativeCanvas, engine: GameEngine, scale: Int, paint: Paint) {
    val scrollY = ((engine.distanceMeters * 6f) % 16f).toInt()

    // Kerbs
    paint.color = Palette.stone.toArgb()
    nCanvas.drawRect(0f, 0f, Constants.KERB_LEFT_X * scale, 320f * scale, paint)
    nCanvas.drawRect(Constants.KERB_RIGHT_X * scale, 0f, 180f * scale, 320f * scale, paint)

    // Road Surface
    val roadColor = when (engine.currentZone) {
        GameZone.VILLAGE_ROAD -> Palette.soil.toArgb()
        GameZone.RAINY_ROAD -> Palette.ink.toArgb()
        else -> Palette.shadow.toArgb()
    }
    paint.color = roadColor
    nCanvas.drawRect(Constants.KERB_LEFT_X * scale, 0f, Constants.KERB_RIGHT_X * scale, 320f * scale, paint)

    // Dashed Lane Lines
    paint.color = Palette.amber.toArgb()
    for (y in -16..320 step 16) {
        val drawY = (y + scrollY) * scale
        // Line 1 (lane 0-1 boundary at x=70)
        nCanvas.drawRect(70f * scale, drawY.toFloat(), 72f * scale, (drawY + 8 * scale).toFloat(), paint)
        // Line 2 (lane 1-2 boundary at x=110)
        nCanvas.drawRect(110f * scale, drawY.toFloat(), 112f * scale, (drawY + 8 * scale).toFloat(), paint)
    }
}

private fun drawEntities(nCanvas: NativeCanvas, engine: GameEngine, scale: Int, paint: Paint) {
    val frameIndex = ((engine.survivalTimeSec * 10f).toInt()) % 4

    // 1. Coins
    for (coin in engine.pools.coinPool) {
        if (!coin.isActive || coin.isCollected) continue
        val sprite = PixelSpriteRenderer.getCoinSprite(frameIndex)
        val dst = Rect(
            (coin.x * scale).toInt(),
            (coin.y * scale).toInt(),
            ((coin.x + 8f) * scale).toInt(),
            ((coin.y + 8f) * scale).toInt()
        )
        nCanvas.drawBitmap(sprite, null, dst, paint)
    }

    // 2. PowerUps
    for (pup in engine.pools.powerUpPool) {
        if (!pup.isActive) continue
        val sprite = PixelSpriteRenderer.getPowerUpSprite(pup.type.name.lowercase(), frameIndex)
        val dst = Rect(
            (pup.x * scale).toInt(),
            (pup.y * scale).toInt(),
            ((pup.x + 16f) * scale).toInt(),
            ((pup.y + 16f) * scale).toInt()
        )
        nCanvas.drawBitmap(sprite, null, dst, paint)
    }

    // 3. Obstacles
    for (obs in engine.pools.obstaclePool) {
        if (!obs.isActive) continue
        val sprite = PixelSpriteRenderer.getObstacleSprite(obs.type, frameIndex)
        val dst = Rect(
            (obs.x * scale).toInt(),
            (obs.y * scale).toInt(),
            ((obs.x + obs.width) * scale).toInt(),
            ((obs.y + obs.height) * scale).toInt()
        )
        nCanvas.drawBitmap(sprite, null, dst, paint)
    }

    // 4. Pedestrians
    for (ped in engine.pools.pedestrianPool) {
        if (!ped.isActive) continue
        val sprite = PixelSpriteRenderer.getPedestrianSprite(frameIndex, ped.isDodging)
        val drawX = if (ped.isDodging) ped.x + ped.dodgeOffset else ped.x
        val dst = Rect(
            (drawX * scale).toInt(),
            (ped.y * scale).toInt(),
            ((drawX + 16f) * scale).toInt(),
            ((ped.y + 24f) * scale).toInt()
        )
        nCanvas.drawBitmap(sprite, null, dst, paint)
    }

    // 5. Passengers
    for (pass in engine.pools.passengerPool) {
        if (!pass.isActive) continue
        val sprite = PixelSpriteRenderer.getPassengerSprite(pass.typeIndex, frameIndex)
        val dst = Rect(
            (pass.x * scale).toInt(),
            (pass.y * scale).toInt(),
            ((pass.x + 16f) * scale).toInt(),
            ((pass.y + 24f) * scale).toInt()
        )
        nCanvas.drawBitmap(sprite, null, dst, paint)
    }

    // 6. Rickshaw Player
    val player = engine.player
    val leanDir = if (player.isChangingLane) (if (player.targetX > player.x) 1 else -1) else 0

    // Turbo Ghost Trails (3 palette-stepped trails)
    if (player.isTurbo) {
        val ghostColor = Palette.shadow.toArgb()
        paint.color = ghostColor
        for (g in 1..3) {
            nCanvas.drawRect(
                (player.x * scale).toFloat(),
                ((player.y + g * 8f) * scale).toFloat(),
                ((player.x + 32f) * scale).toFloat(),
                ((player.y + 40f + g * 8f) * scale).toFloat(),
                paint
            )
        }
    }

    val isBlinking = player.invincibleTimerSec > 0f && ((player.invincibleTimerSec * 15f).toInt() % 2 == 0)
    if (!isBlinking) {
        val rickshawSprite = PixelSpriteRenderer.getRickshawFrame(frameIndex, player.isTurbo, leanDir)
        val rDst = Rect(
            (player.x.toInt() * scale),
            (player.y.toInt() * scale),
            ((player.x + 32f).toInt() * scale),
            ((player.y + 40f).toInt() * scale)
        )
        nCanvas.drawBitmap(rickshawSprite, null, rDst, paint)
    }

    // Shield Aura
    if (player.isShielded) {
        paint.color = Palette.cyan.toArgb()
        nCanvas.drawRect(
            ((player.x - 2f) * scale).toFloat(),
            ((player.y - 2f) * scale).toFloat(),
            ((player.x + 34f) * scale).toFloat(),
            ((player.y + 42f) * scale).toFloat(),
            paint
        )
    }

    // 7. Floating Text
    for (ft in engine.pools.floatingTextPool) {
        if (!ft.isActive) continue
        PixelSpriteRenderer.drawBitmapText(
            canvas = nCanvas,
            text = ft.text,
            startX = (ft.x * scale).toInt(),
            startY = (ft.y * scale).toInt(),
            colorArgb = ft.colorArgb,
            scale = scale,
            useBanglaDigits = engine.useBanglaDigits
        )
    }
}

private fun drawWeatherAndAtmosphere(nCanvas: NativeCanvas, engine: GameEngine, scale: Int, paint: Paint) {
    when (engine.currentZone) {
        GameZone.RAINY_ROAD -> {
            // Pixel Rain drops (1x3px lines in ash/stone)
            paint.color = Palette.ash.toArgb()
            val rainSeed = ((engine.survivalTimeSec * 60f).toInt())
            for (i in 0 until 18) {
                val rx = ((i * 11 + rainSeed * 7) % 180) * scale
                val ry = ((i * 19 + rainSeed * 13) % 320) * scale
                nCanvas.drawRect(rx.toFloat(), ry.toFloat(), (rx + 1 * scale).toFloat(), (ry + 3 * scale).toFloat(), paint)
            }
        }
        GameZone.NIGHT_STREET -> {
            // Dithered headlamp cone in front of rickshaw
            paint.color = Palette.cream.toArgb()
            val px = engine.player.x + 12f
            val py = engine.player.y
            nCanvas.drawRect(
                ((px - 10f) * scale).toFloat(),
                ((py - 40f) * scale).toFloat(),
                ((px + 10f) * scale).toFloat(),
                (py * scale).toFloat(),
                paint
            )
        }
        else -> {}
    }
}

private fun drawHud(nCanvas: NativeCanvas, engine: GameEngine, scale: Int, paint: Paint) {
    // HUD Header Panel (y = 0..24 virtual px)
    paint.color = Palette.ink.toArgb()
    nCanvas.drawRect(0f, 0f, 180f * scale, 24f * scale, paint)
    paint.color = Palette.amber.toArgb()
    nCanvas.drawRect(0f, 23f * scale, 180f * scale, 24f * scale, paint)

    // Distance Meters Left
    val distStr = "${engine.distanceMeters.toInt()}m"
    PixelSpriteRenderer.drawBitmapText(
        canvas = nCanvas,
        text = distStr,
        startX = 6 * scale,
        startY = 6 * scale,
        colorArgb = Palette.paper.toArgb(),
        scale = scale,
        useBanglaDigits = engine.useBanglaDigits
    )

    // Lives Indicator (Center)
    val livesCount = engine.player.lives.coerceAtLeast(0)
    val livesStr = "L:$livesCount"
    PixelSpriteRenderer.drawBitmapText(
        canvas = nCanvas,
        text = livesStr,
        startX = 68 * scale,
        startY = 6 * scale,
        colorArgb = Palette.coral.toArgb(),
        scale = scale,
        useBanglaDigits = engine.useBanglaDigits
    )

    // Money Taka Right
    val moneyStr = "৳${engine.moneyTaka}"
    PixelSpriteRenderer.drawBitmapText(
        canvas = nCanvas,
        text = moneyStr,
        startX = 110 * scale,
        startY = 6 * scale,
        colorArgb = Palette.gold.toArgb(),
        scale = scale,
        useBanglaDigits = engine.useBanglaDigits
    )

    // Pause Button top right [||]
    PixelSpriteRenderer.drawBitmapText(
        canvas = nCanvas,
        text = "[||]",
        startX = 160 * scale,
        startY = 6 * scale,
        colorArgb = Palette.coral.toArgb(),
        scale = scale
    )

    // Combo Gauge (bottom left if active)
    if (engine.comboCount > 1) {
        val comboStr = "x${engine.comboCount}"
        PixelSpriteRenderer.drawBitmapText(
            canvas = nCanvas,
            text = comboStr,
            startX = 6 * scale,
            startY = 296 * scale,
            colorArgb = Palette.gold.toArgb(),
            scale = scale,
            useBanglaDigits = engine.useBanglaDigits
        )
    }
}
