package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.Palette
import com.example.data.AchievementEntity

@Composable
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    onBackClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.voidBlack)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⭐ ACHIEVEMENTS",
            color = Palette.gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(achievements) { ach ->
                val bg = if (ach.isUnlocked) Palette.shadow else Palette.ink
                val border = if (ach.isUnlocked) Palette.gold else Palette.slate
                val titleColor = if (ach.isUnlocked) Palette.gold else Palette.stone

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg, RoundedCornerShape(8.dp))
                        .border(1.dp, border, RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (ach.isUnlocked) "🌟" else "🔒",
                            fontSize = 20.sp
                        )
                        Column {
                            Text(
                                text = ach.title,
                                color = titleColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = ach.description,
                                color = Palette.bone,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        RetroArcadeButton(
            text = "⬅  B A C K",
            bgColor = Palette.slate,
            textColor = Palette.paper,
            testTag = "back_button",
            onClick = onBackClicked
        )
    }
}
