package com.example.game

import com.example.core.Constants
import com.example.core.PowerUpType
import kotlin.random.Random

/**
 * The Spawn Director — Fairness Engine.
 * Guarantees a rolling safe corridor, readable coin arcs, kerb passengers, and fair obstacle placement.
 */
class SpawnDirector {

    private val rng = Random(System.currentTimeMillis())

    private var lastObstacleY = -100f
    private var lastPowerUpTimeMs = System.currentTimeMillis()
    private var nextPowerUpIntervalMs = (18000..26000).random().toLong()

    private var playerCurrentLane = 1
    private var playerLaneEntryTimeMs = System.currentTimeMillis()

    fun reset() {
        lastObstacleY = -100f
        lastPowerUpTimeMs = System.currentTimeMillis()
        playerCurrentLane = 1
        playerLaneEntryTimeMs = System.currentTimeMillis()
    }

    fun onPlayerLaneChanged(newLane: Int) {
        if (newLane != playerCurrentLane) {
            playerCurrentLane = newLane
            playerLaneEntryTimeMs = System.currentTimeMillis()
        }
    }

    /**
     * Safe Corridor Validator:
     * Guarantees at least 1 lane remains completely open for a valid traversable path.
     */
    fun shouldSpawnObstacleRow(
        currentY: Float,
        scrollSpeed: Float,
        tierMaxLanesBlocked: Int
    ): SpawnRowDecision? {
        val minGap = 50f + (scrollSpeed * 0.15f)
        if (currentY - lastObstacleY < minGap) {
            return null // Too close to previous row
        }

        val blockedLanes = mutableListOf<Int>()
        val lanesToBlockCount = if (tierMaxLanesBlocked >= 2 && rng.nextFloat() < 0.6f) 2 else 1

        // Rule 2: Do not spawn obstacle in the player's current lane if they entered < 0.35s ago
        val timeInLaneSec = (System.currentTimeMillis() - playerLaneEntryTimeMs) / 1000f
        val protectedLane = if (timeInLaneSec < 0.35f) playerCurrentLane else -1

        val availableLanes = (0 until Constants.LANE_COUNT).filter { it != protectedLane }.toMutableList()
        availableLanes.shuffle(rng)

        for (i in 0 until lanesToBlockCount.coerceAtMost(2)) { // Max 2 lanes blocked EVER
            if (availableLanes.isNotEmpty()) {
                blockedLanes.add(availableLanes.removeAt(0))
            }
        }

        // Rule 1: Validate at least 1 lane is ALWAYS traversable
        val openLanes = (0 until Constants.LANE_COUNT).filter { !blockedLanes.contains(it) }
        if (openLanes.isEmpty()) {
            // Unsafe! Force open at least one lane
            blockedLanes.removeAt(0)
        }

        lastObstacleY = currentY
        val obstacleType = OBSTACLE_TYPES[rng.nextInt(OBSTACLE_TYPES.size)]

        return SpawnRowDecision(blockedLanes, obstacleType)
    }

    /**
     * Generates a readable arc pattern of 5-9 Taka coins.
     */
    fun generateCoinPattern(startY: Float): List<CoinSpawnInfo> {
        val count = (5..9).random(rng)
        val coins = mutableListOf<CoinSpawnInfo>()
        val startLane = rng.nextInt(Constants.LANE_COUNT)
        val endLane = if (rng.nextBoolean()) (startLane + 1) % Constants.LANE_COUNT else (startLane + 2) % Constants.LANE_COUNT

        val kerbYGap = 16f

        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1)
            val laneFloat = startLane + (endLane - startLane) * progress
            val laneIndex = laneFloat.toInt().coerceIn(0, Constants.LANE_COUNT - 1)
            val coinX = Constants.LANE_CENTERS[laneIndex] - 4f
            val coinY = startY - (i * kerbYGap)
            coins.add(CoinSpawnInfo(coinX, coinY))
        }

        return coins
    }

    /**
     * Checks if a powerup is due to spawn (every 18-26 seconds).
     */
    fun checkPowerUpSpawn(): PowerUpType? {
        val now = System.currentTimeMillis()
        if (now - lastPowerUpTimeMs >= nextPowerUpIntervalMs) {
            lastPowerUpTimeMs = now
            nextPowerUpIntervalMs = (18000..26000).random(rng).toLong()

            // Weighted distribution: Magnet 35%, Shield 30%, Turbo 20%, Money Rain 15%
            val roll = rng.nextFloat()
            return when {
                roll < 0.35f -> PowerUpType.MONEY_MAGNET
                roll < 0.65f -> PowerUpType.SHIELD
                roll < 0.85f -> PowerUpType.TURBO
                else -> PowerUpType.MONEY_RAIN
            }
        }
        return null
    }

    data class SpawnRowDecision(val blockedLanes: List<Int>, val obstacleType: String)
    data class CoinSpawnInfo(val x: Float, val y: Float)

    companion object {
        private val OBSTACLE_TYPES = arrayOf("manhole", "garbage", "stone", "barrier", "puddle")
    }
}
