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
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.data.models.CursorData
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
        fetchAndLoadDataInRecyclerView()
        setUpListeners()


        return binding.root
    }

    private fun setUpListeners() {
        binding.addNewContactFloatingButton.setOnClickListener {
            var action = HomeFragmentDirections.actionHomeFragmentToCreateOrModifyContactFragment(
                false,
                -1
            )
            findNavController().navigate(action)
        }
    }

    private fun fetchAndLoadDataInRecyclerView() {
        if (!viewModel.isFirstTimeLoaded) {
            LoaderManager.getInstance(requireActivity())
                .initLoader(viewModel.loadContactId, null, this)
            viewModel.isFirstTimeLoaded = true
        } else {
            listOfContactsViewModel.listOfContact.value?.let { adapter?.setContact(it) }
        }
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
                viewModel.mSelection,
                viewModel.mSelectionArgs,
                viewModel.mSortOrder
            )
        }
        return CursorLoader(requireActivity())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        var tempListOfContacts = mutableListOf<Contact>()
        var hmOfCiAndIndex = hashMapOf<String, Int>() // ie. hashMap For ContactId and Index
        // this will tell the index at which the contact is saved in the tempListOfContacts so that
        // I could retrieve it fast and then update email or number of that contact and then again
        // put it in that index

        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                var cursorData = retrieveDataFromCursor(cursor)
                if (cursorData.mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                    var typeEnum = when (cursorData.type) {
                        PhoneTypes.Home.codeOfType.toString() -> {
                            PhoneTypes.Home
                        }
                        PhoneTypes.Work.codeOfType.toString() -> {
                            PhoneTypes.Work

                        }
                        PhoneTypes.Mobile.codeOfType.toString() -> {
                            PhoneTypes.Mobile

                        }
                        else -> {
                            PhoneTypes.Home
                        }
                    }
                    if (hmOfCiAndIndex.containsKey(cursorData.cId)) {

                        var idxOfContact = hmOfCiAndIndex.get(cursorData.cId)
                        var retrievedContact = tempListOfContacts.get(idxOfContact!!)
                        var hmForNum = retrievedContact.numbers ?: mutableMapOf()

                        hmForNum.put(typeEnum, cursorData.numberOrEmail)

                        tempListOfContacts.set(
                            idxOfContact,
                            Contact(
                                name = retrievedContact.name,
                                contactId = retrievedContact.contactId,
                                numbers = hmForNum,
                                emails = retrievedContact.emails
                            )
                        )
                    } else {
                        var hmOfPhoneNumbers = mutableMapOf<PhoneTypes, String>()
                        hmOfPhoneNumbers.put(typeEnum, cursorData.numberOrEmail)

                        tempListOfContacts.add(
                            Contact(
                                name = cursorData.name,
                                contactId = cursorData.cId.toInt(),
                                numbers = hmOfPhoneNumbers,
                                emails = null
                            )
                        )
                        hmOfCiAndIndex.put(cursorData.cId, tempListOfContacts.lastIndex)
                    }
                }
                if (cursorData.mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {

                    var typeEnum = when (cursorData.type) {
                        EmailTypes.Home.codeOfType.toString() -> {
                            EmailTypes.Home
                        }
                        EmailTypes.Work.codeOfType.toString() -> {
                            EmailTypes.Work

                        }
                        else -> {
                            EmailTypes.Home
                        }
                    }

                    if (hmOfCiAndIndex.containsKey(cursorData.cId)) {
                        var idxOfContact = hmOfCiAndIndex.get(cursorData.cId)
                        var contact = tempListOfContacts.get(idxOfContact!!)
                        var hmForEmail = contact.emails ?: mutableMapOf<EmailTypes, String>()

                        hmForEmail.put(typeEnum, cursorData.numberOrEmail)

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
                        var hmOfEmail = mutableMapOf<EmailTypes, String>()
                        hmOfEmail.put(typeEnum, cursorData.numberOrEmail)
                        tempListOfContacts.add(
                            Contact(
                                name = cursorData.name,
                                contactId = cursorData.cId.toInt(),
                                numbers = null,
                                emails = hmOfEmail
                            )
                        )
                        hmOfCiAndIndex.put(cursorData.cId, tempListOfContacts.lastIndex)
                    }
                }
            }
        }

        listOfContactsViewModel.setListOfContact(tempListOfContacts)
        listOfContactsViewModel.listOfContact.value?.let { adapter?.setContact(it) }

        Constants.listOfAllContacts = tempListOfContacts // Saving to Dummy DB
    }

    private fun retrieveDataFromCursor(cursor: Cursor): CursorData {
        var nameIdx = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
        var numberOrEmailIdx = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        var cIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        var typeIdx = cursor.getColumnIndex(ContactsContract.Data.DATA2)
        var mimeTypeIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)

        var name = cursor.getString(nameIdx)
        var numberOrEmail =
            cursor.getString(numberOrEmailIdx) // it could hold either number
        // or email it depends on mimetype of that row
        var cId = cursor.getString(cIdIdx)
        var type = cursor.getString(typeIdx)
        var mimeType = cursor.getString(mimeTypeIdx)
        return CursorData(
            name = name,
            numberOrEmail = numberOrEmail,
            cId = cId,
            type = type,
            mimeType = mimeType
        )
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

}
