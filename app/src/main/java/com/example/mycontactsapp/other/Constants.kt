package com.example.mycontactsapp.other

import com.example.mycontactsapp.data.models.Contact

object Constants {
    var debugTag = "Debugging!!"

    var listOfAllContacts = mutableListOf<Contact>() // For search bar feature

    var setOfDeletedContactCidSPKey = "set_of_deleted_contacts_sp" // saving this so that when
    // user presses sync then we could delete these contacts from android db too
    var deletedCidSharedPrefKey = "shared_pref_key_for_deleted_contact"

    var myRequestCode = 101 // this is for permissions
}