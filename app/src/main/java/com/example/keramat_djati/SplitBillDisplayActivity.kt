package com.example.keramat_djati

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SplitBillDisplayActivity : AppCompatActivity() {
    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var adapter: ReceiptAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill_display)

        recyclerViewItems = findViewById(R.id.recyclerViewItems)

        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        adapter = ReceiptAdapter(listOf())  // Initialize with empty list
        recyclerViewItems.adapter = adapter


        val receiptItems: ArrayList<ReceiptItem> = intent.getParcelableArrayListExtra("receiptItems") ?: arrayListOf()

        // Update the RecyclerView with the receipt items
        adapter.updateData(receiptItems)
    }
}