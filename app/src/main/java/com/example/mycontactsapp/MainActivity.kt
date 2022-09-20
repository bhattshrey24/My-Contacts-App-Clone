package com.example.mycontactsapp

import android.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mycontactsapp.databinding.ActivityMainBinding
import com.example.mycontactsapp.ui.fragments.HomeFragment


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

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