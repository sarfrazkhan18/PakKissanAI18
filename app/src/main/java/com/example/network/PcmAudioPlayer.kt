package com.example.network

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

class PcmAudioPlayer {

    private val tag = "PcmAudioPlayer"
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 24000

    @Synchronized
    fun start() {
        if (audioTrack != null) return
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
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
                .setBufferSizeInBytes(minBufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            Log.d(tag, "AudioTrack playing started at 24000Hz Mono")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize AudioTrack: ${e.message}", e)
        }
    }

    @Synchronized
    fun write(pcmData: ByteArray) {
        val track = audioTrack ?: return
        try {
            track.write(pcmData, 0, pcmData.size)
        } catch (e: Exception) {
            Log.e(tag, "Error writing to AudioTrack: ${e.message}")
        }
    }

    @Synchronized
    fun stop() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            Log.d(tag, "AudioTrack stopped and released")
        } catch (e: Exception) {
            Log.e(tag, "Error stopping AudioTrack: ${e.message}")
        } finally {
            audioTrack = null
        }
    }
}
