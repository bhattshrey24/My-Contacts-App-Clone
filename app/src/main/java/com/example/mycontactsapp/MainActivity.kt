package com.example.mycontactsapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.ui.fragments.ShowAllContactsFragment
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    //todo
    // Disable dark mode (D)
    // Add search contact functionality (D)
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
    private lateinit var listOfContactsFilteredFromQuery: List<Contact>

    private val showAllContactsFragment = ShowAllContactsFragment()

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
            replaceFragment(showAllContactsFragment)
        }

        binding.addNewContactFloatingButton.setOnClickListener {
            startActivity(
                Intent(this, SecondActivity::class.java)
                    .putExtra(Constants.booleanIsEditKey, false)
            )
        }

        binding.searchContactEditText.addTextChangedListener {
            var query = it.toString()
            listOfContactsFilteredFromQuery = Constants.listOfAllContacts.filter { contact ->
            contact.name?.contains(query,true)?:false
            }
            showAllContactsFragment.adapter?.setContact(listOfContactsFilteredFromQuery)
            showAllContactsFragment.listOfContacts = listOfContactsFilteredFromQuery.toMutableList()
        }

    }

    override fun onResume() {
        super.onResume()
        binding.searchContactEditText.setText("") // clearing the query
    // edit text when user navigates back to this screen
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
            replaceFragment(showAllContactsFragment) // ie. for the first time we will do this only if user accepted permission
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
        showAllContactsFragment.adapter?.setContact(Constants.listOfAllContacts)
        binding.searchContactEditText.setText("")
        super.onBackPressed()
    }

}