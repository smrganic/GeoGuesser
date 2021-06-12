package com.example.geoguesser.network

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

class Networking(private val client: OkHttpClient) {

    // Execute a async network request and pass a callback to run on completion
    fun execute(callback: Callback, url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(callback)
    }
}