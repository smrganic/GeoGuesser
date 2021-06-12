package com.example.geoguesser.utils

import android.content.Context

class Preferences(context: Context) {

    private val FILE = "GeoGuesserPreferences"
    private val SETUP_KEY = "SETUP_KEY"
    private val HIGH_SCORE_KEY = "HIGH_SCORE_KEY"
    private val preferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private val prefsEditor = preferences.edit()

    fun saveSetup(isSetupComplete: Boolean) =
        prefsEditor.putBoolean(SETUP_KEY, isSetupComplete).apply()

    fun isSetupComplete(): Boolean = preferences.getBoolean(SETUP_KEY, false)

    fun setHighScore(highScore: Float) = prefsEditor.putFloat(HIGH_SCORE_KEY, highScore).apply()

    fun getHighScore(): Float = preferences.getFloat(HIGH_SCORE_KEY, Float.MAX_VALUE)
}