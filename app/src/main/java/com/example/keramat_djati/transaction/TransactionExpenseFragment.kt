package com.example.keramat_djati.transaction

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keramat_djati.MainActivity
import com.example.keramat_djati.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionExpenseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionExpenseFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: TransactionViewModel
    private val categories = mutableListOf<Category>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_expense, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadExpenseCategories()
        setupUIBindings(view)
        setupDateInput()

        view.findViewById<Button>(R.id.save_button).setOnClickListener {
            if (areRequiredFieldsFilled()) {
                (activity as? TransactionActivityHost)?.saveTransactionToFirestore()
            } else {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear the back stack
        startActivity(intent)
        requireActivity().finish() // Close the current activity to prevent the user from coming back to it
    }

    private fun areRequiredFieldsFilled(): Boolean {
        return viewModel.amount.value != null &&
                viewModel.amount.value != 0L &&  // Assuming 0 is not a valid amount
                viewModel.title.value != null &&
                viewModel.category.value != null &&
                viewModel.date.value != null
    }


    private fun setupUIBindings(view: View) {
        view.findViewById<EditText>(R.id.expense_amount).apply {
            afterTextChanged { viewModel.amount.value = it.toLongOrNull() ?: 0L }
        }
        view.findViewById<EditText>(R.id.expense_title).apply {
            afterTextChanged { viewModel.title.value = it }
        }
        view.findViewById<EditText>(R.id.expense_date).apply {
            afterTextChanged { viewModel.date.value = it }
        }
        view.findViewById<EditText>(R.id.expense_note).apply {
            afterTextChanged { viewModel.note.value = it }
        }
        view.findViewById<Spinner>(R.id.spinner_expense_categories).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                viewModel.category.value = selectedCategory.name
                viewModel.categoryId.value = selectedCategory.id
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    /**
     * Helper function to simplify setting text watchers that update LiveData.
     */
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

    private fun loadExpenseCategories() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val expenseCategoriesRef = db.collection("accounts").document(userId)
            .collection("categories").document("Expense")
            .collection("Details")

        categories.clear()

        val spinner: Spinner = requireView().findViewById(R.id.spinner_expense_categories)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        expenseCategoriesRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    val name = document.getString("name") ?: continue
                    val id = document.id
                    categories.add(Category(name, id))
                }
                adapter.clear()
                adapter.addAll(categories.map { it.name })
                adapter.notifyDataSetChanged()
            } else {
                Log.e("LoadExpenseCategories", "Failed to fetch categories: ${task.exception?.message}")
                Toast.makeText(context, "Failed to fetch categories", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun setupDateInput() {
        val dateEditText: EditText = view?.findViewById(R.id.expense_date) ?: return
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                // Note: monthOfYear is zero-based
                // Use Locale.US to ensure consistent date format behavior
                val dateString = String.format(Locale.US, "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
                dateEditText.setText(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dateEditText.setOnClickListener {
            datePickerDialog.show()
        }
    }

}