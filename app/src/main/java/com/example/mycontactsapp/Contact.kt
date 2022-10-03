package com.example.mycontactsapp

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class Contact(
    var contactId: Int?,
    var name: String?,
    var numbers: MutableMap<String, String>?,
    var emails: MutableMap<String, String>?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(MutableMap::class.java.classLoader) as? MutableMap<String, String>,
        parcel.readValue (MutableMap::class.java.classLoader) as? MutableMap<String, String>
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(contactId)
        parcel.writeString(name)
        parcel.writeValue(numbers)
        parcel.writeValue(emails)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contact> {
        override fun createFromParcel(parcel: Parcel): Contact {
            return Contact(parcel)
        }

        override fun newArray(size: Int): Array<Contact?> {
            return arrayOfNulls(size)
        }
    }
}