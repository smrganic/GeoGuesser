package com.example.geoguesser

import android.app.Application
import com.example.geoguesser.di.prefsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GeoGuesser : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GeoGuesser)
            modules(prefsModule)
        }
    }
}