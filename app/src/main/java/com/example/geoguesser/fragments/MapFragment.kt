package com.example.geoguesser.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import com.example.geoguesser.R
import com.example.geoguesser.sounds.AudioPlayer
import com.example.geoguesser.ui.MapsActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.android.ext.android.inject

class MapFragment: SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private var resetGame = false
    private val soundPoolPlayer: AudioPlayer by inject()
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(activity)
        enterTransition = inflater.inflateTransition(R.transition.fade)
        exitTransition = inflater.inflateTransition(R.transition.slide_right)
        getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
    }

    override fun onMapLongClick(clickPosition: LatLng) {
        if (resetGame) return

        soundPoolPlayer.playSound(R.raw.marker)

        mMap.run {
            clear()

            marker = addMarker(
                MarkerOptions().position(clickPosition).title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )!!

            animateCamera(CameraUpdateFactory.newLatLngZoom(clickPosition, 5f))
        }
    }
}