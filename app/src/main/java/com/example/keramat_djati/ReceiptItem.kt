package com.example.keramat_djati

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceiptItem(
    val itemName: String,
    val quantity: Int,  // Change quantity to Int
    val price: Double,
    val total: Double
) : Parcelable
