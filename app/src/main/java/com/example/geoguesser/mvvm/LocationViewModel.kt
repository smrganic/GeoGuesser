package com.example.geoguesser.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class LocationViewModel(
    private var locationData: LocationData,
    private var mapElementsData: MapElementsData,
    private var startGameData: GameData
) : ViewModel() {
    private val _data = MutableLiveData(locationData)
    val data: LiveData<LocationData> = _data

    private val _mapData = MutableLiveData(mapElementsData)
    val mapData: LiveData<MapElementsData> = _mapData

    private val _gameData = MutableLiveData(startGameData)
    val gameData: LiveData<GameData> = _gameData
    fun setData(locationData: LocationData) {
        _data.value = locationData
    }

    fun setMarker(mapElementsData: MapElementsData) {
        _mapData.value = mapElementsData
    }

    fun setGameData(gameData: GameData) {
        _gameData.value = gameData
    }
}