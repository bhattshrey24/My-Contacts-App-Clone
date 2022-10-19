package com.example.mycontactsapp.ui.fragments

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mycontactsapp.MainActivity
import com.example.mycontactsapp.R
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.databinding.FragmentSplashScreenBinding
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.ui.viewmodels.ContentProviderViewModel
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel


class SplashScreenFragment : Fragment() {

    private val binding: FragmentSplashScreenBinding by lazy {
        FragmentSplashScreenBinding.inflate(layoutInflater, null, false)
    }

    private val viewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasReadPermission() || !hasWritePermission()) {
                showDialogueBox()
            } else {
                setUpUi()
            }
        } else {// Before Version M i.e. api 23 (Android 6) ,
            // It was not necessary to ask for permission at
            // runtime , they were granted if user mentioned it
            // in manifest
            setUpUi()
        }
        return binding.root
    }

    private fun showDialogueBox() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Permission disclaimer")
            .setMessage("The app requires permission to read and write contacts!")
            .setPositiveButton(
                "Ask permission"
            ) { dialogueBox, p1 ->
                dialogueBox?.dismiss()
                requestPermission()
            }.setNegativeButton(
                "Exit"
            ) { _, _ -> requireActivity().finish() }
            .create()
            .show()
    }

    private fun setUpUi() {
        viewModel.getListOfContactsFromRoomDB().observe(viewLifecycleOwner) {
            if (it.isEmpty()) { //It means user has opened the app for the 1st time
                // so load from Content provider and store it in room
                loadDataFromContentProvider()
            } else { // Means user has opened the app before which
                // means room is not empty so simply give the list retrieved
                // from room to shared view model
                loadToSharedViewModel(it)
            }
        }
    }

    private fun hasReadPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permissionsToRequest = mutableListOf<String>()

        if (!hasReadPermission()) {
            permissionsToRequest.add(
                android.Manifest.permission.READ_CONTACTS
            )
        }

        if (!hasWritePermission()) {
            permissionsToRequest.add(
                android.Manifest.permission.WRITE_CONTACTS
            )
        }


        if (permissionsToRequest.isNotEmpty()) { // ie. if there are some permissions that
            // user has not accepted so we will now ask user to accept them
            ActivityCompat.requestPermissions(
                requireActivity(), permissionsToRequest.toTypedArray(),
                Constants.myRequestCode
            )
        }

    }

    override fun onResume() {// Doing the isGrantedCheck
        // in onResume because when user closes the android
        // box asking for permission then onResume of
        // fragment is called
        super.onResume()

        val isPermissionGranted = (activity as MainActivity).isPermissionGranted()
        if (isPermissionGranted) {
            setUpUi()
        }

    }

    private fun loadDataFromContentProvider() {
        val contentProviderViewModel: ContentProviderViewModel by viewModels()

        contentProviderViewModel.fetchDataFromAndroidDb() // This will fetch data from
        // content provider

        contentProviderViewModel.listOfRetrievedContacts
            .observe(viewLifecycleOwner) { listFromContentProvider ->
                listFromContentProvider.let {
                    viewModel.saveListOfContactInRoomDB(it) // will be done in background thread
                    viewModel.getListOfContactsFromRoomDB()
                        .observe(viewLifecycleOwner) { listReturnedByRoom ->
                            if (!listReturnedByRoom.isNullOrEmpty()) {
                                viewModel.setListOfContact(listReturnedByRoom) // saving list in shared viewModel
                                findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment)
                                binding.circularProgressBar.visibility =
                                    View.GONE // not necessary though since we are moving to next screen
                            }
                        }
                }
            }
    }

    private fun loadToSharedViewModel(listOfContact: List<Contact>) {
        viewModel.setListOfContact(listOfContact)// Saving list retrieved from Room to shared view Model
        findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment)
        binding.circularProgressBar.visibility =
            View.GONE // not necessary though since we are moving to next screen
    }

}