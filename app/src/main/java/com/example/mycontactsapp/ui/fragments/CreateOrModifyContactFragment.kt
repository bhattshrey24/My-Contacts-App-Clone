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

        val bundle = this.arguments
        val isEdit = bundle?.getBoolean(Constants.booleanIsEditKey)

//      var contactDetails: Contact? = setUpUi(isEdit, bundle) // Will setup the Ui based
        // on whether we reached here after clicking on add new contact button or edit button

//        binding.eocSubmitButton.setOnClickListener {
//            if (isEdit == true) {
//                updateValues(
//                    contactDetails,
//                    Contact(
//                        binding.nameOfPersonET.text.toString(),
//                        binding.numberOfPersonET.text.toString(),
//                        contactDetails?.contactId ?: 0 // passing the same Id
//                    )
//                )
//            } else {
//                createNewContact(
//                    Contact(
//                        binding.nameOfPersonET.text.toString(),
//                        binding.numberOfPersonET.text.toString(),
//                        0 // Just passing dummy ID
//                    )
//                )
//            }
//        }

        return binding.root
    }

//    private fun setUpUi(isEdit: Boolean?, bundle: Bundle?): Contact? {
//        if (isEdit == true) {
//            var contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)
//            binding.createOrEditTV.text = "Edit Contact"
//            binding.nameOfPersonET.setText(contactDetails?.name)
//            binding.numberOfPersonET.setText(contactDetails?.number)
//            binding.eocSubmitButton.text = "Edit"
//            return contactDetails
//        } else {
//            binding.createOrEditTV.text = "Create New Contact"
//            binding.eocSubmitButton.text = "Add Contact"
//        }
//        return null
//    }

//    private fun updateValues(oldContactDetails: Contact?, updatedContactDetails: Contact) {
//        if (!isValidated()) {
//            return
//        }
//        val cpbo = ArrayList<ContentProviderOperation>()
//        cpbo.add(
//            ContentProviderOperation
//                .newUpdate(ContactsContract.Data.CONTENT_URI)
//                .withSelection(
//                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND "
//                            + ContactsContract.CommonDataKinds.Phone.MIMETYPE + " = ? AND "
//                            + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? ",
//                    arrayOf(
//                        "${oldContactDetails?.contactId}",
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
//                        oldContactDetails?.number
//                    )
//                )
//                .withValue(
//                    ContactsContract.CommonDataKinds.Phone.NUMBER,
//                    updatedContactDetails.number
//                )
//                .build()
//        )
//
//        cpbo.add(
//            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
//                .withSelection(
//                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
//                    arrayOf("${oldContactDetails?.contactId}")
//                )
//                .withValue(
//                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
//                    updatedContactDetails.name
//                )
//                .build()
//        )
//
//        try {
//            val res = activity?.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
//            if (res != null) {
//                Toast.makeText(context, "Successfully Edited!", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: OperationApplicationException) {
//            Log.i(
//                Constants.debugTag,
//                "OperationApplicationException caught with message : ${e.message}"
//            )
//        } catch (e: RemoteException) {
//            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
//        }
//        requireActivity().finish()
//
//    }

    private fun isValidated(): Boolean {
        var nameEt = binding.nameOfPersonET
        var numberEt = binding.numberOfPersonET
        if (nameEt.text.isBlank()) {
            nameEt.error = "Name cannot be empty"
            return false
        }
        if (numberEt.text.isBlank()) {
            numberEt.error = "Number cannot be empty"
            return false
        }
        return true
    }

//    private fun createNewContact(newContact: Contact) {
//        if (!isValidated()) {
//            return
//        }
//
//        val cpbo = ArrayList<ContentProviderOperation>()
//
//        // This is mandatory to do even if you don't specify an account with it
//        cpbo.add(
//            ContentProviderOperation.newInsert(
//                ContactsContract.RawContacts.CONTENT_URI
//            )
//                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//                .build()
//        )
//
//        // Adding Name
//        cpbo.add(
//            ContentProviderOperation.newInsert(
//                ContactsContract.Data.CONTENT_URI
//            )
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                .withValue(
//                    ContactsContract.Data.MIMETYPE,
//                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
//                )
//                .withValue(
//                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
//                    newContact.name
//                )
//                .build()
//        )
//
//        // Adding Number
//        cpbo.add(
//            ContentProviderOperation.newInsert(
//                ContactsContract.Data.CONTENT_URI
//            )
//                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//                .withValue(
//                    ContactsContract.Data.MIMETYPE,
//                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
//                )
//                .withValue(
//                    ContactsContract.CommonDataKinds.Phone.NUMBER,
//                    newContact.number
//                ).withValue(
//                    ContactsContract.CommonDataKinds.Phone.TYPE,
//                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
//                )
//                .build()
//        )
//
//        try {
//            val res = activity?.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
//            if (res != null) {
//                Toast.makeText(context, "Successfully Added New Contact!", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        } catch (e: OperationApplicationException) {
//            Log.i(
//                Constants.debugTag,
//                "OperationApplicationException caught with message : ${e.message}"
//            )
//        } catch (e: RemoteException) {
//            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
//        }
//
//        requireActivity().finish()
//    }

}