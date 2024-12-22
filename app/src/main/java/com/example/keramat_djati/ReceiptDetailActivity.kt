package com.example.keramat_djati

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReceiptDetailActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var imageView: ImageView
    private lateinit var buttonSaveChanges: Button
    private var documentId: String? = null // Document ID is passed when starting this activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_detail)

        editTextTitle = findViewById(R.id.editTextTitleDetail)
        editTextDate = findViewById(R.id.editTextDateDetail)
        editTextDescription = findViewById(R.id.editTextDescriptionDetail)
        imageView = findViewById(R.id.imageViewDetail)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)

        // Retrieve the passed data from the intent
        intent.extras?.let {
            editTextTitle.setText(it.getString("title"))
            editTextDate.setText(it.getString("date"))
            editTextDescription.setText(it.getString("description"))
            Glide.with(this).load(it.getString("imageUrl")).into(imageView)
            documentId = it.getString("documentId")
        }

        buttonSaveChanges.setOnClickListener {
            documentId?.let {
                updateReceipt()
            } ?: Toast.makeText(this, "Document ID is missing, cannot update.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateReceipt() {
        val title = editTextTitle.text.toString()
        val date = editTextDate.text.toString()
        val description = editTextDescription.text.toString()

        val receiptUpdates = hashMapOf<String, Any>(
            "title" to title,
            "date" to date,
            "description" to description
        )

        documentId?.let { docId ->
            FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                FirebaseFirestore.getInstance()
                    .collection("accounts")
                    .document(userId)
                    .collection("savedreceipt")
                    .document(docId)
                    .update(receiptUpdates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Receipt updated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update receipt: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } ?: Toast.makeText(this, "Authentication failed, user ID is null.", Toast.LENGTH_LONG).show()
        } ?: Toast.makeText(this, "Document ID is null, cannot perform update.", Toast.LENGTH_LONG).show()
    }
}
