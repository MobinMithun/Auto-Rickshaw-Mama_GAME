import 'package:flutter/material.dart';
import '../core/palette.dart';
import '../data/entities.dart';

class AchievementsScreen extends StatelessWidget {
  final List<Achievement> achievements;
  final VoidCallback onBackClicked;

  const AchievementsScreen({
    required this.achievements,
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
          const Text("ACHIEVEMENTS", style: TextStyle(color: Palette.gold, fontSize: 24, fontFamily: 'monospace', fontWeight: FontWeight.w900)),
          const SizedBox(height: 16),
          Expanded(
            child: ListView.builder(
              itemCount: achievements.length,
              itemBuilder: (context, index) {
                final ach = achievements[index];
                final bg = ach.isUnlocked ? Palette.shadow : Palette.ink;
                final border = ach.isUnlocked ? Palette.gold : Palette.slate;
                final titleColor = ach.isUnlocked ? Palette.gold : Palette.stone;

                return Container(
                  margin: const EdgeInsets.symmetric(vertical: 6),
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: bg,
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: border, width: 1),
                  ),
                  child: Row(
                    children: [
                      Text(ach.isUnlocked ? "🌟" : "🔒", style: const TextStyle(fontSize: 20)),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(ach.title, style: TextStyle(color: titleColor, fontSize: 15, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
                            Text(ach.description, style: const TextStyle(color: Palette.bone, fontSize: 12, fontFamily: 'monospace')),
                          ],
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),
          const SizedBox(height: 16),
          _buildButton("BACK", Palette.slate, Palette.paper, onBackClicked),
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
