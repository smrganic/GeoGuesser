package com.example.geoguesser.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.transition.TransitionInflater
import com.example.geoguesser.R
import com.example.geoguesser.mvvm.LocationViewModel
import com.example.geoguesser.mvvm.MapElementsData
import com.example.geoguesser.sounds.AudioPlayer
import com.example.geoguesser.utils.Preferences
import com.example.geoguesser.utils.addInfoWindow
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MapFragment : SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private var resetGame = false
    private val soundPoolPlayer: AudioPlayer by inject()
    private val viewModel by sharedViewModel<LocationViewModel>()
    private var marker: Marker? = null
    private val preferences: Preferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(activity)

        enterTransition = inflater.inflateTransition(R.transition.fade)
        exitTransition = inflater.inflateTransition(R.transition.slide_right)

        getMapAsync(this)

        viewModel.gameLiveData.observe(this, { resetGame = it.getResetGame() })
        viewModel.mapLiveData.observe(this, { marker = it.getMarker() })
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

            viewModel.setMarker(MapElementsData(marker!!))

            animateCamera(CameraUpdateFactory.newLatLngZoom(clickPosition, 5f))
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

    fun drawPolyLine(selectedPosition: LatLng, actualPosition: LatLng) {
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
}