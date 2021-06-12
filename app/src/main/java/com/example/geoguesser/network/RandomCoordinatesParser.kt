package com.example.geoguesser.network

import com.google.android.gms.maps.model.LatLng

// If api response ever changes a new specific parser needs to be implemented
class RandomCoordinatesParser() : Parser<String, LatLng> {
    override fun parse(inputValue: String): LatLng {

        val position: LatLng

        // Extract coordinates from response
        val latitude = inputValue.substringAfter("\"latt\" : \"").substringBefore("\"")
            .toDoubleOrNull()
        val longitude = inputValue.substringAfter("\"longt\" : \"").substringBefore("\"")
            .toDoubleOrNull()

        // If parser error return Osijek Coordinates
        position = if (latitude != null && longitude != null) LatLng(latitude, longitude)
        else LatLng(45.5550, 18.6955)

        return position
    }
}