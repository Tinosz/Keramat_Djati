package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.keramat_djati.transaction.TransactionViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionHistoryFragment : Fragment(), TransactionAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var viewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_history, container, false)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java] // Initialize ViewModel here

        recyclerView = view.findViewById(R.id.recyclerView_transactions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TransactionAdapter(listOf(), this) // 'this' refers to an OnItemClickListener now correctly
        recyclerView.adapter = adapter

        fetchFirstWalletAndTransactions() // Now it's safe to call this because viewModel is initialized

        return view
    }


    override fun onItemClick(transaction: Transaction) {
        context?.let {
            val intent = Intent(it, TransactionDetailActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            intent.putExtra("wallet_id", viewModel.walletId.value) // Include wallet ID
            it.startActivity(intent)
        } ?: Toast.makeText(context, "Context is not available", Toast.LENGTH_SHORT).show()
    }


    private fun fetchFirstWalletAndTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("accounts").document(userId)
            .collection("wallets")
            .limit(1)  // Limiting to only fetch the first wallet
            .get()
            .addOnSuccessListener { documents ->
                if (documents.documents.isNotEmpty()) {
                    val firstWallet = documents.documents.first()
                    viewModel.walletId.value = firstWallet.id  // Set the walletId in ViewModel
                    fetchTransactions(firstWallet.id)
                } else {
                    Toast.makeText(context, "No wallets found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch wallets: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTransactions(walletId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val accountRef = db.collection("accounts").document(userId).collection("wallets").document(walletId)

        val expensesRef = accountRef.collection("expenses").orderBy("date", Query.Direction.DESCENDING)
        val incomesRef = accountRef.collection("incomes").orderBy("date", Query.Direction.DESCENDING)

        expensesRef.addSnapshotListener { expensesSnapshot, expensesError ->
            incomesRef.addSnapshotListener { incomesSnapshot, incomesError ->
                if (expensesError != null || incomesError != null) {
                    Toast.makeText(context, "Error fetching transactions", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val transactions = (expensesSnapshot?.documents.orEmpty() + incomesSnapshot?.documents.orEmpty())
                    .map { doc ->
                        Transaction(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            category = if (doc.reference.path.contains("expenses")) "Expense" else "Income",
                            amount = doc.getLong("amount") ?: 0L,
                            date = doc.getString("date") ?: "",
                            time = doc.getString("time") ?: "",
                            note = doc.getString("note")
                        )
                    }
                    .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time })

                adapter.updateData(transactions)
            }
        }
    }

}

