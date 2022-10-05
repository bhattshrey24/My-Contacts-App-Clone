package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.ContactDetailsListAdapter
import com.example.mycontactsapp.databinding.FragmentContactDetailsBinding


class ContactDetailsFragment : Fragment() {

    private val binding: FragmentContactDetailsBinding by lazy {
        FragmentContactDetailsBinding.inflate(layoutInflater, null, false)
    }
    private var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: ContactDetailsListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(Constants.debugTag, "Inside onCreate of createDeail")

        val bundle = this.arguments
        val contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)

        binding.nameOfPersonTV.text = contactDetails?.name

        setUpRecyclerView(convertNumAndEmailToList(contactDetails))

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
    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.mainActivityFragmentContainer, myFragment)
            addToBackStack(ContactDetailsFragment::class.java.name)// Giving name so that we
            // can refer to it and pop later
            commit()
        }

    }

}