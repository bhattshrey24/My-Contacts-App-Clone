package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.ContactDetailsListAdapter
import com.example.mycontactsapp.databinding.FragmentContactDetailsBinding
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel


class ContactDetailsFragment : Fragment() {

    private val binding: FragmentContactDetailsBinding by lazy {
        FragmentContactDetailsBinding.inflate(layoutInflater, null, false)
    }
    private var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: ContactDetailsListAdapter? = null
    private val args: ContactDetailsFragmentArgs by navArgs()
    private val listOfContactsViewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val contactId = args.contactID
        val contactDetails = listOfContactsViewModel.listOfContact.value?.find {
            it.contactId == contactId
        }

        binding.nameOfPersonTV.text = contactDetails?.name

        setUpRecyclerView(convertNumAndEmailToList(contactDetails))

        binding.editContactFloatingButton.setOnClickListener {
            val action =
                ContactDetailsFragmentDirections.actionContactDetailsFragmentToCreateOrModifyContactFragment(
                    true,
                    contactId
                )
            findNavController().navigate(action)
        }

        binding.deleteContactFloatingButton.setOnClickListener {
            deleteContact(contactDetails)
        }

        return binding.root
    }

    private fun convertNumAndEmailToList(contactDetails: Contact?): MutableList<Pair<String, String>> {
        var numbers = contactDetails?.numbers
        var emails = contactDetails?.emails
        var list = mutableListOf<Pair<String, String>>()
        if (numbers != null) {
            for (number in numbers) {
                list.add(number.key to number.value)
            }
        }
        if (emails != null) {
            for (email in emails) {
                list.add(email.key to email.value)
            }
        }
        return list
    }

    private fun setUpRecyclerView(list: List<Pair<String, String>>) {
        layoutManager = LinearLayoutManager(context)
        adapter = ContactDetailsListAdapter()
        binding.contactDetailRV.apply {
            this.layoutManager = this@ContactDetailsFragment.layoutManager
            this.adapter = this@ContactDetailsFragment.adapter
        }
        adapter?.setListItem(list)
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
        //requireActivity().finish()
    }

}