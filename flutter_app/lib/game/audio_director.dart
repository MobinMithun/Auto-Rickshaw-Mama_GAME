import 'dart:typed_data';
import 'dart:math';
import 'package:flutter/services.dart';
import 'package:audioplayers/audioplayers.dart';
import '../core/constants.dart';

class AudioDirector {
  bool bgmEnabled = true;
  bool sfxEnabled = true;
  bool voiceEnabled = true;

  int _lastVoiceTime = 0;
  int _lastSfxTime = 0;

  final AudioPlayer _bgmPlayer = AudioPlayer();
  final AudioPlayer _sfxPlayer = AudioPlayer();
  bool _bgmPlaying = false;

  AudioDirector() {
    _startBgmLoop();
  }

  void _startBgmLoop() async {
    try {
      final buffer = _generateBgmBuffer();
      await _bgmPlayer.play(BytesSource(buffer));
      _bgmPlaying = true;
    } catch (e) {
      // ignore
    }
  }

  Uint8List _generateBgmBuffer() {
    const sampleRate = 22050;
    const durationMs = 180;
    final numSamples = (sampleRate * durationMs / 1000).toInt();
    final samples = <int>[];

    final scale = [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33];
    var noteIndex = 0;

    while (samples.length < numSamples) {
      final freq = scale[noteIndex % scale.length];
      final numSamplesNote = (sampleRate * durationMs / 1000).toInt();

      for (var i = 0; i < numSamplesNote && samples.length < numSamples; i++) {
        final t = i / sampleRate;
        final sq = sin(2 * pi * freq * t) > 0 ? 4000 : -4000;
        final triFreq = freq / 2;
        final tri = ((t * triFreq % 1) - 0.5).abs() * 4 - 1;
        final mixed = (sq + tri * 3000).toInt().clamp(-32768, 32767);
        samples.add(mixed);
      }
      noteIndex++;
    }

    return _encodeWav(samples, sampleRate);
  }

  void playSfx(String type) {
    if (!sfxEnabled) return;
    final now = DateTime.now().millisecondsSinceEpoch;
    if (now - _lastSfxTime < Constants.minSfxCooldownMs) return;
    _lastSfxTime = now;

    final buffer = _generateSfxBuffer(type);
    _sfxPlayer.play(BytesSource(buffer));
  }

  Uint8List _generateSfxBuffer(String type) {
    const sampleRate = 22050;
    int durationMs;
    switch (type) {
      case "bell_ting":
        durationMs = 120;
        break;
      case "coin_ching":
        durationMs = 80;
        break;
      case "crash_boom":
        durationMs = 350;
        break;
      case "turbo_boost":
        durationMs = 250;
        break;
      case "near_miss":
        durationMs = 100;
        break;
      default:
        durationMs = 100;
    }

    final numSamples = (sampleRate * durationMs / 1000).toInt();
    final samples = <int>[];

    for (var i = 0; i < numSamples; i++) {
      final t = i / sampleRate;
      int sample;
      switch (type) {
        case "bell_ting":
          final env = 1.0 - (i / numSamples);
          sample = (sin(2 * pi * 2400 * t) * 12000 * env).toInt();
          break;
        case "coin_ching":
          final f = i < numSamples / 2 ? 1200.0 : 1800.0;
          sample = (sin(2 * pi * f * t) * 10000).toInt();
          break;
        case "crash_boom":
          final noise = Random().nextDouble() * 2 - 1;
          final env = 1.0 - (i / numSamples);
          sample = (noise * 18000 * env).toInt();
          break;
        case "turbo_boost":
          final f = 300 + (900 * i / numSamples);
          sample = sin(2 * pi * f * t) > 0 ? 8000 : -8000;
          break;
        default:
          sample = (sin(2 * pi * 800 * t) * 6000).toInt();
      }
      samples.add(sample.clamp(-32768, 32767));
    }

    return _encodeWav(samples, sampleRate);
  }

  void triggerVoice(String phraseKey) {
    if (!voiceEnabled) return;
    final now = DateTime.now().millisecondsSinceEpoch;
    if (now - _lastVoiceTime < Constants.minVoiceCooldownMs) return;
    _lastVoiceTime = now;

    final buffer = _generateVoiceBuffer(phraseKey);
    _sfxPlayer.play(BytesSource(buffer));
  }

  Uint8List _generateVoiceBuffer(String phraseKey) {
    const sampleRate = 16000;
    const durationMs = 300;
    final numSamples = (sampleRate * durationMs / 1000).toInt();
    final samples = <int>[];

    double baseFreq;
    switch (phraseKey) {
      case "mama_side_den":
        baseFreq = 450;
        break;
      case "are_baba":
        baseFreq = 220;
        break;
      case "vada_ache":
        baseFreq = 520;
        break;
      case "abar_suru":
        baseFreq = 400;
        break;
      default:
        baseFreq = 350;
    }

    for (var i = 0; i < numSamples; i++) {
      final t = i / sampleRate;
      final freq = baseFreq + sin(i * 0.05) * 80;
      final sq = sin(2 * pi * freq * t) > 0 ? 9000 : -9000;
      samples.add(sq.toInt().clamp(-32768, 32767));
    }

    return _encodeWav(samples, sampleRate);
  }

  Uint8List _encodeWav(List<int> samples, int sampleRate) {
    final byteData = ByteData(44 + samples.length * 2);
    final buffer = byteData.buffer.asUint8List();

    // RIFF header
    buffer[0] = 0x52; // R
    buffer[1] = 0x49; // I
    buffer[2] = 0x46; // F
    buffer[3] = 0x46; // F
    final dataSize = samples.length * 2;
    _writeInt32(buffer, 4, 36 + dataSize);
    buffer[8] = 0x57; // W
    buffer[9] = 0x41; // A
    buffer[10] = 0x56; // V
    buffer[11] = 0x45; // E

    // fmt chunk
    buffer[12] = 0x66; // f
    buffer[13] = 0x6D; // m
    buffer[14] = 0x74; // t
    buffer[15] = 0x20; // space
    _writeInt32(buffer, 16, 16);
    _writeInt16(buffer, 20, 1); // PCM
    _writeInt16(buffer, 22, 1); // mono
    _writeInt32(buffer, 24, sampleRate);
    _writeInt32(buffer, 28, sampleRate * 2);
    _writeInt16(buffer, 32, 2);
    _writeInt16(buffer, 34, 16);

    // data chunk
    buffer[36] = 0x64; // d
    buffer[37] = 0x61; // a
    buffer[38] = 0x74; // t
    buffer[39] = 0x61; // a
    _writeInt32(buffer, 40, dataSize);

    for (var i = 0; i < samples.length; i++) {
      final sample = samples[i];
      final byteOffset = 44 + i * 2;
      buffer[byteOffset] = (sample & 0xFF);
      buffer[byteOffset + 1] = ((sample >> 8) & 0xFF);
    }

    return buffer;
  }

  void _writeInt32(Uint8List buffer, int offset, int value) {
    buffer[offset] = (value & 0xFF);
    buffer[offset + 1] = ((value >> 8) & 0xFF);
    buffer[offset + 2] = ((value >> 16) & 0xFF);
    buffer[offset + 3] = ((value >> 24) & 0xFF);
  }

  void _writeInt16(Uint8List buffer, int offset, int value) {
    buffer[offset] = (value & 0xFF);
    buffer[offset + 1] = ((value >> 8) & 0xFF);
  }

  void release() {
    _bgmPlayer.dispose();
    _sfxPlayer.dispose();
  }
}
