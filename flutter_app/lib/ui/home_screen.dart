import 'package:flutter/material.dart';
import '../core/palette.dart';
import '../game/game_engine.dart';
import '../sprites/pixel_sprite_renderer.dart';

class HomeScreen extends StatefulWidget {
  final int bestDistanceMeters;
  final int totalMoneyTaka;
  final VoidCallback onStartGame;
  final VoidCallback onOpenLeaderboard;
  final VoidCallback onOpenAchievements;
  final VoidCallback onOpenSettings;

  const HomeScreen({
    required this.bestDistanceMeters,
    required this.totalMoneyTaka,
    required this.onStartGame,
    required this.onOpenLeaderboard,
    required this.onOpenAchievements,
    required this.onOpenSettings,
    super.key,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  late Ticker _ticker;
  var _frameIndex = 0;

  @override
  void initState() {
    super.initState();
    _ticker = createTicker((elapsed) {
      setState(() {
        _frameIndex = (elapsed.inMilliseconds ~/ 120) % 4;
      });
    });
    _ticker.start();
  }

  @override
  void dispose() {
    _ticker.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Palette.voidBlack,
      padding: const EdgeInsets.all(24),
      child: Column(
        children: [
          const SizedBox(height: 28),
          Container(
            decoration: BoxDecoration(
              color: Palette.maroon,
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: Palette.amber, width: 3),
            ),
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            child: Column(
              children: [
                const Text("DESI TURBO", style: TextStyle(color: Palette.gold, fontSize: 28, fontFamily: 'monospace', fontWeight: FontWeight.w900, letterSpacing: 2)),
                const Text("R U S H", style: TextStyle(color: Palette.paper, fontSize: 24, fontFamily: 'monospace', fontWeight: FontWeight.bold, letterSpacing: 4)),
              ],
            ),
          ),
          const SizedBox(height: 8),
          const Text("ঢাকা স্ট্রিট আর্কেড ২০৯০", style: TextStyle(color: Palette.amber, fontSize: 15, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
          const Spacer(),
          Column(
            children: [
              CustomPaint(
                size: const Size(120, 120),
                painter: _RickshawPainter(frameIndex: _frameIndex),
              ),
              const SizedBox(height: 8),
              Text("BEST: ${widget.bestDistanceMeters}m | TOTAL: Taka${widget.totalMoneyTaka}",
                  style: const TextStyle(color: Palette.bone, fontSize: 13, fontFamily: 'monospace')),
            ],
          ),
          const Spacer(),
          Column(
            children: [
              _buildButton("PLAY", Palette.amber, Palette.ink, widget.onStartGame),
              const SizedBox(height: 10),
              _buildButton("LEADERBOARD", Palette.blue, Palette.paper, widget.onOpenLeaderboard),
              const SizedBox(height: 10),
              _buildButton("ACHIEVEMENTS", Palette.leaf, Palette.voidBlack, widget.onOpenAchievements),
              const SizedBox(height: 10),
              _buildButton("SETTINGS", Palette.slate, Palette.paper, widget.onOpenSettings),
            ],
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

class _RickshawPainter extends CustomPainter {
  final int frameIndex;

  _RickshawPainter({required this.frameIndex});

  @override
  void paint(Canvas canvas, Size size) {
    final image = PixelSpriteRenderer.getRickshawFrame(frameIndex);
    final src = Rect.fromLTWH(0, 0, 32, 40);
    final dst = Rect.fromLTWH(0, 0, size.width, size.height);
    canvas.drawImageRect(image, src, dst, Paint());
  }

  @override
  bool shouldRepaint(_RickshawPainter oldDelegate) => oldDelegate.frameIndex != frameIndex;
}
