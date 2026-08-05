import 'package:flutter/material.dart';
import '../core/palette.dart';

class GameOverScreen extends StatelessWidget {
  final int distanceMeters;
  final int moneyTaka;
  final int maxCombo;
  final String zoneName;
  final VoidCallback onPlayAgain;
  final VoidCallback onGoHome;

  const GameOverScreen({
    required this.distanceMeters,
    required this.moneyTaka,
    required this.maxCombo,
    required this.zoneName,
    required this.onPlayAgain,
    required this.onGoHome,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Palette.voidBlack,
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text("GAME OVER", style: TextStyle(color: Palette.coral, fontSize: 28, fontFamily: 'monospace', fontWeight: FontWeight.w900)),
          const SizedBox(height: 20),
          Container(
            width: double.infinity,
            decoration: BoxDecoration(
              color: Palette.shadow,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Palette.bone, width: 2),
            ),
            padding: const EdgeInsets.all(20),
            child: Column(
              children: [
                _buildStatRow("DISTANCE", "${distanceMeters}m", Palette.gold),
                _buildStatRow("MONEY", "Taka$moneyTaka", Palette.amber),
                _buildStatRow("MAX COMBO", "x$maxCombo", Palette.cyan),
                _buildStatRow("ZONE", zoneName, Palette.lime),
              ],
            ),
          ),
          const SizedBox(height: 30),
          GestureDetector(
            onTap: onPlayAgain,
            child: Container(
              width: 220,
              height: 54,
              decoration: BoxDecoration(
                color: Palette.gold,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: Palette.ink, width: 2),
              ),
              padding: const EdgeInsets.all(8),
              child: const Center(
                child: Text("PLAY AGAIN", style: TextStyle(color: Palette.ink, fontSize: 16, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
              ),
            ),
          ),
          const SizedBox(height: 14),
          _buildButton("HOME", Palette.slate, Palette.paper, onGoHome),
        ],
      ),
    );
  }

  Widget _buildStatRow(String label, String value, Color valueColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Palette.bone, fontSize: 14, fontFamily: 'monospace')),
          Text(value, style: TextStyle(color: valueColor, fontSize: 16, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
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
