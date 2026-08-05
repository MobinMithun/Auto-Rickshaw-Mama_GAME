import 'package:flutter/material.dart';
import '../core/palette.dart';

class SettingsScreen extends StatelessWidget {
  final bool bgmEnabled;
  final bool sfxEnabled;
  final bool voiceEnabled;
  final bool useBanglaDigits;
  final bool showFpsOverlay;
  final ValueChanged<bool> onBgmToggled;
  final ValueChanged<bool> onSfxToggled;
  final ValueChanged<bool> onVoiceToggled;
  final ValueChanged<bool> onBanglaDigitsToggled;
  final ValueChanged<bool> onFpsOverlayToggled;
  final VoidCallback onBackClicked;

  const SettingsScreen({
    required this.bgmEnabled,
    required this.sfxEnabled,
    required this.voiceEnabled,
    required this.useBanglaDigits,
    required this.showFpsOverlay,
    required this.onBgmToggled,
    required this.onSfxToggled,
    required this.onVoiceToggled,
    required this.onBanglaDigitsToggled,
    required this.onFpsOverlayToggled,
    required this.onBackClicked,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Palette.voidBlack,
      padding: const EdgeInsets.all(20),
      child: Column(
        children: [
          const Text("SETTINGS", style: TextStyle(color: Palette.gold, fontSize: 24, fontFamily: 'monospace', fontWeight: FontWeight.w900)),
          const SizedBox(height: 16),
          Expanded(
            child: Column(
              children: [
                _buildSettingRow("Chiptune BGM", bgmEnabled, onBgmToggled),
                _buildSettingRow("Retro SFX", sfxEnabled, onSfxToggled),
                _buildSettingRow("Bangla Voice Lines", voiceEnabled, onVoiceToggled),
                _buildSettingRow("Bangla Digits (0-9)", useBanglaDigits, onBanglaDigitsToggled),
                _buildSettingRow("Debug FPS & Pool Overlay", showFpsOverlay, onFpsOverlayToggled),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _buildButton("BACK", Palette.slate, Palette.paper, onBackClicked),
        ],
      ),
    );
  }

  Widget _buildSettingRow(String label, bool value, ValueChanged<bool> onChanged) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.symmetric(vertical: 6),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Palette.shadow,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Palette.slate, width: 1),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Palette.paper, fontSize: 15, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
          Switch(
            value: value,
            onChanged: onChanged,
            activeThumbColor: Palette.gold,
            activeTrackColor: Palette.leaf,
            inactiveThumbColor: Palette.ash,
            inactiveTrackColor: Palette.ink,
          ),
        ],
      ),
    );
  }

  Widget _buildButton(String text, Color bg, Color textColor, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: double.infinity,
        height: 50,
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: Palette.ink, width: 2),
        ),
        child: Center(
          child: Text(text, style: TextStyle(color: textColor, fontSize: 16, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
        ),
      ),
    );
  }
}
