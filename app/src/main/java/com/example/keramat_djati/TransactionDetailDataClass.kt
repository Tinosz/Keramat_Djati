package com.example.keramat_djati

data class Transaction(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val amount: Long = 0L,
    val date: String = "",
    val time: String = "",
    val note: String? = null
)
