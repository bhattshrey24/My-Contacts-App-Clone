package com.example.mycontactsapp

import android.content.pm.PackageManager

import android.os.Bundle

import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.other.Constants


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
                isGranted = if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionRequests", "${permissions[i]} granted!!")
                    true
                } else {
                    Log.i("PermissionRequests", "${permissions[i]} Not granted!!")
                    false
                }
            }
        }
    }

    fun isPermissionGranted() = isGranted
}
