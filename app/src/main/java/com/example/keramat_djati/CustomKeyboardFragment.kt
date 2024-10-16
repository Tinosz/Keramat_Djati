package com.example.keramat_djati

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CustomKeyboardFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    // This is the EditText that the keyboard will be linked to
    private lateinit var targetEditText: EditText

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
        val view = inflater.inflate(R.layout.fragment_custom_keyboard, container, false)

        // Set up button listeners
        setupButtonListeners(view)

        return view
    }

    // This function allows the fragment to set the EditText target for input
    fun setTargetEditText(editText: EditText) {
        this.targetEditText = editText
    }

    // Method to handle button clicks and input text into the target EditText
    private fun setupButtonListeners(view: View) {
        view.findViewById<Button>(R.id.button_1).setOnClickListener { appendTextToEditText("1") }
        view.findViewById<Button>(R.id.button_2).setOnClickListener { appendTextToEditText("2") }
        view.findViewById<Button>(R.id.button_3).setOnClickListener { appendTextToEditText("3") }
        view.findViewById<Button>(R.id.button_4).setOnClickListener { appendTextToEditText("4") }
        view.findViewById<Button>(R.id.button_5).setOnClickListener { appendTextToEditText("5") }
        view.findViewById<Button>(R.id.button_6).setOnClickListener { appendTextToEditText("6") }
        view.findViewById<Button>(R.id.button_7).setOnClickListener { appendTextToEditText("7") }
        view.findViewById<Button>(R.id.button_8).setOnClickListener { appendTextToEditText("8") }
        view.findViewById<Button>(R.id.button_9).setOnClickListener { appendTextToEditText("9") }
        view.findViewById<Button>(R.id.button_0).setOnClickListener { appendTextToEditText("0") }
        view.findViewById<Button>(R.id.button_000).setOnClickListener { appendTextToEditText("000") }

        // Delete button to remove the last character from EditText
        view.findViewById<Button>(R.id.button_delete).setOnClickListener {
            val currentText = targetEditText.text.toString()
            if (currentText.isNotEmpty()) {
                targetEditText.setText(currentText.substring(0, currentText.length - 1))
                targetEditText.setSelection(targetEditText.text.length) // Set cursor to end
            }
        }

        // Done button to hide the keyboard
        view.findViewById<Button>(R.id.button_done).setOnClickListener {
            // You can define behavior here (e.g., close keyboard or trigger an action)
            parentFragmentManager.popBackStack() // This will remove the keyboard fragment
        }
    }

    // Helper function to append text to the target EditText
    private fun appendTextToEditText(value: String) {
        val currentText = targetEditText.text.toString()
        targetEditText.setText(currentText + value)
        targetEditText.setSelection(targetEditText.text.length) // Set cursor to end
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomKeyboardFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CustomKeyboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
