package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.Palette
import com.example.sprites.PixelSpriteRenderer

import androidx.compose.foundation.clickable

@Composable
fun GameOverScreen(
    distanceMeters: Int,
    moneyTaka: Int,
    maxCombo: Int,
    zoneName: String,
    onPlayAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val voidBlack = Palette.voidBlack
    val coral = Palette.coral
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
        verticalArrangement = Arrangement.Center
    ) {
        // --- GAME OVER TITLE ---
        Text(
            text = "G A M E   O V E R",
            color = coral,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // --- STATS BOARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Palette.shadow, RoundedCornerShape(8.dp))
                .border(2.dp, bone, RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatRow(label = "DISTANCE", value = "${distanceMeters} m", valueColor = gold)
                StatRow(label = "MONEY", value = "৳$moneyTaka", valueColor = amber)
                StatRow(label = "MAX COMBO", value = "x$maxCombo", valueColor = Palette.cyan)
                StatRow(label = "ZONE", value = zoneName, valueColor = Palette.lime)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Pre-rendered Bangla "আবার চালাই" Play Again Button
        val playAgainBm = PixelSpriteRenderer.getBanglaTextSprite("abar_chalai")
        Image(
            bitmap = playAgainBm.asImageBitmap(),
            contentDescription = "আবার চালাই Play Again",
            modifier = Modifier
                .width(220.dp)
                .height(54.dp)
                .testTag("play_again_button")
                .background(gold, RoundedCornerShape(8.dp))
                .border(2.dp, ink, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { onPlayAgain() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        RetroArcadeButton(
            text = "🏠  H O M E",
            bgColor = Palette.slate,
            textColor = paper,
            testTag = "home_button",
            onClick = onGoHome
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Palette.bone,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
