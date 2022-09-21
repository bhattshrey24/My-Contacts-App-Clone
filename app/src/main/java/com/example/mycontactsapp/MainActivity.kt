package com.example.mycontactsapp

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.ui.fragments.HomeFragment


class MainActivity : AppCompatActivity() {
    //todo
    //  Add home page functionality
    //  Add contact detail screen functionality
    //  Add new contact functionality
    //  Add edit contact functionality
    //  fix backstack problem ie. how we can maintain backstack in fragment
    //  Add search contact feature for home page

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }
    private var requestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission()
        }
        replaceFragment(HomeFragment())

    }

    private fun requestPermission() {
        var permissionsToRequest = mutableListOf<String>()
        if (!hasReadPermission()) {
            permissionsToRequest.add(
                android.Manifest.permission.READ_CONTACTS
            )
        }
        ActivityCompat.requestPermissions(
            this, permissionsToRequest.toTypedArray(),
            requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCode && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionRequests", "${permissions[i]} granted!!")
                } else {
                    Log.i("PermissionRequests", "${permissions[i]} Not granted!!")
                }
            }
        }
    }

    private fun hasReadPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.mainActivityFragmentContainer.id, myFragment)
        fragmentTransaction.commit()
    }

//    override fun onBackPressed() { // Not working , maybe what we can do is add fragment to backstack before replacing
//    and then remove it or maybe use navigation component
//        var fragmentManager = supportFragmentManager
//        var fragmentTransaction = fragmentManager.beginTransaction()
//        var fragment = fragmentManager.findFragmentById(binding.mainActivityFragmentContainer.id)
//        if (fragment != null) {
//            fragmentTransaction.remove(fragment);
//            fragmentTransaction.commit();
//        } else {
//            super.onBackPressed();
//        }
//    }
}