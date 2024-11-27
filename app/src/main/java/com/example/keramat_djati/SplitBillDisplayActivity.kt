package com.example.keramat_djati

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplitBillDisplayActivity : AppCompatActivity() {
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill_display)

        textView = findViewById(R.id.textView_recognized_text)
        val recognizedText = intent.getStringExtra("recognizedText")
        textView.text = recognizedText ?: "No text recognized."
    }
}