package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.Contact
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

        val bundle = this.arguments
        val contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)
        binding.nameOfPersonTV.text = contactDetails?.name
        binding.numberOfPersonTV.text = contactDetails?.number

        binding.editContactFloatingButton.setOnClickListener {
            val fragment = CreateOrModifyContactFragment()
            val bundle = Bundle()
            bundle.putBoolean(Constants.booleanIsEditKey, true)
            fragment.arguments = bundle
            bundle.putParcelable(Constants.contactDetailsKey, contactDetails)
            replaceFragment(fragment)
        }

        binding.deleteContactFloatingButton.setOnClickListener {
            deleteContact(contactDetails)
        }

        return binding.root
    }


    private fun deleteContact(contact: Contact?) {
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ${contact?.contactId}"
        val res = activity?.contentResolver?.delete(
            ContactsContract.RawContacts.CONTENT_URI,
            whereClause,
            null
        )
        if (res != null) {
            if (res > 0) {
                Toast.makeText(context, "Successfully deleted contact", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Unable to delete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainActivityFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(ContactDetailsFragment::class.java.name)// Giving name so that we
        // can refer to it and pop later
        fragmentTransaction.commit()
    }

}