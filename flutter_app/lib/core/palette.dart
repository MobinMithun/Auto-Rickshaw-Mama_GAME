import 'package:flutter/material.dart';

class Palette {
  static const voidBlack = Color(0xFF0D0B1F);
  static const ink = Color(0xFF1C1A2E);
  static const shadow = Color(0xFF2E2B44);
  static const slate = Color(0xFF4A4460);
  static const stone = Color(0xFF6E6785);
  static const ash = Color(0xFF9A93AD);
  static const bone = Color(0xFFC8C2D4);
  static const paper = Color(0xFFF4F0E6);

  static const skinDeep = Color(0xFF7A4A2B);
  static const skinMid = Color(0xFFA8683C);
  static const skinLight = Color(0xFFC98B58);
  static const skinPale = Color(0xFFE0AB7A);

  static const maroon = Color(0xFF7A1327);
  static const red = Color(0xFFC02040);
  static const coral = Color(0xFFE84C3D);
  static const orange = Color(0xFFF5793A);
  static const amber = Color(0xFFFFB03B);

  static const gold = Color(0xFFFFD83D);
  static const cream = Color(0xFFF9F27A);

  static const forest = Color(0xFF0B4A34);
  static const green = Color(0xFF106B45);
  static const leaf = Color(0xFF2FA15A);
  static const lime = Color(0xFF7FD66F);

  static const navy = Color(0xFF123A63);
  static const blue = Color(0xFF1F6FB2);
  static const cyan = Color(0xFF3FBFE8);
  static const electric = Color(0xFF8FF2FF);

  static const plum = Color(0xFF4B1D6B);
  static const violet = Color(0xFF8C2FA8);
  static const magenta = Color(0xFFD94FA0);
  static const blush = Color(0xFFFF8FC9);

  static const soil = Color(0xFF3A2418);
  static const clay = Color(0xFF6B4A2A);

  static const List<Color> allColors = [
    voidBlack,
    ink,
    shadow,
    slate,
    stone,
    ash,
    bone,
    paper,
    skinDeep,
    skinMid,
    skinLight,
    skinPale,
    maroon,
    red,
    coral,
    orange,
    amber,
    gold,
    cream,
    forest,
    green,
    leaf,
    lime,
    navy,
    blue,
    cyan,
    electric,
    plum,
    violet,
    magenta,
    blush,
    soil,
    clay,
  ];

  static bool isDitherPixel(int x, int y, double density) {
    final threshold = bayer2x2[y % 2][x % 2];
    return density > threshold;
  }

  static const List<List<double>> bayer2x2 = [
    [0.2, 0.6],
    [0.8, 0.4],
  ];
}
