package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class ReceiptAdapter(private val receipts: List<Receipt>) : RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder>() {

    class ReceiptViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        //val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView) // Remove or comment out if not used
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.receipt_item, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        val receipt = receipts[position]
        holder.titleTextView.text = receipt.title
        holder.dateTextView.text = receipt.date
        //holder.descriptionTextView.text = receipt.description // Remove or comment out if not used

        // Use Glide to load the image directly
        if (receipt.imageUrl.isNotEmpty()) {
            Glide.with(holder.imageView.context)
                .load(receipt.imageUrl)
                .placeholder(R.drawable.baseline_android_24)
                .error(R.drawable.baseline_error_24)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.baseline_android_24)
        }
    }

    override fun getItemCount(): Int = receipts.size
}
