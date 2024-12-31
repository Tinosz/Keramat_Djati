package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddCategoryForm : AppCompatActivity() {
    private lateinit var categoryNameEditText: EditText
    private lateinit var categoryTypeSpinner: Spinner
    private lateinit var addButton: Button
    private lateinit var cancelButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val categoryTypes = mutableListOf("Income", "Expense")  // Default options

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_category_form)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        categoryNameEditText = findViewById(R.id.category_name)
        categoryTypeSpinner = findViewById(R.id.spinner_categories)
        addButton = findViewById(R.id.next_button)
        cancelButton = findViewById(R.id.cancel_button)

        // Populate spinner dynamically
        loadCategoryTypes()

        // Set button click listeners
        addButton.setOnClickListener {
            addCategoryToFirestore()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    // Load Category Types (Income/Expense) from Firestore
    private fun loadCategoryTypes() {
        val userId = currentUser?.uid ?: return

        db.collection("accounts").document(userId)
            .collection("categories")
            .get()
            .addOnSuccessListener { documents ->
                categoryTypes.clear()  // Clear existing list to avoid duplication

                for (document in documents) {
                    categoryTypes.add(document.id)  // Add "Income" or "Expense"
                }
                updateSpinner()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load category types. Using defaults.", Toast.LENGTH_SHORT).show()
                updateSpinner()  // Use default options if fetch fails
            }
    }

    // Update Spinner with Data
    private fun updateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryTypeSpinner.adapter = adapter
    }

    // Add Category to Firestore
    private fun addCategoryToFirestore() {
        val categoryName = categoryNameEditText.text.toString().trim()
        val categoryType = categoryTypeSpinner.selectedItem.toString()

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryRef = db.collection("accounts")
            .document(userId)
            .collection("categories")
            .document(categoryType)
            .collection("Details")

        val categoryData = hashMapOf(
            "name" to categoryName
        )

        categoryRef.add(categoryData)
            .addOnSuccessListener {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show()

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add category: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
