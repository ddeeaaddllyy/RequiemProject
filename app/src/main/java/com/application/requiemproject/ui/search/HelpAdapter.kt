package com.application.requiemproject.ui.search

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.graphics.toColorInt

class HelpAdapter(
    private var items: List<HelpItem>,
    private val onClick: (HelpItem) -> Unit
) : RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    lateinit var textView: TextView

    class HelpViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HelpAdapter.HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        textView = view.findViewById(android.R.id.text1)
        textView.setTextColor("#F5F5F5".toColorInt())

        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpAdapter.HelpViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.question

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<HelpItem>) {
        items = newList
        notifyDataSetChanged()
    }

}