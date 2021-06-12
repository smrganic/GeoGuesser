package com.example.geoguesser.sounds

// If you every decide to change Sound Pool Player into something else this needs to be implemented
interface AudioPlayer {
    fun playSound(id: Int)
}