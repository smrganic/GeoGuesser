package com.example.geoguesser.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityMapsBinding
import com.example.geoguesser.fragments.MapFragment
import com.example.geoguesser.fragments.StreetViewFragment
import com.example.geoguesser.mvvm.GameData
import com.example.geoguesser.mvvm.LocationViewModel
import com.example.geoguesser.mvvm.MapElementsData
import com.example.geoguesser.sounds.AudioPlayer
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    private var streetViewIsVisible = true
    private var resetGame = false

    private lateinit var streetViewFragment: StreetViewFragment
    private var panorama: StreetViewPanorama? = null

    private lateinit var mapFragment: MapFragment
    private var marker: Marker? = null

    private val soundPoolPlayer: AudioPlayer by inject()
    private val viewModel by viewModel<LocationViewModel>()

    private lateinit var sensorManager: SensorManager;
    private lateinit var gyroSensor: Sensor
    private var gyroEnabled: Boolean = true

    // React to sensor value changes here
    private val sensorListener = object : SensorEventListener {

        // Don not need this, don't care about accuracy
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {

            // Get values from sensor
            val values = event?.values ?: floatArrayOf(0.0f, 0.0f, 0.0f)

            // Apply camera transformations to panorama only if panorama is not null
            panorama?.let {
                val duration: Long = 200
                val camera = StreetViewPanoramaCamera.Builder()
                    .zoom(it.panoramaCamera.zoom)
                    .tilt(-normalised(values[1]))
                    .bearing(values[0])
                    .build()
                it.animateTo(camera, duration)
            }
        }

        // Adapt the output of the orientation sensor into camera tilt expected degree range
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

        // Explore TYPE_ORIENTATION deprecation and replace accordingly
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

        // Input Start Game Data into View Model and observe changes
        viewModel.setGameData(GameData(streetViewIsVisible, resetGame, gyroEnabled))

        viewModel.locationLiveData.observe(this@MapsActivity, { panorama = it.getPanorama() })

        viewModel.mapLiveData.observe(this, { marker = it.getMarker() })

        viewModel.gameLiveData.observe(this, {
            resetGame = it.getResetGame()
            streetViewIsVisible = it.getStreetViewVisibility()
            gyroEnabled = it.getGyroStatus()
        })

        setStreetView()
    }

    // Added Lifecycle needed to run sensors properly
    override fun onResume() {
        super.onResume()
        if(gyroEnabled){
            sensorManager.registerListener(sensorListener, gyroSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        if(gyroEnabled){
            sensorManager.unregisterListener(sensorListener)
        }
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
        viewModel.setGameData(GameData(streetViewIsVisible, resetGame, !gyroEnabled))
    }

    // Look into refactoring this, there has to be a better way
    private fun onFabClick() {

        // Reset the game by updating all observers and showing the street view
        if (resetGame) {
            viewModel.setGameData(GameData(isStreetViewVisible = true, resetGame = false, gyroEnabled))
            viewModel.setMarker(MapElementsData())
            setStreetView()
            return
        }

        // If a marker exists and the button is pressed that means the user is confirming their guess
        // Play a sound draw the results on the map and update game state
        if (marker != null) {

            soundPoolPlayer.playSound(R.raw.marker)

            mapFragment.drawPolyLine(marker!!.position, viewModel.locationLiveData.value?.getPosition()!!)

            binding.apply {
                fab.setIconResource(R.drawable.ic_baseline_explore_24)
                fab.text = getString(R.string.playAgain)
                viewModel.setGameData(GameData(streetViewIsVisible, resetGame = true, gyroEnabled))
            }

        } else {

            // If there is no marker and street view is visible then inflate the map fragment
            // notify observers of the state change
            if (streetViewIsVisible) {

                setMapView()

                binding.fab.setIconResource(R.drawable.ic_baseline_pin_drop_128)
                binding.fab.text = getString(R.string.mapFabText)

                viewModel.setGameData(GameData(isStreetViewVisible = false, resetGame, gyroEnabled))

                // If there is no marker and no street view that means that user needs to place one
                // for the game to continue. Notify the user with the relevant information
                // Could add snack bar or a dialog here for better visuals
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

        // If there is no instance of street view fragment create one and add it to the activity
        // if an instance exists that is because user is starting a new game from map fragment
        // so street view fragment needs to replace the map view fragment
        if (!this::streetViewFragment.isInitialized) {

            streetViewFragment = StreetViewFragment()

            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragmentContainer, streetViewFragment, STREET_VIEW_TAG).commit()

        } else {

            supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, streetViewFragment, STREET_VIEW_TAG).commit()

        }

    }


}