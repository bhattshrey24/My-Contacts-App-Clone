package com.example.mycontactsapp.ui.fragments

import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.data.models.CursorData
import com.example.mycontactsapp.databinding.FragmentHomeBinding
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes
import com.example.mycontactsapp.ui.viewmodels.ContentProviderViewModel
import com.example.mycontactsapp.ui.viewmodels.ListOfContactsViewModel

class HomeFragment() : Fragment(),
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
            binding.homeFragmentCircularProgressBar.visibility = View.VISIBLE
            // observe to isFinished liveData boolean in order to remove progress bar
            // send list to functions
            contentProviderViewModel.syncData(listOfContactsViewModel.listOfContact.value?.toList())
            contentProviderViewModel.isSyncFinished.observe(viewLifecycleOwner) {
                if (it) {
                    binding.homeFragmentCircularProgressBar.visibility = View.GONE
                    Toast.makeText(context, "Successfully synced!", Toast.LENGTH_SHORT).show()
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


        listOfContactsViewModel.listOfContact.value?.let {
            adapter?.setContact(it)
            Constants.listOfAllContacts
        }

    }

    override fun onContactClick(position: Int) {
        val filteredListFromAdapter = adapter?.getFilteredListOfContacts() ?: listOf<Contact>()
        val action = HomeFragmentDirections.actionHomeFragmentToContactDetailsFragment(
            filteredListFromAdapter[position].roomContactId
        )
        findNavController().navigate(action)
    }

}
