package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_item_recycler, parent, false)
        return TransactionViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position], transactions, listener)
    }

    override fun getItemCount() = transactions.size

    class TransactionViewHolder(itemView: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        fun bind(transaction: Transaction, transactions: List<Transaction>, listener: OnItemClickListener) {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(transactions[position])
                }
            }

            val titleView = itemView.findViewById<TextView>(R.id.text_title)
            val categoryView = itemView.findViewById<TextView>(R.id.text_category)
            val amountView = itemView.findViewById<TextView>(R.id.text_amount)
            val dateView = itemView.findViewById<TextView>(R.id.text_date)
            val colorIndicator = itemView.findViewById<View>(R.id.color_indicator)

            titleView.text = transaction.title
            categoryView.text = transaction.category
            dateView.text = "${transaction.date} ${transaction.time}"

            val isIncome = transaction.category == "Income"
            val amountPrefix = if (isIncome) "+Rp " else "-Rp "
            val colorRes = if (isIncome) R.color.main_green else R.color.red

            amountView.text = "$amountPrefix${Math.abs(transaction.amount)}"
            amountView.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
            colorIndicator.setBackgroundColor(ContextCompat.getColor(itemView.context, colorRes))
        }
    }

    interface OnItemClickListener {
        fun onItemClick(transaction: Transaction)
    }

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}



