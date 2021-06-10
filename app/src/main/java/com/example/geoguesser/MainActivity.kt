package com.example.geoguesser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geoguesser.ui.GeoAppIntro
import com.example.geoguesser.ui.MapsActivity
import com.example.geoguesser.ui.StartGameActivity
import com.example.geoguesser.utils.Preferences
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val preferences: Preferences by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent: Intent
        if(preferences.isSetupComplete()) {
            intent = Intent(this, StartGameActivity::class.java)
            startActivity(intent)
        }else{
            intent = Intent(this, GeoAppIntro::class.java)
            startActivity(intent)
        }

    }
}