import 'package:flutter/material.dart';
import '../data/game_repository.dart';
import '../game/audio_director.dart';
import '../game/game_engine.dart';
import '../game/game_engine.dart';
import '../ui/achievements_screen.dart';
import '../ui/game_canvas.dart';
import '../ui/game_over_screen.dart';
import '../ui/home_screen.dart';
import '../ui/leaderboard_screen.dart';
import '../ui/settings_screen.dart';

class MainGameContainer extends StatefulWidget {
  const MainGameContainer({super.key});

  @override
  State<MainGameContainer> createState() => _MainGameContainerState();
}

class _MainGameContainerState extends State<MainGameContainer> {
  final GameRepository _repository = GameRepository();
  final AudioDirector _audioDirector = AudioDirector();
  final GameEngine _engine = GameEngine(AudioDirector());

  List<dynamic> _topRuns = [];
  int _bestDistance = 0;
  int _totalMoney = 0;
  List<dynamic> _achievements = [];

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    await _repository.load();
    setState(() {
      _topRuns = _repository.topRuns;
      _bestDistance = _repository.bestDistance;
      _totalMoney = _repository.totalMoney;
      _achievements = _repository.achievements;
    });
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _engine,
      builder: (context, child) {
        return switch (_engine.state) {
          GameScreenState.home => HomeScreen(
              bestDistanceMeters: _bestDistance,
              totalMoneyTaka: _totalMoney,
              onStartGame: _engine.startNewGame,
              onOpenLeaderboard: _engine.goToLeaderboard,
              onOpenAchievements: _engine.goToAchievements,
              onOpenSettings: _engine.goToSettings,
            ),
          GameScreenState.playing || GameScreenState.crashing || GameScreenState.paused => GameCanvasView(
              engine: _engine,
              onPauseClicked: () {
                if (_engine.state == GameScreenState.playing) {
                  _engine.pauseGame();
                } else if (_engine.state == GameScreenState.paused) {
                  _engine.resumeGame();
                }
              },
            ),
          GameScreenState.gameOver => GameOverScreen(
              distanceMeters: _engine.distanceMeters.toInt(),
              moneyTaka: _engine.moneyTaka,
              maxCombo: _engine.maxCombo,
              zoneName: _engine.currentZone.displayName,
              onPlayAgain: _engine.startNewGame,
              onGoHome: _engine.goToHome,
            ),
          GameScreenState.leaderboard => LeaderboardScreen(
              topRuns: _topRuns.cast(),
              onBackClicked: _engine.goToHome,
            ),
          GameScreenState.achievements => AchievementsScreen(
              achievements: _achievements.cast(),
              onBackClicked: _engine.goToHome,
            ),
          GameScreenState.settings => SettingsScreen(
              bgmEnabled: _audioDirector.bgmEnabled,
              sfxEnabled: _audioDirector.sfxEnabled,
              voiceEnabled: _audioDirector.voiceEnabled,
              useBanglaDigits: _engine.useBanglaDigits,
              showFpsOverlay: _engine.showFpsOverlay,
              onBgmToggled: (v) => _audioDirector.bgmEnabled = v,
              onSfxToggled: (v) => _audioDirector.sfxEnabled = v,
              onVoiceToggled: (v) => _audioDirector.voiceEnabled = v,
              onBanglaDigitsToggled: (v) => _engine.useBanglaDigits = v,
              onFpsOverlayToggled: (v) => _engine.showFpsOverlay = v,
              onBackClicked: _engine.goToHome,
            ),
        };
      },
    );
  }
}
