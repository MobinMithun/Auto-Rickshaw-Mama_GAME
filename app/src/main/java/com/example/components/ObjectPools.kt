package com.example.components

class ObjectPools {

    val obstaclePool = Array(24) { ObstacleEntity() }
    val pedestrianPool = Array(16) { PedestrianEntity() }
    val coinPool = Array(48) { CoinEntity() }
    val particlePool = Array(128) { PixelParticleEntity() }
    val floatingTextPool = Array(12) { FloatingTextEntity() }
    val passengerPool = Array(8) { PassengerEntity() }
    val powerUpPool = Array(6) { PowerUpEntity() }

    fun obtainObstacle(): ObstacleEntity? {
        return obstaclePool.firstOrNull { !it.isActive }
    }

    fun obtainPedestrian(): PedestrianEntity? {
        return pedestrianPool.firstOrNull { !it.isActive }
    }

    fun obtainCoin(): CoinEntity? {
        return coinPool.firstOrNull { !it.isActive }
    }

    fun obtainParticle(): PixelParticleEntity? {
        return particlePool.firstOrNull { !it.isActive }
    }

    fun obtainFloatingText(): FloatingTextEntity? {
        return floatingTextPool.firstOrNull { !it.isActive }
    }

    fun obtainPassenger(): PassengerEntity? {
        return passengerPool.firstOrNull { !it.isActive }
    }

    fun obtainPowerUp(): PowerUpEntity? {
        return powerUpPool.firstOrNull { !it.isActive }
    }

    fun resetAll() {
        obstaclePool.forEach { it.isActive = false }
        pedestrianPool.forEach { it.isActive = false }
        coinPool.forEach { it.isActive = false }
        particlePool.forEach { it.isActive = false }
        floatingTextPool.forEach { it.isActive = false }
        passengerPool.forEach { it.isActive = false }
        powerUpPool.forEach { it.isActive = false }
    }
}
