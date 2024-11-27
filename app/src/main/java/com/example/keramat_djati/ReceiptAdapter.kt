package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReceiptAdapter(private var items: List<ReceiptItem>) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val quantity: TextView = itemView.findViewById(R.id.textViewQuantity)
        val price: TextView = itemView.findViewById(R.id.textViewPrice)
        val total: TextView = itemView.findViewById(R.id.textViewTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_receipt, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.itemName
        holder.quantity.text = item.quantity.toString()  
        holder.price.text = item.price.toString()
        holder.total.text = item.total.toString()
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ReceiptItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
