package com.example.keramat_djati.splitbill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.keramat_djati.R

class ReceiptAdapter(private var items: List<ReceiptItem>) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val price: TextView = itemView.findViewById(R.id.textViewTotal)  // Display the total price
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_receipt, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.itemName  // Set item name
        holder.price.text = item.total.toString()  // Set total price
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReceiptItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
