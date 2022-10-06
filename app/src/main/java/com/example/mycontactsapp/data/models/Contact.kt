package com.example.mycontactsapp.data.models

import android.os.Parcel
import android.os.Parcelable
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import java.util.ArrayList

data class Contact(
    var contactId: Int?,
    var name: String?,
    var numbers: MutableMap<PhoneTypes, String>?,
    var emails: MutableMap<EmailTypes, String>?,
)