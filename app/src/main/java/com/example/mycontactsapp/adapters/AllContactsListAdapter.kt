package com.example.mycontactsapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.data.models.Contact
import com.example.mycontactsapp.R

private const val SEARCH_BAR_LIST_ITEM = 1
private const val CONTACTS_LIST_ITEM = 2

class AllContactsListAdapter(
    var onContactClickListenerVariable: OnContactClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var masterListOfContacts: List<Contact> = listOf()// using master list because
    // listOfContact will get filtered since it is actually being used to show items on
    // recyclerView so I needed a master copy on which I can filter contacts and get a new list
    // which I will assign to "listOfContacts"

    private var listOfContacts: List<Contact> = listOf()

    fun getFilteredListOfContacts() = listOfContacts

    fun setContact(filteredContactList: List<Contact>) { // Updates the recyclerview
        this.listOfContacts = filteredContactList
        notifyDataSetChanged() // We are changing whole data set
        // cause theres no option like we could have removed all elements and
        // added the once that are in list but its better to just update instead
    }

    fun setMasterListOfContact(list: List<Contact>) {
        masterListOfContacts = list
    }

    inner class MyViewHolderForSearchBar(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var searchBarET: EditText

        init {
            searchBarET = itemView.findViewById(R.id.searchBarEditText)
            searchBarET.addTextChangedListener {
                val query = it.toString().trim()
                val listOfContactsFilteredFromQuery =
                    masterListOfContacts.filter { contact ->
                        contact.name?.contains(query, true) ?: false
                    }
                setContact(listOfContactsFilteredFromQuery)
            }
        }

    }

    inner class MyViewHolderForContacts(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contactName: TextView

        init {
            contactName = itemView.findViewById(R.id.contactNameTV)
            itemView.setOnClickListener {
                val pos = layoutPosition - 1  // gives the position of
                // the holder that is clicked . We doing -1 cause 1st item in recycler view
                // will always be
                // search bar so our contacts starts from pos 2
                onContactClickListenerVariable.onContactClick(pos)
            }
        }
    }

    interface OnContactClickListener {
        fun onContactClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SEARCH_BAR_LIST_ITEM) {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_bar_list_item, parent, false)
            MyViewHolderForSearchBar(view)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_page_list_item, parent, false)
            MyViewHolderForContacts(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == CONTACTS_LIST_ITEM) {
            val contactHolder =
                holder as MyViewHolderForContacts // type casting holder to correct ViewHolder
            contactHolder.contactName.text =
                listOfContacts[position - 1].name// -1 because
            // there's 1 default item
            // which is always present and that item is search bar so we made changes accordingly
            // in getItemCount due to which if we don't do -1 then we will get index out of bound exception
        } else {
            // var searchBarHolder = holder as MyViewHolderForSearchBar // we do nothing
            // right now but we could have bound something
        }

    }

    override fun getItemCount(): Int {
        return listOfContacts.size + 1 // +1 because even if list is
        // empty we still want 1 item to show and that Item is search bar
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            SEARCH_BAR_LIST_ITEM
        } else {
            CONTACTS_LIST_ITEM
        }
    }


}

