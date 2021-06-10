package com.example.geoguesser.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val SYDNEY = LatLng(-33.87365, 151.20689)
    private var streetViewIsVisible = true
    private var streetView: SupportStreetViewPanoramaFragment? = null
    private var mapFragment: SupportMapFragment? = null

    companion object {
        const val STREET_VIEW_TAG = "streetView"
        const val MAP_TAG = "mapTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { onFabClick(savedInstanceState) }

        setStreetView(savedInstanceState)

    }

    private fun onFabClick(savedInstanceState: Bundle?) {
        if (streetViewIsVisible) {
            setMapView(savedInstanceState)
            binding.fab.setIconResource(R.drawable.ic_baseline_pin_drop_128)
            binding.fab.text = "Confirm"
            streetViewIsVisible = false
        } else {
            setStreetView(savedInstanceState)
            binding.fab.setIconResource(R.drawable.ic_baseline_map_128)
            binding.fab.text = "Guess"
            streetViewIsVisible = true
        }
    }

    private fun setMapView(savedInstanceState: Bundle?) {
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance()
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.fragmentContainer, mapFragment!!, MAP_TAG).commit()
        mapFragment?.getMapAsync(this)
    }

    private fun setStreetView(savedInstanceState: Bundle?) {
        if (streetView == null) {
            streetView = SupportStreetViewPanoramaFragment.newInstance()
        }
        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .add(R.id.fragmentContainer, streetView!!, STREET_VIEW_TAG).commit()
        streetView?.getStreetViewPanoramaAsync { panorama ->
            savedInstanceState ?: panorama.setPosition(
                SYDNEY
            )
            panorama.isStreetNamesEnabled = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
    }

    override fun onMapLongClick(clickPosition: LatLng) {
        mMap.clear()
        mMap.addMarker(
            MarkerOptions().position(clickPosition).title("Location Selected")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(clickPosition, 20f))
    }
}