package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class CreateWalletActivity : AppCompatActivity() {
    private val viewModel: CreateWalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_wallet)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateWallet1())
                .commit()
        }
    }
    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    fun saveWallet(){
        val name = viewModel.walletName.value ?: "Unnamed Wallet"
        val amount = viewModel.walletAmount.value ?: 0L
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if(userId != null){
            val walletId = UUID.randomUUID().toString()
            val walletData = mapOf(
                "name" to name,
                "amount" to amount
            )
            FirebaseFirestore.getInstance().collection("accounts").document(userId)
                .collection("wallets").document(walletId)
                .set(walletData)
                .addOnSuccessListener{
                    Toast.makeText(this, "Wallet Created Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create wallet: ${it.message}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

}

