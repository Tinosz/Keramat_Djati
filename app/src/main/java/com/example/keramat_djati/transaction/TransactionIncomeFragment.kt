package com.example.keramat_djati.transaction

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.keramat_djati.CreateWalletActivity
import com.example.keramat_djati.MainActivity
import com.example.keramat_djati.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionIncomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionIncomeFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_transaction_income, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadIncomeCategories()
        setupDateInput()
        setupUIBindings(view)

        // Prefill the form with ViewModel data (if available)
        view.findViewById<EditText>(R.id.income_title).setText(viewModel.title.value)
        view.findViewById<EditText>(R.id.income_note).setText(viewModel.note.value)
        view.findViewById<EditText>(R.id.income_date).setText(viewModel.date.value)
        view.findViewById<EditText>(R.id.income_amount).setText(viewModel.amount.value?.toString())

        val spinnerCategories = view.findViewById<Spinner>(R.id.spinner_income_categories)
        val category = viewModel.categoryType.value
        val position = (spinnerCategories.adapter as ArrayAdapter<String>).getPosition(category)
        spinnerCategories.setSelection(position)

        view.findViewById<Button>(R.id.save_button).setOnClickListener {
            if (areRequiredFieldsFilled()) {
                viewModel.setCurrentDateTime()
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
        view.findViewById<EditText>(R.id.income_amount).apply {
            afterTextChanged { viewModel.amount.value = it.toLongOrNull() ?: 0L }
        }
        view.findViewById<EditText>(R.id.income_title).apply {
            afterTextChanged { viewModel.title.value = it }
        }
        view.findViewById<EditText>(R.id.income_date).apply {
            afterTextChanged { viewModel.date.value = it }
        }
        view.findViewById<EditText>(R.id.income_note).apply {
            afterTextChanged { viewModel.note.value = it }
        }
        view.findViewById<Spinner>(R.id.spinner_income_categories).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

    private fun loadIncomeCategories() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val incomeCategoriesRef = db.collection("accounts").document(userId)
            .collection("categories").document("Income")
            .collection("Details")

        categories.clear()  // Clear existing categories to avoid duplication

        val spinner: Spinner = requireView().findViewById(R.id.spinner_income_categories)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        incomeCategoriesRef.get().addOnCompleteListener { task ->
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
                Log.e("LoadIncomeCategories", "Failed to fetch categories: ${task.exception?.message}")
                Toast.makeText(context, "Failed to fetch categories", Toast.LENGTH_SHORT).show()
            }
        }
    }




    private fun setupDateInput() {
        val dateEditText: EditText = view?.findViewById(R.id.income_date) ?: return
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