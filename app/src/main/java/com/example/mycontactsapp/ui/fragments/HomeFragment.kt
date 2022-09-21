package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentHomeBinding

class HomeFragment() : Fragment(),
    AllContactsListAdapter.OnContactClickListener {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater, null, false)
    }

    var layoutManager: RecyclerView.LayoutManager? = null
    var adapter: AllContactsListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setUpRecyclerView()
        binding.addNewContactFloatingButton.setOnClickListener {
            replaceFragment(CreateOrModifyContactFragment())
        }
        return binding.root
    }

    private fun setUpRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        binding.homePageRecyclerView.layoutManager = layoutManager
        adapter = AllContactsListAdapter(this)
        binding.homePageRecyclerView.adapter = adapter
    }

    override fun onContactClick(position: Int) {
        Toast.makeText(context, "pressed $position", Toast.LENGTH_SHORT).show()
        // Navigate to Contact Display Basically Change the fragment
        replaceFragment(ContactDetailsFragment())
    }

    private fun replaceFragment(myFragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainActivityFragmentContainer, myFragment)
        fragmentTransaction.commit()
    }

    }