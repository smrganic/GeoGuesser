package com.example.geoguesser.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.geoguesser.R

class SoundPoolPlayer(context: Context) : AudioPlayer {

    private val priority: Int = 1
    private val maxStreams: Int = 3

    private val leftVolume = 1f
    private val rightVolume = 1f
    private val shouldLoop = 0
    private val playbackRate = 1f

    private val soundPool: SoundPool

    private var soundLoaded: Boolean = false
    private val soundMap: HashMap<Int, Int> = HashMap()

    init {

        // Setup defaults
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(maxStreams)
            .build()

        // Notify when all the sounds have loaded
        soundPool.setOnLoadCompleteListener { _, _, _ -> soundLoaded = true }

        soundMap[R.raw.marker] = soundPool.load(context, R.raw.marker, priority)
    }

    override fun playSound(id: Int) {

        // Identify the sound and play it, could be used to add more sounds.
        if (soundLoaded)
            when (id) {
                R.raw.marker -> soundPool.play(
                    soundMap[id]!!,
                    leftVolume,
                    rightVolume,
                    priority,
                    shouldLoop,
                    playbackRate
                )
            }
    }
}