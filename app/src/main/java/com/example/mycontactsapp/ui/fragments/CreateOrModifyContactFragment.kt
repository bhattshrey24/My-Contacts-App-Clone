package com.example.mycontactsapp.ui.fragments

import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.os.Bundle
import android.os.RemoteException
import android.provider.ContactsContract
import android.service.controls.Control
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.setMargins
import androidx.core.widget.addTextChangedListener
import com.example.mycontactsapp.Constants
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.databinding.FragmentCreateOrModifyContactBinding


class CreateOrModifyContactFragment : Fragment() {
    enum class Types {
        Home,
        Work,
        Mobile,
    }

    private val binding: FragmentCreateOrModifyContactBinding by lazy {
        FragmentCreateOrModifyContactBinding.inflate(layoutInflater, null, false)
    }

    var hmOfNumbers = mutableMapOf<Types, EditText>()
    var hmOfEmails = mutableMapOf<Types, EditText>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // we reached here after clicking on add new contact button or edit button

        val bundle = this.arguments
        val isEdit = bundle?.getBoolean(Constants.booleanIsEditKey)

        var contactDetails: Contact? = setUpUi(isEdit, bundle) // Will setup the Ui based

        binding.eocSubmitButton.setOnClickListener {
            if (isEdit == true) {
                updateValues(
                    contactDetails,
                    Contact( // Just acting as a data holder
                        name = binding.nameOfPersonET.text.toString().trim(),
                        numbers = null, // Just Dummy Useless Data
                        contactId = contactDetails?.contactId ?: 0, // passing the same Id
                        emails = null
                    )
                )
            } else {
                createNewContact(
                    binding.nameOfPersonET.text.toString().trim(),
                )
            }
        }

        return binding.root
    }
    private fun makeEditText(hint: String, fetchedData: String): EditText {
        var editText = EditText(requireContext())
        editText.apply {
            setHint(hint)
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,// width
                200// height
            ).also {
                it.setMargins(30)
            }
            setPadding(20, 20, 20, 0)
            setBackgroundResource(R.drawable.edit_text_border)
            setText(fetchedData)
        }
        return editText
    }

    private fun setUpUi(isEdit: Boolean?, bundle: Bundle?): Contact? {
        if (isEdit == true) {
            var contactDetails = bundle?.getParcelable<Contact>(Constants.contactDetailsKey)
            binding.createOrEditTV.text = "Edit Contact"
            binding.nameOfPersonET.setText(contactDetails?.name)
            binding.eocSubmitButton.text = "Edit"

            if (contactDetails?.numbers != null) { // Todo fix , make it concise
                for (number in contactDetails?.numbers!!) {
                    var hint: String
                    var type: Types
                    if (number.key.toInt() == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        hint = "Mobile"
                        type = Types.Mobile
                    } else if (number.key.toInt() == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                        hint = "Home"
                        type = Types.Home
                    } else {
                        hint = "Work"
                        type = Types.Work
                    }
                    var et = makeEditText(hint, number.value)
                    binding.linearLayoutCoM.addView(et)
                    hmOfNumbers.put(type, et)
                }
            }
            if (contactDetails?.emails != null) { // todo put it in setUpUI
                for (email in contactDetails?.emails!!) {
                    var hint: String
                    var type: Types
                    if (email.key.toInt() == ContactsContract.CommonDataKinds.Email.TYPE_WORK) {
                        hint = "Work"
                        type = Types.Work
                    } else { // Home
                        hint = "Home"
                        type = Types.Home
                    }

                    var et = makeEditText(hint, email.value)
                    binding.linearLayoutCoM.addView(et)
                    hmOfEmails.put(type, et)
                }
            }
            return contactDetails
        } else {
            binding.createOrEditTV.text = "Create New Contact"
            binding.eocSubmitButton.text = "Add Contact"
            var et = makeEditText("Mobile", "")

            hmOfNumbers.put(Types.Mobile, et)
            binding.linearLayoutCoM.addView(et)
        }
        return null
    }
    private fun updateValues(oldContactDetails: Contact?, updatedContactDetails: Contact) {
        if (!isValidated()) {
            return
        }
        val cpbo = ArrayList<ContentProviderOperation>()

        for (num in hmOfNumbers) {
            var type: String
            if (num.key == Types.Mobile) {
                type = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE.toString()
            } else if (num.key == Types.Home) {
                type = ContactsContract.CommonDataKinds.Phone.TYPE_HOME.toString()
            } else {
                type = ContactsContract.CommonDataKinds.Phone.TYPE_WORK.toString()
            }

            cpbo.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND "
                                + ContactsContract.CommonDataKinds.Phone.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Phone.TYPE + " = ? ",
                        arrayOf(
                            "${oldContactDetails?.contactId}",
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                            type
                        )
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        num.value.text.trim().toString()
                    )
                    .build()
            )
        }

        for (email in hmOfEmails) {
            var type: String
            if (email.key == Types.Home) {
                type = ContactsContract.CommonDataKinds.Email.TYPE_HOME.toString()
            } else {
                type = ContactsContract.CommonDataKinds.Email.TYPE_WORK.toString()
            }
            cpbo.add(
                ContentProviderOperation
                    .newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? AND "
                                + ContactsContract.CommonDataKinds.Email.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Email.TYPE + " = ? ",
                        arrayOf(
                            "${oldContactDetails?.contactId}",
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                            type
                        )
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        email.value.text.trim().toString()
                    )
                    .build()
            )
        }


        cpbo.add(
            ContentProviderOperation.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
                    arrayOf("${oldContactDetails?.contactId}")
                )
                .withValue(
                    ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                    updatedContactDetails.name
                )
                .build()
        )

        try {
            val res = activity?.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
            if (res != null) {
                Toast.makeText(context, "Successfully Edited!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: OperationApplicationException) {
            Log.i(
                Constants.debugTag,
                "OperationApplicationException caught with message : ${e.message}"
            )
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
        }
        //requireActivity().finish()

    }
    private fun isValidated(): Boolean { // todo fix not working
        var nameEt = binding.nameOfPersonET
        //   var numberEt = binding.numberOfPersonET
        if (nameEt.text.isBlank()) {
            nameEt.error = "Name cannot be empty"
            return false
        }
        if (hmOfNumbers.isNullOrEmpty()) {
            for (numbersEt in hmOfNumbers) {
                if (numbersEt.value.text.toString().isNullOrEmpty()) {
                    numbersEt.value.error = "Number cannot be empty"
                    return false
                }
            }
        }
        if (hmOfEmails.isNullOrEmpty()) {
            for (emailsEt in hmOfEmails) {
                if (emailsEt.value.text.toString().isNullOrEmpty()) {
                    emailsEt.value.error = "Email address cannot be empty"
                    return false
                }
            }
        }
        return true
    }

    private fun createNewContact(nameOfContact: String) {
        if (!isValidated()) {
            return
        }
        val number = hmOfNumbers.get(Types.Mobile)?.text.toString().trim()

        Log.i(Constants.debugTag, " Number passed : $number")

        val cpbo = ArrayList<ContentProviderOperation>()

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
                    nameOfContact
                )
                .build()
        )

        // Adding Number
        cpbo.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI
            ).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    number// todo fix
                ).withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE // Hard coding number type
                // later change it to user preference by showing a drop down menu
                )
                .build()
        )

        try {
            val res = activity?.contentResolver?.applyBatch(ContactsContract.AUTHORITY, cpbo)
            if (res != null) {
                Toast.makeText(context, "Successfully Added New Contact!", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: OperationApplicationException) {
            Log.i(
                Constants.debugTag,
                "OperationApplicationException caught with message : ${e.message}"
            )
        } catch (e: RemoteException) {
            Log.i(Constants.debugTag, "Remote Exception caught with message : ${e.message}")
        }

      //  requireActivity().finish()
    }
}

