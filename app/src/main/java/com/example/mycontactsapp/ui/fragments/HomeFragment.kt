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
    }

    private fun setUpRecyclerView() {
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(context)
            // "@" we can distinguish between properties of recyclerView and HomeFragment
            this.adapter = this@HomeFragment.adapter
        }

        val listOfContactsViewModel: ListOfContactsViewModel by activityViewModels()
        listOfContactsViewModel.listOfContact.value?.let {
            adapter?.setContact(it)
            Constants.listOfAllContacts
        }
    }

    override fun onContactClick(position: Int) {
        val filteredListFromAdapter = adapter?.getFilteredListOfContacts() ?: listOf<Contact>()
        val action = HomeFragmentDirections.actionHomeFragmentToContactDetailsFragment(
            filteredListFromAdapter[position].contactId ?: -1
        )
        findNavController().navigate(action)
    }

}
