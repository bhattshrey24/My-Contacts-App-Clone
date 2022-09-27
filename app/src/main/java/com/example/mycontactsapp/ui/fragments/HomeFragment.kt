package com.example.mycontactsapp.ui.fragments

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentHomeBinding

class HomeFragment() : Fragment(),
    AllContactsListAdapter.OnContactClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater, null, false)
    }

    var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: AllContactsListAdapter? = null

    var loadContactId = 10
    private var mColProjection: Array<String> = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
    )
    private var uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    private var isFirstTimeLoaded: Boolean = false
    private var mSortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
    private var listOfContacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setUpRecyclerView()

        binding.addNewContactFloatingButton.setOnClickListener {
            val fragment = CreateOrModifyContactFragment()
            val bundle = Bundle()
            bundle.putBoolean(Constants.booleanIsEditKey, false)
            fragment.arguments = bundle
            replaceFragment(fragment)
        }

        return binding.root
    }


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

    private fun setUpRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        binding.homePageRecyclerView.layoutManager = layoutManager
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.adapter = adapter
    }

    override fun onContactClick(position: Int) {
        val fragment = ContactDetailsFragment()
        val bundle = Bundle()
        bundle.putParcelable(Constants.contactDetailsKey, listOfContacts[position])
        fragment.arguments = bundle
        replaceFragment(fragment)
    }

    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainActivityFragmentContainer, myFragment)
        fragmentTransaction.addToBackStack(HomeFragment::class.java.name)
        fragmentTransaction.commit()
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
            var contactsList = StringBuilder("")
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                val number = cursor.getString(1)
                val id = cursor.getString(2).toInt()
                contactsList.append("$name,$number\n")
                listOfContacts.add(Contact(name, number, id))
            }
        }
        adapter?.setContact(listOfContacts)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

}