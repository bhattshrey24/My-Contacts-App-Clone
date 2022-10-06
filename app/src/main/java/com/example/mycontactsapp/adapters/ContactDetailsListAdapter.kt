package com.example.mycontactsapp.adapters

import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.other.Constants
import com.example.mycontactsapp.R

class ContactDetailsListAdapter() : RecyclerView.Adapter<ContactDetailsListAdapter.MyViewHolder>() {
    var listOfListItems: List<Pair<String, String>> = listOf()
    var sizeOfListItemMap:Int? = 0

    fun setListItem(listItems: List<Pair<String, String>>) { // Updates the recyclerview
        this.listOfListItems = listItems
        this.sizeOfListItemMap = listItems.size
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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var listItem = listOfListItems.get(position)

        if (listItem.second.contains(Regex("@"))) { // email
            holder.iconImageView.setImageResource(R.drawable.ic_email)
            if (listItem.first.toInt() == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) { // todo change it to when
                holder.typeTextView.text = "Mobile"
            } else if (listItem.first.toInt() == ContactsContract.CommonDataKinds.Phone.TYPE_HOME) {
                holder.typeTextView.text = "Home"
            } else {// work
                holder.typeTextView.text = "Work"
            }
        } else { // number
            holder.iconImageView.setImageResource(R.drawable.ic_phone)
            if (listItem.first.toInt() == ContactsContract.CommonDataKinds.Email.TYPE_MOBILE) { // todo change it to when
                holder.typeTextView.text = "Mobile"
            } else if (listItem.first.toInt() == ContactsContract.CommonDataKinds.Email.TYPE_HOME) {
                holder.typeTextView.text = "Home"
            } else {// work
                holder.typeTextView.text = "Work"
            }
        }

        holder.nOETextView.text = listItem.second
    }

    override fun getItemCount(): Int {
        return sizeOfListItemMap?:0
    }
}