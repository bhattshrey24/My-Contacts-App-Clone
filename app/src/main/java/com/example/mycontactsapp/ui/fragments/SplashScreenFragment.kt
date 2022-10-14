package com.example.mycontactsapp.ui.fragments

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mycontactsapp.R
import com.example.mycontactsapp.data.local.ContactDatabase
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.data.models.CursorData
import com.example.mycontactsapp.databinding.FragmentSplashScreenBinding
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.ui.viewmodels.ContentProviderViewModel
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.log


class SplashScreenFragment : Fragment() {

    private val binding: FragmentSplashScreenBinding by lazy {
        FragmentSplashScreenBinding.inflate(layoutInflater, null, false)
    }

    private val viewModel: ListOfContactsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.getListOfContactsFromRoomDB().observe(viewLifecycleOwner) {
            if (it.isEmpty()) { // Load from Content provider and store it in room
                loadDataFromContentProvider()
            } else { // pass this list to shared viewModel
                loadToSharedViewModel(it)
            }
        }
        return binding.root
    }

    private fun loadDataFromContentProvider() {
        val contentProviderViewModel: ContentProviderViewModel by viewModels()

        contentProviderViewModel.fetchDataFromAndroidDb() // This will fetch data from
        // content provider
        contentProviderViewModel.listOfRetrievedContacts
            .observe(viewLifecycleOwner) { listFromContentProvider ->

                listFromContentProvider.let {
                    viewModel.saveListOfContactInRoomDB(it) // will be done in background thread
                }
                viewModel.setListOfContact(listFromContentProvider) // saving list in shared
                // viewModel
                findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment)
                binding.circularProgressBar.visibility =
                    View.GONE // not necessary though since we are moving to next screen

            }
    }

   private fun loadToSharedViewModel(listOfContact: List<Contact>) {
        viewModel.setListOfContact(listOfContact)// Saving list retrieved from Room to shared view Model
        findNavController().navigate(R.id.action_splashScreenFragment_to_homeFragment)
        binding.circularProgressBar.visibility =
            View.GONE // not necessary though since we are moving to next screen
    }

}