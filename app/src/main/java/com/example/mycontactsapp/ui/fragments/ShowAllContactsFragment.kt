package com.example.mycontactsapp.ui.fragments

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
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
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentShowAllContactsBinding

class ShowAllContactsFragment() : Fragment(),
    AllContactsListAdapter.OnContactClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private val binding: FragmentShowAllContactsBinding by lazy {
        FragmentShowAllContactsBinding.inflate(layoutInflater, null, false)
    }

    private var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: AllContactsListAdapter? = null

    private var loadContactId = 10
    private var mColProjection: Array<String> = arrayOf(
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.DISPLAY_NAME,
        ContactsContract.Data.DATA1, // Number or email
        ContactsContract.Data.DATA2, // Type
        ContactsContract.Data.MIMETYPE, // MimeType
    )
    private var uri: Uri = ContactsContract.Data.CONTENT_URI
    private var isFirstTimeLoaded: Boolean = false
    private var mSortOrder = ContactsContract.Data.DISPLAY_NAME
    var listOfContacts = mutableListOf<Contact>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setUpRecyclerView()
        return binding.root
    }

    // todo fix Don't update unecessary
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

    private fun setUpRecyclerView() { // todo fix add .apply
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
                null, // Maybe change selection arg so that it
                // removes rows with unnecessary mimetype
                null,
                mSortOrder
            )
        }
        return CursorLoader(requireActivity())
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        listOfContacts.clear() // clearing any previous data before loading new
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
                        var contact = listOfContacts.get(idxOfContact!!)
                        var hmForNum = contact.numbers ?: mutableMapOf<String, String>()

                        hmForNum.put(type, numberOrEmail)

                        listOfContacts.set(
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
                        listOfContacts.add(
                            Contact(
                                name = name,
                                contactId = cId.toInt(),
                                numbers = hmOfPhoneNumbers,
                                emails = null
                            )
                        )

                        hmOfCiAndIndex.put(cId, listOfContacts.lastIndex)
                    }
                }

                if (mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
                    //   Log.i(Constants.debugTag,"Inside Email $name")
                    if (hmOfCiAndIndex.containsKey(cId.toString())) {
                        var idxOfContact = hmOfCiAndIndex.get(cId)
                        var contact = listOfContacts.get(idxOfContact!!)
                        var hmForEmail = contact.emails ?: mutableMapOf<String, String>()

                        hmForEmail.put(type, numberOrEmail)

                        listOfContacts.set(
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
                        listOfContacts.add(
                            Contact(
                                name = name,
                                contactId = cId.toInt(),
                                numbers = null,
                                emails = hmOfEmail
                            )
                        )

                        hmOfCiAndIndex.put(cId, listOfContacts.lastIndex)
                    }
                }

                //  contactsList.append("Cid : $cId , name : $name, number:  $numberOrEmail , type: $type , mimeType: $mimeType  \n ")
              //  listOfContacts.add(Contact(name = name, numbers = numberOrEmail, contactId =  cid = cId.toInt() , emails = ))
            }
        }

//        for(ele in listOfContacts){
//            Log.i(Constants.debugTag, "Element $ele \n")
//        }

        adapter?.setContact(listOfContacts)
        Constants.listOfAllContacts = listOfContacts // Saving to Dummy DB
        // change to interface method
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}


}

// Below is for debug purpose
//ContactsContract.Data.DISPLAY_NAME,
//ContactsContract.Data.DATA1,
//ContactsContract.Data.DATA2,
//ContactsContract.Data.DATA3,
//ContactsContract.Data.DATA4,
//ContactsContract.Data.DATA5,
//ContactsContract.Data.DATA6,
//ContactsContract.Data.DATA7,
//ContactsContract.Data.DATA8,
//ContactsContract.Data.DATA9,
//ContactsContract.Data.DATA10,
//ContactsContract.Data.DATA11,
//ContactsContract.Data.DATA12,
//ContactsContract.Data.DATA13,
//ContactsContract.Data.DATA14,
//ContactsContract.Data.DATA15,
//ContactsContract.Data.CONTACT_ID,
//ContactsContract.Data.LOOKUP_KEY,
//ContactsContract.Data.MIMETYPE,

//var d1Idx = cursor.getColumnIndex(ContactsContract.Data.DATA1)
//var d2Idx = cursor.getColumnIndex(ContactsContract.Data.DATA2)
//var d3Idx = cursor.getColumnIndex(ContactsContract.Data.DATA3)
//var d4Idx = cursor.getColumnIndex(ContactsContract.Data.DATA4)
//var d5Idx = cursor.getColumnIndex(ContactsContract.Data.DATA5)
//var d6Idx = cursor.getColumnIndex(ContactsContract.Data.DATA6)
//var d7Idx = cursor.getColumnIndex(ContactsContract.Data.DATA7)
//var d8Idx = cursor.getColumnIndex(ContactsContract.Data.DATA8)
//var d9Idx = cursor.getColumnIndex(ContactsContract.Data.DATA9)
//var d10Idx = cursor.getColumnIndex(ContactsContract.Data.DATA10)
//var d11Idx = cursor.getColumnIndex(ContactsContract.Data.DATA11)
//var d12Idx = cursor.getColumnIndex(ContactsContract.Data.DATA12)
//var d13Idx = cursor.getColumnIndex(ContactsContract.Data.DATA13)
//var d14Idx = cursor.getColumnIndex(ContactsContract.Data.DATA14)
//var d15Idx = cursor.getColumnIndex(ContactsContract.Data.DATA15)
//var ciIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
//var lkIdx = cursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY)
//var mtIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
//var nameIdx = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
//
//// var atIdx = cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)
//
//var d1 = cursor.getString(d1Idx)
//var d2 = cursor.getString(d2Idx)
//var d3 = cursor.getString(d3Idx)
//var d4 = cursor.getString(d4Idx)
//var d5 = cursor.getString(d5Idx)
//var d6 = cursor.getString(d6Idx)
//var d7 = cursor.getString(d7Idx)
//var d8 = cursor.getString(d8Idx)
//var d9 = cursor.getString(d9Idx)
//var d10 = cursor.getString(d10Idx)
//var d11 = cursor.getString(d11Idx)
//var d12 = cursor.getString(d12Idx)
//var d13 = cursor.getString(d13Idx)
//var d14 = cursor.getString(d14Idx)
//var d15 = cursor.getString(d15Idx)
//var ci = cursor.getString(ciIdx)
//var lk = cursor.getString(lkIdx)
//var mt = cursor.getString(mtIdx)
//var name = cursor.getString(nameIdx)
//
//
//// var at = cursor.getString(atIdx)
//
//contactsList.append("name : $name, d1: $d1 , d2: $d2 ,d3: $d3 ,d4: $d4 ,d5: $d5, ci: $ci, lk: $lk, mt: $mt,d6: $d6 ,d7: $d7 ,d8: $d8 ,d9: $d9 ,d10: $d10,d11: $d11,d12: $d12,d13: $d13,d14: $d14,d15: $d15\n ")