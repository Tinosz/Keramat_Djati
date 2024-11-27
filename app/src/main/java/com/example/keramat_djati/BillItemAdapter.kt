package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BillItemAdapter(private val items: List<BillItem>) :
    RecyclerView.Adapter<BillItemAdapter.BillItemViewHolder>() {

    class BillItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descriptionText: TextView = view.findViewById(R.id.item_description)
        val priceText: TextView = view.findViewById(R.id.item_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_item, parent, false)
        return BillItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillItemViewHolder, position: Int) {
        val item = items[position]
        holder.descriptionText.text = item.description
        holder.priceText.text = "Rp ${String.format("%.2f", item.subtotal)}"
    }

    override fun getItemCount() = items.size
}