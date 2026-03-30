package com.hanyajasa.blockblast

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var placeSoundId: Int = -1
    private var clearSoundId: Int = -1
    private var selectSoundId: Int = -1
    private var gameOverSoundId: Int = -1

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Attempt to load sounds from res/raw
        // Note: The user needs to add these files to app/src/main/res/raw/
        placeSoundId = loadSound(context, "place_block")
        clearSoundId = loadSound(context, "clear_line")
        selectSoundId = loadSound(context, "select_block")
        gameOverSoundId = loadSound(context, "game_over")
    }

    private fun loadSound(context: Context, resourceName: String): Int {
        val resId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        return if (resId != 0) {
            soundPool.load(context, resId, 1)
        } else {
            -1
        }
    }

    fun playPlaceSound() {
        playSound(placeSoundId)
    }

    fun playClearSound() {
        playSound(clearSoundId)
    }

    fun playSelectSound() {
        playSound(selectSoundId)
    }

    fun playGameOverSound() {
        playSound(gameOverSoundId)
    }

    private fun playSound(soundId: Int) {
        if (soundId != -1) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
