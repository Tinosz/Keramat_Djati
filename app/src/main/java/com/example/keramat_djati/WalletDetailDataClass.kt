package com.example.keramat_djati

data class WalletDetail(
    val id: String = "",
    val name: String = "",
    val balance: Long = 0L,
    val transactions: List<Transaction> = listOf()
)