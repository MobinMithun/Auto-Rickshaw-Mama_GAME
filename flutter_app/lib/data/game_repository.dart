import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'entities.dart';

class GameRepository {
  static const String _runsKey = 'run_results';
  static const String _achievementsKey = 'achievements';

  List<RunResult> _topRuns = [];
  int _bestDistance = 0;
  int _totalMoney = 0;
  List<Achievement> _achievements = [];

  List<RunResult> get topRuns => List.unmodifiable(_topRuns);
  int get bestDistance => _bestDistance;
  int get totalMoney => _totalMoney;
  List<Achievement> get achievements => List.unmodifiable(_achievements);

  GameRepository() {
    _seedDefaultAchievements();
  }

  Future<void> load() async {
    final prefs = await SharedPreferences.getInstance();
    final runsJson = prefs.getStringList(_runsKey) ?? [];
    _topRuns = runsJson.map((e) => RunResult.fromJson(jsonDecode(e))).toList();
    _topRuns.sort((a, b) => b.distanceMeters.compareTo(a.distanceMeters));
    _topRuns = _topRuns.take(20).toList();

    _bestDistance = _topRuns.isNotEmpty ? _topRuns.first.distanceMeters : 0;
    _totalMoney = _topRuns.fold(0, (sum, run) => sum + run.moneyCollected);

    final achJson = prefs.getStringList(_achievementsKey) ?? [];
    _achievements = achJson.map((e) => Achievement.fromJson(jsonDecode(e))).toList();
  }

  Future<void> saveRun(RunResult run) async {
    final prefs = await SharedPreferences.getInstance();
    _topRuns.add(run);
    _topRuns.sort((a, b) => b.distanceMeters.compareTo(a.distanceMeters));
    _topRuns = _topRuns.take(20).toList();

    _bestDistance = _topRuns.isNotEmpty ? _topRuns.first.distanceMeters : 0;
    _totalMoney += run.moneyCollected;

    final runsJson = _topRuns.map((e) => jsonEncode(e.toJson())).toList();
    await prefs.setStringList(_runsKey, runsJson);

    await _evaluateAchievements(run);
  }

  Future<void> _evaluateAchievements(RunResult run) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    if (run.moneyCollected >= 1000) {
      await unlockAchievement("taka_1000", now);
    }
    if (run.distanceMeters >= 1000) {
      await unlockAchievement("club_1000m", now);
    }
    if (run.distanceMeters >= 5000) {
      await unlockAchievement("rickshaw_master", now);
    }
    if (run.maxCombo >= 20) {
      await unlockAchievement("rickshaw_king", now);
    }
  }

  Future<void> unlockAchievement(String id, int timestamp) async {
    final prefs = await SharedPreferences.getInstance();
    final index = _achievements.indexWhere((a) => a.id == id);
    if (index >= 0 && !_achievements[index].isUnlocked) {
      _achievements[index].isUnlocked = true;
      _achievements[index].unlockedAt = timestamp;
      final achJson = _achievements.map((e) => jsonEncode(e.toJson())).toList();
      await prefs.setStringList(_achievementsKey, achJson);
    }
  }

  void _seedDefaultAchievements() {
    _achievements = [
      Achievement(id: "first_ride", title: "First Ride", description: "Complete your first rickshaw trip in Dhaka"),
      Achievement(id: "taka_1000", title: "Taka1000 Earned", description: "Collect Taka1,000 total in a single run"),
      Achievement(id: "club_1000m", title: "1000 Meter Club", description: "Reach 1,000 meters in a single run"),
      Achievement(id: "rickshaw_master", title: "Rickshaw Master", description: "Reach 5,000 meters distance"),
      Achievement(id: "turbo_mama", title: "Turbo Mama", description: "Activate Turbo 5 times in a single run"),
      Achievement(id: "dhaka_champion", title: "Dhaka Champion", description: "Survive through all 9 Dhaka zones"),
      Achievement(id: "street_legend", title: "Street Legend", description: "Perform 20 near-miss pedestrian dodges"),
      Achievement(id: "rickshaw_king", title: "Pixel Rickshaw King", description: "Achieve a x5 Combo (20+ streak)"),
    ];
  }
}
