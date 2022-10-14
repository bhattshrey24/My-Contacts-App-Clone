package com.example.mycontactsapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.mycontactsapp.data.Repository
import com.example.mycontactsapp.data.local.ContactDatabase
import com.example.mycontactsapp.data.local.daos.ContactDao
import com.example.mycontactsapp.data.models.Contact
import kotlinx.coroutines.launch

class ListOfContactsViewModel(application: Application) :
    AndroidViewModel(application) { // It's a Shared View Model

    private val contactDao: ContactDao
    private val repository: Repository
    private var mutableListOfContacts = MutableLiveData<List<Contact>>()

    init {
        contactDao = ContactDatabase.getDatabase(application).savedContactDao()
        repository = Repository(contactDao)
        // mutableListOfContacts = repository.getListOfAllContacts()// Do this later
    }

    val listOfContact: LiveData<List<Contact>> get() = mutableListOfContacts

    fun setListOfContact(list: List<Contact>) {
        mutableListOfContacts.value = list
    }

    fun deleteContactFromSharedViewModel(id: Int) {
        mutableListOfContacts.value?.forEach {

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

}