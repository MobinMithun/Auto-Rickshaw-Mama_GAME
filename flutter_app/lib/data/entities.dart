class RunResult {
  final int id;
  final int distanceMeters;
  final int moneyCollected;
  final int maxCombo;
  final String zoneReached;
  final int timestamp;

  RunResult({
    required this.id,
    required this.distanceMeters,
    required this.moneyCollected,
    required this.maxCombo,
    required this.zoneReached,
    required this.timestamp,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'distanceMeters': distanceMeters,
    'moneyCollected': moneyCollected,
    'maxCombo': maxCombo,
    'zoneReached': zoneReached,
    'timestamp': timestamp,
  };

  factory RunResult.fromJson(Map<String, dynamic> json) => RunResult(
    id: json['id'] as int,
    distanceMeters: json['distanceMeters'] as int,
    moneyCollected: json['moneyCollected'] as int,
    maxCombo: json['maxCombo'] as int,
    zoneReached: json['zoneReached'] as String,
    timestamp: json['timestamp'] as int,
  );
}

class Achievement {
  final String id;
  final String title;
  final String description;
  bool isUnlocked;
  int unlockedAt;

  Achievement({
    required this.id,
    required this.title,
    required this.description,
    this.isUnlocked = false,
    this.unlockedAt = 0,
  });

  Map<String, dynamic> toJson() => {
    'id': id,
    'title': title,
    'description': description,
    'isUnlocked': isUnlocked,
    'unlockedAt': unlockedAt,
  };

  factory Achievement.fromJson(Map<String, dynamic> json) => Achievement(
    id: json['id'] as String,
    title: json['title'] as String,
    description: json['description'] as String,
    isUnlocked: json['isUnlocked'] as bool? ?? false,
    unlockedAt: json['unlockedAt'] as int? ?? 0,
  );
}
