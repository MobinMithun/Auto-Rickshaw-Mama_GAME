import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import '../core/constants.dart';
import '../core/palette.dart';
import '../entities/game_entities.dart';
import '../entities/object_pools.dart';
import '../game/audio_director.dart';
import '../game/difficulty_manager.dart';
import '../game/spawn_director.dart';

enum GameScreenState {
  home,
  playing,
  paused,
  crashing,
  gameOver,
  leaderboard,
  achievements,
  settings,
}

class GameEngine extends ChangeNotifier {
  final AudioDirector audioDirector;

  GameEngine(this.audioDirector);

  var state = GameScreenState.home;

  final player = RickshawPlayer();
  final pools = ObjectPools();
  final spawnDirector = SpawnDirector();
  final difficultyManager = DifficultyManager();

  double survivalTimeSec = 0;
  double distanceMeters = 0;
  int moneyTaka = 0;
  int comboCount = 0;
  double comboTimerSec = 0;
  int maxCombo = 0;
  GameZone currentZone = GameZone.gulistan;

  int shakeOffsetX = 0;
  int shakeOffsetY = 0;
  double shakeTimerSec = 0;

  double crashAnimProgress = 0;
  int wipeColumnIndex = -1;

  PassengerEntity? currentPassenger;

  int lastThousandCoins = 0;
  double obstacleSpawnYTracker = 0;

  bool useBanglaDigits = true;
  bool showFpsOverlay = false;

  void startNewGame() {
    pools.resetAll();
    player.x = Constants.laneCenters[1] - (Constants.rickshawWidth / 2);
    player.lane = 1;
    player.lives = 3;
    player.invincibleTimerSec = 0;
    player.isTurbo = false;
    player.isShielded = false;
    player.isMagnetActive = false;
    player.nearMissCount = 0;

    survivalTimeSec = 0;
    distanceMeters = 0;
    moneyTaka = 0;
    lastThousandCoins = 0;
    obstacleSpawnYTracker = 0;
    spawnDirector.reset();
    comboCount = 0;
    comboTimerSec = 0;
    maxCombo = 0;
    currentZone = GameZone.gulistan;
    currentPassenger = null;
    crashAnimProgress = 0;
    wipeColumnIndex = -1;

    state = GameScreenState.playing;
    audioDirector.triggerVoice("abar_suru");
    notifyListeners();
  }

  void handleSwipe(double deltaX) {
    if (state != GameScreenState.playing) return;
    if (deltaX > 18 && player.lane < Constants.laneCount - 1) {
      final newLane = player.lane + 1;
      player.startLaneChange(newLane);
      spawnDirector.onPlayerLaneChanged(newLane);
      audioDirector.playSfx("near_miss");
    } else if (deltaX < -18 && player.lane > 0) {
      final newLane = player.lane - 1;
      player.startLaneChange(newLane);
      spawnDirector.onPlayerLaneChanged(newLane);
      audioDirector.playSfx("near_miss");
    }
  }

  void handleTapBell() {
    if (state != GameScreenState.playing) return;
    audioDirector.playSfx("bell_ting");

    for (final ped in pools.pedestrianPool) {
      if (ped.isActive && ped.y >= player.y - 40 && ped.y <= player.y + 40) {
        ped.isDodging = true;
        ped.dodgeOffset = ped.x < player.x ? -6 : 6;
      }
    }
  }

  void pauseGame() {
    if (state == GameScreenState.playing) {
      state = GameScreenState.paused;
      notifyListeners();
    }
  }

  void resumeGame() {
    if (state == GameScreenState.paused) {
      state = GameScreenState.playing;
      notifyListeners();
    }
  }

  void goToHome() {
    state = GameScreenState.home;
    notifyListeners();
  }

  void goToLeaderboard() {
    state = GameScreenState.leaderboard;
    notifyListeners();
  }

  void goToAchievements() {
    state = GameScreenState.achievements;
    notifyListeners();
  }

  void goToSettings() {
    state = GameScreenState.settings;
    notifyListeners();
  }

  void update(double dt) {
    if (state != GameScreenState.playing && state != GameScreenState.crashing) return;

    if (state == GameScreenState.crashing) {
      _updateCrashSequence(dt);
      return;
    }

    survivalTimeSec += dt;
    final tier = difficultyManager.getTierForTime(survivalTimeSec);

    final thousandCoinsCount = moneyTaka ~/ 1000;
    if (thousandCoinsCount > lastThousandCoins) {
      lastThousandCoins = thousandCoinsCount;
      addReward(0, "SPEED BOOST!");
      audioDirector.playSfx("turbo_boost");
    }

    final coinSpeedBonus = thousandCoinsCount * 14;
    var currentSpeed = tier.scrollSpeed + coinSpeedBonus;
    if (player.isTurbo) {
      currentSpeed *= Constants.turboSpeedMultiplier;
    }

    distanceMeters += currentSpeed * dt * Constants.metersPerPixel;

    final zoneIndex = ((distanceMeters / Constants.zoneIntervalMeters).toInt()) % GameZone.values.length;
    currentZone = GameZone.values[zoneIndex];

    player.update(dt);

    if (comboCount > 0) {
      comboTimerSec -= dt;
      if (comboTimerSec <= 0) {
        comboCount = 0;
      }
    }

    if (player.isMagnetActive) {
      for (final coin in pools.coinPool) {
        if (coin.isActive && !coin.isCollected) {
          final dx = player.x + 12 - coin.x;
          final dy = player.y + 16 - coin.y;
          final distSq = dx * dx + dy * dy;
          if (distSq < 48 * 48) {
            coin.x += dx / 4;
            coin.y += dy / 4;
          }
        }
      }
    }

    _updateSpawning(dt, currentSpeed, tier.maxLanesBlocked);
    _updateEntitiesAndCollisions(dt, currentSpeed);
  }

  void _updateSpawning(double dt, double currentSpeed, int maxBlockedLanes) {
    obstacleSpawnYTracker += currentSpeed * dt;

    final spawnDecision = spawnDirector.shouldSpawnObstacleRow(obstacleSpawnYTracker, currentSpeed, maxBlockedLanes);
    if (spawnDecision != null) {
      for (final blockedLane in spawnDecision.blockedLanes) {
        final obs = pools.obtainObstacle();
        if (obs != null) {
          obs.isActive = true;
          obs.x = Constants.laneCenters[blockedLane] - 8;
          obs.y = -30;
          obs.type = spawnDecision.obstacleType;
        }
      }
    }

    if (Random().nextDouble() < 0.02) {
      final pattern = spawnDirector.generateCoinPattern(-20);
      for (final info in pattern) {
        final coin = pools.obtainCoin();
        if (coin != null) {
          coin.isActive = true;
          coin.isCollected = false;
          coin.x = info.x;
          coin.y = info.y;
        }
      }
    }

    final pupType = spawnDirector.checkPowerUpSpawn();
    if (pupType != null) {
      final pup = pools.obtainPowerUp();
      if (pup != null) {
        pup.isActive = true;
        pup.type = pupType;
        final lane = Random().nextInt(Constants.laneCount);
        pup.x = Constants.laneCenters[lane] - 8;
        pup.y = -20;
      }
    }

    if (Random().nextDouble() < 0.015) {
      final ped = pools.obtainPedestrian();
      if (ped != null) {
        ped.isActive = true;
        ped.isDodging = false;
        ped.dodgeOffset = 0;
        final lane = Random().nextInt(Constants.laneCount);
        ped.x = Constants.laneCenters[lane] - 8;
        ped.y = -24;
      }
    }

    if (currentPassenger == null && Random().nextDouble() < 0.008) {
      final pass = pools.obtainPassenger();
      if (pass != null) {
        pass.isActive = true;
        pass.isLeftKerb = Random().nextBool();
        pass.x = pass.isLeftKerb ? 10 : 154;
        pass.y = -24;
        pass.typeIndex = Random().nextInt(6);
        pass.isPickedUp = false;
        pass.dropoffTargetMeter = distanceMeters.toInt() + Random().nextInt(500) + 400;
      }
    }
  }

  void _updateEntitiesAndCollisions(double dt, double currentSpeed) {
    final playerHitbox = player.getHitbox();

    for (final obs in pools.obstaclePool) {
      if (!obs.isActive) continue;
      obs.y += currentSpeed * dt;

      if (obs.getHitbox().intersects(playerHitbox)) {
        if (player.isTurbo) {
          obs.isActive = false;
          _spawnBurstParticles(obs.x + 8, obs.y + 8);
          addReward(10, "SMASH!");
        } else if (player.isShielded) {
          player.isShielded = false;
          obs.isActive = false;
          audioDirector.playSfx("near_miss");
          _spawnBurstParticles(obs.x + 8, obs.y + 8);
        } else if (player.invincibleTimerSec > 0) {
          // invincible
        } else {
          player.lives--;
          obs.isActive = false;
          _spawnBurstParticles(obs.x + 8, obs.y + 8, count: 16);
          shakeTimerSec = 0.35;
          audioDirector.playSfx("crash_boom");

          if (player.lives > 0) {
            player.invincibleTimerSec = 1.8;
            addReward(0, "-1 LIFE!");
            audioDirector.triggerVoice("are_baba");
          } else {
            _triggerCrash();
            return;
          }
        }
      }

      if (obs.y > Constants.virtualHeight + 32) obs.isActive = false;
    }

    for (final coin in pools.coinPool) {
      if (!coin.isActive || coin.isCollected) continue;
      coin.y += currentSpeed * dt;

      if (coin.getHitbox().intersects(playerHitbox)) {
        coin.isCollected = true;
        coin.isActive = false;
        addReward(20, "Taka20");
        audioDirector.playSfx("coin_ching");
      }

      if (coin.y > Constants.virtualHeight + 16) coin.isActive = false;
    }

    for (final pup in pools.powerUpPool) {
      if (!pup.isActive) continue;
      pup.y += currentSpeed * dt;

      if (pup.getHitbox().intersects(playerHitbox)) {
        pup.isActive = false;
        audioDirector.playSfx("turbo_boost");
        switch (pup.type) {
          case PowerUpType.turbo:
            player.isTurbo = true;
            player.turboTimeSec = pup.type.durationSec;
            audioDirector.triggerVoice("bachao");
          case PowerUpType.moneyMagnet:
            player.isMagnetActive = true;
            player.magnetTimeSec = pup.type.durationSec;
          case PowerUpType.shield:
            player.isShielded = true;
          case PowerUpType.moneyRain:
            _spawnMoneyRain();
        }
      }

      if (pup.y > Constants.virtualHeight + 16) pup.isActive = false;
    }

    for (final pass in pools.passengerPool) {
      if (!pass.isActive) continue;
      pass.y += currentSpeed * dt;

      if (!pass.isPickedUp && pass.getHitbox().intersects(playerHitbox)) {
        pass.isPickedUp = true;
        pass.isActive = false;
        currentPassenger = pass;
        addReward(50, "PICKUP!");
        audioDirector.playSfx("pickup_ting");
        audioDirector.triggerVoice("vada_ache");
      }

      if (pass.y > Constants.virtualHeight + 24) pass.isActive = false;
    }

    if (currentPassenger != null && distanceMeters >= currentPassenger!.dropoffTargetMeter) {
      addReward(200, "DROPOFF!");
      audioDirector.playSfx("dropoff_chime");
      currentPassenger = null;
    }

    for (final ped in pools.pedestrianPool) {
      if (!ped.isActive) continue;
      ped.y += currentSpeed * dt;

      final pedHitbox = ped.getHitbox();
      if (pedHitbox.intersects(playerHitbox)) {
        ped.isActive = false;
        addReward(100, "+100 PTS!");
        audioDirector.playSfx("coin_ching");
        audioDirector.triggerVoice("mama_side_den");
        _spawnBurstParticles(ped.x + 8, ped.y + 12, count: 12);
      } else {
        final distY = (ped.y - player.y).abs();
        if (distY < 6 && !ped.isDodging) {
          ped.isDodging = true;
          ped.dodgeOffset = ped.x < player.x ? -8 : 8;
          player.nearMissCount++;
          addReward(10, "NEAR MISS!");
          audioDirector.playSfx("near_miss");
          audioDirector.triggerVoice("mama_side_den");
        }
      }

      if (ped.y > Constants.virtualHeight + 24) ped.isActive = false;
    }

    _updateParticles(dt);
    _updateFloatingText(dt);
  }

  void addReward(int baseTaka, String label) {
    comboCount++;
    comboTimerSec = 3.0;
    if (comboCount > maxCombo) maxCombo = comboCount;

    final multiplier = comboCount >= 20
        ? 5
        : comboCount >= 10
            ? 3
            : comboCount >= 5
                ? 2
                : 1;

    final total = baseTaka * multiplier;
    moneyTaka += total;

    final ft = pools.obtainFloatingText();
    if (ft != null) {
      ft.isActive = true;
      ft.text = multiplier > 1 ? "$label x$multiplier" : label;
      ft.x = player.x;
      ft.y = player.y - 10;
      ft.life = 0;
      ft.maxLife = 0.6;
      final color = multiplier == 5
          ? Palette.gold
          : multiplier == 3
              ? Palette.amber
              : multiplier == 2
                  ? Palette.orange
                  : Palette.cream;
      ft.colorArgb = color.toARGB32();
    }
  }

  void _triggerCrash() {
    player.lives = 0;
    state = GameScreenState.crashing;
    audioDirector.playSfx("crash_boom");
    audioDirector.triggerVoice("are_baba");

    _spawnBurstParticles(player.x + 16, player.y + 20, count: 24);
    shakeTimerSec = 0.35;
    notifyListeners();
  }

  void _updateCrashSequence(double dt) {
    crashAnimProgress += dt;
    if (shakeTimerSec > 0) {
      shakeTimerSec -= dt;
      shakeOffsetX = Random().nextInt(7) - 3;
      shakeOffsetY = Random().nextInt(7) - 3;
    } else {
      shakeOffsetX = 0;
      shakeOffsetY = 0;
    }

    if (crashAnimProgress >= 0.8) {
      state = GameScreenState.gameOver;
      notifyListeners();
    }
  }

  void _spawnBurstParticles(double cx, double cy, {int count = 16}) {
    for (var i = 0; i < count; i++) {
      final p = pools.obtainParticle();
      if (p == null) break;
      p.isActive = true;
      p.x = cx;
      p.y = cy;
      p.vx = (Random().nextInt(61) - 30).toDouble();
      p.vy = (Random().nextInt(61) - 40).toDouble();
      p.life = 0;
      p.maxLife = 0.5;
      p.colorStepIndex = 0;
    }
  }

  void _spawnMoneyRain() {
    for (var i = 0; i < 12; i++) {
      final coin = pools.obtainCoin();
      if (coin == null) break;
      coin.isActive = true;
      coin.isCollected = false;
      final minX = Constants.kerbLeftX + 10;
      final maxX = Constants.kerbRightX - 10;
      coin.x = minX + Random().nextDouble() * (maxX - minX);
      coin.y = -((i * 15) + 10);
    }
  }

  void _updateParticles(double dt) {
    for (final p in pools.particlePool) {
      if (!p.isActive) continue;
      p.life += dt;
      if (p.life >= p.maxLife) {
        p.isActive = false;
      } else {
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        p.colorStepIndex = (p.life / p.maxLife * 4).toInt().clamp(0, 3);
      }
    }
  }

  void _updateFloatingText(double dt) {
    for (final ft in pools.floatingTextPool) {
      if (!ft.isActive) continue;
      ft.life += dt;
      if (ft.life >= ft.maxLife) {
        ft.isActive = false;
      } else {
        ft.y -= 12 * dt;
      }
    }
  }
}
