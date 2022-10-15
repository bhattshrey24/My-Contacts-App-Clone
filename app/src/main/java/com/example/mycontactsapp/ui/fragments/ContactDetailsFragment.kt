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
    private val listOfContactsViewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val roomId = args.roomContactId
        val contactDetails = listOfContactsViewModel.listOfContact.value?.find {
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
        adapter.setListItem(list)
    }

    private fun deleteContact(contact: Contact?) {
        if (contact != null) {// delete from sharedViewModel and Room DB
             listOfContactsViewModel.deleteContactFromSharedViewModel(contact)
             listOfContactsViewModel.deleteContactFromRoomDB(contact)
            if (contact.contactId != null) {// because if it's null means it
                // was added by room and not yet synced to Android DB so
                // no need to do anything
               saveCidInSharedPref(contact.contactId)
            }
        }
        findNavController().popBackStack()
    }

    private fun saveCidInSharedPref(cId: Int?) { // todo finish
        // fetch from shared pref
        // put list back in shared pref
        val sharedPref = requireActivity().getSharedPreferences(
            Constants.deletedCidSharedPrefKey,
            Context.MODE_PRIVATE // private means that the shared preference data will
            // be private which means no other app can access it
        )
        val editor = sharedPref.edit()

        var setOfDeletedCid =
            sharedPref.getStringSet(Constants.setOfDeletedContactCidSPKey, mutableSetOf())
                ?: mutableSetOf<String>()

        Log.i(Constants.debugTag, "List Of Deleted Cid $setOfDeletedCid")

        val copyOfSet = hashSetOf<String>() // I have to create a duplicate of the returned set
        // because shared preference returns the "Reference" of the HashSet which is stored in shared pref
        // now when we modify it and again try to store it in shared pref using .putStringSet()
        // then it will compare the reference that we passed with the one that it has and
        // it will realize that both are same so it won't update anything therefore we make a
        // copy so that reference changes because now it will point to a new object and then
        // when we do putString() it will know that we are passing new object so it will simply
        // drop previous reference and store this new duplicate one

        copyOfSet.add(cId.toString())
        copyOfSet.addAll(setOfDeletedCid)

        editor.apply {
            putStringSet(Constants.setOfDeletedContactCidSPKey, copyOfSet)
            apply() // difference between apply and commit is that apply will save data
            // asynchronously
        }


    }

}