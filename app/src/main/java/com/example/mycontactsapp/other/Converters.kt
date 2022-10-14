package com.example.mycontactsapp.other


import androidx.lifecycle.LiveData
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.mycontactsapp.data.models.Contact
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@TypeConverters
class Converters { // Since Room can only store limited types of data like TEXT, INTEGER, BLOB etc
    // So these type converters are used if you want to store some other type of data
    // I used Gson to make my own type converters because Gson can convert ANY type of
    // data(be it user defined class or list or map or list of user defined class etc) to
    // Json which is String(ie. TEXT) and we can convert it back to our data
    // type by again using Gson


    @TypeConverter
    fun fromMapOfPhoneTypesToJson(mapOfPhoneTypes: Map<PhoneTypes, String>): String {
       // What Room does is that when it encounters a type
        // which it cannot store then it will come to our "Converters" class and check the parameter
        // and return type of the functions
        // that are annotated with "@TypeConverter"
        // And it will use the function whose parameter matches with the data type to convert it
        // and while retrieving data it will again come to this class and check the return type of
        // function then the function whose return type matches it will use it to convert it back
        val gson = Gson()
        return gson.toJson(mapOfPhoneTypes)
    }

    @TypeConverter
    fun fromJsonToMapOfPhoneTypes(json: String): Map<PhoneTypes, String> {
        var gson = Gson()
        val type = object : TypeToken<Map<PhoneTypes, String>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromMapOfEmailTypesToJson(mapOfEmailTypes: Map<EmailTypes, String>?): String {
        val gson = Gson()
        return gson.toJson(mapOfEmailTypes)
    }

    @TypeConverter
    fun fromJsonToMapOfEmailTypes(json: String): Map<EmailTypes, String>? {
        var gson = Gson()
        val type = object : TypeToken<Map<EmailTypes, String>?>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromJsonToList(json: String): List<Contact>{
        var gson = Gson()
        val type = object : TypeToken<List<Contact>>() {}.type
        return gson.fromJson(json, type)
    }
    @TypeConverter
    fun fromListTypesToJson(listOfContact:List<Contact>): String {
        val gson = Gson()
        return gson.toJson(listOfContact)
    }

}