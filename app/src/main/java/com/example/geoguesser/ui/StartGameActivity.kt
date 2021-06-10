package com.example.geoguesser.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geoguesser.R
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng

class StartGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_game)

        val streetView = supportFragmentManager.findFragmentById(R.id.streetView) as SupportStreetViewPanoramaFragment?
        streetView?.getStreetViewPanoramaAsync { panorama -> savedInstanceState ?: panorama.setPosition(SYDNEY) }
    }
    companion object {
        // George St, Sydney
        private val SYDNEY = LatLng(-33.87365, 151.20689)
    }
}