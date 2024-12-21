package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ReceiptAdapter(private val receipts: List<Receipt>, private val onClick: (Receipt) -> Unit) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    class ReceiptViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        fun bind(receipt: Receipt, onClick: (Receipt) -> Unit) {
            itemView.setOnClickListener { onClick(receipt) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.receipt_item, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val receipt = receipts[position]
        holder.titleTextView.text = receipt.title
        holder.dateTextView.text = receipt.date
        Glide.with(holder.imageView.context)
            .load(receipt.imageUrl)
            .placeholder(R.drawable.baseline_android_24)
            .error(R.drawable.baseline_error_24)
            .into(holder.imageView)
        holder.bind(receipt, onClick)
    }

    override fun getItemCount(): Int = receipts.size
}
