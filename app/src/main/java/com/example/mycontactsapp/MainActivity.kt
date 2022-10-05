package com.example.mycontactsapp

import android.content.Intent
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
import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.ui.fragments.HomeFragment


class MainActivity : AppCompatActivity() {

    //Todo
    // Add all important details(like work email and contact , home email and contact etc)
    // that user has entered (Don't show data that user has not entered while adding the contact initially)
    // Add MVVM structure, jetpack navigation component
    // Learn how to pass data from activity A to B and vice versa and same for fragment
    // Learn about on activity result
    // Use scope functions wherever you can
    // Try to remove unnecessary if - else (try using ENUM instead)
    // And handle the case of unnecessary loading ie. only update the tile that user updated in recycler view and not whole list and when user comes back without editing then don't load data again just show previous data
    // Handle the case when user doesn't grant permission like show user a dialog box showing that it's necessary to accept the permission and if he she again rejects then close app
    // Change the architecture to single activity ie. main activity and in that simply change call different fragments based on user clicks
    // Integrate Room DB and when ROOM DB is empty load contact from Content Provider and store it in DB and then simply do all crud operations in that DB
    // Add sync button using which user can again update Room DB by getting
    // All new contents that user added using content provider

    //todo fix
    // Get Work and home email from ContactsContract.CommonDataKinds.Email
    // Get Work and Home and Other Number
    // Or maybe use multiple initLoader and start 2 loaders at a time and
    // differentiate using loadContactId
    // and meanwhile show circular progress spinner
    // Or Try using Coroutine and with async await

    //Todo(Today's todo)
    // change architecture to single activity and multiple fragment (D)
    // fix search feature (D)
    // Apply kotlin specific features like .apply etc to some extent
    // Apply MVVM pattern
    // Add nav  graph implementation
    // Integrate Room but not implement yet
    // Fix app like add enum etc

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }

    private var myRequestCode = 101

    private val homeFragment = HomeFragment()

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
            replaceFragment(homeFragment)
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
            replaceFragment(homeFragment) // ie. for the first time we will do this only if user accepted permission
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

//    override fun onBackPressed() {
//        homeFragment.adapter?.setContact(Constants.listOfAllContacts)
//        val count = supportFragmentManager.backStackEntryCount
//        if (count == 1) { // This will close the app if we have just 1 fragment
//            // displayed on screen because that fragment will be show all contact
//            // fragment
//            finish()
//        }
//        super.onBackPressed() // else simply follow normal back button behavior
//    }

}
