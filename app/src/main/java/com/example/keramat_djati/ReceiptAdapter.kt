package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReceiptAdapter(private var items: List<ReceiptItem>) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

    // ViewHolder for the receipt item
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val quantity: TextView = itemView.findViewById(R.id.textViewQuantity)
        val price: TextView = itemView.findViewById(R.id.textViewPrice)
        val total: TextView = itemView.findViewById(R.id.textViewTotal)
    }

    // Bind the data to the RecyclerView item view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receiptItem = items[position]
        holder.itemName.text = receiptItem.itemName
        holder.quantity.text = receiptItem.quantity.toString()
        holder.price.text = receiptItem.price.toString()
        holder.total.text = receiptItem.total.toString()
    }

    // Create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_receipt, parent, false)
        return ViewHolder(itemView)
    }

    // Get the item count
    override fun getItemCount() = items.size

    // Update the adapter data
    fun updateData(newItems: List<ReceiptItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
