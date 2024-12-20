package com.example.keramat_djati.splitbill

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptItem(
    val itemName: String,
    val price: Double,
    val total: Double = price  // Total is just the price if no quantity is involved
) : Parcelable
