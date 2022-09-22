package com.example.mycontactsapp.ui.fragments

import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.os.Bundle
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.databinding.FragmentCreateOrModifyContactBinding


class CreateOrModifyContactFragment : Fragment() {

    private val binding: FragmentCreateOrModifyContactBinding by lazy {
        FragmentCreateOrModifyContactBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var bundle = this.arguments
        var isEdit = bundle?.getBoolean(Constants.booleanIsEditKey)
        var contactDetails: Contact? = null
        if (isEdit == true) {
            contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)
            binding.createOrEditTV.text = "Edit Contact"
            binding.nameOfPersonET.setText(contactDetails?.name) // setText cause text does "Editable"
            binding.numberOfPersonET.setText(contactDetails?.number)
        }

        binding.eocSubmitButton.setOnClickListener {
            if (isEdit == true) {
                updateValues(contactDetails)
            } else {
                createNewContact(
                    Contact(
                        binding.nameOfPersonET.text.toString(),
                        binding.numberOfPersonET.text.toString(),
                        0
                    )
                )
            }
        }

        return binding.root
    }

    private fun updateValues(oldContactDetails: Contact?) {
        var newName: String = binding.nameOfPersonET.text.toString()
        var newNumber: String = binding.numberOfPersonET.text.toString()

        var cpbo = ArrayList<ContentProviderOperation>()

        Log.i(Constants.debugTag, "ID 1 : ${ContactsContract.RawContacts.CONTENT_URI}")
        Log.i(Constants.debugTag, "ID 2 : ${ContactsContract.CommonDataKinds.Phone.CONTENT_URI}")
        Log.i(Constants.debugTag, "ID 3 : ${ContactsContract.Data.CONTENT_URI}")
        Log.i(Constants.debugTag, "ID 9 : ${ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME}")

        Log.i(Constants.debugTag, "ID 4 : ${ContactsContract.Data.DISPLAY_NAME}")
        Log.i(Constants.debugTag, "ID 5 : ${ContactsContract.Data.DISPLAY_NAME_PRIMARY}")
        Log.i(Constants.debugTag, "ID 6 : ${ContactsContract.Data.DISPLAY_NAME_SOURCE}")
        Log.i(Constants.debugTag, "ID 7 : ${ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY}")
        Log.i(Constants.debugTag, "ID 8 : ${ContactsContract.RawContacts.DISPLAY_NAME_SOURCE}")

        cpbo.add(
            ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND "
                            + ContactsContract.CommonDataKinds.Phone.MIMETYPE + " = ? AND "
                            + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? ", arrayOf(
                        "${oldContactDetails?.contactId}",
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                        oldContactDetails?.number
                    )
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                .build()
        )

        cpbo.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
                    arrayOf("${oldContactDetails?.contactId}")
                )
                .withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, newName)
                .build()
        )

        try {
            activity?.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
        } catch (e: OperationApplicationException) {
            Log.i(Constants.debugTag, "OperationApplicationException caught")
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught")
        }


        // Below updates the name
//        val whereClause =
//            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ${oldContactDetails?.contactId} "
//        val contentResolver = activity?.contentResolver
//        val contentValues = ContentValues()
//        contentValues.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY, newName)
//        contentResolver?.update(
//            ContactsContract.RawContacts.CONTENT_URI,
//            contentValues,
//            whereClause,
//            null
//        )

//        val updateValues = ContentValues().apply {
//            put(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
//        }
//
//        val selectionClause: String = ContactsContract.RawContacts.CONTACT_ID + " = ? "
//        val selectionArgs: Array<String> = arrayOf("${oldContactDetails?.contactId}")
//
//        activity?.contentResolver?.update(
//            ContactsContract.RawContacts.CONTENT_URI,   // the user dictionary content URI
//            updateValues,                      // the columns to update
//            selectionClause,                   // the column to select on
//            selectionArgs                      // the value to compare to
//        )

    }

    private fun createNewContact(newContact: Contact) {
        Log.i(Constants.debugTag, "Inside create")
        var cpbo = ArrayList<ContentProviderOperation>()

        // This is mandatory to do even if you don't specify an account with it
        cpbo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI
            )
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        // Adding Name
        cpbo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    newContact.name
                )
                .build()
        )

        // Adding Number
        cpbo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            )
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    newContact.number
                ).withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )

        try {
            activity?.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
        } catch (e: OperationApplicationException) {
            Log.i(Constants.debugTag, "OperationApplicationException caught")
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught")
        }

    }


}