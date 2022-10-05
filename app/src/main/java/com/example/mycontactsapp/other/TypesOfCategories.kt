package com.example.mycontactsapp.other

import android.provider.ContactsContract

enum class PhoneTypes(var nameOfType: String, var codeOfType: Int) {
    Home("Home", ContactsContract.CommonDataKinds.Phone.TYPE_HOME),
    Work("Work", ContactsContract.CommonDataKinds.Phone.TYPE_WORK),
    Mobile("Mobile", ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE),

}

enum class EmailTypes(var nameOfType: String, var codeOfType: Int) {
    Home("Home", ContactsContract.CommonDataKinds.Email.TYPE_HOME),
    Work("Work", ContactsContract.CommonDataKinds.Email.TYPE_WORK),
}