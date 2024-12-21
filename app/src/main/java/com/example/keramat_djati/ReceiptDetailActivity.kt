package com.example.keramat_djati

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ReceiptDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt_detail)

        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val imageUrl = intent.getStringExtra("imageUrl")
        val description = intent.getStringExtra("description")

        val titleTextView: TextView = findViewById(R.id.editTextTitleDetail)
        val dateTextView: TextView = findViewById(R.id.editTextDateDetail)
        val descriptionTextView: TextView = findViewById(R.id.editTextDescriptionDetail)
        val imageView: ImageView = findViewById(R.id.imageViewDetail)

        titleTextView.text = title
        dateTextView.text = date
        descriptionTextView.text = description

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.baseline_android_24)
            .error(R.drawable.baseline_error_24)
            .into(imageView)
    }
}
