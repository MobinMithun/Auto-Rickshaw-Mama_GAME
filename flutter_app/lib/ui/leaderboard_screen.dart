import 'package:flutter/material.dart';
import '../core/palette.dart';
import '../data/entities.dart';

class LeaderboardScreen extends StatelessWidget {
  final List<RunResult> topRuns;
  final VoidCallback onBackClicked;

  const LeaderboardScreen({
    required this.topRuns,
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
          const Text("HIGH SCORES", style: TextStyle(color: Palette.gold, fontSize: 24, fontFamily: 'monospace', fontWeight: FontWeight.w900)),
          const SizedBox(height: 16),
          Expanded(
            child: topRuns.isEmpty
                ? const Center(
                    child: Text("No runs recorded yet!\nStart playing to set high scores.",
                        textAlign: TextAlign.center, style: TextStyle(color: Palette.bone, fontSize: 14, fontFamily: 'monospace')),
                  )
                : ListView.builder(
                    itemCount: topRuns.length,
                    itemBuilder: (context, index) {
                      final run = topRuns[index];
                      return Container(
                        margin: const EdgeInsets.symmetric(vertical: 6),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Palette.shadow,
                          borderRadius: BorderRadius.circular(6),
                          border: Border.all(color: Palette.slate, width: 1),
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text("#${index + 1}  ${run.distanceMeters}m",
                                style: TextStyle(
                                  color: index == 0 ? Palette.gold : Palette.paper,
                                  fontSize: 16,
                                  fontFamily: 'monospace',
                                  fontWeight: FontWeight.bold,
                                )),
                            Text("Taka${run.moneyCollected} (${run.zoneReached})",
                                style: const TextStyle(color: Palette.amber, fontSize: 14, fontFamily: 'monospace')),
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
