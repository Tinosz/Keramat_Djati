package com.example.keramat_djati

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SplitBillDisplayActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillItemAdapter
    private lateinit var textViewTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_bill_display)

        recyclerView = findViewById(R.id.recycler_view)
        textViewTotal = findViewById(R.id.textViewTotal)

        val recognizedText = intent.getStringExtra("recognizedText") ?: ""
        val items = RegexUtils.parseReceiptItems(recognizedText)
        val total = RegexUtils.parseTotal(recognizedText)

        adapter = BillItemAdapter(items)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        textViewTotal.text = "Total Amount: Rp ${String.format("%.2f", total)}"
    }
}
