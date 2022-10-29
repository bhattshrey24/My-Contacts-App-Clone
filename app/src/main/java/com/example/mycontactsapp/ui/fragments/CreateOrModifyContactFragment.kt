package com.example.mycontactsapp.ui.fragments


import android.os.Bundle
import android.provider.ContactsContract

import android.text.InputType
import android.util.Log

import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

import androidx.core.view.setMargins
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.R
import com.example.mycontactsapp.databinding.FragmentCreateOrModifyContactBinding
import com.example.mycontactsapp.other.*
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


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
    private val sharedViewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // we reached here after clicking on add new contact button or edit button

        val isEdit = args.isEdit

        val contactDetails: Contact? = setUpUi(isEdit) // Will setup the Ui
        // based on whether we came here from contact detail screen or
        // from home screen

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

    private fun setUpUi(isEdit: Boolean?): Contact? {
        if (isEdit == true) {
            val contactDetails = sharedViewModel.listOfContact.value?.find {
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
        contactDetails?.numbers?.let { numbers ->
            for (number in numbers) {
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
               // val et = makeEditText(hint, number.value, Type.Phone)
               // binding.linearLayoutNumber.addView(et)

               // hmOfNumbersEditTexts.put(type, et)
            }
        }

        contactDetails?.emails?.let { emails ->
            for (email in emails) {
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
              //  val et = makeEditText(hint, email.value, Type.Email)
//                binding.linearLayoutEmail.addView(et)
//                hmOfEmailsEditTexts.put(type, et)
            }
        }

    }

    private fun setUpUiForCreateNewContact() {
        binding.createOrEditTV.text = "Create New Contact"
        binding.eocSubmitButton.text = "Add Contact"
//        val et = makeEditText("Mobile", "", Type.Phone)
//        hmOfNumbersEditTexts.put(PhoneTypes.Mobile, et)
//        binding.linearLayoutNumber.addView(et)
    }

    private fun updateValues(oldContactDetails: Contact?, updatedContactName: String) {
        if (!isValidated()) {
            return
        }

        val updatedHashMapForNum = mutableMapOf<PhoneTypes, String>()
        val updatedHashMapForEmail = mutableMapOf<EmailTypes, String>()

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
        oldContactDetails?.let {
            it.emails = updatedHashMapForEmail
            it.numbers = updatedHashMapForNum
            it.name = updatedContactName
            sharedViewModel.updateContact(it)
            sharedViewModel.listOfContact.value?.sortWith(ListSortComparator()) // sorting because it's
            // possible that user might changed the name of contact
        }
    }



    private fun createNewContact(nameOfContact: String) {
        if (!isValidated()) {
            return
        }
        for (numb in hmOfNumbersEditTexts.keys) {
            val number = hmOfNumbersEditTexts.get(numb)?.editText?.text?.toString()?.trim()
            val newContact =
                Contact(
                    null, // setting null because this
                    // will tell me whether the contact was there in Android DB or
                    // not
                    nameOfContact,
                    mutableMapOf(PhoneTypes.Mobile to number!!),
                    null
                )
            sharedViewModel.insertContact(newContact)
        }
        sharedViewModel.listOfContact.value?.sortWith(ListSortComparator())
        findNavController().popBackStack()
    }

    private fun isValidated(): Boolean {
        val nameEt = binding.nameOfPersonET
        if (nameEt.text.isBlank()) {
            nameEt.error = "Name cannot be empty"
            return false
        }
        if (hmOfNumbersEditTexts.isEmpty()) {
            for (numbersEt in hmOfNumbersEditTexts) {
                if (numbersEt.value.editText?.text?.toString()?.isBlank() == true) {
                    numbersEt.value.error = "Number cannot be empty"
                    return false
                }
            }
        }
        if (hmOfEmailsEditTexts.isEmpty()) {
            for (emailsEt in hmOfEmailsEditTexts) {
                if (emailsEt.value.editText?.text?.toString()?.isBlank() == true) {
                    emailsEt.value.error = "Email address cannot be empty"
                    return false
                }
            }
        }
        return true
    }

}


//private fun setUpListenerForFab() {
//    fabForNumber?.let {
//        it.setOnClickListener {
//            val et = makeEditText("Work", "", Type.Phone)
//            binding.linearLayoutNumber.addView(et)
//            hmOfNumbersEditTexts.put(PhoneTypes.Work, et)
//        }
//    }
//    fabForEmail?.let {
//        it.setOnClickListener {
//            val et = makeEditText("Home", "", Type.Phone)
//            binding.linearLayoutEmail.addView(et)
//            hmOfEmailsEditTexts.put(EmailTypes.Home, et)
//        }
//    }
//}

//private fun makeFAB(): FloatingActionButton {
//    return FloatingActionButton(requireContext()).apply {
//        setPadding(20, 20, 20, 0)
//        layoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,// width
//            ViewGroup.LayoutParams.WRAP_CONTENT// height
//        ).also {
//            it.setMargins(20, 5, 20, 5)
//        }
//        // size=FloatingActionButton.SIZE_MINI
//        setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add))
//    }
//}


//private fun makeEditText(hint: String, fetchedData: String, type: Type): TextInputLayout {
//    val textInputLayout = TextInputLayout(requireContext()) // this makes it possible
//    // to have floating hint in edit text . It is wrapper class that can wrap Edit text
//    val editText = EditText(requireContext())
//    editText.apply {
//        setHint(hint)
//        layoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,// width
//            200// height
//        ).also {
//            it.setMargins(30)
//        }
//        setPadding(20, 20, 20, 0)
//        setBackgroundResource(R.drawable.edit_text_border)
//        setText(fetchedData)
//        inputType = when (type) {
//            Type.Phone -> {
//                InputType.TYPE_CLASS_NUMBER
//            }
//            Type.Email -> {
//                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
//            }
//        }
//    }
//    textInputLayout.addView(editText)
//    return textInputLayout
//}