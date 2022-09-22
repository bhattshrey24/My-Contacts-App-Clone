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

    private var uri: Uri =
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI// ContactsContract.Data.CONTENT_URI

    var isFirstTimeLoaded: Boolean = false
    var mSortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME

    var listOfContacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.i(Constants.debugTag," raw contact ${ContactsContract.Contacts.NAME_RAW_CONTACT_ID}")

        setUpRecyclerView()
        binding.addNewContactFloatingButton.setOnClickListener {
            var fragment = CreateOrModifyContactFragment()
            var bundle = Bundle()
            bundle.putBoolean(Constants.booleanIsEditKey, false)
            fragment.arguments = bundle
            replaceFragment(fragment)
        }
        return binding.root
    }


    override fun onResume() {
        if (isFirstTimeLoaded) {
            LoaderManager.getInstance(requireActivity()).initLoader(loadContactId, null, this)
            isFirstTimeLoaded = true
        } else {
            LoaderManager.getInstance(requireActivity()).restartLoader(loadContactId, null, this)
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
        Toast.makeText(context, "pressed $position", Toast.LENGTH_SHORT).show()
        // Navigate to Contact Display Basically Change the fragment
        var fragment = ContactDetailsFragment()
        var bundle = Bundle()
        bundle.putParcelable(Constants.contactDetailsKey, listOfContacts[position])
        fragment.arguments = bundle

        replaceFragment(fragment)
    }

    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainActivityFragmentContainer, myFragment)
        fragmentTransaction.commit()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == loadContactId) {
            return CursorLoader(
                requireActivity(),
                uri,
                mColProjection,
                null,
                null,
                mSortOrder
            )
        }
        return CursorLoader(requireActivity()) // Might give error

    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        listOfContacts.clear()
        if (cursor != null && cursor.count > 0) {
            var contactsList = StringBuilder("")
            while (cursor.moveToNext()) {
                var name = cursor.getString(0)
                var number = cursor.getString(1)
                var id = cursor.getString(2).toInt()
                contactsList.append("$name,$number\n")
                Log.i(Constants.debugTag, "name = $name and Id : $id")
                listOfContacts.add(Contact(name, number,id))
            }
        }
        Log.i(Constants.debugTag, "Size!!!!!! : ${listOfContacts.size}")
        adapter?.setContact(listOfContacts)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

}