package com.example.geoguesser.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.model.LatLng

class LocationViewModel(private var locationData: LocationData) : ViewModel() {
    private val _data = MutableLiveData(locationData)
    val data: LiveData<LocationData> = _data


    fun setData(locationData: LocationData){
        _data.value = locationData
    }
}