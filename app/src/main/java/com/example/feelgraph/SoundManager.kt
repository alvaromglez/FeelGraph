package com.example.feelgraph

import android.content.Context
import android.media.MediaPlayer

class SoundManager(private val context: Context) {
    private fun playSound(soundResId: Int){
        val mediaPlayer: MediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }

    fun playForLineType(lineType: LineType){
        val soundResId = when (lineType) {
            LineType.ASCENDING -> R.raw.fast_intermittent_tone
            LineType.DESCENDING -> R.raw.profoundly_slow_intermittent_tone
            LineType.HORIZONTAL -> R.raw.ultra_slow_intermittent_tone
        }
        playSound(soundResId)
    }

    fun playStartPointSound() {
        playSound(R.raw.graph_start_sound)
    }

}