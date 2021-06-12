package com.example.geoguesser.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel(
    locationData: LocationData,
    mapElementsData: MapElementsData,
    startGameData: GameData
) : ViewModel() {

    private val _locationMutableData = MutableLiveData(locationData)
    val locationLiveData: LiveData<LocationData> = _locationMutableData

    fun setData(locationData: LocationData) {
        _locationMutableData.value = locationData
    }

    private val _mapMutableData = MutableLiveData(mapElementsData)
    val mapLiveData: LiveData<MapElementsData> = _mapMutableData

    fun setMarker(mapElementsData: MapElementsData) {
        _mapMutableData.value = mapElementsData
    }

    private val _gameMutableData = MutableLiveData(startGameData)
    val gameLiveData: LiveData<GameData> = _gameMutableData

    fun setGameData(gameData: GameData) {
        _gameMutableData.value = gameData
    }
}