package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.keramat_djati.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Register : AppCompatActivity() {

    lateinit var binding: ActivityRegisterBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()  // Initialize Firestore

        binding.registerButton.setOnClickListener {
            val email = binding.registerEmail.text.toString()
            val password = binding.registerPassword.text.toString()
            val passwordConfirmation = binding.registerPasswordConfirmation.text.toString()

            // Validation rules
            if (email.isEmpty()) {
                binding.registerEmail.error = "Email is required"
                binding.registerEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.registerEmail.error = "Please enter a valid email"
                binding.registerEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.registerPassword.error = "Password is required"
                binding.registerPassword.requestFocus()
                return@setOnClickListener
            }

            if (passwordConfirmation.isEmpty()) {
                binding.registerPasswordConfirmation.error = "Password confirmation is required"
                binding.registerPasswordConfirmation.requestFocus()
                return@setOnClickListener
            }

            if (password != passwordConfirmation) {
                binding.registerPassword.error = "Password does not match"
                binding.registerPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.registerPassword.error = "Password must be at least 6 characters"
                binding.registerPassword.requestFocus()
                return@setOnClickListener
            }

            registerFirebase(email, password)
        }

        val loginTextView = findViewById<TextView>(R.id.login_text)
        val spannableString = SpannableString("Already have an account? Login here.")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Register, Login::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.main_green)
                ds.isUnderlineText = true
            }
        }
        spannableString.setSpan(clickableSpan, 25, 36, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginTextView.text = spannableString
        loginTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    // Function to register user and create Firestore account entry
    private fun registerFirebase(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid  // Get UID of the registered user
                        createFirestoreAccount(userId, email)  // Create Firestore entry
                    }
                    Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Login::class.java))
                } else {
                    Toast.makeText(this, "Register Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to create account document in Firestore
    private fun createFirestoreAccount(userId: String, email: String) {
        val accountData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("accounts").document(userId)
            .set(accountData)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                initializeCategories(userId)  // Initialize categories right after account creation
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeCategories(userId: String) {
        val db = FirebaseFirestore.getInstance()

        val expenseCategories = listOf(
            "Groceries",
            "Bills",
            "Transportation",
            "Entertainment",
            "Health",
            "Education"
        )

        val incomeCategories = listOf(
            "Salary",
            "Gifts",
            "Investments",
            "Sales",
            "Other"
        )

        // Create the Expense and Income documents first with a generic field to ensure they exist
        val expenseDocData = mapOf("type" to "Expense")
        val incomeDocData = mapOf("type" to "Income")

        // Create or update 'Expense' document before adding details
        db.collection("accounts").document(userId).collection("categories").document("Expense")
            .set(expenseDocData, SetOptions.merge())
            .addOnSuccessListener {
                // Now add each expense category
                expenseCategories.forEach { categoryName ->
                    val categoryData = mapOf("name" to categoryName)
                    db.collection("accounts").document(userId).collection("categories")
                        .document("Expense").collection("Details").add(categoryData)
                        .addOnSuccessListener {
                            Log.d("InitCategories", "Expense category created successfully with ID: ${it.id}")
                        }
                        .addOnFailureListener {
                            Log.e("InitCategories", "Failed to create expense category: ${it.message}")
                        }
                }
            }

        db.collection("accounts").document(userId).collection("categories").document("Income")
            .set(incomeDocData, SetOptions.merge())
            .addOnSuccessListener {
                // Now add each income category
                incomeCategories.forEach { categoryName ->
                    val categoryData = mapOf("name" to categoryName)
                    db.collection("accounts").document(userId).collection("categories")
                        .document("Income").collection("Details").add(categoryData)
                        .addOnSuccessListener {
                            Log.d("InitCategories", "Income category created successfully with ID: ${it.id}")
                        }
                        .addOnFailureListener {
                            Log.e("InitCategories", "Failed to create income category: ${it.message}")
                        }
                }
            }
    }
}
