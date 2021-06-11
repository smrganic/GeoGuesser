package com.example.geoguesser.network

interface Parser<T, K> {
    fun parse(inputValue: T) : K
}