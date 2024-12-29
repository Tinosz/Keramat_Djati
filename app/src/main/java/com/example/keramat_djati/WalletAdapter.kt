package com.example.keramat_djati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class WalletAdapter (
    private var wallets: List<WalletDetail>
) : RecyclerView.Adapter<WalletAdapter. WalletViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.wallet_recycler, parent, false)
        return WalletViewHolder(view)
    }

    override fun getItemCount() = wallets.size

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        holder.bind(wallets[position])
    }

    class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(wallet: WalletDetail) {
            val nameView = itemView.findViewById<TextView>(R.id.text_wallet_name)
            val balanceView = itemView.findViewById<TextView>(R.id.text_wallet_balance)

            nameView.text = wallet.name
            balanceView.text = formatToRupiah(wallet.balance)
        }

        private fun formatToRupiah(amount: Long): String {
            val symbols = DecimalFormatSymbols(Locale("id", "ID"))  // Indonesian locale
            symbols.groupingSeparator = '.'
            val decimalFormat = DecimalFormat("#,###", symbols)
            return "Rp " + decimalFormat.format(amount)
        }

    }
    fun getItemAtPosition(position: Int): WalletDetail {
        return wallets[position]
    }

    fun updateWallets(newWallets: List<WalletDetail>) {
        wallets = newWallets
        notifyDataSetChanged()
    }




}