import 'game_entities.dart';

extension FirstWhereOrNullExtension<E> on List<E> {
  E? firstWhereOrNull(bool Function(E) test) {
    for (final element in this) {
      if (test(element)) return element;
    }
    return null;
  }
}

class ObjectPools {
  final List<ObstacleEntity> obstaclePool = List.generate(24, (_) => ObstacleEntity());
  final List<PedestrianEntity> pedestrianPool = List.generate(16, (_) => PedestrianEntity());
  final List<CoinEntity> coinPool = List.generate(48, (_) => CoinEntity());
  final List<PixelParticleEntity> particlePool = List.generate(128, (_) => PixelParticleEntity());
  final List<FloatingTextEntity> floatingTextPool = List.generate(12, (_) => FloatingTextEntity());
  final List<PassengerEntity> passengerPool = List.generate(8, (_) => PassengerEntity());
  final List<PowerUpEntity> powerUpPool = List.generate(6, (_) => PowerUpEntity());

  ObstacleEntity? obtainObstacle() => obstaclePool.firstWhereOrNull((e) => !e.isActive);
  PedestrianEntity? obtainPedestrian() => pedestrianPool.firstWhereOrNull((e) => !e.isActive);
  CoinEntity? obtainCoin() => coinPool.firstWhereOrNull((e) => !e.isActive);
  PixelParticleEntity? obtainParticle() => particlePool.firstWhereOrNull((e) => !e.isActive);
  FloatingTextEntity? obtainFloatingText() => floatingTextPool.firstWhereOrNull((e) => !e.isActive);
  PassengerEntity? obtainPassenger() => passengerPool.firstWhereOrNull((e) => !e.isActive);
  PowerUpEntity? obtainPowerUp() => powerUpPool.firstWhereOrNull((e) => !e.isActive);

  void resetAll() {
    for (final e in obstaclePool) e.isActive = false;
    for (final e in pedestrianPool) e.isActive = false;
    for (final e in coinPool) { e.isActive = false; e.isCollected = false; }
    for (final e in particlePool) e.isActive = false;
    for (final e in floatingTextPool) e.isActive = false;
    for (final e in passengerPool) e.isActive = false;
    for (final e in powerUpPool) e.isActive = false;
  }
}
