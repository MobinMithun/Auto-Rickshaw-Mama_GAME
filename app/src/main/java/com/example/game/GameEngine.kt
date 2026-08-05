package com.example.game

import com.example.components.*
import com.example.core.Constants
import com.example.core.GameZone
import com.example.core.Palette
import com.example.core.PowerUpType
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

enum class GameScreenState {
    HOME,
    PLAYING,
    PAUSED,
    CRASHING,
    GAME_OVER,
    LEADERBOARD,
    ACHIEVEMENTS,
    SETTINGS
}

class GameEngine(val audioDirector: AudioDirector) {

    var state: GameScreenState by mutableStateOf(GameScreenState.HOME)
        private set

    val player = RickshawPlayer()
    val pools = ObjectPools()
    val spawnDirector = SpawnDirector()
    val difficultyManager = DifficultyManager()

    // Game Session Stats
    var survivalTimeSec: Float = 0f
    var distanceMeters: Float = 0f
    var moneyTaka: Int = 0
    var comboCount: Int = 0
    var comboTimerSec: Float = 0f
    var maxCombo: Int = 0
    var currentZone: GameZone = GameZone.GULISTAN

    // Screen Shake Offset (Integer virtual pixels)
    var shakeOffsetX: Int = 0
    var shakeOffsetY: Int = 0
    private var shakeTimerSec: Float = 0f

    // Crash Slow-mo & Wipe State
    var crashAnimProgress: Float = 0f
    var wipeColumnIndex: Int = -1

    // Active Passenger
    var currentPassenger: PassengerEntity? = null

    // Speed scaling tracking
    var lastThousandCoins: Int = 0
    var obstacleSpawnYTracker: Float = 0f

    // Options
    var useBanglaDigits: Boolean by mutableStateOf(true)
    var showFpsOverlay: Boolean by mutableStateOf(false)

    fun startNewGame() {
        pools.resetAll()
        player.x = Constants.LANE_CENTERS[1] - (Constants.RICKSHAW_WIDTH / 2f)
        player.lane = 1
        player.lives = 3
        player.invincibleTimerSec = 0f
        player.isTurbo = false
        player.isShielded = false
        player.isMagnetActive = false
        player.nearMissCount = 0

        survivalTimeSec = 0f
        distanceMeters = 0f
        moneyTaka = 0
        lastThousandCoins = 0
        obstacleSpawnYTracker = 0f
        spawnDirector.reset()
        comboCount = 0
        comboTimerSec = 0f
        maxCombo = 0
        currentZone = GameZone.GULISTAN
        currentPassenger = null
        crashAnimProgress = 0f
        wipeColumnIndex = -1

        state = GameScreenState.PLAYING
        audioDirector.triggerVoice("abar_suru")
    }

    fun handleSwipe(deltaX: Float) {
        if (state != GameScreenState.PLAYING) return
        if (deltaX > 18f && player.lane < Constants.LANE_COUNT - 1) {
            val newLane = player.lane + 1
            player.startLaneChange(newLane)
            spawnDirector.onPlayerLaneChanged(newLane)
            audioDirector.playSfx("near_miss")
        } else if (deltaX < -18f && player.lane > 0) {
            val newLane = player.lane - 1
            player.startLaneChange(newLane)
            spawnDirector.onPlayerLaneChanged(newLane)
            audioDirector.playSfx("near_miss")
        }
    }

    fun handleTapBell() {
        if (state != GameScreenState.PLAYING) return
        audioDirector.playSfx("bell_ting")

        // Skill Mechanic: Bell causes pedestrians within 40px to dodge 4px earlier!
        for (ped in pools.pedestrianPool) {
            if (ped.isActive && ped.y in (player.y - 40f)..(player.y + 40f)) {
                ped.isDodging = true
                ped.dodgeOffset = if (ped.x < player.x) -6f else 6f
            }
        }
    }

    fun pauseGame() {
        if (state == GameScreenState.PLAYING) {
            state = GameScreenState.PAUSED
        }
    }

    fun resumeGame() {
        if (state == GameScreenState.PAUSED) {
            state = GameScreenState.PLAYING
        }
    }

    fun goToHome() {
        state = GameScreenState.HOME
    }

    fun goToLeaderboard() {
        state = GameScreenState.LEADERBOARD
    }

    fun goToAchievements() {
        state = GameScreenState.ACHIEVEMENTS
    }

    fun goToSettings() {
        state = GameScreenState.SETTINGS
    }

    fun update(dt: Float) {
        if (state != GameScreenState.PLAYING && state != GameScreenState.CRASHING) return

        if (state == GameScreenState.CRASHING) {
            updateCrashSequence(dt)
            return
        }

        survivalTimeSec += dt
        val tier = difficultyManager.getTierForTime(survivalTimeSec)

        // Speed gain for every 1,000 coins collected
        val thousandCoinsCount = moneyTaka / 1000
        if (thousandCoinsCount > lastThousandCoins) {
            lastThousandCoins = thousandCoinsCount
            addReward(0, "⚡ SPEED BOOST!")
            audioDirector.playSfx("turbo_boost")
        }

        val coinSpeedBonus = thousandCoinsCount * 14f
        var currentSpeed = (tier.scrollSpeed + coinSpeedBonus)
        if (player.isTurbo) {
            currentSpeed *= Constants.TURBO_SPEED_MULTIPLIER
        }

        // Advance Distance
        distanceMeters += currentSpeed * dt * Constants.METERS_PER_PIXEL

        // Zone Cycle (Every 900m)
        val zoneIndex = ((distanceMeters / Constants.ZONE_INTERVAL_METERS).toInt()) % GameZone.values().size
        currentZone = GameZone.values()[zoneIndex]

        // Player Update
        player.update(dt)

        // Combo Decay
        if (comboCount > 0) {
            comboTimerSec -= dt
            if (comboTimerSec <= 0f) {
                comboCount = 0
            }
        }

        // Magnet Effect (Pulls coins within 48px toward player)
        if (player.isMagnetActive) {
            for (coin in pools.coinPool) {
                if (coin.isActive && !coin.isCollected) {
                    val dx = player.x + 12f - coin.x
                    val dy = player.y + 16f - coin.y
                    val distSq = dx * dx + dy * dy
                    if (distSq < 48f * 48f) {
                        coin.x += (dx / 4f)
                        coin.y += (dy / 4f)
                    }
                }
            }
        }

        // --- ENTITY SPAWNING ---
        updateSpawning(dt, currentSpeed, tier.maxLanesBlocked)

        // --- MOVEMENT & COLLISION WRAPPER ---
        updateEntitiesAndCollisions(dt, currentSpeed)
    }

    private fun updateSpawning(dt: Float, currentSpeed: Float, maxBlockedLanes: Int) {
        obstacleSpawnYTracker += currentSpeed * dt

        // Obstacle Spawning via Spawn Director
        val spawnDecision = spawnDirector.shouldSpawnObstacleRow(obstacleSpawnYTracker, currentSpeed, maxBlockedLanes)
        if (spawnDecision != null) {
            for (blockedLane in spawnDecision.blockedLanes) {
                val obs = pools.obtainObstacle()
                if (obs != null) {
                    obs.isActive = true
                    obs.x = Constants.LANE_CENTERS[blockedLane] - 8f
                    obs.y = -30f
                    obs.type = spawnDecision.obstacleType
                }
            }
        }

        // Coins Arc Spawning
        if (Math.random() < 0.02) {
            val pattern = spawnDirector.generateCoinPattern(-20f)
            for (info in pattern) {
                val coin = pools.obtainCoin()
                if (coin != null) {
                    coin.isActive = true
                    coin.isCollected = false
                    coin.x = info.x
                    coin.y = info.y
                }
            }
        }

        // Power-Up Spawning
        val pupType = spawnDirector.checkPowerUpSpawn()
        if (pupType != null) {
            val pup = pools.obtainPowerUp()
            if (pup != null) {
                pup.isActive = true
                pup.type = pupType
                val lane = (0 until Constants.LANE_COUNT).random()
                pup.x = Constants.LANE_CENTERS[lane] - 8f
                pup.y = -20f
            }
        }

        // Pedestrians
        if (Math.random() < 0.015) {
            val ped = pools.obtainPedestrian()
            if (ped != null) {
                ped.isActive = true
                ped.isDodging = false
                ped.dodgeOffset = 0f
                val lane = (0 until Constants.LANE_COUNT).random()
                ped.x = Constants.LANE_CENTERS[lane] - 8f
                ped.y = -24f
            }
        }

        // Passengers (Kerb side)
        if (currentPassenger == null && Math.random() < 0.008) {
            val pass = pools.obtainPassenger()
            if (pass != null) {
                pass.isActive = true
                pass.isLeftKerb = Math.random() < 0.5
                pass.x = if (pass.isLeftKerb) 10f else 154f
                pass.y = -24f
                pass.typeIndex = (0..5).random()
                pass.isPickedUp = false
                pass.dropoffTargetMeter = distanceMeters.toInt() + (400..900).random()
            }
        }
    }

    private fun updateEntitiesAndCollisions(dt: Float, currentSpeed: Float) {
        val playerHitbox = player.getHitbox()

        // 1. OBSTACLES
        for (obs in pools.obstaclePool) {
            if (!obs.isActive) continue
            obs.y += currentSpeed * dt

            if (obs.getHitbox().intersects(playerHitbox)) {
                if (player.isTurbo) {
                    // Destroy obstacle
                    obs.isActive = false
                    spawnBurstParticles(obs.x + 8f, obs.y + 8f)
                    addReward(10, "SMASH!")
                } else if (player.isShielded) {
                    // Consume shield
                    player.isShielded = false
                    obs.isActive = false
                    audioDirector.playSfx("near_miss")
                    spawnBurstParticles(obs.x + 8f, obs.y + 8f)
                } else if (player.invincibleTimerSec > 0f) {
                    // Temporarily invincible right after losing a life
                } else {
                    // Lose 1 life on obstacle crash
                    player.lives--
                    obs.isActive = false
                    spawnBurstParticles(obs.x + 8f, obs.y + 8f, count = 16)
                    shakeTimerSec = 0.35f
                    audioDirector.playSfx("crash_boom")

                    if (player.lives > 0) {
                        player.invincibleTimerSec = 1.8f
                        addReward(0, "-1 LIFE! ❤️")
                        audioDirector.triggerVoice("are_baba")
                    } else {
                        triggerCrash()
                        return
                    }
                }
            }

            if (obs.y > Constants.VIRTUAL_HEIGHT + 32f) obs.isActive = false
        }

        // 2. COINS
        for (coin in pools.coinPool) {
            if (!coin.isActive || coin.isCollected) continue
            coin.y += currentSpeed * dt

            if (coin.getHitbox().intersects(playerHitbox)) {
                coin.isCollected = true
                coin.isActive = false
                addReward(20, "৳20")
                audioDirector.playSfx("coin_ching")
            }

            if (coin.y > Constants.VIRTUAL_HEIGHT + 16f) coin.isActive = false
        }

        // 3. POWERUPS
        for (pup in pools.powerUpPool) {
            if (!pup.isActive) continue
            pup.y += currentSpeed * dt

            if (pup.getHitbox().intersects(playerHitbox)) {
                pup.isActive = false
                audioDirector.playSfx("turbo_boost")
                when (pup.type) {
                    PowerUpType.TURBO -> {
                        player.isTurbo = true
                        player.turboTimeSec = pup.type.durationSec
                        audioDirector.triggerVoice("bachao")
                    }
                    PowerUpType.MONEY_MAGNET -> {
                        player.isMagnetActive = true
                        player.magnetTimeSec = pup.type.durationSec
                    }
                    PowerUpType.SHIELD -> {
                        player.isShielded = true
                    }
                    PowerUpType.MONEY_RAIN -> {
                        spawnMoneyRain()
                    }
                }
            }

            if (pup.y > Constants.VIRTUAL_HEIGHT + 16f) pup.isActive = false
        }

        // 4. PASSENGERS
        for (pass in pools.passengerPool) {
            if (!pass.isActive) continue
            pass.y += currentSpeed * dt

            if (!pass.isPickedUp && pass.getHitbox().intersects(playerHitbox)) {
                pass.isPickedUp = true
                pass.isActive = false
                currentPassenger = pass
                addReward(50, "PICKUP!")
                audioDirector.playSfx("pickup_ting")
                audioDirector.triggerVoice("vada_ache")
            }

            if (pass.y > Constants.VIRTUAL_HEIGHT + 24f) pass.isActive = false
        }

        // Passenger Dropoff check
        if (currentPassenger != null && distanceMeters >= currentPassenger!!.dropoffTargetMeter) {
            addReward(200, "DROPOFF!")
            audioDirector.playSfx("dropoff_chime")
            currentPassenger = null
        }

        // 5. PEDESTRIANS & PERSON HITS
        for (ped in pools.pedestrianPool) {
            if (!ped.isActive) continue
            ped.y += currentSpeed * dt

            val pedHitbox = ped.getHitbox()
            if (pedHitbox.intersects(playerHitbox)) {
                // Hitting a person EARNS POINTS!
                ped.isActive = false
                addReward(100, "+100 PTS! 👤")
                audioDirector.playSfx("coin_ching")
                audioDirector.triggerVoice("mama_side_den")
                spawnBurstParticles(ped.x + 8f, ped.y + 12f, count = 12)
            } else {
                // Near miss check (within 6px, no contact)
                val distY = Math.abs(ped.y - player.y)
                if (distY < 6f && !ped.isDodging) {
                    ped.isDodging = true
                    ped.dodgeOffset = if (ped.x < player.x) -8f else 8f
                    player.nearMissCount++
                    addReward(10, "NEAR MISS!")
                    audioDirector.playSfx("near_miss")
                    audioDirector.triggerVoice("mama_side_den")
                }
            }

            if (ped.y > Constants.VIRTUAL_HEIGHT + 24f) ped.isActive = false
        }

        // Update Particles & Floating Text
        updateParticles(dt)
        updateFloatingText(dt)
    }

    private fun addReward(baseTaka: Int, label: String) {
        comboCount++
        comboTimerSec = 3.0f
        if (comboCount > maxCombo) maxCombo = comboCount

        val multiplier = when {
            comboCount >= 20 -> 5
            comboCount >= 10 -> 3
            comboCount >= 5 -> 2
            else -> 1
        }

        val total = baseTaka * multiplier
        moneyTaka += total

        // Floating Text
        val ft = pools.obtainFloatingText()
        if (ft != null) {
            ft.isActive = true
            ft.text = if (multiplier > 1) "$label x$multiplier" else label
            ft.x = player.x
            ft.y = player.y - 10f
            ft.life = 0f
            ft.maxLife = 0.6f
            ft.colorArgb = when (multiplier) {
                5 -> Palette.gold.toArgb()
                3 -> Palette.amber.toArgb()
                2 -> Palette.orange.toArgb()
                else -> Palette.cream.toArgb()
            }
        }
    }

    private fun triggerCrash() {
        player.lives = 0
        state = GameScreenState.CRASHING
        audioDirector.playSfx("crash_boom")
        audioDirector.triggerVoice("are_baba")

        // 24 pixel particles burst
        spawnBurstParticles(player.x + 16f, player.y + 20f, count = 24)

        // Screen Shake ±3px integer decay
        shakeTimerSec = 0.35f
    }

    private fun updateCrashSequence(dt: Float) {
        crashAnimProgress += dt
        if (shakeTimerSec > 0f) {
            shakeTimerSec -= dt
            shakeOffsetX = (-3..3).random()
            shakeOffsetY = (-3..3).random()
        } else {
            shakeOffsetX = 0
            shakeOffsetY = 0
        }

        if (crashAnimProgress >= 0.8f) {
            state = GameScreenState.GAME_OVER
        }
    }

    private fun spawnBurstParticles(cx: Float, cy: Float, count: Int = 16) {
        for (i in 0 until count) {
            val p = pools.obtainParticle() ?: break
            p.isActive = true
            p.x = cx
            p.y = cy
            p.vx = ((-30..30).random()).toFloat()
            p.vy = ((-40..20).random()).toFloat()
            p.life = 0f
            p.maxLife = 0.5f
            p.colorStepIndex = 0
        }
    }

    private fun spawnMoneyRain() {
        for (i in 0 until 12) {
            val coin = pools.obtainCoin() ?: break
            coin.isActive = true
            coin.isCollected = false
            val minX = Constants.KERB_LEFT_X + 10f
            val maxX = Constants.KERB_RIGHT_X - 10f
            coin.x = minX + kotlin.random.Random.nextFloat() * (maxX - minX)
            coin.y = -((i * 15f) + 10f)
        }
    }

    private fun updateParticles(dt: Float) {
        for (p in pools.particlePool) {
            if (!p.isActive) continue
            p.life += dt
            if (p.life >= p.maxLife) {
                p.isActive = false
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.colorStepIndex = (p.life / p.maxLife * 4).toInt().coerceIn(0, 3)
            }
        }
    }

    private fun updateFloatingText(dt: Float) {
        for (ft in pools.floatingTextPool) {
            if (!ft.isActive) continue
            ft.life += dt
            if (ft.life >= ft.maxLife) {
                ft.isActive = false
            } else {
                ft.y -= 12f * dt // Rises 12px
            }
        }
    }
}
