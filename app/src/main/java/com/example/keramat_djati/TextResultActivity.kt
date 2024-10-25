package com.example.keramat_djati

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class TextResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_result)

        val textView = findViewById<TextView>(R.id.textViewResult)
        val detectedText = intent.getStringExtra("DetectedText")
        textView.text = detectedText ?: "No text detected."
    }
}
