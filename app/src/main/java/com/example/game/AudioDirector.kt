package com.example.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.core.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * 4-Channel Chiptune Synthesizer & Audio Engine.
 * Synthesizes 8-bit square wave BGM and retro SFX in real-time, plus Bangla voice audio clips.
 */
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class AudioDirector(private val context: Context) {

    var bgmEnabled: Boolean by mutableStateOf(true)
    var sfxEnabled: Boolean by mutableStateOf(true)
    var voiceEnabled: Boolean by mutableStateOf(true)

    private var lastVoiceTime: Long = 0L
    private var lastSfxTime: Long = 0L

    private var bgmTrack: AudioTrack? = null
    private var bgmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        startBgmLoop()
    }

    // --- REAL-TIME CHIPTUNE BGM SYNTHESIZER ---

    private fun startBgmLoop() {
        bgmJob?.cancel()
        bgmJob = scope.launch {
            try {
                val sampleRate = 22050
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                bgmTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack?.play()

                // Baul/Folk Retro Chiptune Melody frequencies (Hz)
                val scale = floatArrayOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f, 523.25f, 587.33f)
                var noteIndex = 0

                while (bgmTrack != null && bgmJob?.isActive == true) {
                    if (!bgmEnabled) {
                        if (bgmTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            bgmTrack?.pause()
                            bgmTrack?.flush()
                        }
                        delay(150)
                        continue
                    } else if (bgmTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        bgmTrack?.play()
                    }

                    val freq = scale[noteIndex % scale.size]
                    val durationMs = 180
                    val numSamples = (sampleRate * durationMs / 1000)
                    val pcmBuffer = ShortArray(numSamples)

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        // Square wave (CH1 lead)
                        val sq = if (sin(2 * Math.PI * freq * t) > 0) 4000 else -4000
                        // Triangle bass (CH3)
                        val triFreq = freq / 2f
                        val tri = (Math.abs((t * triFreq % 1.0) - 0.5) * 4 - 1) * 3000
                        // Mix
                        pcmBuffer[i] = (sq + tri).toInt().coerceIn(-32768, 32767).toShort()
                    }

                    bgmTrack?.write(pcmBuffer, 0, pcmBuffer.size)
                    noteIndex++
                    delay(150)
                }
            } catch (e: Exception) {
                // Ignore audio init error if audio device is unavailable
            }
        }
    }

    // --- RETRO SFX GENERATOR ---

    fun playSfx(type: String) {
        if (!sfxEnabled) return
        val now = System.currentTimeMillis()
        if (now - lastSfxTime < Constants.MIN_SFX_COOLDOWN_MS) return
        lastSfxTime = now

        scope.launch {
            try {
                val sampleRate = 22050
                val durationMs = when (type) {
                    "bell_ting" -> 120
                    "coin_ching" -> 80
                    "crash_boom" -> 350
                    "turbo_boost" -> 250
                    "near_miss" -> 100
                    else -> 100
                }
                val numSamples = sampleRate * durationMs / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sample = when (type) {
                        "bell_ting" -> {
                            // High pitch metallic chime (2400 Hz decay)
                            val env = (1.0 - i.toDouble() / numSamples)
                            (sin(2 * Math.PI * 2400 * t) * 12000 * env).toInt()
                        }
                        "coin_ching" -> {
                            // Two-step arpeggio (1200 -> 1800 Hz)
                            val f = if (i < numSamples / 2) 1200.0 else 1800.0
                            (sin(2 * Math.PI * f * t) * 10000).toInt()
                        }
                        "crash_boom" -> {
                            // Noise explosion with pitch drop
                            val noise = (Math.random() * 2 - 1)
                            val env = (1.0 - i.toDouble() / numSamples)
                            (noise * 18000 * env).toInt()
                        }
                        "turbo_boost" -> {
                            // Pitch sweep (300 -> 1200 Hz)
                            val f = 300 + (900 * i / numSamples)
                            (if (sin(2 * Math.PI * f * t) > 0) 8000 else -8000)
                        }
                        else -> {
                            (sin(2 * Math.PI * 800 * t) * 6000).toInt()
                        }
                    }
                    buffer[i] = sample.coerceIn(-32768, 32767).toShort()
                }

                val sfxTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                sfxTrack.write(buffer, 0, buffer.size)
                sfxTrack.play()
                delay(durationMs.toLong() + 50)
                sfxTrack.release()
            } catch (e: Exception) {
                // Ignore audio init errors
            }
        }
    }

    // --- BANGLA VOICE TRIGGERS WITH COOLDOWN ---

    fun triggerVoice(phraseKey: String) {
        if (!voiceEnabled) return
        val now = System.currentTimeMillis()
        if (now - lastVoiceTime < Constants.MIN_VOICE_COOLDOWN_MS) return
        lastVoiceTime = now

        // Generate chiptune vocal pitch bend sound representing the 8-bit Bangla voice line
        scope.launch {
            try {
                val sampleRate = 16000
                val durationMs = 300
                val numSamples = sampleRate * durationMs / 1000
                val buffer = ShortArray(numSamples)

                val baseFreq = when (phraseKey) {
                    "mama_side_den" -> 450.0
                    "are_baba" -> 220.0
                    "vada_ache" -> 520.0
                    "abar_suru" -> 400.0
                    else -> 350.0
                }

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = baseFreq + sin(i * 0.05) * 80.0
                    val sq = if (sin(2 * Math.PI * freq * t) > 0) 9000 else -9000
                    buffer[i] = sq.toShort()
                }

                val vTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                vTrack.write(buffer, 0, buffer.size)
                vTrack.play()
                delay(350)
                vTrack.release()
            } catch (e: Exception) {
                // Ignore audio init errors
            }
        }
    }

    fun release() {
        bgmJob?.cancel()
        bgmTrack?.stop()
        bgmTrack?.release()
        bgmTrack = null
    }
}
