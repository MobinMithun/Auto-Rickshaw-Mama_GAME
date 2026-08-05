package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GameRepository
import com.example.data.RunResultEntity
import com.example.game.AudioDirector
import com.example.game.GameEngine
import com.example.game.GameScreenState
import kotlinx.coroutines.launch

@Composable
fun MainGameContainer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val audioDirector = remember { AudioDirector(context) }
    val engine = remember { GameEngine(audioDirector) }
    val scope = rememberCoroutineScope()

    val topRuns by repository.topRuns.collectAsStateWithLifecycle(initialValue = emptyList())
    val bestDistance by repository.bestDistance.collectAsStateWithLifecycle(initialValue = 0)
    val totalMoney by repository.totalMoney.collectAsStateWithLifecycle(initialValue = 0)
    val achievements by repository.achievements.collectAsStateWithLifecycle(initialValue = emptyList())

    // Monitor Game State Changes (Save Run on Game Over)
    var previousState by remember { mutableStateOf(engine.state) }
    LaunchedEffect(engine.state) {
        if (previousState == GameScreenState.CRASHING && engine.state == GameScreenState.GAME_OVER) {
            scope.launch {
                repository.saveRun(
                    RunResultEntity(
                        distanceMeters = engine.distanceMeters.toInt(),
                        moneyCollected = engine.moneyTaka,
                        maxCombo = engine.maxCombo,
                        zoneReached = engine.currentZone.displayName
                    )
                )
            }
        }
        previousState = engine.state
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (engine.state) {
            GameScreenState.HOME -> {
                HomeScreen(
                    bestDistanceMeters = bestDistance ?: 0,
                    totalMoneyTaka = totalMoney ?: 0,
                    onStartGame = { engine.startNewGame() },
                    onOpenLeaderboard = { engine.goToLeaderboard() },
                    onOpenAchievements = { engine.goToAchievements() },
                    onOpenSettings = { engine.goToSettings() }
                )
            }
            GameScreenState.PLAYING, GameScreenState.CRASHING, GameScreenState.PAUSED -> {
                GameCanvasView(
                    engine = engine,
                    onPauseClicked = {
                        if (engine.state == GameScreenState.PLAYING) engine.pauseGame()
                        else if (engine.state == GameScreenState.PAUSED) engine.resumeGame()
                    }
                )
            }
            GameScreenState.GAME_OVER -> {
                GameOverScreen(
                    distanceMeters = engine.distanceMeters.toInt(),
                    moneyTaka = engine.moneyTaka,
                    maxCombo = engine.maxCombo,
                    zoneName = engine.currentZone.displayName,
                    onPlayAgain = { engine.startNewGame() },
                    onGoHome = { engine.goToHome() }
                )
            }
            GameScreenState.LEADERBOARD -> {
                LeaderboardScreen(
                    topRuns = topRuns,
                    onBackClicked = { engine.goToHome() }
                )
            }
            GameScreenState.ACHIEVEMENTS -> {
                AchievementsScreen(
                    achievements = achievements,
                    onBackClicked = { engine.goToHome() }
                )
            }
            GameScreenState.SETTINGS -> {
                SettingsScreen(
                    bgmEnabled = audioDirector.bgmEnabled,
                    sfxEnabled = audioDirector.sfxEnabled,
                    voiceEnabled = audioDirector.voiceEnabled,
                    useBanglaDigits = engine.useBanglaDigits,
                    showFpsOverlay = engine.showFpsOverlay,
                    onBgmToggled = { audioDirector.bgmEnabled = it },
                    onSfxToggled = { audioDirector.sfxEnabled = it },
                    onVoiceToggled = { audioDirector.voiceEnabled = it },
                    onBanglaDigitsToggled = { engine.useBanglaDigits = it },
                    onFpsOverlayToggled = { engine.showFpsOverlay = it },
                    onBackClicked = { engine.goToHome() }
                )
            }
        }
    }
}
