package com.example.keramat_djati

import android.annotation.SuppressLint
import android.widget.Button

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.keramat_djati.R
import com.example.keramat_djati.transaction.TransactionActivityHost

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController(R.id.nav_host_fragment)

        // Setting the listener directly to handle specific navigation logic
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.transactionHistoryFragment, R.id.profileFragment -> {
                    // Uses the Navigation Component to navigate between fragments
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
                R.id.addTransactionFragment -> {
                    startActivity(Intent(this, TransactionActivityHost::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
