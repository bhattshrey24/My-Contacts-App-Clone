package com.example.mycontactsapp.data.models


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes


@Entity(tableName = "contacts_table")
data class Contact(
    var contactId: Int?,
    var name: String?,
    var numbers: MutableMap<PhoneTypes, String>?,
    var emails: MutableMap<EmailTypes, String>?,
) {
    @PrimaryKey(autoGenerate = true)
    var roomContactId: Int = 0 // We have it inside because
    // now it's not necessary to pass this when creating object of this class since it is
    // not a primary constructor argument. Also If it's nullable then it should be null
    // if it's not then it should be 0 otherwise room won't increment it. Like if it is nullable
    // type but instead of null you initialized with 0 then room wil get confused and won't increment
    // it
}