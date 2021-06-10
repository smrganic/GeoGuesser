package com.example.geoguesser

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.util.jar.Manifest

class GeoAppIntro : AppIntro(), EasyPermissions.PermissionCallbacks {

    companion object{
        const val FINE_LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.insetsController?.apply {
            hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setup()
    }

    private fun setup() {
        // Make sure you don't call setContentView!
        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(
            AppIntroFragment.newInstance(
                title = "Welcome, Explorer",
                description = "Set out on a journey that takes you from the smallest town in rural China to the biggest skyscraper in New York City.",
                backgroundColor = Color.DKGRAY,
                imageDrawable = R.drawable.ic_baseline_explore_24
            ))
        addSlide(AppIntroFragment.newInstance(
            title = "Walk around",
            description = "Search for flags, shop signs, landmarks or anything else that might help you in the mighty quest for your location.",
            backgroundColor = Color.DKGRAY,
            imageDrawable = R.drawable.ic_baseline_map_128
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Find the right location",
            description = "Beat your high score by being as accurate as possible.",
            backgroundColor = Color.DKGRAY,
            imageDrawable = R.drawable.ic_baseline_pin_drop_128
        ))
    }

    private fun startMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        if(EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            startMapsActivity()
        }
        else{
            EasyPermissions.requestPermissions(this, getString(R.string.locationRationale), FINE_LOCATION_REQUEST_CODE, android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        if(EasyPermissions.hasPermissions(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            startMapsActivity()
        }
        else{
            EasyPermissions.requestPermissions(this, getString(R.string.locationRationale), FINE_LOCATION_REQUEST_CODE, android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            SettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startMapsActivity()
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