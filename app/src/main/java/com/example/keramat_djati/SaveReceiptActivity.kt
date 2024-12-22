package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SaveReceiptActivity : AppCompatActivity() {

    private lateinit var receiptsRecyclerView: RecyclerView
    private lateinit var adapter: ReceiptAdapter

    private val firestoreReference = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_receipt)

        receiptsRecyclerView = findViewById(R.id.receiptsRecyclerView)
        receiptsRecyclerView.layoutManager = LinearLayoutManager(this)
        receiptsRecyclerView.itemAnimator = DefaultItemAnimator()
        fetchReceipts()

        val fabAddReceipt = findViewById<FloatingActionButton>(R.id.fabAddReceipt)
        fabAddReceipt.setOnClickListener {
            val intent = Intent(this, AddReceiptActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        fetchReceipts()  // Ensure data is refreshed every time the activity resumes
    }

    private fun fetchReceipts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show()
            return
        }

        firestoreReference.collection("accounts").document(userId)
            .collection("savedreceipt")
            .get()
            .addOnSuccessListener { documents ->
                val receipts = documents.map { document ->
                    Receipt(
                        title = document.getString("title") ?: "",
                        date = document.getString("date") ?: "",
                        description = document.getString("description") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        documentId = document.id  // Storing the Firestore document ID here
                    )
                }
                adapter = ReceiptAdapter(receipts) { receipt ->
                    Intent(this, ReceiptDetailActivity::class.java).also { intent ->
                        intent.putExtra("title", receipt.title)
                        intent.putExtra("date", receipt.date)
                        intent.putExtra("imageUrl", receipt.imageUrl)
                        intent.putExtra("description", receipt.description)
                        intent.putExtra("documentId", receipt.documentId)
                        startActivity(intent)
                    }
                }
                receiptsRecyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading receipts: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
}
