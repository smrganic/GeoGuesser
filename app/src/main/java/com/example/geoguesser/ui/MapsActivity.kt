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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.fragments.MapFragment
import com.example.geoguesser.fragments.StreetViewFragment
import com.example.geoguesser.mvvm.LocationViewModel
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    private var streetViewIsVisible = true
    private lateinit var streetView: StreetViewFragment
    private lateinit var mapFragment: MapFragment
    private var panorama: StreetViewPanorama? = null

    private val soundPoolPlayer: AudioPlayer by inject()
    private val viewModel by viewModel<LocationViewModel>()
    private lateinit var sensorManager: SensorManager;
    private lateinit var gyroSensor: Sensor
    private var gyroEnabled: Boolean = true
    private var resetGame = false

    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent?) {

            val values = event?.values ?: floatArrayOf(0.0f, 0.0f, 0.0f)

            panorama?.let {
                val duration: Long = 200
                val camera = StreetViewPanoramaCamera.Builder()
                    .zoom(it.panoramaCamera.zoom)
                    .tilt(-normalised(values[1]))
                    .bearing(values[0])
                    .build()
                it.animateTo(camera, duration)
            }

            val test = if (panorama != null) "yea" else "no"
            Log.d("OBSERVER", test)
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

        viewModel.data.observe(this@MapsActivity, Observer {
            panorama = it.getPanorama()
        })

        sensorManager.registerListener(sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_UI)

    }

    private fun onFabToggleGyro() {
        if (gyroEnabled) {
            sensorManager.unregisterListener(sensorListener)
            binding.fabDisableGyro.text = getString(R.string.enable_gyro)
        } else {
            sensorManager.registerListener(
                sensorListener,
                gyroSensor,
                SensorManager.SENSOR_DELAY_UI
            )
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

            setStreetView()
            return


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

    private fun setMapView() {

        this.title = getString(R.string.map_title)
        binding.fabDisableGyro.visibility = View.GONE

        if (!this::mapFragment.isInitialized) {
            mapFragment = MapFragment()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
            .replace(R.id.fragmentContainer, mapFragment, MAP_TAG).commit()
    }


    private fun setStreetView() {

        this.title = getString(R.string.street_view_title)

        binding.apply {
            fab.setIconResource(R.drawable.ic_baseline_map_128)
            fab.text = getString(R.string.streetViewButtonText)
            fabDisableGyro.visibility = View.VISIBLE
        }


        if (!this::streetView.isInitialized) {

            streetView = StreetViewFragment()

            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragmentContainer, streetView, STREET_VIEW_TAG).commit()

        } else {

            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, streetView, STREET_VIEW_TAG).commit()

        }

    }


}