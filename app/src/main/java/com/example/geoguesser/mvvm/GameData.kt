package com.example.geoguesser.mvvm

class GameData(
    private var isStreetViewVisible: Boolean = true,
    private var resetGame: Boolean = false
) {
    fun getResetGame() = resetGame
    fun getStreetViewVisibility() = isStreetViewVisible
}