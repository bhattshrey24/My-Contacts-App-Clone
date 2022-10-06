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
    var adapter: AllContactsListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setUpViewModel()
        setUpRecyclerView()

        binding.addNewContactFloatingButton.setOnClickListener {
            var action = HomeFragmentDirections.actionHomeFragmentToCreateOrModifyContactFragment(
                false,
                null
            )
            findNavController().navigate(action)
        }

        return binding.root
    }
    private fun setUpViewModel() {
        viewModel = ViewModelProvider(requireActivity())[HomePageViewModel::class.java]
    }

    // todo fix Don't update unecessary
    override fun onResume() { // So that we load it new every time user comes back to screen
        if (viewModel.isFirstTimeLoaded) {
            LoaderManager.getInstance(requireActivity())
                .initLoader(viewModel.loadContactId, null, this)
            viewModel.isFirstTimeLoaded = true
        } else {
            LoaderManager.getInstance(requireActivity())
                .restartLoader(viewModel.loadContactId, null, this)
            // We restart loader because after the first time the cursor would have reached end so this will
            // again put cursor on top of records so that we can read again
        }
        super.onResume()
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
            filteredListFromAdapter[position]
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
                null, // Maybe change selection arg so that it
                // removes rows with unnecessary mimetype
                null,
                viewModel.mSortOrder
            )
        }
        return CursorLoader(requireActivity())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        viewModel.listOfContacts.clear() // clearing any previous data before loading new
        var hmOfCiAndIndex = hashMapOf<String, Int>()

        if (cursor != null && cursor.count > 0) {
            var contactsList = StringBuilder("")

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

                    if (hmOfCiAndIndex.containsKey(cId.toString())) {
                        var idxOfContact = hmOfCiAndIndex.get(cId)
                        var contact = viewModel.listOfContacts.get(idxOfContact!!)
                        var hmForNum = contact.numbers ?: mutableMapOf<String, String>()

                        hmForNum.put(type, numberOrEmail)

                        viewModel.listOfContacts.set(
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
                        viewModel.listOfContacts.add(
                            Contact(
                                name = name,
                                contactId = cId.toInt(),
                                numbers = hmOfPhoneNumbers,
                                emails = null
                            )
                        )

                        hmOfCiAndIndex.put(cId, viewModel.listOfContacts.lastIndex)
                    }
                }

                if (mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
                    //   Log.i(Constants.debugTag,"Inside Email $name")
                    if (hmOfCiAndIndex.containsKey(cId.toString())) {
                        var idxOfContact = hmOfCiAndIndex.get(cId)
                        var contact = viewModel.listOfContacts.get(idxOfContact!!)
                        var hmForEmail = contact.emails ?: mutableMapOf<String, String>()

                        hmForEmail.put(type, numberOrEmail)

                        viewModel.listOfContacts.set(
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
                        viewModel.listOfContacts.add(
                            Contact(
                                name = name,
                                contactId = cId.toInt(),
                                numbers = null,
                                emails = hmOfEmail
                            )
                        )

                        hmOfCiAndIndex.put(cId, viewModel.listOfContacts.lastIndex)
                    }
                }
              }
        }

        adapter?.setContact(viewModel.listOfContacts)
        Constants.listOfAllContacts = viewModel.listOfContacts // Saving to Dummy DB
        // change to interface method
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

}
