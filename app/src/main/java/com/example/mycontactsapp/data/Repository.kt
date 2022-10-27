package com.example.mycontactsapp.data

import androidx.lifecycle.LiveData
import com.example.mycontactsapp.data.local.daos.ContactDao
import com.example.mycontactsapp.data.models.Contact


class Repository(
    private val contactDao: ContactDao
) {

    suspend fun insertContact(contact: Contact?) {
        contact?.let {
            contactDao.insertContact(it)
        }
    }

    suspend fun insertListOfContacts(listOfContacts: List<Contact>?) {
        listOfContacts?.let {
            contactDao.insertListOfAllContacts(it)
        }
    }

    suspend fun deleteContact(contact: Contact?) {
        contact?.let {
            contactDao.deleteContact(it)
        }
    }

    suspend fun updateContact(updatedContact: Contact?) {
        updatedContact?.let {
            contactDao.updateContact(it)
        }
    }

    fun getListOfAllContacts(): LiveData<List<Contact>> {
        return contactDao.getAllContacts()
    }

    suspend fun deleteTableData() {
        return contactDao.deleteTableData()
    }

}