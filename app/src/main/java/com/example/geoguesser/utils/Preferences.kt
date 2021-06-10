package com.example.geoguesser.utils

import android.content.Context

class Preferences(context: Context) {
    private val FILE = "GeoGuesserPreferences"
    private val SETUP_KEY = "SETUP_KEY"
    private val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val prefsEditor = preferences.edit()

    fun saveSetup(isSetupComplete: Boolean) = prefsEditor.putBoolean(SETUP_KEY, isSetupComplete).apply()

    fun isSetupComplete(): Boolean = preferences.getBoolean(SETUP_KEY, false)
}