package com.example.mycontactsapp.ui.fragments


import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentHomeBinding
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.ui.viewmodels.ContentProviderViewModel
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel

class HomeFragment : Fragment(),
    AllContactsListAdapter.OnContactClickListener {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater, null, false)
    }

    private var adapter: AllContactsListAdapter? = null

    private val sharedViewModel: ListOfContactsViewModel by activityViewModels()

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
                false // setting false so that if user has previously synced it and again
            // want to sync then he could do so
            binding.syncContactsFloatingButton.isEnabled = false

            binding.homeFragmentCircularProgressBar.visibility = View.VISIBLE

            contentProviderViewModel.syncData()
            sharedViewModel.deleteTableData() // this will delete the current data in the DB

            contentProviderViewModel.isSyncFinished.observe(viewLifecycleOwner) { isSync ->
                if (isSync) {
                    contentProviderViewModel.listOfRetrievedContacts.value?.let { androidContacts ->

                        sharedViewModel.saveListOfContactInRoomDB(androidContacts)// saving to room

                        sharedViewModel.getListOfContactsFromRoomDB()
                            .observe(viewLifecycleOwner) { listReturnedByRoom ->
                                if (!listReturnedByRoom.isNullOrEmpty()) {
                                    sharedViewModel.setListOfContact(listReturnedByRoom) // saving to shared view model
                                    adapter?.setContact(listReturnedByRoom)
                                    binding.homeFragmentCircularProgressBar.visibility = View.GONE
                                    Toast.makeText(context, "Sync successful!", Toast.LENGTH_SHORT)
                                        .show()
                                    binding.syncContactsFloatingButton.isEnabled = true
                                }
                            }

                    }
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            // "@" we can distinguish between properties of recyclerView and HomeFragment
            this.adapter = this@HomeFragment.adapter
        }
        sharedViewModel.listOfContact.value?.let {
            adapter?.setMasterListOfContact(it)
            adapter?.setContact(it) // this
        }

    }

    override fun onContactClick(position: Int) {
        val filteredListFromAdapter =
            adapter?.getFilteredListOfContacts() ?: listOf() // i.e. filtered
        // list that was returned when user searched something
        val action = HomeFragmentDirections.actionHomeFragmentToContactDetailsFragment(
            filteredListFromAdapter[position].roomContactId
        )
        findNavController().navigate(action)
    }
}
