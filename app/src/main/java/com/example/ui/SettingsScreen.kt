package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

@Composable
fun SettingsScreen(
    bgmEnabled: Boolean,
    sfxEnabled: Boolean,
    voiceEnabled: Boolean,
    useBanglaDigits: Boolean,
    showFpsOverlay: Boolean,
    onBgmToggled: (Boolean) -> Unit,
    onSfxToggled: (Boolean) -> Unit,
    onVoiceToggled: (Boolean) -> Unit,
    onBanglaDigitsToggled: (Boolean) -> Unit,
    onFpsOverlayToggled: (Boolean) -> Unit,
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
            text = "⚙ SETTINGS",
            color = Palette.gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingRow(
                label = "Chiptune BGM",
                value = bgmEnabled,
                onCheckedChange = onBgmToggled,
                testTag = "bgm_toggle"
            )
            SettingRow(
                label = "Retro SFX",
                value = sfxEnabled,
                onCheckedChange = onSfxToggled,
                testTag = "sfx_toggle"
            )
            SettingRow(
                label = "Bangla Voice Lines",
                value = voiceEnabled,
                onCheckedChange = onVoiceToggled,
                testTag = "voice_toggle"
            )
            SettingRow(
                label = "Bangla Digits (০১২৩)",
                value = useBanglaDigits,
                onCheckedChange = onBanglaDigitsToggled,
                testTag = "digits_toggle"
            )
            SettingRow(
                label = "Debug FPS & Pool Overlay",
                value = showFpsOverlay,
                onCheckedChange = onFpsOverlayToggled,
                testTag = "fps_toggle"
            )
        }

        RetroArcadeButton(
            text = "⬅  B A C K",
            bgColor = Palette.slate,
            textColor = Palette.paper,
            testTag = "back_button",
            onClick = onBackClicked
        )
    }
}

@Composable
private fun SettingRow(
    label: String,
    value: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.shadow, RoundedCornerShape(8.dp))
            .border(1.dp, Palette.slate, RoundedCornerShape(8.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Palette.paper,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Switch(
                checked = value,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Palette.gold,
                    checkedTrackColor = Palette.leaf,
                    uncheckedThumbColor = Palette.ash,
                    uncheckedTrackColor = Palette.ink
                )
            )
        }
    }
}
