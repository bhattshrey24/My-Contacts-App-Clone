package com.example.mycontactsapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycontactsapp.adapters.ContactDetailsListAdapter
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.databinding.FragmentContactDetailsBinding
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel


class ContactDetailsFragment : Fragment() {

    private val binding: FragmentContactDetailsBinding by lazy {
        FragmentContactDetailsBinding.inflate(layoutInflater, null, false)
    }
    private val args: ContactDetailsFragmentArgs by navArgs()
    private val sharedViewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val roomId = args.roomContactId

        val contactDetails = sharedViewModel.listOfContact.value?.find { // checking
            // using room id and not cid because if user added contact from our app then
            // it won't have cid but all contacts have roomId
            it.roomContactId == roomId
        }

        setUpUi(contactDetails, roomId)

        return binding.root
    }

    private fun setUpUi(contactDetails: Contact?, roomId: Int) {
        val isContactDetailNull = contactDetails?.let {
            binding.nameOfPersonTV.text = contactDetails.name
            setUpRecyclerView(convertNumAndEmailToList(contactDetails))
            setUpListeners(contactDetails, roomId)
        } == null
        if (isContactDetailNull) {
            "Contact doesn't Exist".also { binding.nameOfPersonTV.text = it }
        }
    }

    private fun setUpListeners(contactDetails: Contact?, roomId: Int) {
        binding.editContactFloatingButton.setOnClickListener {
            val action =
                ContactDetailsFragmentDirections.actionContactDetailsFragmentToCreateOrModifyContactFragment(
                    true,
                    roomId
                ) // using safeargs since it gives us compile time safety when doing
            // navigation which is not the case in bundle
            findNavController().navigate(action)
        }
        binding.deleteContactFloatingButton.setOnClickListener {
            deleteContact(contactDetails)
        }
    }

    private fun convertNumAndEmailToList(contactDetails: Contact?): List<Pair<String, String>> {
        // combining numbers and emails in a single list so that recycler view can show it
        val numbers = contactDetails?.numbers
        val emails = contactDetails?.emails
        val list = mutableListOf<Pair<String, String>>()

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
        val adapter = ContactDetailsListAdapter()
        binding.contactDetailRV.apply {
            this.layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        adapter.setListItem(list)
    }

    private fun deleteContact(contact: Contact?) {
        contact?.let { // delete from sharedViewModel and Room DB
            sharedViewModel.deleteContact(contact)
        }
        findNavController().popBackStack()
    }

}