package com.example.mycontactsapp.other

import com.example.mycontactsapp.data.models.Contact

object Constants {
    var debugTag = "Debugging!!"
    var listOfAllContacts = mutableListOf<Contact>()
    var setOfDeletedContactCidSPKey = "set_of_deleted_contacts_sp" // saving this so that when

    // user presses sync button then I'll delete contacts using this
    var deletedCidSharedPrefKey = "shared_pref_key_for_deleted_contact"
}