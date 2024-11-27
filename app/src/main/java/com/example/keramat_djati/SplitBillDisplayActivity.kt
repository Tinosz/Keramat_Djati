package com.example.keramat_djati

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SplitBillDisplayActivity : AppCompatActivity() {
    private lateinit var textViewRecognizedText: TextView
    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var adapter: ReceiptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill_display)

        textViewRecognizedText = findViewById(R.id.textViewRecognizedText)
        recyclerViewItems = findViewById(R.id.recyclerViewItems)

        // Set up RecyclerView
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        adapter = ReceiptAdapter(listOf())  // Initialize with empty list
        recyclerViewItems.adapter = adapter

        // Get the recognized text and receipt items from the intent
        val recognizedText = intent.getStringExtra("recognizedText") ?: "No text recognized."
        textViewRecognizedText.text = recognizedText

        val receiptItems: List<ReceiptItem> = intent.getParcelableArrayListExtra("receiptItems") ?: emptyList()

        // Update the RecyclerView with the receipt items
        adapter.updateData(receiptItems)
    }
}
