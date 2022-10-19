package com.example.mycontactsapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mycontactsapp.data.local.daos.ContactDao
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.other.Converters


@Database(entities = [Contact::class], version = 1)
@TypeConverters(Converters::class)
abstract class ContactDatabase :
    RoomDatabase() { // abstract class so that no one can make object of this class

    abstract fun savedContactDao(): ContactDao// providing instance of our DAO
    // so that we can access it and perform crud operations on DB

    companion object {
        // Applying thread safe singleton pattern

        @Volatile// what this does is that whenever the below variable(ie. INSTANCE variable)
        // gets assigned a value , all the threads will get notified about it and they
        // get the updated value
        private var INSTANCE: ContactDatabase? = null

        fun getDatabase(context: Context): ContactDatabase {// only this is accessible to
            // outside classes since its public
            if (INSTANCE == null) {
                synchronized(this) {// this is how we provide thread safety , here
                    // we are giving class level lock i guess
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ContactDatabase::class.java,
                        "contacts_db"
                    ).build()
                }
                // The name "contact_db" is the name of the whole DB
            }
            return INSTANCE!!
        }
    }

}
