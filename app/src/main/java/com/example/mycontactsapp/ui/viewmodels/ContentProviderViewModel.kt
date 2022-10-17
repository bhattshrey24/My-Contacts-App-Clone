package com.example.mycontactsapp.ui.viewmodels

import android.app.Application
import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.net.Uri
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.data.models.CursorData
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import kotlinx.coroutines.*

class ContentProviderViewModel(application: Application) : AndroidViewModel(application) {


    var listOfRetrievedContacts = MutableLiveData<List<Contact>>()

    var isSyncFinished = MutableLiveData<Boolean>(false)

    private val app: Application

    init {
        app = application
    }

    //var isFirstTimeLoaded: Boolean = false
    private val mColProjection: Array<String> = arrayOf(
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.DISPLAY_NAME,
        ContactsContract.Data.DATA1, // Number or email
        ContactsContract.Data.DATA2, // Type
        ContactsContract.Data.MIMETYPE, // MimeType
    )
    private val uri: Uri = ContactsContract.Data.CONTENT_URI
    private val mSortOrder = ContactsContract.Data.DISPLAY_NAME
    private val mSelection =
        "${ContactsContract.Data.MIMETYPE} = ? OR ${ContactsContract.Data.MIMETYPE} = ?"
    private val mSelectionArgs = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
    )

    fun fetchDataFromAndroidDb() {
        viewModelScope.launch {
            var tempListOfContacts = mutableListOf<Contact>()
            var hmOfCiAndIndex = hashMapOf<String, Int>()
            var cursor = app.contentResolver.query(
                uri,
                mColProjection,
                mSelection,
                mSelectionArgs,
                mSortOrder
            )
            if (cursor != null && cursor.isBeforeFirst) {
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
                                    emails = hmOfEmail,
                                )
                            )
                            hmOfCiAndIndex.put(cursorData.cId, tempListOfContacts.lastIndex)
                        }
                    }
                }
            }
            listOfRetrievedContacts.value = tempListOfContacts
        }
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

    fun syncData(listOfContacts: List<Contact>?) {
        // Delete
        val sharedPref =
            app.getSharedPreferences(Constants.deletedCidSharedPrefKey, Context.MODE_PRIVATE)
        var setOfDeletedCid =
            sharedPref.getStringSet(Constants.setOfDeletedContactCidSPKey, mutableSetOf())
                ?: mutableSetOf<String>()
        Log.i(Constants.debugTag, "set of deleted cID : $setOfDeletedCid")

        for (ele in setOfDeletedCid) {
            Log.i(Constants.debugTag, "**inside for loop")
            deleteContactFromAndroidDB(ele.toInt())
        }
        val editor = sharedPref.edit()
        editor.apply {
            putStringSet(Constants.setOfDeletedContactCidSPKey, mutableSetOf<String>()) // since
            // we deleted all contacts so I'm simply putting empty set in share pref
            apply()
        }
        // Insert // todo later : simply combine all this forEach into one
        val listOfContactsToAddInAndroidRoom = mutableListOf<Contact>()
        listOfContacts?.forEach { contact ->
            if (contact.contactId == null) {
                listOfContactsToAddInAndroidRoom.add(contact)
            }
        }
        for (contact in listOfContactsToAddInAndroidRoom) {
            insertContactToAndroidDB(contact)
        }
        // update
        val listOfContactsToUpdate = mutableListOf<Contact>()
        listOfContacts?.forEach { contact ->
            if (contact.isUpdated) {
                listOfContactsToUpdate.add(contact)
            }
        }
        for (contact in listOfContactsToUpdate) {
            updateContactInAndroidDB(contact)
        }
        isSyncFinished.value = true
    }

    private fun deleteContactFromAndroidDB(cId: Int) {
        Log.i(Constants.debugTag, "inside delete with cID: ${cId}")
        //viewModelScope.launch {
        val cpbo = ArrayList<ContentProviderOperation>()
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
        cpbo.add(
            ContentProviderOperation
                .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    whereClause,
                    arrayOf(
                        "$cId",
                    )
                ).build()
        )

        try {
            val res = app.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
        } catch (e: OperationApplicationException) {
            Log.i(
                Constants.debugTag,
                "OperationApplicationException caught with message : ${e.message}"
            )
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
        } catch (e: Exception) {
            Log.i(Constants.debugTag, " Exception caught with message : ${e.message}")
        }

//        val res = app.contentResolver?.delete(
//            ContactsContract.RawContacts.CONTENT_URI,
//            whereClause,
//            null
//        )
//        if (res != null) {
//            if (res > 0) {
//                Log.i(Constants.debugTag, "Successfully deleted contact")
//            } else {
//                Log.i(Constants.debugTag, "Unable to delete")
//            }
//            // }
//        }
    }

    private fun insertContactToAndroidDB(contact: Contact) {
        // todo , get the CID of contact and update it in ROOM db as well
        val cpbo = ArrayList<ContentProviderOperation>()

        // This is mandatory to do even if you don't specify an account with it
        cpbo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Adding Name
        cpbo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    contact.name
                )
                .build()
        )

        if (contact.numbers != null) {
            // Adding Numbers
            for (num in contact.numbers!!) {
                cpbo.add(
                    ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI
                    ).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            num.value// todo fix
                        ).withValue(
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            num.key.codeOfType
                        )
                        .build()
                )
            }
        }

        try {
            val res = app.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
        } catch (e: OperationApplicationException) {
            Log.i(
                Constants.debugTag,
                "OperationApplicationException caught with message : ${e.message}"
            )
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
        } catch (e: Exception) {
            Log.i(Constants.debugTag, " Exception caught with message : ${e.message}")
        }
    }

    private fun updateContactInAndroidDB(contact: Contact) {


        val cpbo = ArrayList<ContentProviderOperation>()

        for (num in contact.numbers!!) {
            cpbo.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND "
                                + ContactsContract.CommonDataKinds.Phone.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Phone.TYPE + " = ? ",
                        arrayOf(
                            "${contact.contactId}",
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                            num.key.codeOfType.toString()
                        )
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        num.value
                    )
                    .build()
            )
        }

        for (email in contact.emails!!) {

            cpbo.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND "
                                + ContactsContract.CommonDataKinds.Email.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Email.TYPE + " = ? ",
                        arrayOf(
                            "${contact.contactId}",
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                            email.key.codeOfType.toString()
                        )
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        email.value
                    )
                    .build()
            )
        }

        cpbo.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
                    arrayOf("${contact.contactId}")
                )
                .withValue(
                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                    contact.name
                )
                .build()
        )

        try {
            val res = app.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
            if (res != null) {
                Log.i(Constants.debugTag, "Successfully Edited!")
            }
        } catch (e: OperationApplicationException) {
            Log.i(
                Constants.debugTag,
                "OperationApplicationException caught with message : ${e.message}"
            )
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
        } catch (e: Exception) {
            Log.i(Constants.debugTag, " Exception caught with message : ${e.message}")

        }
    }
}