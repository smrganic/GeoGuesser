package com.example.geoguesser.mvvm

class GameData(
    private var isStreetViewVisible: Boolean = true,
    private var resetGame: Boolean = false,
    private var isGyroEnabled: Boolean = true
) {
    fun getResetGame() = resetGame
    fun getStreetViewVisibility() = isStreetViewVisible
    fun getGyroStatus() = isGyroEnabled
}