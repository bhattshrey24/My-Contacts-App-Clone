package com.example.mycontactsapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.mycontactsapp.data.Repository
import com.example.mycontactsapp.data.local.ContactDatabase
import com.example.mycontactsapp.data.local.daos.ContactDao
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.other.Constants
import kotlinx.coroutines.launch

class ListOfContactsViewModel(application: Application) :
    AndroidViewModel(application) { // It's a Shared View Model

    private val contactDao: ContactDao
    private val repository: Repository
    private var mutableListOfContacts = MutableLiveData<MutableList<Contact>>()

    init {
        contactDao = ContactDatabase.getDatabase(application).savedContactDao()
        repository = Repository(contactDao)
        // mutableListOfContacts = repository.getListOfAllContacts()// Do this later
    }

    val listOfContact: LiveData<MutableList<Contact>> get() = mutableListOfContacts

    fun setListOfContact(list: List<Contact>) {
        Constants.listOfAllContacts = list.toMutableList()
        mutableListOfContacts.value = list.toMutableList()
    }

    fun deleteContactFromSharedViewModel(contact: Contact) {
        mutableListOfContacts.value?.remove(contact)
    }

    fun getListOfContactsFromRoomDB(): LiveData<List<Contact>> {
        return repository.getListOfAllContacts()
    }

    fun saveListOfContactInRoomDB(listOfContact: List<Contact>) {
        viewModelScope.launch {
            repository.insertListOfContacts(listOfContact)
        }
    }

    fun deleteContactFromRoomDB(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun insertContactToSharedViewModel(contact: Contact) {
        mutableListOfContacts.value?.add(contact)
    }

    fun insertContactToRoomDb(contact: Contact) {
        viewModelScope.launch {
            repository.insertContact(contact)
        }
    }

    fun updateContactInSharedViewModel(contact: Contact) {
        // Find index of element in list so that we can update it
        var idxOfOldEle = mutableListOfContacts.value?.indexOfFirst {
            it.roomContactId == contact.roomContactId
        }
        if (idxOfOldEle != null) {
            mutableListOfContacts.value?.set(idxOfOldEle, contact)
        }
    }

    fun updateContactInRoomDB(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

}