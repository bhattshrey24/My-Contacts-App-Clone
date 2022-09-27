package com.example.mycontactsapp

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.ui.fragments.ContactDetailsFragment
import com.example.mycontactsapp.ui.fragments.HomeFragment


class MainActivity : AppCompatActivity() {

    //todo
    // Disable dark mode
    // Add search contact functionality
    // Add all details that user has entered (Don't show data that user has not entered while
    // adding the contact initially)
    // Add MVVM structure and Room DB
    // Initially when ROOM DB is empty load contact from Content
    // Provider and store it in DB and then simply do all crud operations in that DB
    // Add sync button using which user can again update Room DB by getting
    // All new contents that user added using content provider
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }

    private var myRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // This
        // forces the app to stay in Light mode even if user switched to dark mode

        supportActionBar?.hide()

        Log.i(Constants.debugTag, "Has write permission? ${hasWritePermission()}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission()
        }
        if (hasReadPermission()) {
            replaceFragment(HomeFragment())
        }

    }

    private fun requestPermission() {
        Log.i(Constants.debugTag, "Inside Request Permission")

        var permissionsToRequest = mutableListOf<String>()

        if (!hasReadPermission()) {
            permissionsToRequest.add(
                android.Manifest.permission.READ_CONTACTS
            )
        }
        if (!hasWritePermission()) {
            permissionsToRequest.add(
                android.Manifest.permission.WRITE_CONTACTS
            )
        }

        if (permissionsToRequest.isNotEmpty()) { // ie. if there are some permissions that
            // user has not accepted so we will now ask user to accept them
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(),
                myRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == myRequestCode && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionRequests", "${permissions[i]} granted!!")
                } else {
                    Log.i("PermissionRequests", "${permissions[i]} Not granted!!")
                }
            }
            replaceFragment(HomeFragment()) // ie. for the first time we will do this only if user accepted permission
        }
    }

    private fun hasReadPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.mainActivityFragmentContainer.id, myFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        if (count == 1) {
            finish()
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack(
                HomeFragment::class.java.name,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
    }

}