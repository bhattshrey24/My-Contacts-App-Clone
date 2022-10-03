package com.example.mycontactsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.mycontactsapp.R
import com.example.mycontactsapp.databinding.ActivitySecondBinding
import com.example.mycontactsapp.ui.fragments.ContactDetailsFragment
import com.example.mycontactsapp.ui.fragments.CreateOrModifyContactFragment

class SecondActivity : AppCompatActivity() {

    private val binding: ActivitySecondBinding by lazy {
        ActivitySecondBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var contactDetails:Contact? =
        intent.getParcelableExtra(Constants.contactDetailsKey)

        var isEdit = intent.getBooleanExtra(Constants.booleanIsEditKey, true)
        Log.i(Constants.debugTag, "IsEdit in oncreate : $isEdit")

        if (isEdit) {
            val fragment = ContactDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable(Constants.contactDetailsKey, contactDetails)
            fragment.arguments = bundle
            replaceFragment(fragment)// Contact Detail
        } else {
            val fragment = CreateOrModifyContactFragment()
            val bundle = Bundle()
            bundle.putBoolean(Constants.booleanIsEditKey, false)
            fragment.arguments = bundle
            replaceFragment(fragment)// Create or modify
        }

    }

    private fun replaceFragment(mFragment: Fragment) {
        Log.i(Constants.debugTag, "Inside replace frag of second activity")

        var fragmentManager = supportFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.secondActivityFragmentContainer.id, mFragment)
        fragmentTransaction.commit()
    }

}