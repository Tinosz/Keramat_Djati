package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.keramat_djati.databinding.ActivityLoginBinding
import com.example.keramat_djati.transaction.TransactionActivityHost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener{
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            //rules
            if(email.isEmpty()){
                binding.loginEmail.error = "Email is required"
                binding.loginEmail.requestFocus()
                return@setOnClickListener
            }

            if(password.isEmpty()){
                binding.loginPassword.error = "Password is required"
                binding.loginPassword.requestFocus()
                return@setOnClickListener
            }

            loginFirebase(email, password)
        }



        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the TextView for "Register"
        val registerTextView = findViewById<TextView>(R.id.register_text)

        // Create a SpannableString for the text
        val spannableString = SpannableString("Don't have an account? Register here.")

        // Define a clickable span for "Register"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to the RegisterActivity when "Register" is clicked
                val intent = Intent(this@Login, Register::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.main_green)  // Set "Register" text to green
                ds.isUnderlineText = true  // Optional: Remove underline
            }
        }

        // Apply the clickable span to the "Register" part of the text
        spannableString.setSpan(clickableSpan, 23, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the spannable string to the TextView
        registerTextView.text = spannableString
        registerTextView.movementMethod = LinkMovementMethod.getInstance() // Make the link clickable
    }

    private fun loginFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    checkAndInitializedUserData(userId, email)
                } else {
                    Log.d("LoginError", "Login failed: ${task.exception?.message}")
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkAndInitializedUserData(userId: String, email: String) {
        val userDocRef = FirebaseFirestore.getInstance().collection("accounts").document(userId)
        val walletsRef = userDocRef.collection("wallets")

        // First, check if there are any wallets
        walletsRef.get().addOnCompleteListener { walletTask ->
            if (walletTask.isSuccessful) {
                val walletSnapshot = walletTask.result
                if (!walletSnapshot.isEmpty) {
                    // Wallets exist, navigate to MainActivity
                    Log.d("LoginFlow", "Returning user with wallets, navigate to MainActivity")
                    startActivity(Intent(this, TransactionActivityHost::class.java))
                    finish()
                } else {
                    // No wallets found, now check user document for first login flag
                    userDocRef.get().addOnSuccessListener { document ->
                        if (document.exists() && document.getBoolean("isFirstLogin") == true) {
                            // Handle first-time login
                            userDocRef.update("isFirstLogin", false).addOnSuccessListener {
                                Log.d("LoginFlow", "First login, navigate to CreateWalletActivity")
                                startActivity(Intent(this, CreateWalletActivity::class.java))
                                finish()
                            }
                        } else {
                            // No wallets and not first login, still go to CreateWalletActivity
                            Log.d("LoginFlow", "No wallets found, navigate to CreateWalletActivity")
                            startActivity(Intent(this, CreateWalletActivity::class.java))
                            finish()
                        }
                    }.addOnFailureListener {
                        Log.e("LoginFlow", "Failed to get user document: ${it.message}")
                        Toast.makeText(this, "Error checking user status", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("LoginFlow", "Failed to check wallets: ${walletTask.exception?.message}")
                Toast.makeText(this, "Error checking wallets", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
