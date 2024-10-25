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
        viewModel.transactionId.value = transactionId // Store transactionId in ViewModel

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
        val transactionId = viewModel.transactionId.value // Check if transactionId exists

        if (userId == null || walletId.isNullOrEmpty()) {
            Toast.makeText(this, "User or wallet not selected", Toast.LENGTH_LONG).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val collectionPath = when (viewModel.categoryType.value) {
            "Expense" -> "expenses"
            "Income" -> "incomes"
            else -> {
                Toast.makeText(this, "Invalid category type", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Define the reference to the collection path (without transactionId for now)
        val transactionCollectionRef = db.collection("accounts").document(userId)
            .collection("wallets").document(walletId)
            .collection(collectionPath)

        val transactionData: MutableMap<String, Any> = hashMapOf(
            "amount" to (viewModel.amount.value ?: 0L),
            "title" to (viewModel.title.value ?: ""),
            "category" to (viewModel.category.value ?: ""),
            "date" to (viewModel.date.value ?: ""),
            "note" to (viewModel.note.value ?: ""),
            "time" to (viewModel.time.value ?: System.currentTimeMillis().toString())
        ) as MutableMap<String, Any>


        if (transactionId != null && transactionId.isNotEmpty()) {
            // UPDATE EXISTING TRANSACTION
            val transactionDocRef = transactionCollectionRef.document(transactionId) // Use transactionId in path

            transactionDocRef.get(Source.SERVER).addOnSuccessListener { document ->
                if (document.exists()) {
                    val oldAmount = document.getLong("amount") ?: 0L
                    val newAmount = viewModel.amount.value ?: 0L

                    transactionDocRef.update(transactionData)  // Use update instead of set()
                        .addOnSuccessListener {
                            Log.d("TransactionActivityHost", "Transaction updated successfully.")
                            updateWalletBalance(userId, walletId, oldAmount, newAmount, viewModel.categoryType.value ?: "Expense")
                            navigateToMainActivity()
                        }
                        .addOnFailureListener { e ->
                            Log.e("TransactionActivityHost", "Failed to update transaction", e)
                            Toast.makeText(this, "Failed to update transaction: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                } else {
                    Log.e("TransactionActivityHost", "Transaction not found: $transactionId")
                    Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            transactionCollectionRef.add(transactionData)
                .addOnSuccessListener { documentReference ->
                    Log.d("TransactionActivityHost", "New transaction added with ID: ${documentReference.id}")
                    updateWalletBalance(userId, walletId, 0L, viewModel.amount.value ?: 0L, viewModel.categoryType.value ?: "Expense")
                    navigateToMainActivity()
                }
                .addOnFailureListener { e ->
                    Log.e("TransactionActivityHost", "Failed to add new transaction", e)
                    Toast.makeText(this, "Failed to add new transaction: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }


    private fun updateWalletBalance(
        userId: String,
        walletId: String,
        oldAmount: Long,
        newAmount: Long,
        type: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val walletRef = db.collection("accounts").document(userId).collection("wallets").document(walletId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(walletRef)
            val currentBalance = snapshot.getLong("amount") ?: 0L

            // Calculate the balance change
            val balanceChange = newAmount - oldAmount

            // Update the balance based on transaction type (expense or income)
            val newBalance = if (type == "Expense") {
                currentBalance - balanceChange // Expense: Subtract net change
            } else {
                currentBalance + balanceChange // Income: Add net change
            }

            if (newBalance < 0) {
                throw IllegalStateException("Insufficient funds for this transaction.")
            }

            // Commit the new balance
            transaction.update(walletRef, "amount", newBalance)
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