package com.example.components

import com.example.core.Constants
import com.example.core.PowerUpType

class RickshawPlayer {
    var lane: Int = 1
    var x: Float = Constants.LANE_CENTERS[1] - (Constants.RICKSHAW_WIDTH / 2f)
    var y: Float = Constants.RICKSHAW_Y
    var startX: Float = x
    var targetX: Float = x
    var isChangingLane: Boolean = false
    var laneChangeProgress: Float = 1f

    var lives: Int = 3
    var maxLives: Int = 3
    var invincibleTimerSec: Float = 0f

    var isTurbo: Boolean = false
    var turboTimeSec: Float = 0f

    var isShielded: Boolean = false

    var isMagnetActive: Boolean = false
    var magnetTimeSec: Float = 0f

    var nearMissCount: Int = 0

    fun startLaneChange(newLane: Int) {
        if (newLane in 0 until Constants.LANE_COUNT && newLane != lane) {
            lane = newLane
            startX = x
            targetX = Constants.LANE_CENTERS[lane] - (Constants.RICKSHAW_WIDTH / 2f)
            isChangingLane = true
            laneChangeProgress = 0f
        }
    }

    fun update(dt: Float) {
        if (isChangingLane) {
            laneChangeProgress += dt / Constants.LANE_CHANGE_DURATION
            if (laneChangeProgress >= 1f) {
                laneChangeProgress = 1f
                isChangingLane = false
                x = targetX
            } else {
                x = startX + (targetX - startX) * laneChangeProgress
            }
        }

        if (isTurbo) {
            turboTimeSec -= dt
            if (turboTimeSec <= 0f) {
                isTurbo = false
                turboTimeSec = 0f
            }
        }

        if (isMagnetActive) {
            magnetTimeSec -= dt
            if (magnetTimeSec <= 0f) {
                isMagnetActive = false
                magnetTimeSec = 0f
            }
        }

        if (invincibleTimerSec > 0f) {
            invincibleTimerSec -= dt
            if (invincibleTimerSec < 0f) invincibleTimerSec = 0f
        }
    }

    fun getHitbox(): HitboxRect {
        // 20x24 inset anchored to lower body
        return HitboxRect(x + 6f, y + 16f, 20f, 24f)
    }
}

data class HitboxRect(val x: Float, val y: Float, val w: Float, val h: Float) {
    fun intersects(other: HitboxRect): Boolean {
        return x < other.x + other.w &&
                x + w > other.x &&
                y < other.y + other.h &&
                y + h > other.y
    }
}

class ObstacleEntity {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 16f
    var height: Float = 16f
    var type: String = "manhole"
    var isActive: Boolean = false

    fun getHitbox(): HitboxRect {
        return HitboxRect(x + 1f, y + 2f, width - 2f, height - 3f)
    }
}

class CoinEntity {
    var x: Float = 0f
    var y: Float = 0f
    var isActive: Boolean = false
    var isCollected: Boolean = false

    fun getHitbox(): HitboxRect {
        return HitboxRect(x, y, 8f, 8f)
    }
}

class PowerUpEntity {
    var x: Float = 0f
    var y: Float = 0f
    var type: PowerUpType = PowerUpType.TURBO
    var isActive: Boolean = false

    fun getHitbox(): HitboxRect {
        return HitboxRect(x, y, 16f, 16f)
    }
}

class PassengerEntity {
    var x: Float = 0f
    var y: Float = 0f
    var isLeftKerb: Boolean = true
    var typeIndex: Int = 0
    var isPickedUp: Boolean = false
    var dropoffTargetMeter: Int = 0
    var isActive: Boolean = false

    fun getHitbox(): HitboxRect {
        return HitboxRect(x, y, 16f, 24f)
    }
}

class PedestrianEntity {
    var x: Float = 0f
    var y: Float = 0f
    var isDodging: Boolean = false
    var dodgeOffset: Float = 0f
    var isActive: Boolean = false

    fun getHitbox(): HitboxRect {
        val curX = if (isDodging) x + dodgeOffset else x
        return HitboxRect(curX + 3f, y + 4f, 10f, 16f)
    }
}

class PixelParticleEntity {
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var colorStepIndex: Int = 0
    var life: Float = 0f
    var maxLife: Float = 0.5f
    var isActive: Boolean = false
}

class FloatingTextEntity {
    var text: String = ""
    var x: Float = 0f
    var y: Float = 0f
    var life: Float = 0f
    var maxLife: Float = 0.6f
    var colorArgb: Int = 0
    var isActive: Boolean = false
}
