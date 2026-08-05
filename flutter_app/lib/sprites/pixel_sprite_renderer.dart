import 'dart:ui' as ui;
import '../core/palette.dart';

class PixelSpriteRenderer {
  static final Map<String, ui.Picture> _bitmapCache = {};
  static final Paint _paint = Paint()..isAntiAlias = false..filterQuality = ui.FilterQuality.none;

  static ui.Picture createPixelBitmap(int width, int height, void Function(ui.Canvas) drawBlock) {
    final recorder = ui.PictureRecorder();
    final canvas = ui.Canvas(recorder, ui.Rect.fromLTWH(0, 0, width.toDouble(), height.toDouble()));
    drawBlock(canvas);
    return recorder.endRecording();
  }

  static void drawPixel(ui.Canvas canvas, int x, int y, int colorArgb) {
    _paint.color = ui.Color(colorArgb);
    canvas.drawRect(ui.Rect.fromLTWH(x.toDouble(), y.toDouble(), 1, 1), _paint);
  }

  static void drawPixelRect(ui.Canvas canvas, int left, int top, int width, int height, int colorArgb) {
    _paint.color = ui.Color(colorArgb);
    canvas.drawRect(ui.Rect.fromLTWH(left.toDouble(), top.toDouble(), width.toDouble(), height.toDouble()), _paint);
  }

  static void drawPixelOutline(ui.Canvas canvas, int left, int top, int width, int height, int outlineColorArgb) {
    _paint.color = ui.Color(outlineColorArgb);
    canvas.drawRect(ui.Rect.fromLTWH(left.toDouble(), top.toDouble(), width.toDouble(), 1), _paint);
    canvas.drawRect(ui.Rect.fromLTWH(left.toDouble(), (top + height - 1).toDouble(), width.toDouble(), 1), _paint);
    canvas.drawRect(ui.Rect.fromLTWH(left.toDouble(), top.toDouble(), 1, height.toDouble()), _paint);
    canvas.drawRect(ui.Rect.fromLTWH((left + width - 1).toDouble(), top.toDouble(), 1, height.toDouble()), _paint);
  }

  static ui.Picture getSprite(String key, int width, int height, ui.Picture Function() generator) {
    return _bitmapCache.putIfAbsent(key, () => generator());
  }

  static ui.Picture getRickshawFrame(int frameIndex, {bool isTurbo = false, int leanDir = 0}) {
    final key = "rickshaw_f${frameIndex}_t${isTurbo}_l$leanDir";
    return getSprite(key, 32, 40, _drawRickshaw(frameIndex, isTurbo, leanDir));
  }

  static ui.Picture _drawRickshaw(int frameIndex, bool isTurbo, int leanDir) {
    return createPixelBitmap(32, 40, (canvas) {
      final ink = Palette.ink.value;
      final red = Palette.red.value;
      final coral = Palette.coral.value;
      final orange = Palette.orange.value;
      final amber = Palette.amber.value;
      final gold = Palette.gold.value;
      final cream = Palette.cream.value;
      final green = Palette.green.value;
      final leaf = Palette.leaf.value;
      final skin = Palette.skinMid.value;
      final shadow = Palette.shadow.value;
      final bone = Palette.bone.value;

      final leanOffset = leanDir == -1 ? -2 : leanDir == 1 ? 2 : 0;
      final bounce = frameIndex % 2 == 1 ? 1 : 0;

      drawPixelRect(canvas, 4 + leanOffset, 34, 24, 5, shadow);

      final wheelY = 26 + bounce;
      drawPixelRect(canvas, 3 + leanOffset, wheelY, 4, 10, ink);
      drawPixelRect(canvas, 4 + leanOffset, wheelY + 2, 2, 6, bone);
      drawPixelRect(canvas, 25 + leanOffset, wheelY, 4, 10, ink);
      drawPixelRect(canvas, 26 + leanOffset, wheelY + 2, 2, 6, bone);

      drawPixelRect(canvas, 14 + leanOffset, 4, 4, 8, ink);
      drawPixelRect(canvas, 15 + leanOffset, 6, 2, 4, bone);

      final hoodY = 12 + bounce;
      drawPixelRect(canvas, 6 + leanOffset, hoodY, 20, 16, red);
      drawPixelOutline(canvas, 6 + leanOffset, hoodY, 20, 16, ink);
      drawPixelRect(canvas, 8 + leanOffset, hoodY + 2, 16, 2, coral);
      drawPixelRect(canvas, 10 + leanOffset, hoodY + 5, 12, 3, amber);
      drawPixelRect(canvas, 12 + leanOffset, hoodY + 9, 8, 2, green);

      final headX = 8 + leanOffset;
      final headY = 2 + bounce;
      drawPixelRect(canvas, headX + 4, headY + 1, 8, 3, green);
      drawPixelRect(canvas, headX + 4, headY + 4, 8, 5, skin);
      drawPixelRect(canvas, headX + 4 + (frameIndex % 2), headY + 7, 8, 2, ink);
      drawPixel(canvas, headX + 5, headY + 5, ink);
      drawPixel(canvas, headX + 10, headY + 5, ink);
      drawPixelRect(canvas, headX + 3, headY + 9, 10, 5, leaf);

      drawPixelRect(canvas, 14 + leanOffset, 2, 4, 3, frameIndex % 2 == 0 ? gold : cream);

      if (isTurbo) {
        final flameY = 32;
        final flameColor1 = frameIndex % 2 == 0 ? amber : orange;
        final flameColor2 = frameIndex % 2 == 0 ? cream : gold;
        drawPixelRect(canvas, 8 + leanOffset, flameY, 4, 6, flameColor1);
        drawPixelRect(canvas, 20 + leanOffset, flameY, 4, 6, flameColor2);
        drawPixelRect(canvas, 14 + leanOffset, flameY + 2, 4, 6, flameColor1);
      }
    });
  }

  static ui.Picture getObstacleSprite(String type, [int frame = 0]) {
    final key = "obs_${type}_$frame";
    return getSprite(key, 16, 16, () => _drawObstacle(type, frame));
  }

  static ui.Picture _drawObstacle(String type, int frame) {
    return createPixelBitmap(16, 16, (canvas) {
      final ink = Palette.ink.value;
      final ash = Palette.ash.value;
      final stone = Palette.stone.value;
      final clay = Palette.clay.value;
      final red = Palette.red.value;
      final voidB = Palette.voidBlack.value;

      switch (type) {
        case "manhole":
          drawPixelRect(canvas, 2, 4, 12, 8, stone);
          drawPixelOutline(canvas, 2, 4, 12, 8, ink);
          drawPixelRect(canvas, 5, 7, 6, 2, voidB);
          break;
        case "garbage":
          drawPixelRect(canvas, 1, 3, 14, 10, clay);
          drawPixelOutline(canvas, 1, 3, 14, 10, ink);
          drawPixelRect(canvas, 3, 5, 4, 4, ash);
          drawPixelRect(canvas, 9, 6, 3, 3, Palette.forest.value);
          break;
        case "stone":
          drawPixelRect(canvas, 3, 4, 10, 9, ash);
          drawPixelOutline(canvas, 3, 4, 10, 9, ink);
          drawPixelRect(canvas, 5, 6, 4, 3, Palette.bone.value);
          break;
        case "barrier":
          drawPixelRect(canvas, 1, 2, 14, 12, red);
          drawPixelOutline(canvas, 1, 2, 14, 12, ink);
          drawPixelRect(canvas, 3, 4, 10, 3, Palette.paper.value);
          drawPixelRect(canvas, 3, 9, 10, 3, Palette.paper.value);
          break;
        case "puddle":
          drawPixelRect(canvas, 2, 5, 12, 7, Palette.blue.value);
          drawPixelOutline(canvas, 2, 5, 12, 7, Palette.navy.value);
          drawPixelRect(canvas, 4, 7, 4, 2, Palette.cyan.value);
          break;
        default:
          drawPixelRect(canvas, 2, 2, 12, 12, red);
          drawPixelOutline(canvas, 2, 2, 12, 12, ink);
      }
    });
  }

  static ui.Picture getCowSprite(int frame) {
    return getSprite("cow_$frame", 32, 24, () => _drawCow(frame));
  }

  static ui.Picture _drawCow(int frame) {
    return createPixelBitmap(32, 24, (canvas) {
      final ink = Palette.ink.value;
      final white = Palette.paper.value;
      final black = Palette.shadow.value;
      final pink = Palette.blush.value;
      final bounce = frame % 2 == 1 ? 1 : 0;

      drawPixelRect(canvas, 4, 6 + bounce, 22, 12, white);
      drawPixelOutline(canvas, 4, 6 + bounce, 22, 12, ink);
      drawPixelRect(canvas, 8, 8 + bounce, 6, 6, black);
      drawPixelRect(canvas, 18, 10 + bounce, 5, 5, black);
      drawPixelRect(canvas, 22, 2 + bounce, 8, 10, white);
      drawPixelOutline(canvas, 22, 2 + bounce, 8, 10, ink);
      drawPixelRect(canvas, 28, 6 + bounce, 3, 4, pink);

      final leg1X = frame == 0 || frame == 2 ? 6 : 4;
      final leg2X = frame == 0 || frame == 2 ? 20 : 22;
      drawPixelRect(canvas, leg1X, 18 + bounce, 4, 5, ink);
      drawPixelRect(canvas, leg2X, 18 + bounce, 4, 5, ink);
    });
  }

  static ui.Picture getCoinSprite(int frame) {
    return getSprite("coin_$frame", 8, 8, () => _drawCoin(frame));
  }

  static ui.Picture _drawCoin(int frame) {
    return createPixelBitmap(8, 8, (canvas) {
      final ink = Palette.ink.value;
      final gold = Palette.gold.value;
      final cream = Palette.cream.value;

      final width = [8, 6, 4, 6][frame % 4];
      final left = (8 - width) / 2;

      drawPixelRect(canvas, left.toInt(), 1, width, 6, gold);
      drawPixelOutline(canvas, left.toInt(), 1, width, 6, ink);
      if (width >= 6) {
        drawPixelRect(canvas, left.toInt() + 2, 3, 2, 2, cream);
      }
    });
  }

  static ui.Picture getPowerUpSprite(String type, int frame) {
    final key = "pup_${type}_$frame";
    return getSprite(key, 16, 16, () => _drawPowerUp(type, frame));
  }

  static ui.Picture _drawPowerUp(String type, int frame) {
    return createPixelBitmap(16, 16, (canvas) {
      final ink = Palette.ink.value;
      final gold = Palette.gold.value;
      final cyan = Palette.cyan.value;
      final red = Palette.red.value;
      final lime = Palette.lime.value;

      drawPixelRect(canvas, 1, 1, 14, 14, Palette.slate.value);
      drawPixelOutline(canvas, 1, 1, 14, 14, ink);

      switch (type) {
        case "turbo":
          final c = frame % 2 == 0 ? gold : Palette.cream.value;
          drawPixelRect(canvas, 8, 3, 4, 4, c);
          drawPixelRect(canvas, 6, 6, 6, 4, c);
          drawPixelRect(canvas, 4, 9, 4, 4, c);
          break;
        case "magnet":
          drawPixelRect(canvas, 4, 4, 3, 8, red);
          drawPixelRect(canvas, 9, 4, 3, 8, red);
          drawPixelRect(canvas, 4, 10, 8, 3, red);
          drawPixelRect(canvas, 4, 4, 3, 3, Palette.paper.value);
          drawPixelRect(canvas, 9, 4, 3, 3, Palette.paper.value);
          break;
        case "shield":
          final c = frame % 2 == 0 ? cyan : Palette.electric.value;
          drawPixelRect(canvas, 4, 3, 8, 8, c);
          drawPixelRect(canvas, 6, 11, 4, 3, c);
          break;
        case "moneyrain":
          final c = frame % 2 == 0 ? lime : Palette.green.value;
          drawPixelRect(canvas, 3, 5, 10, 6, c);
          drawPixelRect(canvas, 7, 7, 2, 2, gold);
          break;
      }
    });
  }

  static ui.Picture getPassengerSprite(int typeIndex, int frame) {
    final key = "pass_${typeIndex}_$frame";
    return getSprite(key, 16, 24, () => _drawPassenger(typeIndex, frame));
  }

  static ui.Picture _drawPassenger(int typeIndex, int frame) {
    return createPixelBitmap(16, 24, (canvas) {
      final ink = Palette.ink.value;
      final skin = Palette.skinLight.value;
      final bounce = frame % 2 == 1 ? 1 : 0;

      final clothColor = [
        Palette.red.value,
        Palette.navy.value,
        Palette.leaf.value,
        Palette.violet.value,
        Palette.amber.value,
        Palette.cyan.value,
      ][typeIndex % 6];

      drawPixelRect(canvas, 4, 2 + bounce, 8, 7, skin);
      drawPixelOutline(canvas, 4, 2 + bounce, 8, 7, ink);
      drawPixelRect(canvas, 3, 9 + bounce, 10, 11, clothColor);
      drawPixelOutline(canvas, 3, 9 + bounce, 10, 11, ink);

      if (frame % 2 == 1) {
        drawPixelRect(canvas, 12, 4, 3, 6, skin);
      } else {
        drawPixelRect(canvas, 12, 8, 3, 6, skin);
      }
    });
  }

  static ui.Picture getPedestrianSprite(int frame, bool isDodging) {
    final key = "ped_${frame}_$isDodging";
    return getSprite(key, 16, 24, () => _drawPedestrian(frame, isDodging));
  }

  static ui.Picture _drawPedestrian(int frame, bool isDodging) {
    return createPixelBitmap(16, 24, (canvas) {
      final ink = Palette.ink.value;
      final skin = Palette.skinMid.value;
      final shirt = Palette.orange.value;
      final pants = Palette.slate.value;
      final bounce = frame % 2 == 1 ? 1 : 0;
      final dodgeShift = isDodging ? 3 : 0;

      drawPixelRect(canvas, 4 + dodgeShift, 2 + bounce, 8, 6, skin);
      drawPixelOutline(canvas, 4 + dodgeShift, 2 + bounce, 8, 6, ink);
      drawPixelRect(canvas, 3 + dodgeShift, 8 + bounce, 10, 8, shirt);
      drawPixelOutline(canvas, 3 + dodgeShift, 8 + bounce, 10, 8, ink);
      drawPixelRect(canvas, 4 + dodgeShift, 16 + bounce, 8, 7, pants);
    });
  }

  static void drawBitmapText(
    ui.Canvas canvas,
    String text,
    int startX,
    int startY, {
    int colorArgb = 0xFFF4F0E6,
    int scale = 1,
    bool useBanglaDigits = false,
  }) {
    final processedText = useBanglaDigits ? _convertToBanglaDigits(text) : text;
    var curX = startX;

    for (final ch in processedText.runes) {
      final glyph = _getGlyphBitmap(String.fromCharCode(ch), colorArgb);
      if (glyph != null) {
        canvas.save();
        canvas.translate(curX.toDouble(), startY.toDouble());
        canvas.scale(scale.toDouble(), scale.toDouble());
        canvas.drawPicture(glyph);
        canvas.restore();
        curX += (glyph.width + 1) * scale;
      } else {
        curX += 5 * scale;
      }
    }
  }

  static String _convertToBanglaDigits(String text) {
    const banglaDigits = ['০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'];
    final sb = StringBuffer();
    for (final c in text.runes) {
      if (c >= 0x30 && c <= 0x39) {
        sb.write(banglaDigits[c - 0x30]);
      } else {
        sb.writeCharCode(c);
      }
    }
    return sb.toString();
  }

  static ui.Picture? _getGlyphBitmap(String ch, int colorArgb) {
    final key = "glyph_${ch}_$colorArgb";
    return _bitmapCache.putIfAbsent(key, () => _createGlyphBitmap(ch, colorArgb));
  }

  static ui.Picture _createGlyphBitmap(String ch, int colorArgb) {
    final glyphData = glyphMap[ch] ?? glyphMap['?'];
    if (glyphData == null) {
      return createPixelBitmap(6, 8, (canvas) {});
    }

    return createPixelBitmap(6, 8, (canvas) {
      final paint = Paint()..color = Color(colorArgb)..isAntiAlias = false;
      for (var r = 0; r < 8; r++) {
        final line = glyphData[r];
        for (var c = 0; c < 5; c++) {
          if ((line & (1 << (5 - c))) != 0) {
            canvas.drawRect(Rect.fromLTWH(c.toDouble(), r.toDouble(), 1, 1), paint);
          }
        }
      }
    });
  }

  static ui.Picture getBanglaTextSprite(String key) {
    final cacheKey = "bn_text_$key";
    return _bitmapCache.putIfAbsent(cacheKey, () {
      switch (key) {
        case "abar_chalai":
          return _createBanglaWordBitmap("আবার চালাই", 72, 16, Palette.gold.value, Palette.maroon.value);
        case "mama_side_den":
          return _createBanglaWordBitmap("মামা সাইড দেন!", 96, 16, Palette.cream.value, Palette.red.value);
        case "vada_ache":
          return _createBanglaWordBitmap("ভাড়া আছে!", 64, 16, Palette.amber.value, Palette.navy.value);
        case "game_over":
          return _createBanglaWordBitmap("গেম ওভার", 64, 18, Palette.coral.value, Palette.ink.value);
        case "dhaka_champion":
          return _createBanglaWordBitmap("ঢাকা চ্যাম্পিয়ন", 96, 16, Palette.lime.value, Palette.forest.value);
        case "notun_record":
          return _createBanglaWordBitmap("নতুন রেকর্ড!", 80, 16, Palette.gold.value, Palette.plum.value);
        case "super_mama":
          return _createBanglaWordBitmap("সুপার মামা!", 72, 16, Palette.cyan.value, Palette.blue.value);
        default:
          return _createBanglaWordBitmap("দেশি টার্বো রাশ", 96, 18, Palette.amber.value, Palette.maroon.value);
      }
    });
  }

  static ui.Picture _createBanglaWordBitmap(String textLabel, int width, int height, int textColorArgb, int bgColorArgb) {
    return createPixelBitmap(width, height, (canvas) {
      drawPixelRect(canvas, 0, 0, width, height, bgColorArgb);
      drawPixelOutline(canvas, 0, 0, width, height, Palette.ink.value);
      drawPixelRect(canvas, 1, 1, width - 2, 1, Palette.paper.value);
      drawBitmapText(canvas, textLabel, 6, (height - 8) ~/ 2, colorArgb: textColorArgb, scale: 1);
    });
  }

  static void draw9SlicePanel(ui.Canvas canvas, int left, int top, int width, int height) {
    final ink = Palette.ink.value;
    final shadow = Palette.shadow.value;
    final bone = Palette.bone.value;

    drawPixelRect(canvas, left, top, width, height, shadow);
    drawPixelRect(canvas, left + 1, top + 1, width - 2, 2, bone);
    drawPixelRect(canvas, left + 1, top + 1, 2, height - 2, bone);
    drawPixelRect(canvas, left + 1, top + height - 3, width - 2, 2, ink);
    drawPixelRect(canvas, left + width - 3, top + 1, 2, height - 2, ink);
    drawPixelRect(canvas, left + 3, top + 3, width - 6, height - 6, Palette.slate.value);
    drawPixel(canvas, left, top, ink);
    drawPixel(canvas, left + width - 1, top, ink);
    drawPixel(canvas, left, top + height - 1, ink);
    drawPixel(canvas, left + width - 1, top + height - 1, ink);
  }

  static const glyphMap = {
    '0': [0x1E, 0x21, 0x25, 0x29, 0x21, 0x1E, 0x00, 0x00],
    '1': [0x0C, 0x1C, 0x0C, 0x0C, 0x0C, 0x1E, 0x00, 0x00],
    '2': [0x1E, 0x21, 0x02, 0x0C, 0x10, 0x3F, 0x00, 0x00],
    '3': [0x1E, 0x21, 0x0E, 0x01, 0x21, 0x1E, 0x00, 0x00],
    '4': [0x22, 0x22, 0x22, 0x3F, 0x02, 0x02, 0x00, 0x00],
    '5': [0x3F, 0x20, 0x3E, 0x01, 0x21, 0x1E, 0x00, 0x00],
    '6': [0x1E, 0x20, 0x3E, 0x21, 0x21, 0x1E, 0x00, 0x00],
    '7': [0x3F, 0x01, 0x02, 0x04, 0x08, 0x10, 0x00, 0x00],
    '8': [0x1E, 0x21, 0x1E, 0x21, 0x21, 0x1E, 0x00, 0x00],
    '9': [0x1E, 0x21, 0x1F, 0x01, 0x01, 0x1E, 0x00, 0x00],
    '০': [0x1E, 0x21, 0x21, 0x21, 0x21, 0x1E, 0x00, 0x00],
    '১': [0x0E, 0x11, 0x0E, 0x04, 0x04, 0x0E, 0x00, 0x00],
    '২': [0x1C, 0x02, 0x1C, 0x10, 0x3E, 0x00, 0x00, 0x00],
    '৩': [0x1E, 0x02, 0x0E, 0x02, 0x3C, 0x00, 0x00, 0x00],
    '৪': [0x11, 0x1B, 0x0E, 0x1B, 0x11, 0x00, 0x00, 0x00],
    '৫': [0x3C, 0x20, 0x38, 0x04, 0x38, 0x00, 0x00, 0x00],
    '৬': [0x1E, 0x20, 0x3C, 0x22, 0x1C, 0x00, 0x00, 0x00],
    '৭': [0x1C, 0x02, 0x0C, 0x10, 0x20, 0x00, 0x00, 0x00],
    '৮': [0x22, 0x22, 0x1C, 0x0A, 0x12, 0x00, 0x00, 0x00],
    '৯': [0x1C, 0x22, 0x1E, 0x02, 0x1C, 0x00, 0x00, 0x00],
    '৳': [0x3E, 0x08, 0x1C, 0x28, 0x28, 0x1E, 0x00, 0x00],
    'A': [0x0C, 0x12, 0x21, 0x3F, 0x21, 0x21, 0x00, 0x00],
    'B': [0x3E, 0x21, 0x3E, 0x21, 0x21, 0x3E, 0x00, 0x00],
    'C': [0x1E, 0x21, 0x20, 0x20, 0x21, 0x1E, 0x00, 0x00],
    'D': [0x3C, 0x22, 0x21, 0x21, 0x22, 0x3C, 0x00, 0x00],
    'E': [0x3F, 0x20, 0x3E, 0x20, 0x20, 0x3F, 0x00, 0x00],
    'F': [0x3F, 0x20, 0x3E, 0x20, 0x20, 0x20, 0x00, 0x00],
    'G': [0x1E, 0x21, 0x20, 0x27, 0x21, 0x1E, 0x00, 0x00],
    'H': [0x21, 0x21, 0x3F, 0x21, 0x21, 0x21, 0x00, 0x00],
    'I': [0x1C, 0x08, 0x08, 0x08, 0x08, 0x1C, 0x00, 0x00],
    'J': [0x07, 0x02, 0x02, 0x02, 0x22, 0x1C, 0x00, 0x00],
    'K': [0x21, 0x22, 0x3C, 0x22, 0x21, 0x21, 0x00, 0x00],
    'L': [0x20, 0x20, 0x20, 0x20, 0x20, 0x3F, 0x00, 0x00],
    'M': [0x21, 0x33, 0x2D, 0x21, 0x21, 0x21, 0x00, 0x00],
    'N': [0x21, 0x31, 0x29, 0x25, 0x23, 0x21, 0x00, 0x00],
    'O': [0x1E, 0x21, 0x21, 0x21, 0x21, 0x1E, 0x00, 0x00],
    'P': [0x3E, 0x21, 0x3E, 0x20, 0x20, 0x20, 0x00, 0x00],
    'Q': [0x1E, 0x21, 0x21, 0x25, 0x22, 0x1D, 0x00, 0x00],
    'R': [0x3E, 0x21, 0x3E, 0x24, 0x22, 0x21, 0x00, 0x00],
    'S': [0x1E, 0x20, 0x1E, 0x01, 0x01, 0x1E, 0x00, 0x00],
    'T': [0x3F, 0x08, 0x08, 0x08, 0x08, 0x08, 0x00, 0x00],
    'U': [0x21, 0x21, 0x21, 0x21, 0x21, 0x1E, 0x00, 0x00],
    'V': [0x21, 0x21, 0x21, 0x12, 0x12, 0x0C, 0x00, 0x00],
    'W': [0x21, 0x21, 0x21, 0x2D, 0x33, 0x21, 0x00, 0x00],
    'X': [0x21, 0x12, 0x0C, 0x0C, 0x12, 0x21, 0x00, 0x00],
    'Y': [0x21, 0x12, 0x0C, 0x08, 0x08, 0x08, 0x00, 0x00],
    'Z': [0x3F, 0x02, 0x04, 0x08, 0x10, 0x3F, 0x00, 0x00],
    '!': [0x08, 0x08, 0x08, 0x08, 0x00, 0x08, 0x00, 0x00],
    '?': [0x1E, 0x02, 0x0C, 0x08, 0x00, 0x08, 0x00, 0x00],
    ':': [0x00, 0x0C, 0x0C, 0x00, 0x0C, 0x0C, 0x00, 0x00],
  };
}
