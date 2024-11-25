package com.example.keramat_djati

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonToSplitBill = findViewById<Button>(R.id.button_to_split_bill)
        buttonToSplitBill.setOnClickListener {
            val intent = Intent(this, SplitBillActivity::class.java)
            startActivity(intent)
        }
    }
}
