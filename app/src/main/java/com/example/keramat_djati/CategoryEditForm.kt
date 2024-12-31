package com.example.keramat_djati

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CategoryEditForm : AppCompatActivity() {

    private lateinit var categoryNameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private val db = FirebaseFirestore.getInstance()
    private var categoryType: String? = null  // Income or Expense
    private var categoryId: String? = null  // Firestore ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_edit_form)

        categoryNameEditText = findViewById(R.id.category_name)
        saveButton = findViewById(R.id.next_button)
        cancelButton = findViewById(R.id.cancel_button)

        // Retrieve passed data (Category name, type, and ID)
        categoryType = intent.getStringExtra("categoryType")  // Income or Expense
        categoryId = intent.getStringExtra("categoryId")
        val categoryName = intent.getStringExtra("categoryName")

        // Prefill category name if available
        categoryNameEditText.setText(categoryName)

        // Save Button Click - Update Category
        saveButton.setOnClickListener {
            updateCategory()
        }

        // Cancel Button - Close Activity
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun updateCategory() {
        val newCategoryName = categoryNameEditText.text.toString()

        if (newCategoryName.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        categoryId?.let { id ->
            categoryType?.let { type ->
                // Firestore path: accounts > userId > categories > Income/Expense > details > categoryId
                db.collection("accounts").document(userId)
                    .collection("categories").document(type)
                    .collection("Details").document(id)
                    .update("name", newCategoryName)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show()
                        finish()  // Return to previous screen
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update category", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }
}
