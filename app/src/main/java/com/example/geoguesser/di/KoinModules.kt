package com.example.geoguesser.di

import com.example.geoguesser.mvvm.GameData
import com.example.geoguesser.mvvm.LocationData
import com.example.geoguesser.mvvm.LocationViewModel
import com.example.geoguesser.mvvm.MapElementsData
import com.example.geoguesser.network.Networking
import com.example.geoguesser.network.Parser
import com.example.geoguesser.network.RandomCoordinatesParser
import com.example.geoguesser.sounds.AudioPlayer
import com.example.geoguesser.sounds.SoundPoolPlayer
import com.example.geoguesser.utils.Preferences
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val prefsModule = module {

    single { Preferences(androidContext()) }

    factory { OkHttpClient() }
    single { Networking(get()) }

    // Pass an instance of Random coordinates parser to any parser requests
    single<Parser<String, LatLng>> { RandomCoordinatesParser() }

    // Pass an instance of SoundPool to any AudioPlayer requests
    single<AudioPlayer> { SoundPoolPlayer(androidContext()) }
}

val modelModule = module {
    factory { LocationData() }
    factory { MapElementsData() }
    factory { GameData() }
}

val viewModelModule = module {
    viewModel { LocationViewModel(get(), get(), get()) }
}