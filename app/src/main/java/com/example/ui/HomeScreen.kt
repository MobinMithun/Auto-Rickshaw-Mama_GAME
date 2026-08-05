package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.Palette
import com.example.sprites.PixelSpriteRenderer
import com.example.ui.theme.AnekBanglaFontFamily

@Composable
fun HomeScreen(
    bestDistanceMeters: Int,
    totalMoneyTaka: Int,
    onStartGame: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var frameIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(120)
            frameIndex = (frameIndex + 1) % 4
        }
    }

    val voidBlack = Palette.voidBlack
    val maroon = Palette.maroon
    val amber = Palette.amber
    val gold = Palette.gold
    val paper = Palette.paper
    val bone = Palette.bone
    val ink = Palette.ink

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(voidBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- TITLE LOGO PANEL ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(maroon, RoundedCornerShape(8.dp))
                    .border(3.dp, amber, RoundedCornerShape(8.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DESI TURBO",
                        color = gold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "R U S H",
                        color = paper,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ঢাকা স্ট্রিট আর্কেড ২০৯০",
                color = amber,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AnekBanglaFontFamily
            )
        }

        // --- ANIMATED IDLE RICKSHAW MAMA ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val rickshawBm = remember(frameIndex) {
                PixelSpriteRenderer.getRickshawFrame(frameIndex, isTurbo = false, leanDir = 0)
            }
            Image(
                bitmap = rickshawBm.asImageBitmap(),
                contentDescription = "Animated Rickshaw Mama",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "BEST: ${bestDistanceMeters}m | TOTAL: ৳$totalMoneyTaka",
                color = bone,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // --- BUTTONS MENU ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RetroArcadeButton(
                text = "▶  P L A Y",
                bgColor = amber,
                textColor = ink,
                testTag = "play_button",
                onClick = onStartGame
            )
            RetroArcadeButton(
                text = "🏆  LEADERBOARD",
                bgColor = Palette.blue,
                textColor = paper,
                testTag = "leaderboard_button",
                onClick = onOpenLeaderboard
            )
            RetroArcadeButton(
                text = "⭐  ACHIEVEMENTS",
                bgColor = Palette.leaf,
                textColor = voidBlack,
                testTag = "achievements_button",
                onClick = onOpenAchievements
            )
            RetroArcadeButton(
                text = "⚙  SETTINGS",
                bgColor = Palette.slate,
                textColor = paper,
                testTag = "settings_button",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
fun RetroArcadeButton(
    text: String,
    bgColor: Color,
    textColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(50.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(2.dp, Palette.ink, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
