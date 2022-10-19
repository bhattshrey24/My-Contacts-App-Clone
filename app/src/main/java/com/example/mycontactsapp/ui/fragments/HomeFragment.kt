package com.example.mycontactsapp.ui.fragments



import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.mycontactsapp.adapters.AllContactsListAdapter

import com.example.mycontactsapp.databinding.FragmentHomeBinding

import com.example.mycontactsapp.ui.viewmodels.ContentProviderViewModel
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel

class HomeFragment : Fragment(),
    AllContactsListAdapter.OnContactClickListener {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater, null, false)
    }

    private var adapter: AllContactsListAdapter? = null

    private val listOfContactsViewModel: ListOfContactsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setUpRecyclerView()
        setUpListeners()

        return binding.root
    }


    private fun setUpListeners() {
        binding.addNewContactFloatingButton.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToCreateOrModifyContactFragment(
                false,
                -1
            )
            findNavController().navigate(action)
        }

        val contentProviderViewModel: ContentProviderViewModel by viewModels()

        binding.syncContactsFloatingButton.setOnClickListener {
            contentProviderViewModel.isSyncFinished.value =
                false // setting false so that if user ha previously synced it and again want to sync then he could do so
            binding.syncContactsFloatingButton.isEnabled = false

            binding.homeFragmentCircularProgressBar.visibility = View.VISIBLE

            contentProviderViewModel.syncData(listOfContactsViewModel.listOfContact.value?.toList()) // this will sync the room data to android contact db

            contentProviderViewModel.isSyncFinished.observe(viewLifecycleOwner) { isSync ->
                if (isSync) {
                    binding.homeFragmentCircularProgressBar.visibility = View.GONE
                    Toast.makeText(context, "Sync successful!", Toast.LENGTH_SHORT).show()
                    binding.syncContactsFloatingButton.isEnabled = true
                }
            }
            contentProviderViewModel.listOfContactsWithUpdatedContactID.observe(viewLifecycleOwner) { listOfContact ->
                if (!listOfContact.isNullOrEmpty()) {
                    for (contact in listOfContact) {
                        val retrievedContactIdx =
                            listOfContactsViewModel.listOfContact.value?.indexOfFirst {
                                it.roomContactId == contact.roomContactId
                            }
                        retrievedContactIdx?.let {
                            listOfContactsViewModel.updateContact(contact)
                        }
                    }
                }
            }
        }

        listOfContactsViewModel.listOfContact.value?.let {
            adapter?.setContact(it)
        }

    }

    private fun setUpRecyclerView() {
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            // "@" we can distinguish between properties of recyclerView and HomeFragment
            this.adapter = this@HomeFragment.adapter
        }
    }

    override fun onContactClick(position: Int) {
        val filteredListFromAdapter = adapter?.getFilteredListOfContacts() ?: listOf()
        val action = HomeFragmentDirections.actionHomeFragmentToContactDetailsFragment(
            filteredListFromAdapter[position].roomContactId
        )
        findNavController().navigate(action)
    }
}
