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
    val listOfContact: LiveData<MutableList<Contact>> get() = mutableListOfContacts

    init {
        contactDao = ContactDatabase.getDatabase(application).savedContactDao()
        repository = Repository(contactDao)
    }


    fun setListOfContact(list: List<Contact>) {
        mutableListOfContacts.value = list.toMutableList()
    }

    fun deleteTableData() { // Delete all the data in table but not the table itself
        viewModelScope.launch {
            repository.deleteTableData()
        }
    }

    fun deleteContact(contact: Contact) {
        mutableListOfContacts.value?.remove(contact)// deleting from shared viewModel
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun getListOfContactsFromRoomDB(): LiveData<List<Contact>> {
        return repository.getListOfAllContacts()
    }

    fun saveListOfContactInRoomDB(listOfContact: List<Contact>) {
        viewModelScope.launch {
            repository.insertListOfContacts(listOfContact)
        }
    }

    fun insertContact(contact: Contact) {
        mutableListOfContacts.value?.add(contact) // insert contact to shared viewModel
        viewModelScope.launch {// inserting contact to room
            repository.insertContact(contact)
        }
    }

    private fun updateContactInSharedViewModel(contact: Contact) {
        // Find index of element in list so that we can update it
        val idxOfOldEle = mutableListOfContacts.value?.indexOfFirst {
            it.roomContactId == contact.roomContactId
        }
        if (idxOfOldEle != null) {
            mutableListOfContacts.value?.set(idxOfOldEle, contact)
        }
    }

    fun updateContact(contact: Contact) {
        updateContactInSharedViewModel(contact)
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

}