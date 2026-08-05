package com.example.game

class DifficultyManager {

    data class DifficultyTier(
        val tierIndex: Int,
        val scrollSpeed: Float,
        val obstacleIntervalSec: Float,
        val maxLanesBlocked: Int,
        val pedestriansPerSec: Float
    )

    fun getTierForTime(survivalTimeSec: Float): DifficultyTier {
        return when {
            survivalTimeSec < 20f -> DifficultyTier(0, 55f, 1.60f, 1, 0.15f)
            survivalTimeSec < 40f -> DifficultyTier(1, 62f, 1.45f, 1, 0.20f)
            survivalTimeSec < 60f -> DifficultyTier(2, 69f, 1.30f, 2, 0.25f)
            survivalTimeSec < 80f -> DifficultyTier(3, 90f, 1.15f, 2, 0.30f)
            survivalTimeSec < 100f -> DifficultyTier(4, 111f, 1.00f, 2, 0.35f)
            survivalTimeSec < 120f -> DifficultyTier(5, 132f, 0.88f, 2, 0.40f)
            survivalTimeSec < 140f -> DifficultyTier(6, 153f, 0.78f, 2, 0.45f)
            survivalTimeSec < 160f -> DifficultyTier(7, 174f, 0.68f, 2, 0.50f)
            else -> DifficultyTier(8, 190f, 0.62f, 2, 0.50f)
        }
    }
}
