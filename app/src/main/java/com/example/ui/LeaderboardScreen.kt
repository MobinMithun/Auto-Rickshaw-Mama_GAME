package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.RunResultEntity

@Composable
fun LeaderboardScreen(
    topRuns: List<RunResultEntity>,
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
            text = "🏆 HIGH SCORES",
            color = Palette.gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (topRuns.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No runs recorded yet!\nStart playing to set high scores.",
                    color = Palette.bone,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(topRuns) { index, run ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Palette.shadow, RoundedCornerShape(6.dp))
                            .border(1.dp, Palette.slate, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${index + 1}  ${run.distanceMeters}m",
                                color = if (index == 0) Palette.gold else Palette.paper,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "৳${run.moneyCollected} (${run.zoneReached})",
                                color = Palette.amber,
                                fontSize = 14.sp,
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
