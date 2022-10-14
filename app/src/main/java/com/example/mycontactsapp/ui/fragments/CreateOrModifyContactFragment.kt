package com.example.mycontactsapp.ui.fragments

import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.graphics.Outline
import android.graphics.Paint
import android.os.Bundle
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text


class CreateOrModifyContactFragment : Fragment() {

    private val binding: FragmentCreateOrModifyContactBinding by lazy {
        FragmentCreateOrModifyContactBinding.inflate(layoutInflater, null, false)
    }

    private var hmOfNumbersEditTexts =
        mutableMapOf<PhoneTypes, TextInputLayout>() // This will hold the
    // reference to edit texts since I'm programmatically making them based on number of emails or phone numbers
    // user has for a particular contact

    private var hmOfEmailsEditTexts = mutableMapOf<EmailTypes, TextInputLayout>()

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

    private fun makeEditText(hint: String, fetchedData: String): TextInputLayout {
        var textInputLayout = TextInputLayout(requireContext()) // this makes it possible
        // to have floating hint in edit text . It is wrapper class that can wrap Edit text
        var editText = EditText(requireContext())
        editText.apply {
            setHint(hint)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,// width
                200// height
            ).also {
                it.setMargins(30)
            }
            setPadding(20, 20, 20, 0)
            setBackgroundResource(R.drawable.edit_text_border)
            setText(fetchedData)
        }
        textInputLayout.addView(editText)
        return textInputLayout
    }

    private fun setUpUi(isEdit: Boolean?): Contact? {
        if (isEdit == true) {
            var contactDetails = listOfContactsViewModel.listOfContact.value?.find {
                it.roomContactId == args.roomId
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

        for (num in hmOfNumbersEditTexts) {
            updatedHashMapForNum.put(num.key, num.value.editText?.text?.trim().toString())
        }
        for (email in hmOfEmailsEditTexts) {
            updatedHashMapForEmail.put(email.key, email.value.editText?.text?.trim().toString())
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
        var updatedContact = Contact(
            name = updatedContactName,
            contactId = oldContactDetails?.contactId,
            numbers = updatedHashMapForNum,
            emails = updatedHashMapForEmail,
        )
        oldContactDetails?.let {
            it.emails = updatedHashMapForEmail
            it.numbers = updatedHashMapForNum
            it.name = updatedContactName
            listOfContactsViewModel.updateContactInSharedViewModel(it)
            listOfContactsViewModel.updateContactInRoomDB(it)
        }
        // todo mark for isUpdated = true
    }

    private fun isValidated(): Boolean {
        var nameEt = binding.nameOfPersonET
        if (nameEt.text.isBlank()) {
            nameEt.error = "Name cannot be empty"
            return false
        }
        if (hmOfNumbersEditTexts.isNullOrEmpty()) {
            for (numbersEt in hmOfNumbersEditTexts) {
                if (numbersEt.value.editText?.text?.toString()?.isBlank() == true) {
                    numbersEt.value.error = "Number cannot be empty"
                    return false
                }
            }
        }
        if (hmOfEmailsEditTexts.isNullOrEmpty()) {
            for (emailsEt in hmOfEmailsEditTexts) {
                if (emailsEt.value.editText?.text?.toString()?.isBlank() == true) {
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
        val number = hmOfNumbersEditTexts.get(PhoneTypes.Mobile)?.editText?.text?.toString()?.trim()
        val newContact =
            Contact(
                null, // setting null because this
                // will tell me whether the contact was there in Android DB or
                // not when user presses sync button , because if it's null it means
                // it was added later
                nameOfContact,
                mutableMapOf(PhoneTypes.Mobile to number!!),
                null
            )

        listOfContactsViewModel.insertContactToSharedViewModel(newContact)
        listOfContactsViewModel.insertContactToRoomDb(newContact)

        findNavController().popBackStack()
    }

}

