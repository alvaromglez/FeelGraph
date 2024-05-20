package com.example.feelgraph

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("opencv_java4")
    }
}