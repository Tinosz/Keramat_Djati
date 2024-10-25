package com.example.keramat_djati.transaction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keramat_djati.CreateWallet1
import com.example.keramat_djati.MainActivity
import com.example.keramat_djati.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TransactionActivityHost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_host)
        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.transaction_fragment_container, TransactionFragment1())
                .commit()
        }
    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.transaction_fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    private lateinit var viewModel: TransactionViewModel

    fun saveTransactionToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val walletId = viewModel.walletId.value

        if (userId == null || walletId.isNullOrEmpty()) {
            Toast.makeText(this, "User or wallet not selected", Toast.LENGTH_LONG).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val transactionAmount = viewModel.amount.value ?: 0L
        val transactionData = hashMapOf(
            "amount" to transactionAmount,
            "title" to (viewModel.title.value ?: ""),
            "category" to (viewModel.category.value ?: ""),
            "date" to (viewModel.date.value ?: ""),
            "note" to (viewModel.note.value ?: "")
        )

        val collectionPath = when (viewModel.categoryType.value) {
            "Expense" -> "expenses"
            "Income" -> "incomes"
            else -> {
                Toast.makeText(this, "Invalid category type", Toast.LENGTH_LONG).show()
                return
            }
        }

        db.collection("accounts").document(userId)
            .collection("wallets").document(walletId)
            .collection(collectionPath)
            .add(transactionData)
            .addOnSuccessListener { documentReference ->
                Log.d("TransactionActivityHost", "Transaction saved with ID: ${documentReference.id}")
                updateWalletBalance(userId, walletId, transactionAmount, collectionPath)
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                Log.e("TransactionActivityHost", "Failed to save transaction", e)
                Toast.makeText(this, "Failed to save transaction: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateWalletBalance(userId: String, walletId: String, amount: Long, type: String) {
        val db = FirebaseFirestore.getInstance()
        val walletRef = db.collection("accounts").document(userId).collection("wallets").document(walletId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(walletRef)
            val currentBalance = snapshot.getLong("amount") ?: 0L

            val newBalance = if (type == "expenses") {
                if (currentBalance - amount < 0) {
                    throw IllegalStateException("Insufficient funds for this transaction.")
                }
                currentBalance - amount
            } else {
                currentBalance + amount
            }

            transaction.update(walletRef, "amount", newBalance)
            null
        }.addOnSuccessListener {
            Log.d("TransactionActivityHost", "Wallet balance updated successfully.")
        }.addOnFailureListener { e ->
            Log.e("TransactionActivityHost", "Error updating wallet balance: ${e.message}")
            Toast.makeText(this, "Error updating wallet balance: ${e.message}", Toast.LENGTH_LONG).show()
            if (e is IllegalStateException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()  // Display specific error message for insufficient funds
            }
        }
    }




    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}