package com.example.keramat_djati

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WalletViewModel : ViewModel() {
    private val walletData = MutableLiveData<List<WalletDetail>>()

    fun getWalletData(): LiveData<List<WalletDetail>> = walletData

    fun fetchWalletData() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("accounts").document(userId)
            .collection("wallets")
            .get()
            .addOnSuccessListener { documents ->
                val walletDetails = documents.map { doc ->
                    WalletDetail(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unnamed Wallet",
                        balance = doc.getLong("amount") ?: 0L
                    )
                }
                walletData.value = walletDetails
            }
            .addOnFailureListener {
            }
    }
}
