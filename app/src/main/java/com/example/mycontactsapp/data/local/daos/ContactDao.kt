package com.example.mycontactsapp.data.local.daos


import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mycontactsapp.data.models.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListOfAllContacts(listOfAllContacts: List<Contact>)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contacts_table ORDER BY name ASC")
    fun getAllContacts(): LiveData<List<Contact>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateContact(updatedContact: Contact)

    @Query("SELECT COUNT(*) FROM contacts_table")
    suspend fun getRowCount(): Int

}