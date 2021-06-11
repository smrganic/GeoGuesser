package com.example.geoguesser.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.network.Networker
import com.example.geoguesser.network.Parser
import com.example.geoguesser.network.RandomCoordinatesParser
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

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

    private lateinit var position: LatLng
    private var streetViewIsVisible = true

    private var streetView: SupportStreetViewPanoramaFragment? = null
    private var mapFragment: SupportMapFragment? = null
    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null

    companion object {
        const val STREET_VIEW_TAG = "streetViewTag"
        const val MAP_TAG = "mapTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { onFabClick() }

        setStreetView()

    }

    private fun onFabClick() {

        marker?.let {
            val results = FloatArray(3)
            Location.distanceBetween(
                it.position.latitude,
                it.position.longitude,
                position.latitude,
                position.longitude,
                results
            )

            val bundle = Bundle()
            val poly = mMap.addPolyline(PolylineOptions().clickable(false).add(it.position).add(position))
            poly.width = 12f
            poly.color = Color.DKGRAY
            poly.pattern = listOf(Dot(), Gap(20f))

            mMap.addMarker(
                MarkerOptions().position(position).title("Street view Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            val latLngBoundsBuilder = LatLngBounds.builder()
            latLngBoundsBuilder.include(it.position)
            latLngBoundsBuilder.include(position)
            val bounds = latLngBoundsBuilder.build()

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
            /*val intent = Intent(this, StartGameActivity::class.java)
            bundle.putFloat("HIGH_SCORE", results[0])
            startActivity(intent, bundle)*/
        }

        if (streetViewIsVisible) {
            setMapView()
            binding.fab.setIconResource(R.drawable.ic_baseline_pin_drop_128)
            binding.fab.text = "Confirm"
            streetViewIsVisible = false
        } else {
            Toast.makeText(this, "Place a marker by long pressing the map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMapView() {
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

                    string?.let {
                        position = parser.parse(it)
                        runOnUiThread { panorama.setPosition(position, 2500) }
                        Log.d("MAPSLATITUDE", position.toString())
                    }
                }
            }
        }
        networker.execute(callback, "https://api.3geonames.org/?randomland=HR&json=1")
    }

    private fun setStreetView() {

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
        marker = mMap.addMarker(
            MarkerOptions().position(clickPosition).title("Selected Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(clickPosition, 5f))
    }

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        panorama.isStreetNamesEnabled = false
        setNewLocation(panorama)
    }
}