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

import androidx.lifecycle.*
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.data.models.CursorData
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import kotlinx.coroutines.*

class ContentProviderViewModel(application: Application) : AndroidViewModel(application) {

    var listOfRetrievedContacts = MutableLiveData<List<Contact>>()
    var isSyncFinished = MutableLiveData(false)
    private val app: Application

    init {
        app = application
    }

    private val mColProjection: Array<String> = arrayOf(
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.DISPLAY_NAME,
        ContactsContract.Data.DATA1, // Number or email
        ContactsContract.Data.DATA2, // Type
        ContactsContract.Data.MIMETYPE,
    )
    private val uri: Uri = ContactsContract.Data.CONTENT_URI
    private val mSortOrder = ContactsContract.Data.DISPLAY_NAME
    private val mSelection =
        "${ContactsContract.Data.MIMETYPE} = ? OR ${ContactsContract.Data.MIMETYPE} = ?"
    private val mSelectionArgs = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
    )

    fun fetchDataFromAndroidDb() =
        viewModelScope.launch { // viewModelScope.launch runs on main thread
            val cursor = app.contentResolver.query(
                uri,
                mColProjection,
                mSelection,
                mSelectionArgs,
                mSortOrder
            )
            convertAndSaveInLiveDataList(cursor)
        }

    private fun convertAndSaveInLiveDataList(cursor: Cursor?) {
        val tempListOfContacts = mutableListOf<Contact>()
        val hmOfCiAndIndex = hashMapOf<String, Int>()

        if (cursor != null && cursor.isBeforeFirst) {
            while (cursor.moveToNext()) {
                val cursorData = retrieveDataFromCursor(cursor)
                if (cursorData.mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                    val typeEnum = when (cursorData.type) {
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

                        val idxOfContact = hmOfCiAndIndex.get(cursorData.cId)
                        val retrievedContact = tempListOfContacts.get(idxOfContact!!)
                        val hmForNum = retrievedContact.numbers ?: mutableMapOf()

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
                        val hmOfPhoneNumbers = mutableMapOf<PhoneTypes, String>()
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

                    val typeEnum = when (cursorData.type) {
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
                        val idxOfContact = hmOfCiAndIndex.get(cursorData.cId)
                        val contact = tempListOfContacts.get(idxOfContact!!)
                        val hmForEmail = contact.emails ?: mutableMapOf()

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
                        val hmOfEmail = mutableMapOf<EmailTypes, String>()
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

    private fun retrieveDataFromCursor(cursor: Cursor): CursorData {
        val nameIdx = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
        val numberOrEmailIdx = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        val cIdIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val typeIdx = cursor.getColumnIndex(ContactsContract.Data.DATA2)
        val mimeTypeIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)

        val name = cursor.getString(nameIdx)
        val numberOrEmail =
            cursor.getString(numberOrEmailIdx) // it could hold either number
        // or email it depends on mimetype of that row
        val cId = cursor.getString(cIdIdx)
        val type = cursor.getString(typeIdx)
        val mimeType = cursor.getString(mimeTypeIdx)
        return CursorData(
            name = name,
            numberOrEmail = numberOrEmail,
            cId = cId,
            type = type,
            mimeType = mimeType
        )
    }

    fun syncData() {
        viewModelScope.launch {
            delay(2500) // just so people can see that sync actually worked
            fetchDataFromAndroidDb().join()
            isSyncFinished.value = true
        }
    }

}