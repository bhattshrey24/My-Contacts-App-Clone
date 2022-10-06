package com.example.mycontactsapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mycontactsapp.Contact

class ListOfContactsViewModel() : ViewModel() { // It's a Shared View Model

    private var mutableListOfContacts = MutableLiveData<List<Contact>>()

    val listOfContact: LiveData<List<Contact>> get() = mutableListOfContacts

    fun setListOfContact(list: List<Contact>) {
        mutableListOfContacts.value = list
    }

}