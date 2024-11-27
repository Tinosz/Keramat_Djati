package com.example.keramat_djati

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class SplitBillDisplayActivity : AppCompatActivity() {
    private lateinit var textViewRecognizedText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill_display)

        textViewRecognizedText = findViewById(R.id.textViewRecognizedText)

        // Get the recognized text from the intent
        val recognizedText = intent.getStringExtra("recognizedText") ?: "No text recognized."
        textViewRecognizedText.text = recognizedText
    }
}