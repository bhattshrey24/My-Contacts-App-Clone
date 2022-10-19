package com.example.mycontactsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.R
import com.example.mycontactsapp.other.EmailTypes
import com.example.mycontactsapp.other.PhoneTypes

class ContactDetailsListAdapter: RecyclerView.Adapter<ContactDetailsListAdapter.MyViewHolder>() {

    private var listOfListItems: List<Pair<String, String>> =
        listOf() // in Pair the "first" is the Type Code and "second" is the value

    private var isFirstNum =
        true // So that I only show icon in front of 1st phone
    // number (this is just for UI purpose)

    private var isFirstEmail = true

    fun setListItem(listItems: List<Pair<String, String>>) { // Updates the recyclerview
        this.listOfListItems = listItems
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nOETextView: TextView
        var typeTextView: TextView
        var iconImageView: ImageView

        init {
            nOETextView = itemView.findViewById(R.id.contactDetailNumOrEmailRVTV)
            iconImageView = itemView.findViewById(R.id.contactDetailRVIconIV)
            typeTextView = itemView.findViewById(R.id.contactDetailTypeRVTV)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.contact_details_list_item, parent, false)
        return MyViewHolder(view)
    }

    private fun regExToCheckEmail(str: String): Boolean {
        return str.matches(Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val listItem = listOfListItems[position]

        if (regExToCheckEmail(listItem.second)) { // email

            if (isFirstEmail) { // This makes sure that we show symbol only on start of 1st email
                holder.iconImageView.setImageResource(R.drawable.ic_email)
                isFirstEmail = false
            }

            when (listItem.first.toInt()) {
                EmailTypes.Work.codeOfType -> {
                    holder.typeTextView.text = EmailTypes.Work.nameOfType
                }
                EmailTypes.Home.codeOfType -> {
                    holder.typeTextView.text = EmailTypes.Home.nameOfType
                }
                else -> {
                    holder.typeTextView.text = EmailTypes.Home.nameOfType
                }
            }
        } else { // number
            if (isFirstNum) {
                holder.iconImageView.setImageResource(R.drawable.ic_phone)
                isFirstNum = false
            }
            when (listItem.first.toInt()) {
                PhoneTypes.Mobile.codeOfType -> {
                    holder.typeTextView.text = PhoneTypes.Mobile.nameOfType
                }
                PhoneTypes.Home.codeOfType -> {
                    holder.typeTextView.text = PhoneTypes.Home.nameOfType
                }
                PhoneTypes.Work.codeOfType -> {
                    holder.typeTextView.text = PhoneTypes.Work.nameOfType
                }
                else -> {
                    holder.typeTextView.text = PhoneTypes.Mobile.nameOfType
                }
            }
        }
        holder.nOETextView.text = listItem.second
    }

    override fun getItemCount(): Int {
        return listOfListItems.size
    }
}