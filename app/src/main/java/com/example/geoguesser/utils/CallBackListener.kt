package com.example.geoguesser.utils

import com.google.android.gms.maps.model.LatLng
import java.lang.Exception

interface CallBackListener {
    fun onSuccess(position: LatLng)
    fun onFailure(exception: Exception)
}