package com.example.geoguesser.network

// Added generics for added flexibility if coordinates api is every changed
interface Parser<T, K> {
    fun parse(inputValue: T): K
}