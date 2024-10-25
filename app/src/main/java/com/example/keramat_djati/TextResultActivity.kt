package com.example.keramat_djati

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TextResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_result)

        val textView: TextView = findViewById(R.id.textView)
        val recognizedText = intent.getStringExtra("recognized_text")
        textView.text = recognizedText ?: "No text recognized"
    }
}