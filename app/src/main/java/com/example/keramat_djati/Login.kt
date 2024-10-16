package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.keramat_djati.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

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
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    Toast.makeText(this, "Welcome $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CreateWalletActivity::class.java) // Change to the next activity
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
