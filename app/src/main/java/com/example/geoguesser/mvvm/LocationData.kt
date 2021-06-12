package com.example.geoguesser.mvvm

import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.model.LatLng

class LocationData() {
    private var position: LatLng? = null
    private var panorama: StreetViewPanorama? = null

    constructor(position: LatLng, panorama: StreetViewPanorama) : this() {
        this.position = position
        this.panorama = panorama
    }

    fun getPanorama(): StreetViewPanorama? = panorama
    fun getPosition(): LatLng? = position

}