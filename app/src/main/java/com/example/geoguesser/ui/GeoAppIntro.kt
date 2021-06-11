package com.example.geoguesser.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.geoguesser.R
import com.example.geoguesser.utils.Preferences
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import org.koin.android.ext.android.inject

class GeoAppIntro : AppIntro(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val FINE_LOCATION_REQUEST_CODE = 1
    }

    private val preferences: Preferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersiveMode()
        setup()
    }

    private fun setup() {
        // Make sure you don't call setContentView!
        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.introTitle1),
                description = getString(R.string.descCard1),
                backgroundColor = Color.parseColor("#1D3557"),
                imageDrawable = R.drawable.ic_baseline_explore_24
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.introTitle2),
                description = getString(R.string.descCard2),
                backgroundColor = Color.parseColor("#1D3557"),
                imageDrawable = R.drawable.ic_baseline_map_128
            )
        )
        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.introTitle3),
                description = getString(R.string.descCard3),
                backgroundColor = Color.parseColor("#1D3557"),
                imageDrawable = R.drawable.ic_baseline_pin_drop_128
            )
        )
    }

    private fun startGameActivity() {
        preferences.saveSetup(true)
        val intent = Intent(this, StartGameActivity::class.java)
        startActivity(intent)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        if (EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            startGameActivity()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.locationRationale),
                FINE_LOCATION_REQUEST_CODE,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        if (EasyPermissions.hasPermissions(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            startGameActivity()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.locationRationale),
                FINE_LOCATION_REQUEST_CODE,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startGameActivity()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}