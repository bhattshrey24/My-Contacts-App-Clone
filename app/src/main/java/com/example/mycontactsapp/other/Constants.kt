package com.example.mycontactsapp.other

import com.example.mycontactsapp.data.models.Contact

object Constants {
    var debugTag = "Debugging!!"
    var listOfAllContacts = mutableListOf<Contact>()
    var listOfDeletedContactCidSPKey = "deleted_contacts_sp" // saving this so that when
    // user presses sync button then I'll delete contacts using this
}