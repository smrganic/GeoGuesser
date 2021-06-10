package com.example.geoguesser.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.databinding.ActivityStartGameBinding
import com.example.geoguesser.utils.CallBackListener
import com.example.geoguesser.utils.LatLngGenerator
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng

class StartGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fab.setOnClickListener{
            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

}