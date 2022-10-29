package com.example.mycontactsapp

import android.content.pm.PackageManager

import android.os.Bundle

import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.other.Constants


class MainActivity : AppCompatActivity() {
    //todo must
    // change linear to relative or constraint layout and remove those 3 extra layouts
    // add drop down menu with every edit text so that user can choose
    //   which type he/she wants
    // add a "-" fab so that user can remove unwanted fab
    // always check whether newly added et was filled or not cause maybe
    //  user add it and forget to remove it
    // add feature where in new contact user can add email too

    //todo optional
    // if time then add a feature where user can even delete previously added numbers or emails
    // if time then maybe add more types since I'm not using content provider anymore so we can always increase types


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
