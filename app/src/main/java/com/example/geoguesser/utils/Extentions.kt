package com.example.geoguesser.utils

import android.graphics.Bitmap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline

fun Polyline.addInfoWindow(map: GoogleMap, title: String, message: String, infoLatLng: LatLng) {

    // Create marker that is only used for its info window. Make it small so it is not seen
    val invisibleMarker =
        BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))

    // Add marker to the map
    val marker = map.addMarker(
        MarkerOptions()
            .position(infoLatLng)
            .title(title)
            .snippet(message)
            .alpha(0f)
            .icon(invisibleMarker)
            .anchor(0f, 0f)
    )

    marker?.showInfoWindow()
}