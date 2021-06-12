package com.example.geoguesser.fragments

import android.os.Bundle
import android.transition.TransitionInflater
import com.example.geoguesser.R
import com.example.geoguesser.mvvm.LocationData
import com.example.geoguesser.mvvm.LocationViewModel
import com.example.geoguesser.network.Networking
import com.example.geoguesser.network.Parser
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.IOException

class StreetViewFragment : SupportStreetViewPanoramaFragment(), OnStreetViewPanoramaReadyCallback {

    private val networking: Networking by inject()
    private val parser by inject<Parser<String, LatLng>>()

    // Share view Model with maps fragment and maps activity
    private val viewModel by sharedViewModel<LocationViewModel>()
    private lateinit var position: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set transitions
        val inflater = TransitionInflater.from(activity)
        this.enterTransition = inflater.inflateTransition(R.transition.fade)
        this.exitTransition = inflater.inflateTransition(R.transition.slide_left)

        // Start async task to get panorama, respond in callback on completion
        getStreetViewPanoramaAsync(this)
    }

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        // Disable street names, that would not be a fun game
        panorama.isStreetNamesEnabled = false

        // Set the location of the newly created panorama
        setNewLocation(panorama)
    }

    private fun setNewLocation(panorama: StreetViewPanorama) {

        // Create callback for async network request
        val callback = object : Callback {

            override fun onFailure(call: Call, e: IOException) = e.printStackTrace()

            override fun onResponse(call: Call, response: Response) {

                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val string: String? = response.body?.string()

                // If string is not null we have valid coordinates.
                // And load the random location into panorama within Street View Fragment
                string?.let {

                    position = parser.parse(it)

                    // Panorama and observers need to be handled on the UI thread
                    activity?.runOnUiThread {
                        panorama.setPosition(position, 2500)
                        viewModel.setData(LocationData(position, panorama))
                    }
                }

            }

        }

        // Get new random coordinates from API
        networking.execute(callback, "https://api.3geonames.org/?randomland=HR&json=1")

    }

}