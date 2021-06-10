package com.example.geoguesser.di

import com.example.geoguesser.utils.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val prefsModule = module {
    single { Preferences(androidContext()) }
}