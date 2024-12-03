package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.keramat_djati.transaction.TransactionActivityHost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var dateTextView: TextView
    private lateinit var transactionTypeTextView: TextView
    private lateinit var amountTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var noteTextView: TextView
    private lateinit var headerTitle: FrameLayout // Ensure this matches with XML
    private lateinit var walletNameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_detail)

        // Initialize views
        dateTextView = findViewById(R.id.date)
        transactionTypeTextView = findViewById(R.id.transaction_type)
        amountTextView = findViewById(R.id.amount)
        titleTextView = findViewById(R.id.title)
        categoryTextView = findViewById(R.id.transaction_category)
        noteTextView = findViewById(R.id.note)

        // Make sure the ID matches the correct type in XML
        headerTitle = findViewById(R.id.header_title) // FrameLayout
        walletNameTextView = findViewById(R.id.wallet_name) // TextView for wallet name

        val transactionId = intent.getStringExtra("transaction_id") ?: ""
        val walletId = intent.getStringExtra("wallet_id") ?: ""

        if (transactionId.isNotEmpty() && walletId.isNotEmpty()) {
            fetchWalletName(walletId)
            fetchTransactionDetails(walletId, transactionId)
        } else {
            Toast.makeText(this, "Invalid transaction ID or wallet ID", Toast.LENGTH_SHORT).show()
            finish()
        }
        findViewById<Button>(R.id.edit_button).setOnClickListener {
            val intent = Intent(this, TransactionActivityHost::class.java)

            val transactionId = this.intent.getStringExtra("transaction_id")
            val walletId = this.intent.getStringExtra("wallet_id")

            intent.putExtra("transaction_id", transactionId)
            intent.putExtra("wallet_id", walletId)
            intent.putExtra("title", titleTextView.text.toString())
            intent.putExtra("category", categoryTextView.text.toString())
            intent.putExtra("amount", amountTextView.text.toString().removePrefix("Rp. ").toLong())
            intent.putExtra("date", dateTextView.text.toString())
            intent.putExtra("note", noteTextView.text.toString())
            intent.putExtra("transaction_type", transactionTypeTextView.text.toString())

            startActivity(intent)
        }
        findViewById<Button>(R.id.delete_transaction_button).setOnClickListener {
            deleteTransaction(walletId, transactionId)
        }

    }

    private fun deleteTransaction(walletId: String, transactionId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val accountRef = db.collection("accounts").document(userId)
            .collection("wallets").document(walletId)

        val expenseRef = accountRef.collection("expenses").document(transactionId)
        val incomeRef = accountRef.collection("incomes").document(transactionId)

        // Try to delete from expense collection first
        expenseRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                expenseRef.delete().addOnSuccessListener {
                    Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity after deletion
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // If not found in expenses, try incomes
                incomeRef.get().addOnSuccessListener { incomeDoc ->
                    if (incomeDoc.exists()) {
                        incomeRef.delete().addOnSuccessListener {
                            Toast.makeText(this, "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
                            navigateBackWithRefresh()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to delete income: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching income: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error fetching expense: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateBackWithRefresh() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun fetchWalletName(walletId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("accounts").document(userId)
            .collection("wallets").document(walletId)
            .get()
            .addOnSuccessListener { document ->
                val walletName = document.getString("name") ?: "Unnamed Wallet"
                walletNameTextView.text = "$walletName's"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch wallet name", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTransactionDetails(walletId: String, transactionId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val accountRef = db.collection("accounts").document(userId)
            .collection("wallets").document(walletId)

        val expenseRef = accountRef.collection("expenses").document(transactionId)
        val incomeRef = accountRef.collection("incomes").document(transactionId)

        expenseRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                displayTransactionDetails(document, true)
            } else {
                incomeRef.get().addOnSuccessListener { incomeDoc ->
                    if (incomeDoc.exists()) {
                        displayTransactionDetails(incomeDoc, false)
                    } else {
                        Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error fetching income", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayTransactionDetails(document: DocumentSnapshot, isExpense: Boolean) {
        val date = document.getString("date") ?: "N/A"
        val amount = document.getLong("amount") ?: 0L
        val title = document.getString("title") ?: "N/A"
        val category = document.getString("category") ?: "N/A"
        val note = document.getString("note") ?: "N/A"

        dateTextView.text = date
        transactionTypeTextView.text = if (isExpense) "Expense Transaction" else "Income Transaction"
        amountTextView.text = "Rp. $amount"

        val backgroundColor = if (isExpense) R.color.red else R.color.main_green
        val textColor = if (isExpense) R.color.red else R.color.main_green

        headerTitle.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        amountTextView.setTextColor(ContextCompat.getColor(this, textColor))

        titleTextView.text = title
        categoryTextView.text = category
        noteTextView.text = note
    }
}
