class Constants {
  static const double virtualWidth = 180;
  static const double virtualHeight = 320;

  static const double kerbLeftX = 30;
  static const double kerbRightX = 150;
  static const double roadWidth = 120;

  static const List<double> laneCenters = [50, 90, 130];
  static const int laneCount = 3;

  static const double rickshawY = 232;
  static const double rickshawWidth = 32;
  static const double rickshawHeight = 40;

  static const double baseScrollSpeed = 55;
  static const double speedIncPer20s = 7;
  static const double maxScrollSpeed = 190;
  static const double turboSpeedMultiplier = 1.6;
  static const double laneChangeDuration = 0.14;
  static const double metersPerPixel = 1 / 6;

  static const int minVoiceCooldownMs = 2500;
  static const int minSfxCooldownMs = 60;

  static const double zoneIntervalMeters = 900;
}

enum GameZone {
  gulistan("Gulistan", "গুলিস্তান", "Bustling junction with dense bus traffic & horns"),
  farmgate("Farmgate", "ফার্মগেট", "Footpath vendors & footoverbridge silhouettes"),
  motijheel("Motijheel", "মতিঝিল", "Financial hub with office tower backdrop"),
  mirpur("Mirpur", "মিরপুর", "Construction zone with roadworks & barriers"),
  uttara("Uttara", "উত্তরা", "Wide avenue, high-speed smooth cruising"),
  oldDhaka("Old Dhaka", "পুরান ঢাকা", "Narrow heritage alleys with roaming goats"),
  villageRoad("Village Road", "গ্রামের পথ", "Earthy dirt paths with wandering cows"),
  nightStreet("Night Street", "নাইট স্ট্রিট", "Atmospheric night run with headlamp beams"),
  rainyRoad("Rainy Road", "বৃষ্টির দিন", "Slick wet asphalt with pixel rain drops");

  final String displayName;
  final String banglaName;
  final String description;

  const GameZone(this.displayName, this.banglaName, this.description);
}

enum PowerUpType {
  turbo(6.0),
  moneyMagnet(8.0),
  shield(0.0),
  moneyRain(5.0);

  final double durationSec;

  const PowerUpType(this.durationSec);
}
