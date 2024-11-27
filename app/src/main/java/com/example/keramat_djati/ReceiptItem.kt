package com.example.keramat_djati

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptItem(
    val itemName: String,
    val quantity: String,  // Adjust the type according to your data
    val price: Double,
    val total: Double
) : Parcelable
