package com.example.mycontactsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.R
import com.example.mycontactsapp.adapters.AllContactsListAdapter
import com.example.mycontactsapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
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

        return binding.root
    }

    fun setUpRecyclerView() {
        layoutManager = LinearLayoutManager(context)
        binding.homePageRecyclerView.layoutManager = layoutManager
        adapter = AllContactsListAdapter()
        binding.homePageRecyclerView.adapter = adapter
    }
}