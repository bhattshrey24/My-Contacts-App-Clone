package com.example.mycontactsapp

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ProxyFileDescriptorCallback
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mycontactsapp.data.local.ContactDatabase
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import com.example.mycontactsapp.ui.fragments.HomeFragment
import com.example.mycontactsapp.ui.fragments.SplashScreenFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    //Todo
    // Clean the code
    // apply diffUtil for updating recyclerView
    // fix Warnings
    // fix app floating hint make it the way Zeel showed


    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }
    private var isGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // This
        // forces the app to stay in Light mode even if user switched to dark mode

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.myRequestCode && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionRequests", "${permissions[i]} granted!!")
                    isGranted = true
                } else {
                    Log.i("PermissionRequests", "${permissions[i]} Not granted!!")
                    isGranted = false
                }
            }
        }
    }

    fun isPermissionGranted() = isGranted
}
