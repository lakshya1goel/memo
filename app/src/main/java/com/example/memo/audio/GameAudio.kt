package com.example.memo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log
import com.example.memo.R
import java.util.ArrayDeque

// Owned by MainActivity: resume/pause with the Activity and release on destruction.
class GameAudio(context: Context) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val musicAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val loadedSounds = mutableSetOf<Int>()
    private val recentStreams = ArrayDeque<Int>()
    private var released = false
    private var foreground = false
    private var hasAudioFocus = false
    private var music: MediaPlayer? = null

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (!released && foreground) {
            when (change) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    startMusic()
                }
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    hasAudioFocus = false
                    pauseMusic()
                    stopEffects()
                }
            }
        }
    }

    private val focusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(musicAttributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener(focusListener)
            .build()
    } else {
        null
    }

    private val tapSound: Int
    private val matchSound: Int
    private val missSound: Int

    init {
        // SoundPool loads asynchronously; play only once a sample is ready.
        soundPool.setOnLoadCompleteListener { _, soundId, status ->
            if (!released && status == 0) loadedSounds.add(soundId)
        }
        tapSound = soundPool.load(appContext, R.raw.sfx_card_tap, 1)
        matchSound = soundPool.load(appContext, R.raw.sfx_pair_match, 1)
        missSound = soundPool.load(appContext, R.raw.sfx_pair_miss, 1)

        music = MediaPlayer.create(
            appContext,
            R.raw.music_garden_loop,
            musicAttributes,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )?.apply {
            isLooping = true
            setVolume(0.45f, 0.45f)
            setOnErrorListener { player, what, extra ->
                Log.w("GameAudio", "Music playback error: $what / $extra")
                if (music === player) music = null
                player.release()
                true
            }
        }
    }

    @Suppress("DEPRECATION")
    fun resume() {
        if (released) return
        foreground = true
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(requireNotNull(focusRequest))
        } else {
            audioManager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (hasAudioFocus) startMusic()
    }

    @Suppress("DEPRECATION")
    fun pause() {
        if (released) return
        foreground = false
        hasAudioFocus = false
        pauseMusic()
        stopEffects()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(requireNotNull(focusRequest))
        } else {
            audioManager.abandonAudioFocus(focusListener)
        }
    }

    fun playTap() = playEffect(tapSound, 0.70f)

    fun playMatch() = playEffect(matchSound, 0.85f)

    fun playMiss() = playEffect(missSound, 0.80f)

    fun release() {
        if (released) return
        pause()
        released = true
        soundPool.setOnLoadCompleteListener(null)
        soundPool.release()
        loadedSounds.clear()
        music?.release()
        music = null
    }

    private fun startMusic() {
        if (foreground && hasAudioFocus) {
            music?.let { if (!it.isPlaying) it.start() }
        }
    }

    private fun pauseMusic() {
        music?.let { if (it.isPlaying) it.pause() }
    }

    private fun playEffect(soundId: Int, volume: Float) {
        if (released || !foreground || !hasAudioFocus || soundId !in loadedSounds) return
        val streamId = soundPool.play(soundId, volume, volume, 1, 0, 1f)
        if (streamId != 0) {
            recentStreams.addLast(streamId)
            // Bound the bookkeeping; older one-shot effects have already finished.
            if (recentStreams.size > 8) soundPool.stop(recentStreams.removeFirst())
        }
    }

    private fun stopEffects() {
        recentStreams.forEach { soundPool.stop(it) }
        recentStreams.clear()
    }
}
