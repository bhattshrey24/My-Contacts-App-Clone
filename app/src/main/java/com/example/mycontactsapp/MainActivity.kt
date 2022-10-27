package com.example.mycontactsapp

import android.content.pm.PackageManager

import android.os.Bundle

import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.other.Constants


class MainActivity : AppCompatActivity() {

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
                isGranted = if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionRequests", "${permissions[0]} granted!!")
                    true
                } else {
                    Log.i("PermissionRequests", "${permissions[0]} Not granted!!")
                    false
                }
        }
    }

    fun isPermissionGranted() = isGranted
}
