package com.example.core

object Constants {
    // Virtual Canvas Dimensions (180 x 320 px portrait)
    const val VIRTUAL_WIDTH = 180f
    const val VIRTUAL_HEIGHT = 320f

    // Road Layout
    const val KERB_LEFT_X = 30f
    const val KERB_RIGHT_X = 150f
    const val ROAD_WIDTH = 120f
    
    // Lanes
    val LANE_CENTERS = floatArrayOf(50f, 90f, 130f)
    const val LANE_COUNT = 3

    // Player Rickshaw Anchor
    const val RICKSHAW_Y = 232f
    const val RICKSHAW_WIDTH = 32f
    const val RICKSHAW_HEIGHT = 40f

    // Motion Tuning
    const val BASE_SCROLL_SPEED = 55f // px / second
    const val SPEED_INC_PER_20S = 7f // +7 px/s every 20s
    const val MAX_SCROLL_SPEED = 190f // speed cap
    const val TURBO_SPEED_MULTIPLIER = 1.6f
    const val LANE_CHANGE_DURATION = 0.14f // seconds
    const val METERS_PER_PIXEL = 1f / 6f // 6 px = 1 meter

    // Audio & Voice Cooldown
    const val MIN_VOICE_COOLDOWN_MS = 2500L
    const val MIN_SFX_COOLDOWN_MS = 60L

    // Zone Cycling Interval (in meters)
    const val ZONE_INTERVAL_METERS = 900f
}

enum class GameZone(
    val displayName: String,
    val banglaName: String,
    val description: String
) {
    GULISTAN("Gulistan", "গুলিস্তান", "Bustling junction with dense bus traffic & horns"),
    FARMGATE("Farmgate", "ফার্মগেট", "Footpath vendors & footoverbridge silhouettes"),
    MOTIJHEEL("Motijheel", "মতিঝিল", "Financial hub with office tower backdrop"),
    MIRPUR("Mirpur", "মিরপুর", "Construction zone with roadworks & barriers"),
    UTTARA("Uttara", "উত্তরা", "Wide avenue, high-speed smooth cruising"),
    OLD_DHAKA("Old Dhaka", "পুরান ঢাকা", "Narrow heritage alleys with roaming goats"),
    VILLAGE_ROAD("Village Road", "গ্রামের পথ", "Earthy dirt paths with wandering cows"),
    NIGHT_STREET("Night Street", "নাইট স্ট্রিট", "Atmospheric night run with headlamp beams"),
    RAINY_ROAD("Rainy Road", "বৃষ্টির দিন", "Slick wet asphalt with pixel rain drops")
}

enum class PowerUpType(val durationSec: Float) {
    TURBO(6.0f),
    MONEY_MAGNET(8.0f),
    SHIELD(0.0f), // Until consumed
    MONEY_RAIN(5.0f)
}
