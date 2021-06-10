package com.example.geoguesser

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class GeoGuesser : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GeoGuesser)
        }
    }
}