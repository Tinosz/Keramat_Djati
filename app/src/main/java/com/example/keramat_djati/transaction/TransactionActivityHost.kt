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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source

class TransactionActivityHost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_host)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]

        val transactionId = intent.getStringExtra("transaction_id")
        viewModel.transactionId.value = transactionId

        val walletId = intent.getStringExtra("wallet_id")
        val title = intent.getStringExtra("title")
        val category = intent.getStringExtra("category")
        val amount = intent.getLongExtra("amount", 0L)
        val date = intent.getStringExtra("date")
        val note = intent.getStringExtra("note")
        val transactionType = intent.getStringExtra("transaction_type")

        viewModel.walletId.value = walletId
        viewModel.title.value = title
        viewModel.category.value = category
        viewModel.amount.value = amount
        viewModel.date.value = date
        viewModel.note.value = note
        viewModel.categoryType.value = if (transactionType == "Expense Transaction") "Expense" else "Income"

        if (savedInstanceState == null) {
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
        val transactionId = viewModel.transactionId.value

        if (userId == null || walletId.isNullOrEmpty()) {
            Toast.makeText(this, "User or wallet not selected", Toast.LENGTH_LONG).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Determine the collection path based on category type
        val collectionPath = when (viewModel.categoryType.value) {
            "Expense" -> "expenses"
            "Income" -> "incomes"
            else -> {
                Toast.makeText(this, "Invalid category type", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Reference to the current transaction collection
        val transactionCollectionRef = db.collection("accounts").document(userId)
            .collection("wallets").document(walletId)
            .collection(collectionPath)

        // Transaction data to be saved
        val transactionData: MutableMap<String, Any> = hashMapOf(
            "amount" to (viewModel.amount.value ?: 0L),
            "title" to (viewModel.title.value ?: ""),
            "category" to (viewModel.category.value ?: ""),
            "date" to (viewModel.date.value ?: ""),
            "note" to (viewModel.note.value ?: ""),
            "time" to (viewModel.time.value ?: System.currentTimeMillis().toString())
        )

        if (transactionId != null && transactionId.isNotEmpty()) {
            // If transaction ID exists, try to update the existing transaction
            val transactionDocRef = transactionCollectionRef.document(transactionId)

            transactionDocRef.get(Source.SERVER).addOnSuccessListener { document ->
                if (document.exists()) {
                    val oldAmount = document.getLong("amount") ?: 0L
                    val newAmount = viewModel.amount.value ?: 0L

                    // Update existing transaction if it exists
                    transactionDocRef.update(transactionData)
                        .addOnSuccessListener {
                            Log.d("TransactionActivityHost", "Transaction updated successfully.")
                            updateWalletBalance(userId, walletId, viewModel.oldWalletId.value ?: "", oldAmount, newAmount, viewModel.categoryType.value ?: "Expense")
                            navigateToMainActivity()
                        }
                        .addOnFailureListener {
                            Log.e("TransactionActivityHost", "Failed to update transaction.", it)
                            Toast.makeText(this, "Failed to update transaction", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // If transaction doesn't exist, move the transaction to the new wallet

                    // 1. Move data to the new wallet
                    val oldAmount = document.getLong("amount") ?: 0L

                    transactionCollectionRef.add(transactionData)
                        .addOnSuccessListener {
                            Log.d("TransactionActivityHost", "Transaction moved successfully to new wallet.")

                            // 2. Delete the old transaction from the original wallet
                            val oldTransactionDocRef = db.collection("accounts").document(userId)
                                .collection("wallets").document(viewModel.oldWalletId.value ?: "")
                                .collection(collectionPath).document(transactionId)

                            Log.d("TransactionActivityHost", "Old wallet ID: ${viewModel.oldWalletId.value}")

                            oldTransactionDocRef.delete()
                                .addOnSuccessListener {
                                    updateWalletBalance(userId, walletId,  viewModel.oldWalletId.value ?: "", oldAmount, viewModel.amount.value ?: 0L, viewModel.categoryType.value ?: "Expense")
                                    navigateToMainActivity()
                                }
                                .addOnFailureListener {
                                    Log.e("TransactionActivityHost", "Failed to delete old transaction.", it)
                                    Toast.makeText(this, "Failed to delete old transaction", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Log.e("TransactionActivityHost", "Failed to move transaction.", it)
                            Toast.makeText(this, "Failed to move transaction", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } else {
            // If no transaction ID exists, create a new transaction
            transactionCollectionRef.add(transactionData)
                .addOnSuccessListener {
                    updateWalletBalance(userId, walletId, viewModel.oldWalletId.value ?: "", 0L, viewModel.amount.value ?: 0L, viewModel.categoryType.value ?: "Expense")
                    Log.d("TransactionActivityHost", "New transaction created successfully.")
                    navigateToMainActivity()
                }
                .addOnFailureListener {
                    Log.e("TransactionActivityHost", "Failed to create new transaction.", it)
                    Toast.makeText(this, "Failed to create transaction", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun updateWalletBalance(
        userId: String,
        walletId: String,
        oldWalletId: String,
        oldAmount: Long,
        newAmount: Long,
        type: String
    ) {
        if (walletId.isEmpty()) {
            Toast.makeText(this, "User or wallet not selected", Toast.LENGTH_LONG).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Reference to wallet collections for both old and new wallets
        val oldWalletRef = db.collection("accounts").document(userId)
            .collection("wallets").document(oldWalletId ?: "")
        val newWalletRef = db.collection("accounts").document(userId)
            .collection("wallets").document(walletId ?: "")

        db.runTransaction { transaction ->
            // Get current balances for both old and new wallets
            val oldWalletSnapshot = transaction.get(oldWalletRef)
            val newWalletSnapshot = transaction.get(newWalletRef)

            val oldBalance = oldWalletSnapshot.getLong("amount") ?: 0L
            val newBalance = newWalletSnapshot.getLong("amount") ?: 0L

            // Calculate the balance change
            val balanceChange = newAmount - oldAmount

            // Handle balance update for old wallet
            if (walletId != oldWalletId) {
                // Update the old wallet balance
                val newOldBalance = if (type == "Expense") {
                    oldBalance - balanceChange
                } else {
                    oldBalance + balanceChange
                }

                if (newOldBalance < 0) {
                    throw IllegalStateException("Insufficient funds in the old wallet.")
                }

                // Update old wallet balance
                transaction.update(oldWalletRef, "amount", newOldBalance)

                // Update the new wallet balance
                val newNewBalance = if (type == "Expense") {
                    newBalance - balanceChange
                } else {
                    newBalance + balanceChange
                }

                transaction.update(newWalletRef, "amount", newNewBalance)
            } else {
                // If walletId has not changed, just update the current wallet balance
                val newWalletAmount = if (type == "Expense") {
                    newBalance - balanceChange
                } else {
                    newBalance + balanceChange
                }

                if (newWalletAmount < 0) {
                    throw IllegalStateException("Insufficient funds in the wallet.")
                }

                transaction.update(newWalletRef, "amount", newWalletAmount)
            }
        }.addOnSuccessListener {
            Log.d("TransactionActivityHost", "Wallet balance updated successfully.")
        }.addOnFailureListener { e ->
            Log.e("TransactionActivityHost", "Error updating wallet balance: ${e.message}")
            Toast.makeText(this, "Error updating wallet balance: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}