package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_results")
data class RunResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val distanceMeters: Int,
    val moneyCollected: Int,
    val maxCombo: Int,
    val zoneReached: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)
