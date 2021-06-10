package com.example.geoguesser.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geoguesser.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.*

class tester : AppCompatActivity(), GoogleMap.OnMarkerDragListener,
    StreetViewPanorama.OnStreetViewPanoramaChangeListener {
        var streetViewPanorama: StreetViewPanorama? = null
        var marker: Marker? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_tester)
            val markerPosition = savedInstanceState?.getParcelable(MARKER_POSITION_KEY) ?: SYDNEY

            val streetViewPanoramaFragment =
                supportFragmentManager.findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment?
            streetViewPanoramaFragment?.getStreetViewPanoramaAsync { panorama ->
                streetViewPanorama = panorama
                streetViewPanorama?.setOnStreetViewPanoramaChangeListener(
                    this
                )
                // Only need to set the position once as the streetview fragment will maintain
                // its state.
                savedInstanceState ?: streetViewPanorama?.setPosition(SYDNEY)
            }
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync { map ->
                map.setOnMarkerDragListener(this)
                // Creates a draggable marker. Long press to drag.
                marker = map.addMarker(
                    MarkerOptions()
                        .position(markerPosition)
                        .draggable(true)
                )
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putParcelable(
                MARKER_POSITION_KEY,
                marker?.position
            )
        }

        override fun onStreetViewPanoramaChange(location: StreetViewPanoramaLocation) {
            marker?.position = location.position
        }

        override fun onMarkerDragStart(marker: Marker) {}
        override fun onMarkerDragEnd(marker: Marker) {
            streetViewPanorama?.setPosition(marker.position, 150)
        }

        override fun onMarkerDrag(marker: Marker) {}

        companion object {
            private const val MARKER_POSITION_KEY = "MarkerPosition"

            // George St, Sydney
            private val SYDNEY = LatLng(-33.87365, 151.20689)
        }
}