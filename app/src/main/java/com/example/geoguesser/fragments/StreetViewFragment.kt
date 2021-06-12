package com.example.geoguesser.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.transition.TransitionInflater
import com.example.geoguesser.R
import com.example.geoguesser.mvvm.LocationData
import com.example.geoguesser.mvvm.LocationViewModel
import com.example.geoguesser.network.Networking
import com.example.geoguesser.network.Parser
import com.example.geoguesser.utils.Preferences
import com.example.geoguesser.utils.addInfoWindow
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class StreetViewFragment : SupportStreetViewPanoramaFragment(), OnStreetViewPanoramaReadyCallback {

    private val networking: Networking by inject()
    private val parser by inject<Parser<String, LatLng>>()

    private val viewModel by sharedViewModel<LocationViewModel>()
    private lateinit var position: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = TransitionInflater.from(activity)
        this.enterTransition = inflater.inflateTransition(R.transition.fade)
        this.exitTransition = inflater.inflateTransition(R.transition.slide_left)

        getStreetViewPanoramaAsync(this)
    }

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        panorama.isStreetNamesEnabled = false
        setNewLocation(panorama)
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
                        activity?.runOnUiThread {
                            position = parser.parse(it)
                            panorama.setPosition(position, 2500)
                            viewModel.setData(LocationData(position, panorama))
                        }
                    }
                }
            }
        }

        networking.execute(callback, "https://api.3geonames.org/?randomland=HR&json=1")

    }

}