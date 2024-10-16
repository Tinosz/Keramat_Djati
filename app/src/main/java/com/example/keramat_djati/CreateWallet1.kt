package com.example.keramat_djati

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.keramat_djati.databinding.ActivityCreateWalletBinding
import com.example.keramat_djati.databinding.FragmentCreateWallet1Binding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 * Use the [CreateWallet1.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateWallet1 : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding : FragmentCreateWallet1Binding? = null
    private val binding get() = _binding!!

    private val viewModel : CreateWalletViewModel by activityViewModels()

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
        _binding = FragmentCreateWallet1Binding.inflate(inflater, container, false)

        viewModel.walletName.value?.let {
            binding.walletNameInput.setText(it)
        }

        binding.nextButton.setOnClickListener{
            val walletName = binding.walletNameInput.text.toString().trim()

            if (walletName.isEmpty()){
                binding.walletNameInput.error = "Wallet name is require"
                binding.walletNameInput.requestFocus()
            } else {
                viewModel.walletName.value = walletName
                (activity as CreateWalletActivity).replaceFragment(CreateWallet2())
            }
        }
        return binding.root
    }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CreateWallet1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateWallet1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}