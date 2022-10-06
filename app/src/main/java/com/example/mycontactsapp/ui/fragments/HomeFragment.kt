package com.example.mycontactsapp.ui.fragments

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentHomeBinding
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import com.example.mycontactsapp.ui.viewmodels.HomePageViewModel
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel

class HomeFragment() : Fragment(),
    AllContactsListAdapter.OnContactClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater, null, false)
    }

    private lateinit var viewModel: HomePageViewModel
    private val listOfContactsViewModel: ListOfContactsViewModel by activityViewModels()


    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: AllContactsListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setUpViewModel()
        setUpRecyclerView()

        if (!viewModel.isFirstTimeLoaded) {
            LoaderManager.getInstance(requireActivity())
                .initLoader(viewModel.loadContactId, null, this)
            viewModel.isFirstTimeLoaded = true
        } else {
            listOfContactsViewModel.listOfContact.value?.let { adapter?.setContact(it) }
        }


        binding.addNewContactFloatingButton.setOnClickListener {
            var action = HomeFragmentDirections.actionHomeFragmentToCreateOrModifyContactFragment(
                false,
                -1
            )
            findNavController().navigate(action)
        }
        return binding.root
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(requireActivity())[HomePageViewModel::class.java]
    }

    private fun setUpRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.apply {
            this.layoutManager = this@HomeFragment.layoutManager // using
            // "@" we can distinguish between properties of recyclerView and HomeFragment
            this.adapter = this@HomeFragment.adapter
        }
    }

    override fun onContactClick(position: Int) {
        val filteredListFromAdapter = adapter?.getFilteredListOfContacts() ?: listOf<Contact>()
        var action = HomeFragmentDirections.actionHomeFragmentToContactDetailsFragment(
            filteredListFromAdapter[position].contactId ?: -1
        )
        findNavController().navigate(action)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == viewModel.loadContactId) { // Since there can be many loaders working at a time so this
            // loadContactId helps us to distinguish our loader .This can be any value that we want
            return CursorLoader(
                requireActivity(),
                viewModel.uri,
                viewModel.mColProjection,
                null,
                null,
                viewModel.mSortOrder
            )
        }
        return CursorLoader(requireActivity())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        var tempListOfContacts = mutableListOf<Contact>()
        var hmOfCiAndIndex = hashMapOf<String, Int>()

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                var nameIdx = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                var numberOrEmailIdx = cursor.getColumnIndex(ContactsContract.Data.DATA1)
                var cIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                var typeIdx = cursor.getColumnIndex(ContactsContract.Data.DATA2)
                var mimeTypeIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)

                var name = cursor.getString(nameIdx)
                var numberOrEmail = cursor.getString(numberOrEmailIdx)
                var cId = cursor.getString(cIdIdx)
                var type = cursor.getString(typeIdx)
                var mimeType = cursor.getString(mimeTypeIdx)


                if (mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                    Log.i(Constants.debugTag, "Type inside phone  :$type")
                    if (hmOfCiAndIndex.containsKey(cId.toString())) {
                        var idxOfContact = hmOfCiAndIndex.get(cId)
                        var contact = tempListOfContacts.get(idxOfContact!!)
                        var hmForNum = contact.numbers ?: mutableMapOf<String, String>()

                        hmForNum.put(type, numberOrEmail)

                        tempListOfContacts.set(
                            idxOfContact,
                            Contact(
                                name = contact.name,
                                contactId = contact.contactId,
                                numbers = hmForNum,
                                emails = contact.emails
                            )
                        )
                    } else {
                        var hmOfPhoneNumbers = mutableMapOf<String, String>()
                        hmOfPhoneNumbers.put(type, numberOrEmail)
                        tempListOfContacts.add(
                            Contact(
                                name = name,
                                contactId = cId.toInt(),
                                numbers = hmOfPhoneNumbers,
                                emails = null
                            )
                        )

                        hmOfCiAndIndex.put(cId, tempListOfContacts.lastIndex)
                    }
                }

                if (mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
                    Log.i(Constants.debugTag, "Type inside email  :$type")
                    if (hmOfCiAndIndex.containsKey(cId.toString())) {
                        var idxOfContact = hmOfCiAndIndex.get(cId)
                        var contact = tempListOfContacts.get(idxOfContact!!)
                        var hmForEmail = contact.emails ?: mutableMapOf<String, String>()

                        hmForEmail.put(type, numberOrEmail)

                        tempListOfContacts.set(
                            idxOfContact,
                            Contact(
                                name = contact.name,
                                contactId = contact.contactId,
                                numbers = contact.numbers,
                                emails = hmForEmail
                            )
                        )
                    } else {
                        var hmOfEmail = mutableMapOf<String, String>()
                        hmOfEmail.put(type, numberOrEmail)
                        tempListOfContacts.add(
                            Contact(
                                name = name,
                                contactId = cId.toInt(),
                                numbers = null,
                                emails = hmOfEmail
                            )
                        )
                        hmOfCiAndIndex.put(cId, tempListOfContacts.lastIndex)
                    }
                }
            }
        }
        listOfContactsViewModel.setListOfContact(tempListOfContacts)
        listOfContactsViewModel.listOfContact.value?.let { adapter?.setContact(it) }

        Constants.listOfAllContacts = tempListOfContacts // Saving to Dummy DB
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

}
