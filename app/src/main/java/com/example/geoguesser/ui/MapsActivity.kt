package com.example.geoguesser.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.network.Networking
import com.example.geoguesser.network.Parser
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

    private val networking by inject<Networking>()
    private val parser by inject<Parser<String, LatLng>>()

    private lateinit var position: LatLng
    private var streetViewIsVisible = true

    private lateinit var streetView: SupportStreetViewPanoramaFragment
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private var marker: Marker? = null
    private var resetGame = false

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

        if(resetGame){
            resetGame = false
            streetViewIsVisible = true
            marker = null
            setStreetView()
            return
        }

        if (marker != null) {

            drawPolyLine(marker!!.position, position)

            // save high score

            binding.apply {
                fab.setIconResource(R.drawable.ic_baseline_explore_24)
                fab.text = "Play Again"
                resetGame = true
            }
            val bundle = Bundle()

            /*val intent = Intent(this, StartGameActivity::class.java)
            bundle.putFloat("HIGH_SCORE", calculateResult(marker.position, position))
            startActivity(intent, bundle)*/

        } else {
            if (streetViewIsVisible) {
                setMapView()
                binding.fab.setIconResource(R.drawable.ic_baseline_pin_drop_128)
                binding.fab.text = getString(R.string.mapFabText)
                streetViewIsVisible = false
            } else {
                Toast.makeText(this, "Place a marker by long pressing the map", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun calculateResult(position: LatLng, position1: LatLng): Float {
        TODO("Create high score")
    }

    fun Polyline.addInfoWindow(map: GoogleMap, title: String, message: String, infoLatLng: LatLng) {
        val invisibleMarker =
            BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
        val marker = map.addMarker(
            MarkerOptions()
                .position(infoLatLng)
                .title(title)
                .snippet(message)
                .alpha(0f)
                .icon(invisibleMarker)
                .anchor(0f, 0f)
        )
        marker.showInfoWindow()
    }

    private fun drawPolyLine(selectedPosition: LatLng, actualPosition: LatLng) {
        val poly = mMap.addPolyline(
            PolylineOptions().clickable(false).add(selectedPosition).add(actualPosition)
        )
        poly.width = 12f
        poly.color = Color.DKGRAY
        poly.pattern = listOf(Dot(), Gap(20f))

        mMap.addMarker(
            MarkerOptions().position(actualPosition).title("Street view Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )

        val xValue = (selectedPosition.latitude + actualPosition.latitude) / 2
        val yValue = (selectedPosition.longitude + actualPosition.longitude) / 2

        val infoWindowPosition = LatLng(xValue, yValue)

        val results = FloatArray(3)
        Location.distanceBetween(
            selectedPosition.latitude,
            selectedPosition.longitude,
            actualPosition.latitude,
            actualPosition.longitude,
            results
        )

        val formatedResult = String.format("%.2f", results[0] / 1000)

        poly.addInfoWindow(mMap, "Distance", "You guessed $formatedResult km from the correct location.", infoWindowPosition)

        val latLngBoundsBuilder = LatLngBounds.builder()
        latLngBoundsBuilder.include(selectedPosition)
        latLngBoundsBuilder.include(actualPosition)
        val bounds = latLngBoundsBuilder.build()

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
    }

    private fun setMapView() {

        if (!this::mapFragment.isInitialized) {
            mapFragment = SupportMapFragment.newInstance()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.fragmentContainer, mapFragment, MAP_TAG).commit()

        mapFragment.getMapAsync(this)
    }

    private fun setNewLocation(panorama: StreetViewPanorama) {

        val callback = object : Callback {

            override fun onFailure(call: Call, e: IOException) = e.printStackTrace()

            override fun onResponse(call: Call, response: Response) {
                var string: String?
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    string = response.body?.string()

                    string?.let {
                        position = parser.parse(it)
                        runOnUiThread { panorama.setPosition(position, 2500) }
                    }
                }
            }
        }

        networking.execute(callback, "https://api.3geonames.org/?randomland=HR&json=1")

    }

    private fun setStreetView() {

        binding.apply {
            fab.setIconResource(R.drawable.ic_baseline_map_128)
            fab.text = getString(R.string.streetViewButtonText)
        }


        if (!this::streetView.isInitialized) {

            streetView = SupportStreetViewPanoramaFragment.newInstance()

            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragmentContainer, streetView, STREET_VIEW_TAG).commit()

        } else {

            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, streetView, STREET_VIEW_TAG).commit()

        }

        streetView.getStreetViewPanoramaAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Register all map listeners here.
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
    }

    override fun onMapLongClick(clickPosition: LatLng) {
        
        if(resetGame) return

        mMap.run {
            clear()

            marker = addMarker(
                MarkerOptions().position(clickPosition).title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )!!

            animateCamera(CameraUpdateFactory.newLatLngZoom(clickPosition, 5f))
        }
    }

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        panorama.isStreetNamesEnabled = false
        setNewLocation(panorama)
    }
}