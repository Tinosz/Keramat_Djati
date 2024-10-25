package com.example.keramat_djati

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateWallet2.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateWallet2 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var walletBalanceEditText: EditText
    private val viewModel : CreateWalletViewModel by activityViewModels()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

        val view = inflater.inflate(R.layout.fragment_create_wallet2, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        val doneButton = view.findViewById<Button>(R.id.done_button)

        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }


        walletBalanceEditText = view.findViewById(R.id.wallet_name_input)




        walletBalanceEditText.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    walletBalanceEditText.removeTextChangedListener(this)

                    // Clean up the input to remove non-numeric characters (like commas)
                    val cleanString = s.toString().replace(",", "")

                    if (cleanString.isNotEmpty()) {
                        // Parse the clean string to a long number
                        val parsed = cleanString.toLong()

                        // Format the number with commas
                        val formatted = formatNumber(parsed)

                        // Set the formatted text and move the cursor to the end
                        current = formatted
                        walletBalanceEditText.setText(formatted)
                        walletBalanceEditText.setSelection(formatted.length)
                    }

                    walletBalanceEditText.addTextChangedListener(this)
                }
            }
        })
        doneButton.setOnClickListener{
            saveWalletToFireStore()
        }

        return view

    }

    private fun formatNumber(number: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val decimalFormat = DecimalFormat("#,###", symbols)
        return decimalFormat.format(number)
    }

    private fun saveWalletToFireStore(){
        val walletName = viewModel.walletName.value ?: "Unnamed Wallet"
        val walletAmount = getPlainWalletBalance()

        val userId = auth.currentUser?.uid
        if (userId != null){
            val walletId = UUID.randomUUID().toString()

            val walletData = mapOf(
                "name" to walletName,
                "amount" to walletAmount
            )

            db.collection("accounts").document(userId)
                .collection("wallets").document(walletId)
                .set(walletData)
                .addOnSuccessListener{
                    Toast.makeText(requireContext(), "Wallet created successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish() // Close CreateWalletActivity
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to create wallet: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    fun getPlainWalletBalance(): Long {
        val rawInput = walletBalanceEditText.text.toString()
        return try {
            // Remove commas and any non-numeric characters, then parse the clean string to Long
            rawInput.replace(",", "").toLong()
        } catch (e: NumberFormatException) {
            0L // Return 0 if the input is invalid
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateWallet2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateWallet2().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}