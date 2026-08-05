class DifficultyManager {
  DifficultyTier getTierForTime(double survivalTimeSec) {
    if (survivalTimeSec < 20) {
      return DifficultyTier(0, 55, 1.60, 1, 0.15);
    } else if (survivalTimeSec < 40) {
      return DifficultyTier(1, 62, 1.45, 1, 0.20);
    } else if (survivalTimeSec < 60) {
      return DifficultyTier(2, 69, 1.30, 2, 0.25);
    } else if (survivalTimeSec < 80) {
      return DifficultyTier(3, 90, 1.15, 2, 0.30);
    } else if (survivalTimeSec < 100) {
      return DifficultyTier(4, 111, 1.00, 2, 0.35);
    } else if (survivalTimeSec < 120) {
      return DifficultyTier(5, 132, 0.88, 2, 0.40);
    } else if (survivalTimeSec < 140) {
      return DifficultyTier(6, 153, 0.78, 2, 0.45);
    } else if (survivalTimeSec < 160) {
      return DifficultyTier(7, 174, 0.68, 2, 0.50);
    } else {
      return DifficultyTier(8, 190, 0.62, 2, 0.50);
    }
  }
}

class DifficultyTier {
  final int tierIndex;
  final double scrollSpeed;
  final double obstacleIntervalSec;
  final int maxLanesBlocked;
  final double pedestriansPerSec;

  DifficultyTier(this.tierIndex, this.scrollSpeed, this.obstacleIntervalSec, this.maxLanesBlocked, this.pedestriansPerSec);
}
