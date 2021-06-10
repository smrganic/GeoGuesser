package com.example.geoguesser.utils

import android.util.Log
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.google.android.gms.maps.model.LatLng
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.StringReader

object LatLngGenerator {

    private val client = OkHttpClient()
    private var string: String? = null

    var position: LatLng? = null

    fun run(): LatLng? {
        val request = Request.Builder()
            .url("https://api.3geonames.org/?randomland=yes&json=1")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    string = response.body?.string()

                    val latitude = string?.substringAfter("\"latt\" : \"")?.substringBefore("\"")
                    val longitude = string?.substringAfter("\"longt\" : \"")?.substringBefore("\"")

                    val formattedLat = latitude.toString().toDoubleOrNull()
                    val formattedLong = longitude.toString().toDoubleOrNull()

                    if (formattedLat != null && formattedLong != null)
                        position = LatLng(formattedLat, formattedLong)


                }
            }
        })
        return position
    }
}