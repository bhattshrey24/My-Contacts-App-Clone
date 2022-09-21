package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.databinding.FragmentCreateOrModifyContactBinding


class CreateOrModifyContactFragment : Fragment() {
    private val binding: FragmentCreateOrModifyContactBinding by lazy {
        FragmentCreateOrModifyContactBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var bundle = this.arguments
        var isEdit = bundle?.getBoolean(Constants.booleanIsEditKey)

        if (isEdit == true) {
            var contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)
            binding.createOrEditTV.text = "Edit Contact"
            binding.nameOfPersonET.setText(contactDetails?.name) // setText cause text does "Editable"
            binding.numberOfPersonET.setText(contactDetails?.number)
        }

        binding.eocSubmitButton.setOnClickListener {
            if (isEdit == true) {
                updateValues()
            } else {
                createNewContact()
            }
        }

        return binding.root
    }

    private fun updateValues() {
        Log.i("DUMMMYY", "Inside Update")
    }

    private fun createNewContact() {
        Log.i("DUMMMYY", "Inside create")

    }

}