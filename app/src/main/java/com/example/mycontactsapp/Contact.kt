package com.example.mycontactsapp

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class Contact(
    var contactId: Int?,
    var name: String?,
    var numbers: MutableMap<String, String>?,
    var emails: MutableMap<String, String>?,
)