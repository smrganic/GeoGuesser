package com.example.geoguesser.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
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
    private val backgroundColor = Color.parseColor("#1D3557")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove title and status bar
        setImmersiveMode()

        setup()
    }

    private fun setup() {

        // Make sure you don't call setContentView!
        // Call addSlide passing your Fragments.

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.introTitle1),
                description = getString(R.string.descCard1),
                backgroundColor = backgroundColor,
                imageDrawable = R.drawable.ic_baseline_explore_24
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.introTitle2),
                description = getString(R.string.descCard2),
                backgroundColor = backgroundColor,
                imageDrawable = R.drawable.ic_baseline_map_128
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                title = getString(R.string.introTitle3),
                description = getString(R.string.descCard3),
                backgroundColor = backgroundColor,
                imageDrawable = R.drawable.ic_baseline_pin_drop_128
            )
        )
    }

    private fun startGameActivity() {

        preferences.saveSetup(isSetupComplete = true)

        val intent = Intent(this, StartGameActivity::class.java)
        startActivity(intent)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        // Request permissions
        requestPermission(ACCESS_FINE_LOCATION, FINE_LOCATION_REQUEST_CODE)

    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        // Request permissions
        requestPermission(ACCESS_FINE_LOCATION, FINE_LOCATION_REQUEST_CODE)

    }

    private fun requestPermission(permission: String, requestCode: Int) {

        // If the permission is not granted this will ask for it, but if it is granted
        // this line will go to directly to onPermissionGranted
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.locationRationale),
            requestCode,
            permission
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {

        // If permissions permanently denied open settings
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startGameActivity()
    }

    // Need to forward the results to Easy Permissions in order for the lib to function
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}