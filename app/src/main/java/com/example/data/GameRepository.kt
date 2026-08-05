package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GameRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        GameDatabase::class.java,
        "desi_turbo_rush.db"
    ).build()

    private val runDao = db.runResultDao()
    private val achievementDao = db.achievementDao()

    val topRuns: Flow<List<RunResultEntity>> = runDao.getTopRuns()
    val bestDistance: Flow<Int?> = runDao.getBestDistance()
    val totalMoney: Flow<Int?> = runDao.getTotalMoney()
    val achievements: Flow<List<AchievementEntity>> = achievementDao.getAllAchievements()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultAchievements()
        }
    }

    suspend fun saveRun(runResult: RunResultEntity) {
        runDao.insertRun(runResult)
        evaluateAchievements(runResult)
    }

    private suspend fun evaluateAchievements(run: RunResultEntity) {
        val now = System.currentTimeMillis()
        achievementDao.unlock("first_ride", now)

        if (run.moneyCollected >= 1000) {
            achievementDao.unlock("taka_1000", now)
        }
        if (run.distanceMeters >= 1000) {
            achievementDao.unlock("club_1000m", now)
        }
        if (run.distanceMeters >= 5000) {
            achievementDao.unlock("rickshaw_master", now)
        }
        if (run.maxCombo >= 20) {
            achievementDao.unlock("rickshaw_king", now)
        }
    }

    suspend fun unlockAchievement(id: String) {
        achievementDao.unlock(id)
    }

    private suspend fun seedDefaultAchievements() {
        val defaults = listOf(
            AchievementEntity("first_ride", "First Ride", "Complete your first rickshaw trip in Dhaka"),
            AchievementEntity("taka_1000", "৳1000 Earned", "Collect ৳1,000 total in a single run"),
            AchievementEntity("club_1000m", "1000 Meter Club", "Reach 1,000 meters in a single run"),
            AchievementEntity("rickshaw_master", "Rickshaw Master", "Reach 5,000 meters distance"),
            AchievementEntity("turbo_mama", "Turbo Mama", "Activate Turbo 5 times in a single run"),
            AchievementEntity("dhaka_champion", "Dhaka Champion", "Survive through all 9 Dhaka zones"),
            AchievementEntity("street_legend", "Street Legend", "Perform 20 near-miss pedestrian dodges"),
            AchievementEntity("rickshaw_king", "Pixel Rickshaw King", "Achieve a x5 Combo (20+ streak)")
        )
        for (ach in defaults) {
            achievementDao.insertOrUpdate(ach)
        }
    }
}
