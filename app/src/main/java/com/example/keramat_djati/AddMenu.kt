package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.keramat_djati.databinding.ActivityAddMenuBinding
import com.example.keramat_djati.transaction.TransactionActivityHost

class AddMenu : AppCompatActivity() {

    private lateinit var binding: ActivityAddMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners for the buttons
        binding.addWalletButton.setOnClickListener {
            val intent = Intent(this, CreateWalletActivity::class.java)
            startActivity(intent)
        }

        binding.addCategoryButton.setOnClickListener {
            val intent = Intent(this, addCategory::class.java)
            startActivity(intent)
        }

        binding.addTransactionButton.setOnClickListener {
            val intent = Intent(this, TransactionActivityHost::class.java)
            startActivity(intent)
        }
    }
}
