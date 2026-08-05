import 'dart:math';
import '../core/constants.dart';
import '../entities/game_entities.dart';

class SpawnDirector {
  final Random rng = Random();
  double lastObstacleY = -100;
  int lastPowerUpTimeMs = DateTime.now().millisecondsSinceEpoch;
  int nextPowerUpIntervalMs = Random().nextInt(8000) + 18000;
  int playerCurrentLane = 1;
  int playerLaneEntryTimeMs = DateTime.now().millisecondsSinceEpoch;

  void reset() {
    lastObstacleY = -100;
    lastPowerUpTimeMs = DateTime.now().millisecondsSinceEpoch;
    playerCurrentLane = 1;
    playerLaneEntryTimeMs = DateTime.now().millisecondsSinceEpoch;
  }

  void onPlayerLaneChanged(int newLane) {
    if (newLane != playerCurrentLane) {
      playerCurrentLane = newLane;
      playerLaneEntryTimeMs = DateTime.now().millisecondsSinceEpoch;
    }
  }

  SpawnRowDecision? shouldSpawnObstacleRow(double currentY, double scrollSpeed, int tierMaxLanesBlocked) {
    final minGap = 50 + (scrollSpeed * 0.15);
    if (currentY - lastObstacleY < minGap) {
      return null;
    }

    final blockedLanes = <int>[];
    final lanesToBlockCount = (tierMaxLanesBlocked >= 2 && rng.nextDouble() < 0.6) ? 2 : 1;

    final timeInLaneSec = (DateTime.now().millisecondsSinceEpoch - playerLaneEntryTimeMs) / 1000.0;
    final protectedLane = timeInLaneSec < 0.35 ? playerCurrentLane : -1;

    final availableLanes = List.generate(Constants.laneCount, (i) => i).where((i) => i != protectedLane).toList();
    availableLanes.shuffle(rng);

    for (var i = 0; i < min(lanesToBlockCount, 2); i++) {
      if (availableLanes.isNotEmpty) {
        blockedLanes.add(availableLanes.removeAt(0));
      }
    }

    final openLanes = List.generate(Constants.laneCount, (i) => i).where((i) => !blockedLanes.contains(i)).toList();
    if (openLanes.isEmpty && blockedLanes.isNotEmpty) {
      blockedLanes.removeAt(0);
    }

    lastObstacleY = currentY;
    final obstacleType = obstacleTypes[rng.nextInt(obstacleTypes.length)];

    return SpawnRowDecision(blockedLanes, obstacleType);
  }

  List<CoinSpawnInfo> generateCoinPattern(double startY) {
    final count = rng.nextInt(5) + 5;
    final coins = <CoinSpawnInfo>[];
    final startLane = rng.nextInt(Constants.laneCount);
    final endLane = rng.nextBool() ? (startLane + 1) % Constants.laneCount : (startLane + 2) % Constants.laneCount;

    const kerbYGap = 16.0;

    for (var i = 0; i < count; i++) {
      final progress = i / (count - 1);
      final laneFloat = startLane + (endLane - startLane) * progress;
      final laneIndex = laneFloat.toInt().clamp(0, Constants.laneCount - 1);
      final coinX = Constants.laneCenters[laneIndex] - 4;
      final coinY = startY - (i * kerbYGap);
      coins.add(CoinSpawnInfo(coinX, coinY));
    }

    return coins;
  }

  PowerUpType? checkPowerUpSpawn() {
    final now = DateTime.now().millisecondsSinceEpoch;
    if (now - lastPowerUpTimeMs >= nextPowerUpIntervalMs) {
      lastPowerUpTimeMs = now;
      nextPowerUpIntervalMs = Random().nextInt(8000) + 18000;

      final roll = rng.nextDouble();
      if (roll < 0.35) return PowerUpType.moneyMagnet;
      if (roll < 0.65) return PowerUpType.shield;
      if (roll < 0.85) return PowerUpType.turbo;
      return PowerUpType.moneyRain;
    }
    return null;
  }
}

class SpawnRowDecision {
  final List<int> blockedLanes;
  final String obstacleType;

  SpawnRowDecision(this.blockedLanes, this.obstacleType);
}

class CoinSpawnInfo {
  final double x;
  final double y;

  CoinSpawnInfo(this.x, this.y);
}

const obstacleTypes = ["manhole", "garbage", "stone", "barrier", "puddle"];
