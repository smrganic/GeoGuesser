package com.example.geoguesser.di

import com.example.geoguesser.network.Networking
import com.example.geoguesser.network.Parser
import com.example.geoguesser.network.RandomCoordinatesParser
import com.example.geoguesser.sounds.AudioPlayer
import com.example.geoguesser.sounds.SoundPoolPlayer
import com.example.geoguesser.utils.Preferences
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val prefsModule = module {
    single { Preferences(androidContext()) }
    factory { OkHttpClient() }
    single { Networking(get()) }
    single<Parser<String, LatLng>> { RandomCoordinatesParser() }
    single<AudioPlayer> { SoundPoolPlayer(androidContext()) }
}