import '../core/palette.dart';

class HitboxRect {
  final double x;
  final double y;
  final double w;
  final double h;

  HitboxRect(this.x, this.y, this.w, this.h);

  bool intersects(HitboxRect other) {
    return x < other.x + other.w &&
        x + w > other.x &&
        y < other.y + other.h &&
        y + h > other.y;
  }
}

class RickshawPlayer {
  int lane = 1;
  late double x;
  late double y;
  late double startX;
  late double targetX;
  bool isChangingLane = false;
  double laneChangeProgress = 1;

  int lives = 3;
  int maxLives = 3;
  double invincibleTimerSec = 0;

  bool isTurbo = false;
  double turboTimeSec = 0;

  bool isShielded = false;

  bool isMagnetActive = false;
  double magnetTimeSec = 0;

  int nearMissCount = 0;

  RickshawPlayer() {
    _initPosition();
  }

  void _initPosition() {
    final laneCenters = [50.0, 90.0, 130.0];
    x = laneCenters[1] - 16;
    y = 232;
    startX = x;
    targetX = x;
  }

  void startLaneChange(int newLane) {
    final laneCenters = [50.0, 90.0, 130.0];
    if (newLane >= 0 && newLane < 3 && newLane != lane) {
      lane = newLane;
      startX = x;
      targetX = laneCenters[lane] - 16;
      isChangingLane = true;
      laneChangeProgress = 0;
    }
  }

  void update(double dt) {
    if (isChangingLane) {
      laneChangeProgress += dt / 0.14;
      if (laneChangeProgress >= 1) {
        laneChangeProgress = 1;
        isChangingLane = false;
        x = targetX;
      } else {
        x = startX + (targetX - startX) * laneChangeProgress;
      }
    }

    if (isTurbo) {
      turboTimeSec -= dt;
      if (turboTimeSec <= 0) {
        isTurbo = false;
        turboTimeSec = 0;
      }
    }

    if (isMagnetActive) {
      magnetTimeSec -= dt;
      if (magnetTimeSec <= 0) {
        isMagnetActive = false;
        magnetTimeSec = 0;
      }
    }

    if (invincibleTimerSec > 0) {
      invincibleTimerSec -= dt;
      if (invincibleTimerSec < 0) invincibleTimerSec = 0;
    }
  }

  HitboxRect getHitbox() {
    return HitboxRect(x + 6, y + 16, 20, 24);
  }
}

class ObstacleEntity {
  double x = 0;
  double y = 0;
  double width = 16;
  double height = 16;
  String type = "manhole";
  bool isActive = false;

  HitboxRect getHitbox() {
    return HitboxRect(x + 1, y + 2, width - 2, height - 3);
  }
}

class CoinEntity {
  double x = 0;
  double y = 0;
  bool isActive = false;
  bool isCollected = false;

  HitboxRect getHitbox() {
    return HitboxRect(x, y, 8, 8);
  }
}

class PowerUpEntity {
  double x = 0;
  double y = 0;
  PowerUpType type = PowerUpType.turbo;
  bool isActive = false;

  HitboxRect getHitbox() {
    return HitboxRect(x, y, 16, 16);
  }
}

class PassengerEntity {
  double x = 0;
  double y = 0;
  bool isLeftKerb = true;
  int typeIndex = 0;
  bool isPickedUp = false;
  int dropoffTargetMeter = 0;
  bool isActive = false;

  HitboxRect getHitbox() {
    return HitboxRect(x, y, 16, 24);
  }
}

class PedestrianEntity {
  double x = 0;
  double y = 0;
  bool isDodging = false;
  double dodgeOffset = 0;
  bool isActive = false;

  HitboxRect getHitbox() {
    final curX = isDodging ? x + dodgeOffset : x;
    return HitboxRect(curX + 3, y + 4, 10, 16);
  }
}

class PixelParticleEntity {
  double x = 0;
  double y = 0;
  double vx = 0;
  double vy = 0;
  int colorStepIndex = 0;
  double life = 0;
  double maxLife = 0.5;
  bool isActive = false;
}

class FloatingTextEntity {
  String text = "";
  double x = 0;
  double y = 0;
  double life = 0;
  double maxLife = 0.6;
  int colorArgb = 0;
  bool isActive = false;
}
