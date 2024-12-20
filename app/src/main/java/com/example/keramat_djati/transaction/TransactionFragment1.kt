package com.example.keramat_djati.transaction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keramat_djati.MainActivity
import com.example.keramat_djati.R
import com.example.keramat_djati.splitbill.SplitBillActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionFragment1.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionFragment1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transaction1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadWallets()
        setupCategorySpinner()



        val spinnerCategories = view.findViewById<Spinner>(R.id.spinner_categories)
        val category = viewModel.categoryType.value
        val position = (spinnerCategories.adapter as ArrayAdapter<String>).getPosition(category)
        spinnerCategories.setSelection(position)

        view.findViewById<Button>(R.id.camera_button).setOnClickListener{
            navigateToCamera()
        }

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            navigateToMainActivity()
        }

        view.findViewById<Button>(R.id.next_button).setOnClickListener {
            val selectedCategory = viewModel.categoryType.value
            when (selectedCategory) {
                "Expense" -> (activity as? TransactionActivityHost)?.replaceFragment(TransactionExpenseFragment())
                "Income" -> (activity as? TransactionActivityHost)?.replaceFragment(TransactionIncomeFragment())
                else -> Toast.makeText(context, "Invalid category selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToCamera(){
        val intent = Intent(requireContext(), SplitBillActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }


    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        requireActivity().finish()
    }


    data class WalletItem(val name: String, val id: String)

    fun loadWallets() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val spinnerWallet: Spinner = view?.findViewById(R.id.spinner_wallet) ?: return
        val db = FirebaseFirestore.getInstance()
        val walletsRef = db.collection("accounts").document(userId).collection("wallets")

        val walletItems = ArrayList<WalletItem>() // This will hold our wallets
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, walletItems.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWallet.adapter = adapter

        walletsRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                walletItems.clear()  // Clear existing data to avoid duplicates
                task.result.forEach { document ->
                    val walletName = document.getString("name") ?: "Unnamed Wallet"
                    val walletId = document.id
                    walletItems.add(WalletItem(walletName, walletId))
                }

                adapter.clear()
                adapter.addAll(walletItems.map { it.name })  // Update adapter with new data
                adapter.notifyDataSetChanged()  // Notify the adapter of data changes

                // Check if it's the first load (if oldWalletId is null or empty)
                val currentWalletId = viewModel.walletId.value
                // First, check if oldWalletId is null or empty, meaning it's the first load
                if (currentWalletId.isNullOrEmpty()) {
                    // If no walletId in ViewModel, this is the first load, set it from the selected wallet
                    if (walletItems.isNotEmpty()) {
                        val selectedWallet = walletItems[0]
                        viewModel.walletId.value =
                            selectedWallet.id  // Set the selected wallet ID in ViewModel
                        viewModel.oldWalletId.value =
                            selectedWallet.id  // Set the oldWalletId to the same ID
                        spinnerWallet.setSelection(0)  // Set the spinner to the first item
                    }
                } else {
                    // If there is an existing walletId, set the spinner selection to the wallet ID from ViewModel
                    val selectedWalletIndex = walletItems.indexOfFirst { it.id == currentWalletId }
                    if (selectedWalletIndex != -1) {
                        spinnerWallet.setSelection(selectedWalletIndex)
                    } else {
                        spinnerWallet.setSelection(0)  // If no match, default to the first item
                    }
                }

// Listen for spinner item selection (only when user selects a different wallet)
                spinnerWallet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedWallet = walletItems[position]
                        // If the wallet is different from the old one, update both walletId and oldWalletId
                        if (viewModel.walletId.value != selectedWallet.id) {
                            viewModel.oldWalletId.value =
                                viewModel.walletId.value  // Store the previous wallet ID
                            viewModel.walletId.value =
                                selectedWallet.id  // Update with the new wallet ID
                        }
                    }

                    override fun onNothingSelected(parentView: AdapterView<*>?) {
                        // Handle case when no item is selected
                    }
                }

            }
        }
    }


    fun setupCategorySpinner() {
        val categories = listOf("Income", "Expense") // Ensure consistent naming: Income, Expense
        val spinnerCategories: Spinner = view?.findViewById(R.id.spinner_categories) ?: return

        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategories.adapter = categoryAdapter

        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                viewModel.categoryType.value = selectedCategory // Store the selected category type in ViewModel
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                viewModel.categoryType.value = "Income" // Default to Income if nothing is selected
            }
        }
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TransactionFragment1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TransactionFragment1().apply {
                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
                }
            }
    }
}