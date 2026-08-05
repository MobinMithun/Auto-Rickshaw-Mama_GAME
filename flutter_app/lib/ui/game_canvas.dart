import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import '../core/constants.dart';
import '../core/palette.dart';
import '../game/game_engine.dart';
import '../sprites/pixel_sprite_renderer.dart';

class GameCanvasView extends StatefulWidget {
  final GameEngine engine;
  final VoidCallback onPauseClicked;

  const GameCanvasView({
    required this.engine,
    required this.onPauseClicked,
    super.key,
  });

  @override
  State<GameCanvasView> createState() => _GameCanvasViewState();
}

class _GameCanvasViewState extends State<GameCanvasView> with SingleTickerProviderStateMixin {
  late Ticker _ticker;
  double _lastTime = 0;
  var _frameTick = 0;

  @override
  void initState() {
    super.initState();
    _ticker = createTicker(_onTick);
    _ticker.start();
  }

  void _onTick(Duration elapsed) {
    final now = elapsed.inMilliseconds / 1000.0;
    if (_lastTime == 0) _lastTime = now;
    final dt = (now - _lastTime).clamp(0.001, 0.05);
    _lastTime = now;

    widget.engine.update(dt);
    _frameTick++;
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _ticker.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return KeyboardListener(
      focusNode: FocusNode()..requestFocus(),
      onKeyEvent: (event) {
        if (widget.engine.state != GameScreenState.playing) return;
        if (event is KeyDownEvent) {
          switch (event.logicalKey) {
            case LogicalKeyboardKey.arrowLeft:
            case LogicalKeyboardKey.keyA:
              widget.engine.handleSwipe(-20);
              break;
            case LogicalKeyboardKey.arrowRight:
            case LogicalKeyboardKey.keyD:
              widget.engine.handleSwipe(20);
              break;
            case LogicalKeyboardKey.space:
              widget.engine.handleTapBell();
              break;
            case LogicalKeyboardKey.escape:
            case LogicalKeyboardKey.keyP:
              widget.onPauseClicked();
              break;
          }
        }
      },
      child: GestureDetector(
        onHorizontalDragUpdate: (details) {
          if (widget.engine.state == GameScreenState.playing) {
            widget.engine.handleSwipe(details.primaryDelta ?? 0);
          }
        },
        onDoubleTap: () {
          if (widget.engine.state == GameScreenState.playing) {
            widget.engine.handleTapBell();
          }
        },
        child: CustomPaint(
          size: Size.infinite,
          painter: _GamePainter(engine: widget.engine, frameTick: _frameTick),
          child: _buildOverlay(),
        ),
      ),
    );
  }

  Widget _buildOverlay() {
    if (widget.engine.state != GameScreenState.playing) {
      if (widget.engine.state == GameScreenState.paused) {
        return _PauseOverlay(engine: widget.engine, onPauseClicked: widget.onPauseClicked);
      }
      return const SizedBox.shrink();
    }

    return Stack(
      children: [
        Positioned(
          top: 8,
          right: 8,
          child: Row(
            children: [
              _buildMusicButton(),
              const SizedBox(width: 8),
              _buildPauseButton(),
            ],
          ),
        ),
        Positioned(
          bottom: 16,
          left: 16,
          right: 16,
          child: Row(
            children: [
              Expanded(
                child: _buildControlButton("LEFT", () => widget.engine.handleSwipe(-20)),
              ),
              const SizedBox(width: 10),
              _buildBellButton(),
              const SizedBox(width: 10),
              Expanded(
                child: _buildControlButton("RIGHT", () => widget.engine.handleSwipe(20)),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildMusicButton() {
    final isOn = widget.engine.audioDirector.bgmEnabled;
    return GestureDetector(
      onTap: () => widget.engine.audioDirector.bgmEnabled = !isOn,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: Palette.ink,
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: isOn ? Palette.gold : Palette.ash, width: 2),
        ),
        child: Text(
          isOn ? "MUSIC" : "MUTED",
          style: const TextStyle(color: Palette.paper, fontSize: 12, fontFamily: 'monospace', fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildPauseButton() {
    return GestureDetector(
      onTap: widget.onPauseClicked,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
          color: Palette.ink,
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: Palette.coral, width: 2),
        ),
        child: const Text(
          "PAUSE",
          style: TextStyle(color: Palette.paper, fontSize: 12, fontFamily: 'monospace', fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildControlButton(String label, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 56,
        decoration: BoxDecoration(
          color: Palette.maroon,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Palette.amber, width: 2),
        ),
        child: Center(
          child: Text(
            label == "LEFT" ? "<< LEFT" : "RIGHT >>",
            style: const TextStyle(color: Palette.gold, fontSize: 16, fontFamily: 'monospace', fontWeight: FontWeight.bold),
          ),
        ),
      ),
    );
  }

  Widget _buildBellButton() {
    return GestureDetector(
      onTap: widget.engine.handleTapBell,
      child: Container(
        width: 70,
        height: 56,
        decoration: BoxDecoration(
          color: Palette.leaf,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Palette.gold, width: 2),
        ),
        child: const Center(
          child: Text("🔔", style: TextStyle(fontSize: 20)),
        ),
      ),
    );
  }
}

class _GamePainter extends CustomPainter {
  final GameEngine engine;
  final int frameTick;

  _GamePainter({required this.engine, required this.frameTick});

  @override
  void paint(Canvas canvas, Size size) {
    final deviceW = size.width;
    final deviceH = size.height;

    final scaleFloat = (min(deviceW / Constants.virtualWidth, deviceH / Constants.virtualHeight)).floorToDouble().clamp(1.0, double.infinity);
    final scale = scaleFloat.toInt();
    final virtualDrawW = Constants.virtualWidth * scale;
    final virtualDrawH = Constants.virtualHeight * scale;
    final letterboxX = ((deviceW - virtualDrawW) / 2).toInt();
    final letterboxY = ((deviceH - virtualDrawH) / 2).toInt();

    final bgPaint = Paint()..color = Palette.voidBlack;
    canvas.drawRect(Rect.fromLTWH(0, 0, deviceW, deviceH), bgPaint);

    canvas.save();
    canvas.translate((letterboxX + engine.shakeOffsetX * scale).toDouble(), (letterboxY + engine.shakeOffsetY * scale).toDouble());
    canvas.clipRect(Rect.fromLTWH(0, 0, virtualDrawW, virtualDrawH));

    _drawParallaxBackground(canvas, scale);
    _drawRoadAndKerbs(canvas, scale);
    _drawEntities(canvas, scale);
    _drawWeatherAndAtmosphere(canvas, scale);
    _drawHud(canvas, scale);

    canvas.restore();
  }

  void _drawParallaxBackground(Canvas canvas, int scale) {
    final distance = engine.distanceMeters;
    final skyColor = engine.currentZone == GameZone.nightStreet
        ? Palette.voidBlack
        : engine.currentZone == GameZone.rainyRoad
            ? Palette.slate
            : engine.currentZone == GameZone.oldDhaka
                ? Palette.plum
                : Palette.navy;

    final paint = Paint()..color = skyColor;
    canvas.drawRect(Rect.fromLTWH(0, 0, Constants.virtualWidth * scale, 40 * scale), paint);

    final skyScroll = ((distance * 0.2) % 64).toInt();
    final bldgColor = engine.currentZone == GameZone.nightStreet ? Palette.ink : Palette.shadow;
    final bldgPaint = Paint()..color = bldgColor;

    for (var x = -64; x <= 180; x += 32) {
      final drawX = (x - skyScroll) * scale;
      canvas.drawRect(Rect.fromLTWH(drawX.toDouble(), 10 * scale, 24 * scale, 30 * scale), bldgPaint);
    }
  }

  void _drawRoadAndKerbs(Canvas canvas, int scale) {
    final scrollY = ((engine.distanceMeters * 6) % 16).toInt();
    final kerbPaint = Paint()..color = Palette.stone;
    canvas.drawRect(Rect.fromLTWH(0, 0, Constants.kerbLeftX * scale, Constants.virtualHeight * scale), kerbPaint);
    canvas.drawRect(Rect.fromLTWH(Constants.kerbRightX * scale, 0, Constants.virtualWidth * scale, Constants.virtualHeight * scale), kerbPaint);

    final roadColor = engine.currentZone == GameZone.villageRoad
        ? Palette.soil
        : engine.currentZone == GameZone.rainyRoad
            ? Palette.ink
            : Palette.shadow;
    final roadPaint = Paint()..color = roadColor;
    canvas.drawRect(Rect.fromLTWH(Constants.kerbLeftX * scale, 0, Constants.roadWidth * scale, Constants.virtualHeight * scale), roadPaint);

    final linePaint = Paint()..color = Palette.amber;
    for (var y = -16; y <= 320; y += 16) {
      final drawY = (y + scrollY) * scale;
      canvas.drawRect(Rect.fromLTWH(70 * scale, drawY.toDouble(), 2 * scale, 8 * scale), linePaint);
      canvas.drawRect(Rect.fromLTWH(110 * scale, drawY.toDouble(), 2 * scale, 8 * scale), linePaint);
    }
  }

  void _drawEntities(Canvas canvas, int scale) {
    final frameIndex = (frameTick ~/ 6) % 4;

    for (final coin in engine.pools.coinPool) {
      if (!coin.isActive || coin.isCollected) continue;
      final sprite = PixelSpriteRenderer.getCoinSprite(frameIndex);
        coin.x * scale,
        coin.y * scale,
        8 * scale,
        8 * scale,
      );
      canvas.drawPicture(sprite);
    }

    for (final pup in engine.pools.powerUpPool) {
      if (!pup.isActive) continue;
      final sprite = PixelSpriteRenderer.getPowerUpSprite(pup.type.name.toLowerCase(), frameIndex);
      canvas.drawPicture(sprite);
    }

    for (final obs in engine.pools.obstaclePool) {
      if (!obs.isActive) continue;
      final sprite = PixelSpriteRenderer.getObstacleSprite(obs.type, frameIndex);
      canvas.drawPicture(sprite);
    }

    for (final ped in engine.pools.pedestrianPool) {
      if (!ped.isActive) continue;
      final sprite = PixelSpriteRenderer.getPedestrianSprite(frameIndex, ped.isDodging);
      final drawX = ped.isDodging ? ped.x + ped.dodgeOffset : ped.x;
      canvas.drawPicture(sprite);
    }

    for (final pass in engine.pools.passengerPool) {
      if (!pass.isActive) continue;
      final sprite = PixelSpriteRenderer.getPassengerSprite(pass.typeIndex, frameIndex);
      canvas.drawPicture(sprite);
    }

    final player = engine.player;
    final leanDir = player.isChangingLane ? (player.targetX > player.x ? 1 : -1) : 0;

    if (player.isTurbo) {
      final ghostPaint = Paint()..color = Palette.shadow;
      for (var g = 1; g <= 3; g++) {
        canvas.drawRect(
          Rect.fromLTWH(
            player.x * scale,
            (player.y + g * 8) * scale,
            Constants.rickshawWidth * scale,
            8 * scale,
          ),
          ghostPaint,
        );
      }
    }

    final isBlinking = player.invincibleTimerSec > 0 && ((player.invincibleTimerSec * 15).toInt() % 2 == 0);
    if (!isBlinking) {
      final rickshawSprite = PixelSpriteRenderer.getRickshawFrame(frameIndex, player.isTurbo, leanDir);
        player.x * scale,
        player.y * scale,
        Constants.rickshawWidth * scale,
        Constants.rickshawHeight * scale,
      );
      canvas.drawPicture(rickshawSprite);
    }

    if (player.isShielded) {
      final shieldPaint = Paint()..color = Palette.cyan;
      canvas.drawRect(
        Rect.fromLTWH(
          (player.x - 2) * scale,
          (player.y - 2) * scale,
          (Constants.rickshawWidth + 4) * scale,
          (Constants.rickshawHeight + 4) * scale,
        ),
        shieldPaint,
      );
    }

    for (final ft in engine.pools.floatingTextPool) {
      if (!ft.isActive) continue;
      PixelSpriteRenderer.drawBitmapText(
        canvas,
        ft.text,
        (ft.x * scale).toInt(),
        (ft.y * scale).toInt(),
        colorArgb: ft.colorArgb,
        scale: scale,
        useBanglaDigits: engine.useBanglaDigits,
      );
    }
  }

  void _drawWeatherAndAtmosphere(Canvas canvas, int scale) {
    switch (engine.currentZone) {
      case GameZone.rainyRoad:
        final rainPaint = Paint()..color = Palette.ash;
        final rainSeed = (engine.survivalTimeSec * 60).toInt();
        for (var i = 0; i < 18; i++) {
          final rx = ((i * 11 + rainSeed * 7) % 180) * scale;
          final ry = ((i * 19 + rainSeed * 13) % 320) * scale;
          canvas.drawRect(Rect.fromLTWH(rx.toDouble(), ry.toDouble(), scale.toDouble(), 3 * scale), rainPaint);
        }
        break;
      case GameZone.nightStreet:
        final headPaint = Paint()..color = Palette.cream;
        final px = engine.player.x + 12;
        final py = engine.player.y;
        canvas.drawRect(
          Rect.fromLTWH(
            (px - 10) * scale,
            (py - 40) * scale,
            20 * scale,
            40 * scale,
          ),
          headPaint,
        );
        break;
      default:
        break;
    }
  }

  void _drawHud(Canvas canvas, int scale) {
    final inkPaint = Paint()..color = Palette.ink;
    canvas.drawRect(Rect.fromLTWH(0, 0, Constants.virtualWidth * scale, 24 * scale), inkPaint);

    final amberPaint = Paint()..color = Palette.amber;
    canvas.drawRect(Rect.fromLTWH(0, 23 * scale, Constants.virtualWidth * scale, 1 * scale), amberPaint);

    PixelSpriteRenderer.drawBitmapText(
      canvas,
      "${engine.distanceMeters.toInt()}m",
      6 * scale,
      6 * scale,
      colorArgb: Palette.paper.value,
      scale: scale,
    );

    final livesCount = max(0, engine.player.lives);
    PixelSpriteRenderer.drawBitmapText(
      canvas,
      "L:$livesCount",
      68 * scale,
      6 * scale,
      colorArgb: Palette.coral.value,
      scale: scale,
    );

    PixelSpriteRenderer.drawBitmapText(
      canvas,
      "Taka${engine.moneyTaka}",
      110 * scale,
      6 * scale,
      colorArgb: Palette.gold.value,
      scale: scale,
      useBanglaDigits: engine.useBanglaDigits,
    );

    PixelSpriteRenderer.drawBitmapText(
      canvas,
      "[||]",
      160 * scale,
      6 * scale,
      colorArgb: Palette.coral.value,
      scale: scale,
    );

    if (engine.comboCount > 1) {
      PixelSpriteRenderer.drawBitmapText(
        canvas,
        "x${engine.comboCount}",
        6 * scale,
        296 * scale,
        colorArgb: Palette.gold.value,
        scale: scale,
      );
    }
  }

  @override
  bool shouldRepaint(_GamePainter oldDelegate) {
    return oldDelegate.frameTick != frameTick ||
        oldDelegate.engine.state != engine.state ||
        oldDelegate.engine.distanceMeters != engine.distanceMeters;
  }
}

class _PauseOverlay extends StatelessWidget {
  final GameEngine engine;
  final VoidCallback onPauseClicked;

  const _PauseOverlay({required this.engine, required this.onPauseClicked});

  @override
  Widget build(BuildContext context) {
    return Container(
      color: Palette.voidBlack.withAlpha(217),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text("PAUSED", style: TextStyle(color: Palette.gold, fontSize: 28, fontFamily: 'monospace', fontWeight: FontWeight.w900)),
            const SizedBox(height: 12),
            _buildButton("RESUME", Palette.amber, Palette.ink, engine.resumeGame),
            const SizedBox(height: 10),
            _buildButton(engine.audioDirector.bgmEnabled ? "MUTE MUSIC" : "UNMUTE MUSIC", Palette.slate, Palette.paper, () {
              engine.audioDirector.bgmEnabled = !engine.audioDirector.bgmEnabled;
            }),
            const SizedBox(height: 10),
            _buildButton("QUIT TO MENU", Palette.coral, Palette.paper, engine.goToHome),
          ],
        ),
      ),
    );
  }

  Widget _buildButton(String text, Color bg, Color textColor, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: Palette.ink, width: 2),
        ),
        child: Text(text, style: TextStyle(color: textColor, fontSize: 16, fontFamily: 'monospace', fontWeight: FontWeight.bold)),
      ),
    );
  }
}
