package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RunResultDao {
    @Query("SELECT * FROM run_results ORDER BY distanceMeters DESC LIMIT 20")
    fun getTopRuns(): Flow<List<RunResultEntity>>

    @Query("SELECT MAX(distanceMeters) FROM run_results")
    fun getBestDistance(): Flow<Int?>

    @Query("SELECT SUM(moneyCollected) FROM run_results")
    fun getTotalMoney(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(runResult: RunResultEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(achievement: AchievementEntity)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id AND isUnlocked = 0")
    suspend fun unlock(id: String, timestamp: Long = System.currentTimeMillis())
}

@Database(entities = [RunResultEntity::class, AchievementEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun runResultDao(): RunResultDao
    abstract fun achievementDao(): AchievementDao
}
