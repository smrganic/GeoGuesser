package com.example.geoguesser.ui

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.widget.Toast
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.network.Networking
import com.example.geoguesser.network.Parser
import com.example.geoguesser.sounds.AudioPlayer
import com.example.geoguesser.utils.Preferences
import com.example.geoguesser.utils.addInfoWindow
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

    private val networking: Networking by inject()
    private val parser by inject<Parser<String, LatLng>>()
    private val preferences: Preferences by inject()
    private val soundPoolPlayer: AudioPlayer by inject()

    private lateinit var position: LatLng
    private var streetViewIsVisible = true

    private lateinit var streetView: SupportStreetViewPanoramaFragment
    private lateinit var globalPanorama: StreetViewPanorama

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap

    private var marker: Marker? = null
    private var resetGame = false


    private lateinit var sensorManager: SensorManager;
    private lateinit var gyroSensor: Sensor
    private var gyroEnabled: Boolean = true

    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent?) {
            val values = event?.values ?: floatArrayOf(0.0f, 0.0f, 0.0f)

            if (this@MapsActivity::globalPanorama.isInitialized) {
                val duration: Long = 200
                val camera = StreetViewPanoramaCamera.Builder()
                    .zoom(globalPanorama.panoramaCamera.zoom)
                    .tilt(-normalised(values[1]))
                    .bearing(values[0])
                    .build()
                globalPanorama.animateTo(camera, duration)
            }
        }

        private fun normalised(x: Float): Float {
            val inputMin = -180
            val inputMax = 180
            val outputRangeMin = -90
            val outputRangeMax = 90
            return ((outputRangeMax - outputRangeMin) * (x - inputMin)) / (inputMax - inputMin) + outputRangeMin
        }
    }

    companion object {
        const val STREET_VIEW_TAG = "streetViewTag"
        const val MAP_TAG = "mapTag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { onFabClick() }
        binding.fabDisableGyro.setOnClickListener { onFabToggleGyro() }

        setStreetView()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    }

    private fun onFabToggleGyro() {
        if (gyroEnabled) {
            sensorManager.unregisterListener(sensorListener)
            binding.fabDisableGyro.text = getString(R.string.enable_gyro)
        } else {
            sensorManager.registerListener(sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_UI)
            binding.fabDisableGyro.text = getString(R.string.disable_gyro)
        }
        gyroEnabled = !gyroEnabled
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }

    private fun onFabClick() {

        if (resetGame) {
            resetGame = false
            streetViewIsVisible = true
            marker = null
            setStreetView()
            return
        }

        if (marker != null) {

            soundPoolPlayer.playSound(R.raw.marker)

            drawPolyLine(marker!!.position, position)

            // save high score

            binding.apply {
                fab.setIconResource(R.drawable.ic_baseline_explore_24)
                fab.text = getString(R.string.playAgain)
                resetGame = true
            }

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

    private fun calculateResult(selectedPosition: LatLng, actualPosition: LatLng): String {
        val results = FloatArray(3)
        Location.distanceBetween(
            selectedPosition.latitude,
            selectedPosition.longitude,
            actualPosition.latitude,
            actualPosition.longitude,
            results
        )

        val formattedResult = String.format("%.2f", results[0] / 1000)

        if (preferences.getHighScore() > results[0]) preferences.setHighScore(results[0])

        return formattedResult
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

        val result = calculateResult(selectedPosition, actualPosition)

        val xValue = (selectedPosition.latitude + actualPosition.latitude) / 2
        val yValue = (selectedPosition.longitude + actualPosition.longitude) / 2
        val infoWindowPosition = LatLng(xValue, yValue)

        poly.addInfoWindow(
            mMap,
            "Distance",
            "You guessed $result km from the correct location.",
            infoWindowPosition
        )

        val latLngBoundsBuilder = LatLngBounds.builder()
        latLngBoundsBuilder.include(selectedPosition)
        latLngBoundsBuilder.include(actualPosition)
        val bounds = latLngBoundsBuilder.build()

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
    }

    private fun setMapView() {

        this.title = getString(R.string.map_title)
        binding.fabDisableGyro.visibility = View.GONE

        if (!this::mapFragment.isInitialized) {
            mapFragment = SupportMapFragment.newInstance()
            val inflater = TransitionInflater.from(this)
            mapFragment.enterTransition = inflater.inflateTransition(R.transition.fade)
            mapFragment.exitTransition = inflater.inflateTransition(R.transition.slide_right)
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

        this.title = getString(R.string.street_view_title)

        binding.apply {
            fab.setIconResource(R.drawable.ic_baseline_map_128)
            fab.text = getString(R.string.streetViewButtonText)
            fabDisableGyro.visibility = View.VISIBLE
        }


        if (!this::streetView.isInitialized) {

            streetView = SupportStreetViewPanoramaFragment.newInstance()

            val inflater = TransitionInflater.from(this)
            streetView.enterTransition = inflater.inflateTransition(R.transition.fade)
            streetView.exitTransition = inflater.inflateTransition(R.transition.slide_left)

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

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        panorama.isStreetNamesEnabled = false
        globalPanorama = panorama
        setNewLocation(panorama)
    }
}