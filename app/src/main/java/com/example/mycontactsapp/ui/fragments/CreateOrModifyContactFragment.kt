package com.example.mycontactsapp.ui.fragments

import android.content.ContentProviderResult
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
        var contactDetails: Contact? = null
        if (isEdit == true) {
            contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)
            binding.createOrEditTV.text = "Edit Contact"
            binding.nameOfPersonET.setText(contactDetails?.name) // setText cause text does "Editable"
            binding.numberOfPersonET.setText(contactDetails?.number)
        }

        binding.eocSubmitButton.setOnClickListener {
            if (isEdit == true) {
                updateValues(contactDetails)
            } else {
                createNewContact()
            }
        }

        return binding.root
    }

    private fun updateValues(oldContactDetails: Contact?) {
        var newName: String = binding.nameOfPersonET.text.toString()
        var newNumber: String = binding.numberOfPersonET.text.toString()
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ${oldContactDetails?.contactId} "
        val contentResolver = activity?.contentResolver

        val contentValues = ContentValues()
         contentValues.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY, newName)
       // contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)

        contentResolver?.update(
            ContactsContract.RawContacts.CONTENT_URI,
            contentValues,
            whereClause,
            null
        )
    }

    private fun createNewContact() {
        Log.i("DUMMMYY", "Inside create")

    }

}