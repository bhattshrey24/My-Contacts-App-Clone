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
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setMargins
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.databinding.FragmentCreateOrModifyContactBinding
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel


class CreateOrModifyContactFragment : Fragment() {

    private val binding: FragmentCreateOrModifyContactBinding by lazy {
        FragmentCreateOrModifyContactBinding.inflate(layoutInflater, null, false)
    }

    private var hmOfNumbersEditTexts = mutableMapOf<PhoneTypes, EditText>() // This will hold the
    // reference to edit texts since I'm programmatically making them based on number of emails or phone numbers
    // user has for a particular contact

    private var hmOfEmailsEditTexts = mutableMapOf<EmailTypes, EditText>()

    private val args: CreateOrModifyContactFragmentArgs by navArgs()
    private val listOfContactsViewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // we reached here after clicking on add new contact button or edit button

        val isEdit = args.isEdit

        var contactDetails: Contact? = setUpUi(isEdit) // Will setup the Ui based

        setUpListeners(isEdit, contactDetails)


        return binding.root
    }

    private fun setUpListeners(isEdit: Boolean?, contactDetails: Contact?) {
        binding.eocSubmitButton.setOnClickListener {
            if (isEdit == true) {
                updateValues(
                    contactDetails,
                    binding.nameOfPersonET.text.toString().trim()
                )
            } else {
                createNewContact(
                    binding.nameOfPersonET.text.toString().trim(),
                )
            }
        }
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

    private fun setUpUi(isEdit: Boolean?): Contact? {
        if (isEdit == true) {
            var contactDetails = listOfContactsViewModel.listOfContact.value?.find {
                it.contactId == args.contactID
            }
            setUpUiForEditScreen(contactDetails)
            return contactDetails
        } else {
            setUpUiForCreateNewContact()
        }
        return null
    }

    private fun setUpUiForEditScreen(contactDetails: Contact?) {
        binding.createOrEditTV.text = "Edit Contact"
        binding.nameOfPersonET.setText(contactDetails?.name)
        binding.eocSubmitButton.text = "Edit"
        if (contactDetails?.numbers != null) {
            for (number in contactDetails?.numbers!!) {
                var hint: String
                var type: PhoneTypes
                when (number.key.codeOfType) {
                    PhoneTypes.Mobile.codeOfType -> {
                        hint = PhoneTypes.Mobile.nameOfType
                        type = PhoneTypes.Mobile
                    }
                    PhoneTypes.Home.codeOfType -> {
                        hint = PhoneTypes.Home.nameOfType
                        type = PhoneTypes.Home
                    }
                    PhoneTypes.Work.codeOfType -> {
                        hint = PhoneTypes.Work.nameOfType
                        type = PhoneTypes.Work
                    }
                    else -> {
                        hint = PhoneTypes.Mobile.nameOfType
                        type = PhoneTypes.Mobile
                    }
                }
                var et = makeEditText(hint, number.value)
                binding.linearLayoutCoM.addView(et)

                hmOfNumbersEditTexts.put(type, et)
            }
        }
        if (contactDetails?.emails != null) {
            for (email in contactDetails?.emails!!) {
                var hint: String
                var type: EmailTypes
                when (email.key.codeOfType) {
                    EmailTypes.Work.codeOfType -> {
                        hint = EmailTypes.Work.nameOfType
                        type = EmailTypes.Work
                    }
                    EmailTypes.Home.codeOfType -> {
                        hint = EmailTypes.Home.nameOfType
                        type = EmailTypes.Home
                    }
                    else -> {
                        hint = EmailTypes.Home.nameOfType
                        type = EmailTypes.Home
                    }
                }
                var et = makeEditText(hint, email.value)
                binding.linearLayoutCoM.addView(et)
                hmOfEmailsEditTexts.put(type, et)
            }
        }
    }

    private fun setUpUiForCreateNewContact() {
        binding.createOrEditTV.text = "Create New Contact"
        binding.eocSubmitButton.text = "Add Contact"
        var et = makeEditText("Mobile", "")
        hmOfNumbersEditTexts.put(PhoneTypes.Mobile, et)
        binding.linearLayoutCoM.addView(et)
    }

    private fun updateValues(oldContactDetails: Contact?, updatedContactName: String) {
        if (!isValidated()) {
            return
        }

        var updatedHashMapForNum = mutableMapOf<PhoneTypes, String>()
        var updatedHashMapForEmail = mutableMapOf<EmailTypes, String>()

        val cpbo = ArrayList<ContentProviderOperation>()

        for (num in hmOfNumbersEditTexts) {
            var type: String
            if (num.key == PhoneTypes.Mobile) {
                type = PhoneTypes.Mobile.codeOfType.toString()
            } else if (num.key == PhoneTypes.Home) {
                type = PhoneTypes.Home.codeOfType.toString()
            } else {
                type = PhoneTypes.Work.codeOfType.toString()
            }
            updatedHashMapForNum.put(num.key, num.value.text.trim().toString())
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
        for (email in hmOfEmailsEditTexts) {
            var type: String
            if (email.key == EmailTypes.Home) {
                type = EmailTypes.Home.codeOfType.toString()
            } else {
                type = EmailTypes.Work.codeOfType.toString()
            }
            updatedHashMapForEmail.put(email.key, email.value.text.trim().toString())
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
                    updatedContactName
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

        // updating the list in shared viewModel
        updateContactList(
            oldContactDetails,
            updatedContactName,
            updatedHashMapForNum,
            updatedHashMapForEmail
        )

        findNavController().popBackStack()
    }

    private fun updateContactList(
        oldContactDetails: Contact?,
        updatedContactName: String,
        updatedHashMapForNum: MutableMap<PhoneTypes, String>,
        updatedHashMapForEmail: MutableMap<EmailTypes, String>
    ) {

        var list = listOfContactsViewModel.listOfContact.value?.toMutableList() ?: mutableListOf()
        var updatedContact = Contact(
            name = updatedContactName,
            contactId = oldContactDetails?.contactId,
            numbers = updatedHashMapForNum,
            emails = updatedHashMapForEmail
        )
        var idxOfOldEle = list.indexOfFirst {
            it.contactId == oldContactDetails?.contactId
        }
        list.set(idxOfOldEle, updatedContact)
        listOfContactsViewModel.setListOfContact(list)

    }

    private fun isValidated(): Boolean {
        var nameEt = binding.nameOfPersonET
        if (nameEt.text.isBlank()) {
            nameEt.error = "Name cannot be empty"
            return false
        }
        if (hmOfNumbersEditTexts.isNullOrEmpty()) {
            for (numbersEt in hmOfNumbersEditTexts) {
                if (numbersEt.value.text.toString().isBlank()) {
                    numbersEt.value.error = "Number cannot be empty"
                    return false
                }
            }
        }
        if (hmOfEmailsEditTexts.isNullOrEmpty()) {
            for (emailsEt in hmOfEmailsEditTexts) {
                if (emailsEt.value.text.toString().isBlank()) {
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
        val number = hmOfNumbersEditTexts.get(PhoneTypes.Mobile)?.text.toString().trim()

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
        findNavController().popBackStack()
    }

}

