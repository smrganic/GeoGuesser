package com.example.geoguesser.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.network.Networker
import com.example.geoguesser.network.Parser
import com.example.geoguesser.network.RandomCoordinatesParser
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.koin.android.ext.android.inject
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    OnStreetViewPanoramaReadyCallback {


    private lateinit var binding: ActivityMapsBinding

    private val networker by inject<Networker>()
    private val parser by inject<Parser<String, LatLng>>()

    private var position: LatLng? = null
    private var streetViewIsVisible = true

    private var streetView: SupportStreetViewPanoramaFragment? = null
    private var mapFragment: SupportMapFragment? = null
    private lateinit var mMap: GoogleMap

    companion object {
        const val STREET_VIEW_TAG = "streetViewTag"
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

    private fun setNewLocation(panorama: StreetViewPanorama) {
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) = e.printStackTrace()

            override fun onResponse(call: Call, response: Response) {
                var string: String? = null
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    string = response.body?.string()

                    string?.let { position = parser.parse(it)
                        runOnUiThread { panorama.setPosition(position!!, 2500) }
                        Log.d("MAPSLATITUDE", position.toString())
                    }
                }
            }
        }
        networker.execute(callback, "https://api.3geonames.org/?randomland=HR&json=1")
    }

    private fun setStreetView(savedInstanceState: Bundle?) {

        if (streetView == null) {
            streetView = SupportStreetViewPanoramaFragment.newInstance()
            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragmentContainer, streetView!!, STREET_VIEW_TAG).commit()
        } else {
            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, streetView!!, STREET_VIEW_TAG).commit()
        }

        streetView?.getStreetViewPanoramaAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Register all map listeners here.
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

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        panorama.isStreetNamesEnabled = false
        setNewLocation(panorama)
    }
}