package com.example.geoguesser

import android.app.Application
import com.example.geoguesser.di.modelModule
import com.example.geoguesser.di.prefsModule
import com.example.geoguesser.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GeoGuesser : Application() {

    // Application class start koin with the needed modules and that is it.
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@GeoGuesser)
            modules(prefsModule, modelModule, viewModelModule)
        }
    }
}