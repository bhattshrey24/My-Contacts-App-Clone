package com.example.mycontactsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.Contact
import com.example.mycontactsapp.R

class AllContactsListAdapter(
    var onContactClickListenerVariable: OnContactClickListener
) : RecyclerView.Adapter<AllContactsListAdapter.MyViewHolder>() {

    private var contacts: List<Contact>? = listOf()
    private var numOfContacts: Int? = 0

    fun setContact(contacts: List<Contact>) {
        this.contacts = contacts
        this.numOfContacts = contacts.size
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contactName: TextView

        init {
            contactName = itemView.findViewById(R.id.contactNameTV)
            itemView.setOnClickListener {
                val pos = layoutPosition // use position if this doesn't work
                onContactClickListenerVariable.onContactClick(pos)
            }
        }
    }

    interface OnContactClickListener {
        fun onContactClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.home_page_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.contactName.text = contacts?.get(position)?.name ?: "No Name"

    }

    override fun getItemCount(): Int {
        return numOfContacts ?: 0
    }


}