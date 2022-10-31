package com.example.mycontactsapp.ui.fragments


import android.os.Bundle
import android.provider.ContactsContract

import android.text.InputType
import android.util.Log

import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
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
import java.util.*


class CreateOrModifyContactFragment : Fragment() {

    private val binding: FragmentCreateOrModifyContactBinding by lazy {
        FragmentCreateOrModifyContactBinding.inflate(layoutInflater, null, false)
    }

    private val args: CreateOrModifyContactFragmentArgs by navArgs()
    private val sharedViewModel: ListOfContactsViewModel by activityViewModels()

    private var isEdit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // we reached here after clicking on add new contact button or edit button

        isEdit = args.isEdit

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

        binding.myBtnForAddingNumber.setOnClickListener {
            addViewToPhoneLL()
        }
        binding.myBtnForAddingEmail.setOnClickListener {
            addViewToEmailLL()
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

    private fun setUpUiForCreateNewContact() {
        binding.createOrEditTV.text = "Create New Contact"
        binding.eocSubmitButton.text = "Add Contact"
        addViewToPhoneLL()
        addViewToEmailLL()
    }

    private fun setUpUiForEditScreen(contactDetails: Contact?) {
        binding.createOrEditTV.text = "Edit Contact"
        binding.nameOfPersonET.setText(contactDetails?.name)
        binding.eocSubmitButton.text = "Edit"

        contactDetails?.numbers?.let { numbers ->
            for (number in numbers) {
                var category = number.key.nameOfType
                setViewWithDataToPhoneLL(category, number.value)
            }
        }
        contactDetails?.emails?.let { emails ->
            for (email in emails) {
                var category = email.key.nameOfType
                setViewWithDataToEmailLL(category, email.value)
            }
        }
    }

    private fun updateValues(oldContactDetails: Contact?, updatedContactName: String) {
        val updatedHashMapForNum = mutableMapOf<PhoneTypes, String>()
        val updatedHashMapForEmail = mutableMapOf<EmailTypes, String>()

        val countForNum = binding.parentLLayoutForNumber.childCount
        var viewForNum: View?
        for (i in 0 until countForNum) {
            viewForNum = binding.parentLLayoutForNumber.getChildAt(i)
            val phoneNumber: EditText = viewForNum.findViewById(R.id.et_phone)
            val phoneType: Spinner = viewForNum.findViewById(R.id.phoneTypeSpinner)
            val numberEntered = phoneNumber.text.toString()
            if (numberEntered.isEmpty()) {
                phoneNumber.setError("Enter a number")
                return
            }
            val category = phoneType.selectedItem as String
            updatedHashMapForNum.put(getEnumPhoneType(category), numberEntered)
        }

        val countForEmail = binding.parentLLayoutForEmail.childCount
        var viewForEmail: View?
        for (i in 0 until countForEmail) {
            viewForEmail = binding.parentLLayoutForEmail.getChildAt(i)
            val emailAddress: EditText = viewForEmail.findViewById(R.id.et_email)
            val emailType: Spinner = viewForEmail.findViewById(R.id.emailTypeSpinner)
            val emailEntered = emailAddress.text.toString()
            if (emailEntered.isEmpty()) {
                emailAddress.setError("Enter an email address")
                return
            }
            if (!regExToCheckEmail(emailEntered)){
                emailAddress.setError("Enter a valid email address")
                return
            }
            val category = emailType.selectedItem as String
            updatedHashMapForEmail.put(getEnumEmailType(category), emailEntered)
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

    private fun regExToCheckEmail(str: String): Boolean {
        return str.matches(Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"))
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
        val updatedHashMapForNum = mutableMapOf<PhoneTypes, String>()
        val updatedHashMapForEmail = mutableMapOf<EmailTypes, String>()
        val countForNum = binding.parentLLayoutForNumber.childCount
        var viewForNum: View?
        for (i in 0 until countForNum) {
            viewForNum = binding.parentLLayoutForNumber.getChildAt(i)
            val phoneNumber: EditText = viewForNum.findViewById(R.id.et_phone)
            val phoneType: Spinner = viewForNum.findViewById(R.id.phoneTypeSpinner)
            val numberEntered = phoneNumber.text.toString()
            if (numberEntered.isEmpty()) {
                phoneNumber.setError("Enter a number")
                return
            }
            val category = phoneType.selectedItem as String
            updatedHashMapForNum.put(getEnumPhoneType(category), numberEntered)
        }

        val countForEmail = binding.parentLLayoutForEmail.childCount
        var viewForEmail: View?
        for (i in 0 until countForEmail) {
            viewForEmail = binding.parentLLayoutForEmail.getChildAt(i)
            val emailAddress: EditText = viewForEmail.findViewById(R.id.et_email)
            val emailType: Spinner = viewForEmail.findViewById(R.id.emailTypeSpinner)
            val emailEntered = emailAddress.text.toString()
            if (emailEntered.isEmpty()) {
                emailAddress.setError("Enter an email address")
                return
            }
            if (!regExToCheckEmail(emailEntered)){
                emailAddress.setError("Enter a valid email address")
                return
            }
            val category = emailType.selectedItem as String
            updatedHashMapForEmail.put(getEnumEmailType(category), emailEntered)
        }


        val newContact =
            Contact(
                null,
                nameOfContact,
                updatedHashMapForNum,
                updatedHashMapForEmail
            )

        sharedViewModel.insertContact(newContact)
        sharedViewModel.listOfContact.value?.sortWith(ListSortComparator())

        findNavController().popBackStack()
    }

    private fun getCategoryIndexInSpinner(spinner: Spinner, category: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(category)) {
                return i
            }
        }
        return 0
    }

    private fun setViewWithDataToPhoneLL(category: String, number: String) {
        addViewToPhoneLL()
        val countForNum = binding.parentLLayoutForNumber.childCount - 1
        var view: View? = binding.parentLLayoutForNumber.getChildAt(countForNum)
        view?.let {
            val phoneNumber: EditText = view.findViewById(R.id.et_phone)
            phoneNumber.setText(number)
            val phoneType: Spinner = it.findViewById(R.id.phoneTypeSpinner)
            val selectionIdx = getCategoryIndexInSpinner(phoneType, category)
            phoneType.setSelection(selectionIdx)

            val delBtn: FloatingActionButton = view.findViewById(R.id.deleteBtnPhone)
            delBtn.setOnClickListener {
                if (binding.parentLLayoutForNumber.childCount <= 1) {
                    Toast.makeText(context , "Contact should atleast have 1 number",Toast.LENGTH_SHORT).show()
                }else{
                    binding.parentLLayoutForNumber.removeView(view)
                }
            }
        }
    }

    private fun setViewWithDataToEmailLL(category: String, emailAddress: String) {
        addViewToEmailLL()
        val countForEmail = binding.parentLLayoutForEmail.childCount - 1
        var view: View? = binding.parentLLayoutForEmail.getChildAt(countForEmail)
        view?.let {
            val email: EditText = view.findViewById(R.id.et_email)
            email.setText(emailAddress)
            val emailType: Spinner = it.findViewById(R.id.emailTypeSpinner)
            val selectionIdx = getCategoryIndexInSpinner(emailType, category)
            emailType.setSelection(selectionIdx)

            val delBtn: FloatingActionButton = view.findViewById(R.id.deleteBtnEmail)
            delBtn.setOnClickListener {
                binding.parentLLayoutForEmail.removeView(view)
            }
        }
    }

    private fun addViewToPhoneLL() {
        val inflater = LayoutInflater.from(context).inflate(R.layout.child_layout_phone, null)
        binding.parentLLayoutForNumber.addView(inflater, binding.parentLLayoutForNumber.childCount)

        val countForNum = binding.parentLLayoutForNumber.childCount - 1
        var view: View? = binding.parentLLayoutForNumber.getChildAt(countForNum)
        view?.let {
            val delBtn: FloatingActionButton = view.findViewById(R.id.deleteBtnPhone)
            delBtn.setOnClickListener {
                if (binding.parentLLayoutForNumber.childCount <= 1) {
                    Toast.makeText(context , "Contact should atleast have 1 number",Toast.LENGTH_SHORT).show()
                }else{
                    binding.parentLLayoutForNumber.removeView(view)
                }
            }
        }
    }

    private fun addViewToEmailLL() {
        val inflater = LayoutInflater.from(context).inflate(R.layout.child_layout_email, null)
        binding.parentLLayoutForEmail.addView(inflater, binding.parentLLayoutForEmail.childCount)

        val countForEmail = binding.parentLLayoutForEmail.childCount - 1
        val view: View? = binding.parentLLayoutForEmail.getChildAt(countForEmail)
        view?.let {
            val delBtn: FloatingActionButton = view.findViewById(R.id.deleteBtnEmail)
            delBtn.setOnClickListener {
                binding.parentLLayoutForEmail.removeView(view)
            }
        }
    }

    private fun getEnumPhoneType(category: String): PhoneTypes {
        return when (category) {
            "Mobile" -> {
                PhoneTypes.Mobile
            }
            "Work" -> {
                PhoneTypes.Work
            }
            "Home" -> {
                PhoneTypes.Home
            }
            else -> {
                PhoneTypes.Mobile
            }
        }
    }

    private fun getEnumEmailType(category: String): EmailTypes {
        return when (category) {
            "Work" -> {
                EmailTypes.Work
            }
            "Home" -> {
                EmailTypes.Home
            }
            else -> {
                EmailTypes.Home
            }
        }
    }
}
