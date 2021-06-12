package com.example.geoguesser.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geoguesser.R
import com.example.geoguesser.databinding.ActivityStartGameBinding
import com.example.geoguesser.utils.Preferences
import org.koin.android.ext.android.inject

class StartGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartGameBinding
    private val preferences: Preferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener {

            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)

        }

        updateUI()
    }

    override fun onResume() {
        super.onResume()

        updateUI()
    }

    private fun updateUI() {

        // Get the current high score form shared preferences
        if (preferences.getHighScore() != Float.MAX_VALUE) {

            val formattedResult = String.format("%.2f", preferences.getHighScore() / 1000)

            binding.apply {
                scoreTitle.text = getString(R.string.titleAfterPlayedGame)
                points.text =
                    String.format(getString(R.string.pointsTextWithValue), formattedResult)
            }

        }

    }

}