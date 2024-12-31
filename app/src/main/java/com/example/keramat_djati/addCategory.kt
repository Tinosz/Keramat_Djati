package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.keramat_djati.com.example.keramat_djati.Category
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class addCategory : AppCompatActivity() {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var incomeToggleButton: Button
    private lateinit var expenseToggleButton: Button
    private lateinit var categoryAdapter: CategoryAdapter
    private val db = FirebaseFirestore.getInstance()
    private var isIncomeSelected = true  // Track current state
    private lateinit var addButton: Button
    private lateinit var backButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)  // This inflates the layout

        // Set edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.categoryList)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI Elements
        categoryRecyclerView = findViewById(R.id.category_recycler_view)
        incomeToggleButton = findViewById(R.id.income_toggle_button)
        expenseToggleButton = findViewById(R.id.expense_toggle_button)


        val greenColor = ContextCompat.getColor(this, R.color.main_green)
        val greyColor = ContextCompat.getColor(this, R.color.grey)

        categoryRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter
        categoryAdapter = CategoryAdapter(emptyList(), ::onEditClick, ::onDeleteClick)
        categoryRecyclerView.adapter = categoryAdapter


        loadCategories("Income")
        toggleButtonUI(true)

        incomeToggleButton.setOnClickListener {
            if (!isIncomeSelected) {
                isIncomeSelected = true
                toggleButtonUI(true)
                loadCategories("Income")
            }
        }

        expenseToggleButton.setOnClickListener {
            if (isIncomeSelected) {
                isIncomeSelected = false
                toggleButtonUI(false)
                loadCategories("Expense")
            }
        }

        // Add Button - Navigate to Add Category Form
        addButton = findViewById(R.id.add_button)
        addButton.setOnClickListener {
            val intent = Intent(this, AddCategoryForm::class.java)
            startActivity(intent)
        }

        // Cancel Button - Return to Main Activity
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Change Button UI
    private fun toggleButtonUI(isIncomeSelected: Boolean) {
        val greenColor = ContextCompat.getColor(this, R.color.main_green)
        val greyColor = ContextCompat.getColor(this, R.color.grey)

        if (isIncomeSelected) {
            incomeToggleButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.main_green)
            expenseToggleButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey)
        } else {
            incomeToggleButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey)
            expenseToggleButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.main_green)
        }
    }


    // Real-time category loading
    private fun loadCategories(type: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("accounts").document(userId)
            .collection("categories").document(type)
            .collection("Details")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("addCategory", "Failed to listen for $type categories", error)
                    return@addSnapshotListener
                }

                // Map Firestore documents to Category objects
                val categories = snapshot?.documents?.map { doc ->
                    Category(
                        doc.id,
                        doc.getString("name") ?: "Unnamed",
                        type
                    )
                } ?: emptyList()

                // Update RecyclerView with new data
                categoryAdapter.updateData(categories)
            }
    }


    // Handle Edit Action
    private fun onEditClick(category: Category) {
        val intent = Intent(this, CategoryEditForm::class.java)
        intent.putExtra("categoryType", category.type)
        intent.putExtra("categoryId", category.id)
        intent.putExtra("categoryName", category.name)
        startActivity(intent)
    }



    // Handle Delete Action
    private fun onDeleteClick(category: Category) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("accounts").document(userId)
            .collection("categories").document(category.type)
            .collection("Details").document(category.id)
            .delete()
            .addOnSuccessListener {
                Log.d("addCategory", "${category.name} deleted")
                loadCategories(category.type)  // Refresh after delete
            }
            .addOnFailureListener {
                Log.e("addCategory", "Failed to delete ${category.name}")
            }
    }
}
