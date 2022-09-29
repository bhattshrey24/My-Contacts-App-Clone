package com.example.mycontactsapp.ui.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.SecondActivity
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentShowAllContactsBinding

class ShowAllContactsFragment() : Fragment(),
    AllContactsListAdapter.OnContactClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private val binding: FragmentShowAllContactsBinding by lazy {
        FragmentShowAllContactsBinding.inflate(layoutInflater, null, false)
    }

    var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: AllContactsListAdapter? = null

    var loadContactId = 10
    private var mColProjection: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
//        ContactsContract.CommonDataKinds.Phone.ACCOUNT_TYPE_AND_DATA_SET,
//        ContactsContract.RawContacts.ACCOUNT_TYPE,
//        ContactsContract.RawContacts.ACCOUNT_NAME,
    )
    private var uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    private var isFirstTimeLoaded: Boolean = false
    private var mSortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
     var listOfContacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setUpRecyclerView()
        return binding.root
    }

     // Don't update unecessary
    override fun onResume() { // So that we load it new every time user comes back to screen
        if (isFirstTimeLoaded) {
            LoaderManager.getInstance(requireActivity()).initLoader(loadContactId, null, this)
            isFirstTimeLoaded = true
        } else {
            LoaderManager.getInstance(requireActivity()).restartLoader(loadContactId, null, this)
            // We restart loader because after the first time the cursor would have reached end so this will
            // again put cursor on top of records so that we can read again
        }
        super.onResume()
    }

    private fun setUpRecyclerView() { // add .apply
        layoutManager = LinearLayoutManager(context)
        binding.homePageRecyclerView.layoutManager = layoutManager
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.adapter = adapter
    }

    override fun onContactClick(position: Int) {
        requireActivity().startActivity(
            Intent(activity, SecondActivity::class.java)
                .putExtra(Constants.contactDetailsKey, listOfContacts[position])
        )
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == loadContactId) { // Since there can be many loaders working at a time so this
            // loadContactId helps us to distinguish our loader .This can be any value that we want
            return CursorLoader(
                requireActivity(),
                uri,
                mColProjection,
                null,
                null,
                mSortOrder
            )
        }
        return CursorLoader(requireActivity())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        listOfContacts.clear() // clearing any previous data before loading new
        if (cursor != null && cursor.count > 0) {
           // var contactsList = StringBuilder("")
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                val number = cursor.getString(1)
                val id = cursor.getString(2).toInt()
             //   contactsList.append("$name,$number\n")
                listOfContacts.add(Contact(name, number, id))
            }
        }
        adapter?.setContact(listOfContacts)

        Constants.listOfAllContacts=listOfContacts // Saving to Dummy DB
        // change to interface method
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

}