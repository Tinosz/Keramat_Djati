package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.keramat_djati.transaction.TransactionActivityHost
import com.example.keramat_djati.transaction.TransactionViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class TransactionHistoryFragment : Fragment(), TransactionAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var viewModel: TransactionViewModel
    private lateinit var walletViewModel: WalletViewModel
    private lateinit var walletAdapter: WalletAdapter
    private lateinit var walletRecyclerView: RecyclerView
    private lateinit var snapHelper: PagerSnapHelper

    private var incomeListener: ListenerRegistration? = null
    private var expenseListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_history, container, false)

        viewModel = ViewModelProvider(this)[TransactionViewModel::class.java]
        walletViewModel = ViewModelProvider(this)[WalletViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerView_transactions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(listOf(), this)
        recyclerView.adapter = transactionAdapter

        walletRecyclerView = view.findViewById(R.id.recyclerView_wallets)
        walletRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(walletRecyclerView)

        walletAdapter = WalletAdapter(listOf())
        walletRecyclerView.adapter = walletAdapter


        walletRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = walletRecyclerView.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()

                val activeWallet = walletAdapter.getItemAtPosition(currentPosition)
                viewModel.walletId.value = activeWallet.id
                fetchTransactions(activeWallet.id)
            }
        })

        walletViewModel.getWalletData().observe(viewLifecycleOwner, Observer { walletDetails ->
            updateWalletsList(walletDetails)
        })

        viewModel.walletId.observe(viewLifecycleOwner, Observer { walletId ->
            if (walletId != null) {
                Log.d("TransactionHistoryFragment", "Current Wallet ID: $walletId")
            } else {
                Log.d("TransactionHistoryFragment", "Wallet ID is null")
            }
        }
        )


        fetchFirstWalletAndTransactions()

        fetchWalletsInRealTime()


        val fabAddClickable = view.findViewById<FloatingActionButton>(R.id.fabAddCLickablet)
        fabAddClickable.setOnClickListener {
            val intent = Intent(activity, AddMenu::class.java)
            startActivity(intent)
        }



        return view
    }



    override fun onResume(){
        super.onResume()
        walletViewModel.fetchWalletData()
    }

    private fun fetchFirstWalletAndTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("accounts").document(userId)
            .collection("wallets")
            .get()
            .addOnSuccessListener { documents ->
                val walletDetails = documents.documents.map { doc ->
                    WalletDetail(
                        id = doc.id,  // Document ID as the wallet ID
                        name = doc.getString("name") ?: "Unnamed Wallet",
                        balance = doc.getLong("amount") ?: 0L
                    )
                }

                updateWalletsList(walletDetails)

                val firstWallet = documents.documents.first()
                fetchTransactions(firstWallet.id)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch wallets: $exception", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateWalletsList(wallets: List<WalletDetail>) {
        walletAdapter = WalletAdapter(wallets)
        walletRecyclerView.adapter = walletAdapter
        walletAdapter.updateWallets(wallets)
    }

    private var walletListener: ListenerRegistration? = null

    private fun fetchWalletsInRealTime() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        walletListener?.remove()  // Remove the existing listener before adding a new one

        walletListener = db.collection("accounts").document(userId)
            .collection("wallets")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to listen for wallet changes.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshots?.let {
                    val walletDetails = it.documents.map { doc ->
                        WalletDetail(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unnamed Wallet",
                            balance = doc.getLong("amount") ?: 0L
                        )
                    }
                    updateWalletsList(walletDetails)

                    if (walletDetails.isNotEmpty()) {
                        fetchTransactions(walletDetails.first().id)
                    }
                }
            }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firestore listeners when the fragment is destroyed
        incomeListener?.remove()
        expenseListener?.remove()
        walletListener?.remove()  // Clean up listener when fragment is destroyed

    }

    private fun fetchTransactions(walletId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val accountRef = db.collection("accounts").document(userId).collection("wallets").document(walletId)

        val expensesRef = accountRef.collection("expenses").orderBy("date", Query.Direction.DESCENDING)
        val incomesRef = accountRef.collection("incomes").orderBy("date", Query.Direction.DESCENDING)

        expenseListener = expensesRef.addSnapshotListener { expensesSnapshot, expensesError ->
            incomeListener = incomesRef.addSnapshotListener { incomesSnapshot, incomesError ->
                if (expensesError != null || incomesError != null) {
                    return@addSnapshotListener
                }

                val transactions =
                    (expensesSnapshot?.documents.orEmpty() + incomesSnapshot?.documents.orEmpty())
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

                transactionAdapter.updateTransactions(transactions)
            }
        }
    }


    override fun onItemClick(transaction: Transaction) {

        val walletId = viewModel.walletId.value
        context?.let {
            val intent = Intent(it, TransactionDetailActivity::class.java)
            intent.putExtra("transaction_id", transaction.id)
            intent.putExtra("wallet_id", walletId)
            Log.d("TransactionHistoryFragment", "Wallet ID: ${walletId}")
            Log.d("TransactionHistoryFragment", "Transaction ID: ${transaction.id}")
            it.startActivity(intent)
        } ?: Toast.makeText(context, "Context is not available", Toast.LENGTH_SHORT).show()
    }
}
