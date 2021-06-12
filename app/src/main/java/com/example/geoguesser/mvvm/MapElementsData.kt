package com.example.geoguesser.mvvm

import com.google.android.gms.maps.model.Marker

class MapElementsData() {
    private var marker: Marker? = null

    constructor(marker: Marker) : this() {
        this.marker = marker
    }

    fun getMarker(): Marker? = marker
}