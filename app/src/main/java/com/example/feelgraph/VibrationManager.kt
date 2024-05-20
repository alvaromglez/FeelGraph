package com.example.feelgraph

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator


class VibrationManager(private val context: Context) {

    companion object{
        private var instance: VibrationManager? = null
        private var intensity = 50

        fun getInstance(context: Context): VibrationManager{
            if(instance == null){
                instance = VibrationManager(context)
            }
            return instance!!
        }

        fun setIntensity(newIntensity: Int){
            intensity = (newIntensity * 255) / 100
        }

    }

    private val vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


    private fun vibrate(pattern: LongArray, repeat: Boolean){
        val effect = VibrationEffect.createWaveform(pattern, IntArray(pattern.size) { i ->
            if (i % 2 == 0) 0 else intensity
        }, if (repeat) 0 else -1)
        vibrator.vibrate(effect)
    }

    //Vibracion intermitente para inicio del grafico.
    fun vibrateStartPoint(){
        val pattern = longArrayOf(0,100, 50,100)
        vibrate(pattern, false)
    }

    fun vibrateForLineType(lineType: LineType) {
            val pattern = when(lineType) {
                LineType.ASCENDING -> longArrayOf(0,50,25,50,25,50,25,50)
                LineType.DESCENDING -> longArrayOf(0,150,700,150)
                LineType.HORIZONTAL -> longArrayOf(0,200,200,200)
            }
            vibrate(pattern, false)
    }

}