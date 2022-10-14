package com.example.mycontactsapp.ui.viewmodels

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
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

}