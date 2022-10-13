package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.adapters.ContactDetailsListAdapter
import com.example.mycontactsapp.databinding.FragmentContactDetailsBinding
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel


class ContactDetailsFragment : Fragment() {

    private val binding: FragmentContactDetailsBinding by lazy {
        FragmentContactDetailsBinding.inflate(layoutInflater, null, false)
    }
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

        setUpUi(contactDetails,contactId)

        return binding.root
    }

    private fun setUpUi(contactDetails: Contact?, contactId: Int) {
        val isContactDetailNull = contactDetails?.let {
            binding.nameOfPersonTV.text = contactDetails.name
            setUpRecyclerView(convertNumAndEmailToList(contactDetails))
            setUpListeners(contactDetails, contactId)
        } == null
        if (isContactDetailNull) {
            binding.nameOfPersonTV.text = "Contact doesn't Exist"
        }
    }

    private fun setUpListeners(contactDetails: Contact?, contactId: Int) {
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
    }

    private fun convertNumAndEmailToList(contactDetails: Contact?): MutableList<Pair<String, String>> {
        // combining numbers and emails in a single list so that recycler view can show it
        var numbers = contactDetails?.numbers
        var emails = contactDetails?.emails
        var list = mutableListOf<Pair<String, String>>()

        if (numbers != null) {
            for (number in numbers) {
                list.add(number.key.codeOfType.toString() to number.value)
            }
        }
        if (emails != null) {
            for (email in emails) {
                list.add(email.key.codeOfType.toString() to email.value)
            }
        }
        return list
    }

    private fun setUpRecyclerView(list: List<Pair<String, String>>) {
        var adapter = ContactDetailsListAdapter()
        binding.contactDetailRV.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
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
        findNavController().popBackStack()
    }

}