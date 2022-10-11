package com.example.mycontactsapp.ui.viewmodels

import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel

class HomePageViewModel() : ViewModel() {

    var isFirstTimeLoaded: Boolean = false
    val loadContactId = 10
    val mColProjection: Array<String> = arrayOf(
        ContactsContract.Data.CONTACT_ID,
        ContactsContract.Data.DISPLAY_NAME,
        ContactsContract.Data.DATA1, // Number or email
        ContactsContract.Data.DATA2, // Type
        ContactsContract.Data.MIMETYPE, // MimeType
    )
    val uri: Uri = ContactsContract.Data.CONTENT_URI
    val mSortOrder = ContactsContract.Data.DISPLAY_NAME
    val mSelection =
        "${ContactsContract.Data.MIMETYPE} = ? OR ${ContactsContract.Data.MIMETYPE} = ?"
    val mSelectionArgs = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
    )


}