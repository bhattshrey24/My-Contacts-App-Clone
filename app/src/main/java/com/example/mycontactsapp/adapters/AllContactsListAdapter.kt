package com.example.mycontactsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycontactsapp.R

class AllContactsListAdapter : RecyclerView.Adapter<AllContactsListAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contactName: TextView

        init {
            contactName = itemView.findViewById(R.id.contactNameTV)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.home_page_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.contactName.text = "Stuti"
    }

    override fun getItemCount(): Int {
        return 20
    }
}