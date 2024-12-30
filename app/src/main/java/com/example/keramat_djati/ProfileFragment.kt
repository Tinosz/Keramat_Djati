package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.compose.material3.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var logoutButton: Button

    private var incomeListener: ListenerRegistration? = null
    private var expenseListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the logout button
        logoutButton = view.findViewById(R.id.logout_button)

        // Set up click listener for logout
        logoutButton.setOnClickListener {
            logout()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            fetchAllTransactions()
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchAllTransactions()
    }




    private fun fetchAllTransactions() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val accountRef = db.collection("accounts").document(userId).collection("wallets")

        accountRef.get().addOnSuccessListener { walletSnapshots ->
            val allTransactions = mutableListOf<Transaction>()
            var walletsProcessed = 0

            walletSnapshots.documents.forEach { walletDoc ->
                val walletId = walletDoc.id
                val walletRef = accountRef.document(walletId)

                val expensesRef = walletRef.collection("expenses").orderBy("date", Query.Direction.ASCENDING)
                val incomesRef = walletRef.collection("incomes").orderBy("date", Query.Direction.ASCENDING)

                expensesRef.get().addOnSuccessListener { expensesSnapshot ->
                    incomesRef.get().addOnSuccessListener { incomesSnapshot ->

                        val transactions = (expensesSnapshot.documents + incomesSnapshot.documents).map { doc ->
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

                        allTransactions.addAll(transactions)
                        walletsProcessed++

                        // Check if the fragment view exists before updating the UI
                        if (view != null && walletsProcessed == walletSnapshots.size()) {
                            plotTransactionsOnChart(allTransactions.sortedWith(compareBy<Transaction> { it.date }.thenBy { it.time }))
                            updateIncomeExpenseBoxes(allTransactions)
                        } else {
                            Log.e("ProfileFragment", "View not created yet.")
                        }
                    }
                }
            }
        }
    }


    private fun updateIncomeExpenseBoxes(transactions: List<Transaction>) {
        // Calculate total income and expenses
        val totalIncome = transactions
            .filter { it.category == "Income" }
            .sumOf { it.amount }

        val totalExpense = transactions
            .filter { it.category == "Expense" }
            .sumOf { it.amount }

        // Format the numbers with commas and currency
        val formattedIncome = "Rp. " + String.format("%,d", totalIncome)
        val formattedExpense = "Rp. " + String.format("%,d", totalExpense)

        // Find views for income and expense boxes
        val incomeTextView = view?.findViewById<TextView>(R.id.income_text) ?: return
        val expenseTextView = view?.findViewById<TextView>(R.id.expense_text) ?: return

        // Update text in the boxes
        incomeTextView.text = "+ $formattedIncome"
        expenseTextView.text = "- $formattedExpense"

        // Set the text color (green for income, red for expense)
        incomeTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_green))
        expenseTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
    }



    private fun plotTransactionsOnChart(transactions: List<Transaction>) {
        val lineChart: LineChart = view?.findViewById(R.id.line_chart) ?: return


        val entries = mutableListOf<Entry>()
        var cumulativeBalance = 0f

        val dailyBalances = transactions.groupBy { it.date }
            .mapValues { (_, trans) -> trans.sumByLong { if (it.category == "Income") it.amount else -it.amount } }
            .toSortedMap()

        dailyBalances.entries.forEachIndexed { index, (_, balance) ->
            cumulativeBalance += balance
            entries.add(Entry(index.toFloat(), cumulativeBalance))
        }

        val dataSet = LineDataSet(entries, "Income/Expense Flow").apply {
            color = ContextCompat.getColor(requireContext(), R.color.main_green)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.main_green))
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.main_green)
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }


    // Extension function to sum Long
    private fun List<Transaction>.sumByLong(selector: (Transaction) -> Long): Long {
        return this.fold(0L) { acc, transaction -> acc + selector(transaction) }
    }



    private fun logout() {
        // Log out the user using FirebaseAuth
        FirebaseAuth.getInstance().signOut()

        // Redirect to the Login activity
        val intent = Intent(requireContext(), Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Optionally, finish the current fragment's parent activity
        activity?.finish()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}