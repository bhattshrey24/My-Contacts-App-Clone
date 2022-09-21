package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.R
import com.example.mycontactsapp.databinding.FragmentContactDetailsBinding


class ContactDetailsFragment : Fragment() {
    private val binding: FragmentContactDetailsBinding by lazy {
        FragmentContactDetailsBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.editContactFloatingButton.setOnClickListener {
            replaceFragment(CreateOrModifyContactFragment())
        }
        var bundle = this.arguments
        var name = bundle?.getString(Constants.contactNameKey)
        var number = bundle?.getString(Constants.contactNumberKey)

        binding.nameOfPersonTV.text = name
        binding.numberOfPersonTV.text = number

        return binding.root
    }

    private fun replaceFragment(myFragment: Fragment) {
        var fragmentManager = parentFragmentManager
        var fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainActivityFragmentContainer, myFragment)
        fragmentTransaction.commit()
    }
}